'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';
import { fileAPI, FileMetadataDTO } from '@/lib/api';
import { FileText, Upload, LogOut, Download, Eye, Share2, Lock, Globe, Plus } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

export default function FilesPage() {
  const [files, setFiles] = useState<FileMetadataDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isPublic, setIsPublic] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const router = useRouter();
  const { user, logout, isAuthenticated, isLoading } = useAuth();

  useEffect(() => {
    const fetchFiles = async () => {
      try {
        const list = await fileAPI.listMine();
        setFiles(list);
      } catch (e) {
        // ignore for now; could show a toast
      } finally {
        setLoading(false);
      }
    };

    if (!isLoading && !isAuthenticated) {
      router.push('/login');
    } else if (!isLoading && isAuthenticated) {
      fetchFiles();
    }
  }, [isAuthenticated, isLoading, router]);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setUploading(true);
    try {
      const metadata = await fileAPI.upload(selectedFile, isPublic);
      setFiles([metadata, ...files]);
      setSelectedFile(null);
      setIsPublic(false);
      setShowUploadModal(false);
    } catch (error) {
      alert('Failed to upload file. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  const handleLogout = () => {
    logout();
    router.push('/');
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  const copyShareLink = (fileId: string) => {
    const url = `${window.location.origin}/file/${fileId}`;
    navigator.clipboard.writeText(url);
    alert('Link copied to clipboard!');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200">
        <div className="container-custom py-4">
          <div className="flex justify-between items-center">
            <div className="flex items-center space-x-3">
              <FileText className="w-8 h-8 text-primary-600" />
              <div>
                <h1 className="text-xl font-bold text-gray-900">Foglio</h1>
                <p className="text-sm text-gray-600">Welcome, {user?.name}</p>
              </div>
            </div>
            <button
              onClick={handleLogout}
              className="btn btn-secondary flex items-center space-x-2"
            >
              <LogOut className="w-4 h-4" />
              <span className="hidden sm:inline">Logout</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container-custom py-8">
        {/* Upload Section */}
        <div className="mb-8">
          <button
            onClick={() => setShowUploadModal(true)}
            className="btn btn-primary flex items-center space-x-2"
          >
            <Plus className="w-5 h-5" />
            <span>Upload File</span>
          </button>
        </div>

        {/* Files Grid */}
        {files.length === 0 ? (
          <div className="text-center py-20">
            <Upload className="w-16 h-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">No files yet</h3>
            <p className="text-gray-600 mb-6">Upload your first file to get started</p>
            <button
              onClick={() => setShowUploadModal(true)}
              className="btn btn-primary"
            >
              Upload Now
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {files.map((file) => (
              <div key={file.id} className="card p-6 hover:shadow-lg transition-shadow">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex-1 min-w-0">
                    <h3 className="font-semibold text-gray-900 truncate">{file.originalName}</h3>
                    <p className="text-sm text-gray-500">{formatFileSize(file.size)}</p>
                  </div>
                  <div className={`ml-2 px-2 py-1 rounded text-xs font-medium ${
                    file.isPublic
                      ? 'bg-green-100 text-green-800'
                      : 'bg-gray-100 text-gray-800'
                  }`}>
                    {file.isPublic ? (
                      <span className="flex items-center space-x-1">
                        <Globe className="w-3 h-3" />
                        <span>Public</span>
                      </span>
                    ) : (
                      <span className="flex items-center space-x-1">
                        <Lock className="w-3 h-3" />
                        <span>Private</span>
                      </span>
                    )}
                  </div>
                </div>

                <div className="text-xs text-gray-500 mb-4">
                  {file.createdAt ? (
                    <>Uploaded {formatDistanceToNow(new Date(file.createdAt), { addSuffix: true })}</>
                  ) : (
                    <>Uploaded recently</>
                  )}
                </div>

                <div className="flex space-x-2">
                  {file.contentType.startsWith('image/') || file.contentType.startsWith('video/') ? (
                    <a
                      href={fileAPI.getPreviewUrl(file.id, file.isPublic)}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="btn btn-secondary flex-1 flex items-center justify-center space-x-1 text-sm"
                    >
                      <Eye className="w-4 h-4" />
                      <span>Preview</span>
                    </a>
                  ) : null}
                  <a
                    href={fileAPI.getDownloadUrl(file.id, file.isPublic)}
                    download
                    className="btn btn-primary flex-1 flex items-center justify-center space-x-1 text-sm"
                  >
                    <Download className="w-4 h-4" />
                    <span>Download</span>
                  </a>
                  {file.isPublic && (
                    <button
                      onClick={() => copyShareLink(file.id)}
                      className="btn btn-secondary p-2"
                      title="Copy share link"
                    >
                      <Share2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Upload Modal */}
      {showUploadModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6 animate-slide-up">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Upload File</h2>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Select File
                </label>
                <input
                  type="file"
                  onChange={handleFileSelect}
                  className="w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-primary-50 file:text-primary-700 hover:file:bg-primary-100"
                />
                {selectedFile && (
                  <p className="mt-2 text-sm text-gray-600">
                    Selected: {selectedFile.name} ({formatFileSize(selectedFile.size)})
                  </p>
                )}
              </div>

              <div className="flex items-center">
                <input
                  type="checkbox"
                  id="isPublic"
                  checked={isPublic}
                  onChange={(e) => setIsPublic(e.target.checked)}
                  className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                />
                <label htmlFor="isPublic" className="ml-2 text-sm text-gray-700">
                  Make file publicly accessible
                </label>
              </div>

              <div className="flex space-x-3 mt-6">
                <button
                  onClick={() => {
                    setShowUploadModal(false);
                    setSelectedFile(null);
                    setIsPublic(false);
                  }}
                  className="btn btn-secondary flex-1"
                  disabled={uploading}
                >
                  Cancel
                </button>
                <button
                  onClick={handleUpload}
                  disabled={!selectedFile || uploading}
                  className="btn btn-primary flex-1"
                >
                  {uploading ? 'Uploading...' : 'Upload'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
