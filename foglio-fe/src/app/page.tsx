'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';
import Link from 'next/link';
import { Upload, FileText, Zap } from 'lucide-react';

export default function Home() {
  const router = useRouter();
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) {
      router.push('/files');
    }
  }, [isAuthenticated, router]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 via-white to-primary-100">
      {/* Header */}
      <header className="container-custom py-6">
        <div className="flex justify-between items-center">
          <div className="flex items-center space-x-2">
            <FileText className="w-8 h-8 text-primary-600" />
            <h1 className="text-2xl font-bold text-gray-900">Foglio</h1>
          </div>
          <div className="flex space-x-4">
            <Link href="/login" className="btn btn-secondary">
              Login
            </Link>
            <Link href="/signup" className="btn btn-primary">
              Sign Up
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="container-custom">
        <div className="py-20 md:py-32 text-center">
          <div className="animate-fade-in">
            <h2 className="text-4xl md:text-6xl font-bold text-gray-900 mb-6">
              Share Files
              <span className="text-primary-600"> Instantly</span>
            </h2>
            <p className="text-xl md:text-2xl text-gray-600 mb-12 max-w-3xl mx-auto">
              Fast, secure, and simple file sharing platform. Upload, share, and manage your files with ease.
            </p>
            <div className="flex flex-col sm:flex-row justify-center gap-4">
              <Link href="/signup" className="btn btn-primary text-lg px-8 py-4">
                Get Started Free
              </Link>
              <Link href="/login" className="btn btn-secondary text-lg px-8 py-4">
                Sign In
              </Link>
            </div>
          </div>
        </div>

        {/* Features */}
        <div className="py-20 grid grid-cols-1 md:grid-cols-3 gap-8 animate-slide-up">
          <div className="card p-8 hover:shadow-lg transition-shadow">
            <div className="bg-primary-100 w-16 h-16 rounded-lg flex items-center justify-center mb-4">
              <Upload className="w-8 h-8 text-primary-600" />
            </div>
            <h3 className="text-xl font-semibold mb-3">Easy Upload</h3>
            <p className="text-gray-600">
              Drag and drop your files or click to upload. It's that simple.
            </p>
          </div>

          <div className="card p-8 hover:shadow-lg transition-shadow">
            <div className="bg-primary-100 w-16 h-16 rounded-lg flex items-center justify-center mb-4">
              <Zap className="w-8 h-8 text-primary-600" />
            </div>
            <h3 className="text-xl font-semibold mb-3">Lightning Fast</h3>
            <p className="text-gray-600">
              Our optimized infrastructure ensures your files are delivered instantly.
            </p>
          </div>

          <div className="card p-8 hover:shadow-lg transition-shadow">
            <div className="bg-primary-100 w-16 h-16 rounded-lg flex items-center justify-center mb-4">
              <FileText className="w-8 h-8 text-primary-600" />
            </div>
            <h3 className="text-xl font-semibold mb-3">Secure Storage</h3>
            <p className="text-gray-600">
              Your files are stored securely with API key authentication and private/public options.
            </p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="container-custom py-8 mt-20 border-t border-gray-200">
        <div className="text-center text-gray-600">
          <p>&copy; 2025 Foglio. Built with ❤️ using Next.js and Spring Boot.</p>
        </div>
      </footer>
    </div>
  );
}
