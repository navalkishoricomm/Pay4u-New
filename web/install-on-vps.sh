#!/usr/bin/env bash
# ==============================================================================
# pay4u-v2 - Universal Automated Installer for VPS
# ==============================================================================
set -e

APP_DIR="/var/www/pay4u-v2"
PORT=${PORT:-5003}

echo "========================================="
echo "📦 Installing pay4u-v2 on VPS"
echo "========================================="

# 1. Create target app directory
mkdir -p "$APP_DIR"

# 2. If current directory has server.js, copy from here.
if [ -f "./server.js" ]; then
  echo "📂 Copying files from current directory..."
  cp -rf ./* "$APP_DIR/" 2>/dev/null || true
elif [ -f "./web/server.js" ]; then
  echo "📂 Copying files from web directory..."
  cp -rf ./web/* "$APP_DIR/" 2>/dev/null || true
fi

cd "$APP_DIR"

# 3. Ensure Node.js & PM2 are present
if ! command -v node &> /dev/null; then
  echo "📦 Installing Node.js..."
  curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
  apt-get install -y nodejs || true
fi

if ! command -v pm2 &> /dev/null; then
  echo "📦 Installing PM2 process manager..."
  npm install -g pm2 || true
fi

# 4. Start / Restart application under PM2
echo "🔄 Starting pay4u-v2 under PM2 on port $PORT..."
pm2 delete pay4u-v2 2>/dev/null || true
PORT=$PORT pm2 start server.js --name "pay4u-v2"
pm2 save

echo "========================================="
echo "✅ pay4u-v2 is now running on PORT $PORT!"
echo "========================================="
echo "• PM2 Status: pm2 status pay4u-v2"
echo "• Live Logs:  pm2 logs pay4u-v2"
echo "• Test Check: curl http://127.0.0.1:$PORT/api/health"
echo "========================================="
echo "Credentials for Login:"
echo "• Retailer: 9810610554 / 123456"
echo "• Admin:    admin / admin (or 9999999999)"
echo "========================================="
