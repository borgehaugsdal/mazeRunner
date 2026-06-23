#!/bin/bash
# Start maze-frontend dev server

cd "$(dirname "$0")"

echo "=== maze-frontend ==="

if [ ! -d "node_modules" ]; then
  echo "Installerer avhengigheter..."
  npm install
fi

# Finn alle LAN IP-adresser (unntatt loopback og point-to-point)
WIFI_IP=$(ipconfig getifaddr en0 2>/dev/null)
VPN_IP=$(ipconfig getifaddr en1 2>/dev/null || ifconfig | grep "inet " | grep -v "127.0.0.1" | grep -v "$WIFI_IP" | awk '{print $2}' | head -1)

# Bruk første tilgjengelige IP for backend
PRIMARY_IP="${VPN_IP:-$WIFI_IP}"
if [ -n "$PRIMARY_IP" ]; then
  echo "VITE_API_BASE=http://$PRIMARY_IP:8080" > .env.local
fi

echo ""
echo "  Lokal adresse    : http://localhost:5173"
[ -n "$WIFI_IP" ] && echo "  WiFi             : http://$WIFI_IP:5173"
[ -n "$VPN_IP"  ] && echo "  VPN/annet        : http://$VPN_IP:5173"
echo "  Backend API      : http://$PRIMARY_IP:8080"
echo ""

echo "Starter Vite dev server..."
npm run dev
