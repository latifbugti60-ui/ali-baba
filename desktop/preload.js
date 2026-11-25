const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("latifControl", {
  openEmbeddedUrl: (url) => ipcRenderer.invoke("browser:open", url),
  goBack: () => ipcRenderer.invoke("browser:back"),
  reloadBrowser: () => ipcRenderer.invoke("browser:reload"),
  toggleFullScreen: () => ipcRenderer.invoke("browser:toggle-fullscreen"),
  connectNordCountry: (country) => ipcRenderer.invoke("nordvpn:connect-country", country),
  disconnectNord: () => ipcRenderer.invoke("nordvpn:disconnect"),
  onUrlChanged: (cb) => ipcRenderer.on("browser:url-changed", (_e, url) => cb(url)),
});
