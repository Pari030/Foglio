/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'export',
  images: {
    domains: ['localhost'],
    unoptimized: true
  },
  trailingSlash: true,
  // Remove rewrites as they don't work with static export
  // We'll use Nginx for proxying instead
};

module.exports = nextConfig;
