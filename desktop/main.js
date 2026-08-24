const { app, BrowserWindow, net, protocol, session } = require('electron');
const path = require('path');
const { pathToFileURL } = require('url');

protocol.registerSchemesAsPrivileged([
  {
    scheme: 'settings-connect',
    privileges: {
      standard: true,
      secure: true,
      supportFetchAPI: true,
      corsEnabled: true,
      stream: true
    }
  }
]);

function createWindow() {
  const win = new BrowserWindow({
    width: 460,
    height: 820,
    minWidth: 380,
    minHeight: 650,
    backgroundColor: '#070b19',
    title: 'Settings Connect',
    icon: path.join(__dirname, 'icon.png'),
    autoHideMenuBar: true,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: true,
      webSecurity: true
    }
  });

  const allowedOrigin = 'settings-connect://app';
  session.defaultSession.setPermissionCheckHandler((webContents, permission, origin) => {
    return webContents === win.webContents && origin.startsWith(allowedOrigin) && permission === 'media';
  });
  session.defaultSession.setPermissionRequestHandler((webContents, permission, callback, details) => {
    const allowed = webContents === win.webContents
      && String(details.requestingUrl || '').startsWith(allowedOrigin)
      && permission === 'media';
    callback(allowed);
  });

  win.webContents.setWindowOpenHandler(() => ({ action: 'deny' }));
  win.webContents.on('will-navigate', (event, url) => {
    if (!url.startsWith(allowedOrigin)) event.preventDefault();
  });
  win.loadURL('settings-connect://app/index.html');
}

app.whenReady().then(async () => {
  protocol.handle('settings-connect', () => {
    return net.fetch(pathToFileURL(path.join(__dirname, 'index.html')).toString());
  });
  createWindow();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});
