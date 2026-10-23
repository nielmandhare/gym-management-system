import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Keep development controls clear of the navigation and Logout button.
  devIndicators: { position: "bottom-right" },
  experimental: {
    agentFeedback: true,
  },
  cacheComponents: true,
  async headers() {
    return [
      {
        // The browser must always re-check the service worker file for updates.
        source: "/sw.js",
        headers: [
          { key: "Cache-Control", value: "no-cache, no-store, must-revalidate" },
          { key: "Service-Worker-Allowed", value: "/" },
        ],
      },
    ];
  },
  partialPrefetching: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
