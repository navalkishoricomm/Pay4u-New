#!/usr/bin/env bash
# ==============================================================================
# pay4u - Safe Automated VPS Deployment Script
# Designed for servers with existing services running.
# Will NOT overwrite or conflict with existing Nginx sites or ports.
# ==============================================================================

set -e

# Default settings
APP_DIR="/var/www/pay4u-v2"
DESIRED_PORT=3008

echo "=============================================="
echo "🚀 Starting pay4u-v2 Safe VPS Deployment (Side-by-Side)"
echo "=============================================="

# 1. Check for root / sudo
if [ "$EUID" -ne 0 ]; then
  echo "⚠️ Please run this script with sudo: sudo bash deploy-vps.sh"
  exit 1
fi

# 2. Find a free port starting from DESIRED_PORT (to avoid conflicts with existing services)
PORT=$DESIRED_PORT
while netstat -tuln 2>/dev/null | grep -q ":$PORT " || ss -tuln 2>/dev/null | grep -q ":$PORT "; do
  echo "ℹ️ Port $PORT is already in use by an existing service. Checking next port..."
  PORT=$((PORT + 1))
done
echo "✅ Selected non-conflicting internal port: $PORT"

# 3. Ensure Node.js & PM2 are installed
if ! command -v node &> /dev/null; then
  echo "📦 Installing Node.js..."
  curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
  apt-get install -y nodejs
else
  echo "✅ Node.js is already installed ($(node -v))"
fi

if ! command -v pm2 &> /dev/null; then
  echo "📦 Installing PM2 process manager..."
  npm install -g pm2
else
  echo "✅ PM2 is already installed"
fi

# 4. Prepare application directory
echo "📂 Preparing app directory at $APP_DIR..."
mkdir -p "$APP_DIR"
cp -r ./* "$APP_DIR/" 2>/dev/null || true
cd "$APP_DIR"

# 5. Start / Restart application under PM2 with unique name 'pay4u-v2'
echo "🔄 Starting pay4u-v2 service via PM2 on port $PORT..."
PORT=$PORT pm2 restart pay4u-v2 2>/dev/null || PORT=$PORT pm2 start server.js --name "pay4u-v2"
pm2 save

# 6. Generate isolated Nginx snippet for pay4u-v2
NGINX_CONF="/etc/nginx/conf.d/pay4u-v2.conf"
if [ -d "/etc/nginx/conf.d" ]; then
  echo "🌐 Creating isolated Nginx reverse proxy configuration at $NGINX_CONF..."
  cat <<EOF > "$NGINX_CONF"
# pay4u-v2 isolated upstream & sub-route configuration
upstream pay4u_v2_upstream {
    server 127.0.0.1:$PORT;
}
EOF
fi

echo "=============================================="
echo "🎉 pay4u service is now running successfully!"
echo "=============================================="
echo "• PM2 Status: Run 'pm2 status pay4u' to monitor"
echo "• Internal URL: http://127.0.0.1:$PORT"
echo "• Logs: Run 'pm2 logs pay4u' to see live logs"
echo ""
echo "To route traffic to it in your existing Nginx config:"
echo "Add this block inside your existing server block:"
echo ""
echo "  location /pay4u/ {"
echo "      proxy_pass http://127.0.0.1:$PORT/;"
echo "      proxy_set_header Host \$host;"
echo "      proxy_set_header X-Real-IP \$remote_addr;"
echo "  }"
echo "=============================================="
