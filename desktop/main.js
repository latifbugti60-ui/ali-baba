const { app, BrowserWindow, WebContentsView, ipcMain, clipboard, shell } = require("electron");
const path = require("path");
const { execFile } = require("child_process");

const ELEVENLABS_URL = "https://elevenlabs.io/";
const NORD_ACCOUNT_URL = "https://my.nordaccount.com/";

const ALLOWED_HTTPS_HOSTS = new Set([
  "elevenlabs.io",
  "my.nordaccount.com",
  "nordaccount.com",
  "nordvpn.com",
  "mail.tm",
  "mail.gw",
  "tempmail.ninja",
  "accounts.google.com",
  "appleid.apple.com",
  "github.com",
  "login.microsoftonline.com",
  "checkout.stripe.com",
  "challenges.cloudflare.com",
]);

const ALLOWED_SUFFIXES = [
  ".elevenlabs.io",
  ".nordaccount.com",
  ".nordvpn.com",
  ".mail.tm",
  ".mail.gw",
  ".tempmail.ninja",
  ".stripe.com",
  ".cloudflare.com",
  ".hcaptcha.com",
  ".arkoselabs.com",
  ".recaptcha.net",
  ".gstatic.com",
  ".googleapis.com",
];

function isAllowedUrl(rawUrl) {
  try {
    const parsed = new URL(rawUrl);
    if (parsed.protocol !== "https:") return false;
    const host = parsed.hostname.toLowerCase();
    if (ALLOWED_HTTPS_HOSTS.has(host)) return true;
    return ALLOWED_SUFFIXES.some((suffix) => host.endsWith(suffix));
  } catch {
    return false;
  }
}

let mainWindow = null;
let embeddedView = null;
let isBrowserOpen = false;
let isFullScreen = false;

function layoutViews() {
  if (!mainWindow) return;
  const [winWidth, winHeight] = mainWindow.getContentSize();
  if (!embeddedView || !isBrowserOpen) {
    if (embeddedView) {
      embeddedView.setBounds({ x: 0, y: 0, width: 0, height: 0 });
    }
    return;
  }
  if (isFullScreen) {
    embeddedView.setBounds({
      x: 12,
      y: 58,
      width: Math.max(200, winWidth - 24),
      height: Math.max(200, winHeight - 70),
    });
  } else {
    const leftSidebarWidth = Math.min(390, Math.floor(winWidth * 0.34));
    embeddedView.setBounds({
      x: leftSidebarWidth + 28,
      y: 120,
      width: Math.max(300, winWidth - leftSidebarWidth - 44),
      height: Math.max(300, winHeight - 136),
    });
  }
}

function ensureEmbeddedView() {
  if (embeddedView || !mainWindow) return;
  embeddedView = new WebContentsView({
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  });
  mainWindow.contentView.addChildView(embeddedView);

  // Never open external browser tabs — keep safe links inside the embedded WebContentsView.
  embeddedView.webContents.setWindowOpenHandler(({ url }) => {
    if (isAllowedUrl(url)) {
      embeddedView.webContents.loadURL(url);
    }
    return { action: "deny" };
  });

  embeddedView.webContents.on("will-navigate", (event, url) => {
    if (!isAllowedUrl(url)) {
      event.preventDefault();
    }
  });

  embeddedView.webContents.on("did-navigate", (_event, url) => {
    mainWindow?.webContents.send("browser:url-changed", url);
  });
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 820,
    minWidth: 900,
    minHeight: 620,
    backgroundColor: "#060914",
    title: "LATIF VPN CONTROL",
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  });

  mainWindow.loadFile(path.join(__dirname, "index.html"));
  mainWindow.on("resize", layoutViews);
}

ipcMain.handle("browser:open", async (_event, url) => {
  const safeUrl = isAllowedUrl(url) ? url : ELEVENLABS_URL;
  ensureEmbeddedView();
  isBrowserOpen = true;
  await embeddedView.webContents.loadURL(safeUrl);
  layoutViews();
  return safeUrl;
});

ipcMain.handle("browser:back", () => {
  if (embeddedView && embeddedView.webContents.navigationHistory.canGoBack()) {
    embeddedView.webContents.navigationHistory.goBack();
  }
});

ipcMain.handle("browser:reload", () => {
  embeddedView?.webContents.reload();
});

ipcMain.handle("browser:toggle-fullscreen", () => {
  isFullScreen = !isFullScreen;
  layoutViews();
  return isFullScreen;
});

ipcMain.handle("nordvpn:connect-country", (_event, country) => {
  const cleanCountry = country === "Sweden" ? "Sweden" : "Ireland";
  clipboard.writeText(cleanCountry);

  // Attempt official NordVPN CLI on Linux/macOS or official desktop URI/app if installed.
  if (process.platform === "linux") {
    return new Promise((resolve) => {
      execFile("nordvpn", ["connect", cleanCountry], (err, stdout, stderr) => {
        if (!err) {
          resolve({ ok: true, connected: true, message: stdout.trim() || `Connected to ${cleanCountry}` });
        } else {
          resolve({
            ok: false,
            connected: false,
            message: `Official NordVPN CLI returned: ${(stderr || err.message).trim()}. Copied "${cleanCountry}".`,
          });
        }
      });
    });
  }
  if (process.platform === "win32") {
    shell.openPath("C:\\Program Files\\NordVPN\\NordVPN.exe");
  } else if (process.platform === "darwin") {
    shell.openPath("/Applications/NordVPN.app");
  }
  return {
    ok: true,
    connected: null,
    message: `Copied "${cleanCountry}" and launched official NordVPN desktop app.`,
  };
});

ipcMain.handle("nordvpn:disconnect", () => {
  if (process.platform === "linux") {
    return new Promise((resolve) => {
      execFile("nordvpn", ["disconnect"], (err, stdout, stderr) => {
        if (!err) {
          resolve({ ok: true, connected: false, message: stdout.trim() || "Disconnected" });
        } else {
          resolve({ ok: false, connected: null, message: (stderr || err.message).trim() });
        }
      });
    });
  }
  if (process.platform === "win32") {
    shell.openPath("C:\\Program Files\\NordVPN\\NordVPN.exe");
  } else if (process.platform === "darwin") {
    shell.openPath("/Applications/NordVPN.app");
  }
  return {
    ok: true,
    connected: null,
    message: "Opened official NordVPN desktop app to disconnect.",
  };
});

app.whenReady().then(createWindow);
app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});
