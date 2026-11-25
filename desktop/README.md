# LATIF VPN CONTROL — Desktop App (Electron WebContentsView)

Standard web browsers (including GitHub Pages) block `<iframe>` embedding for `https://elevenlabs.io/` because ElevenLabs sends `X-Frame-Options` / `CSP` security headers.

This `desktop/` Electron wrapper uses Electron's native **`WebContentsView`** so **ElevenLabs** and **NordVPN Account** load directly inside the right-hand panel (with Full Screen mode and zero external browser tabs).

## Run on Windows / macOS / Linux

1. Install Node.js 18+
2. From the `desktop/` folder, run:
   ```bash
   npm install
   npm start
   ```
