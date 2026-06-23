import { createRequire } from 'module'

// Ensure `globalThis.crypto.getRandomValues` exists on Node <18 for Vite and plugins
if (!(globalThis as any).crypto || !(globalThis as any).crypto.getRandomValues) {
  const require = createRequire(import.meta.url)
  const nodeCrypto = require('crypto')
  ;(globalThis as any).crypto = (globalThis as any).crypto || {}
  ;(globalThis as any).crypto.getRandomValues = function <T extends ArrayBufferView>(array: T): T {
    const uint8 = new Uint8Array(array.buffer, array.byteOffset, array.byteLength)
    nodeCrypto.randomFillSync(uint8)
    return array
  }
}

import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',  // Bind eksplisitt til IPv4 alle grensesnitt
    port: 5173,
  },
  define: {
    global: "globalThis"
  },
  optimizeDeps: {
    esbuildOptions: {
      define: {
        global: "globalThis"
      }
    }
  }
});

