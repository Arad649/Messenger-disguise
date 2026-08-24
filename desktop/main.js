const { app, BrowserWindow, dialog, protocol, session } = require('electron');
const fs = require('fs');
const os = require('os');
const path = require('path');

// Software rendering avoids startup crashes on PCs with older or broken GPU drivers.
app.disableHardwareAcceleration();

let mainWindow = null;
let rendererRecoveryAttempted = false;

function logEvent(event, details = '') {
  try {
    const logPath = path.join(app.getPath('userData'), 'settings-connect.log');
    fs.mkdirSync(path.dirname(logPath), { recursive: true });
    fs.appendFileSync(
      logPath,
      `${new Date().toISOString()} ${event}${details ? `: ${details}` : ''}\n`,
      'utf8'
    );
    return logPath;
  } catch {
    return null;
  }
}

function failStartup(error) {
  const message = error instanceof Error ? `${error.stack || error.message}` : String(error);
  const logPath = logEvent('Startup failure', message);
  dialog.showErrorBox(
    'Settings Connect could not start',
    `The app could not open its interface. ${logPath ? `A diagnostic log was saved to:\n${logPath}` : 'No diagnostic log could be written.'}`
  );
  app.quit();
}

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

  mainWindow = win;

  const allowedOrigin = 'settings-connect://app';
  session.defaultSession.setPermissionCheckHandler((webContents, permission, origin) => {
    return webContents === win.webContents
      && String(origin || '').startsWith(allowedOrigin)
      && permission === 'media';
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

  win.webContents.on('render-process-gone', (_event, details) => {
    if (details.reason === 'clean-exit') return;
    logEvent('Renderer stopped', `${details.reason} (exit code ${details.exitCode})`);
    if (!rendererRecoveryAttempted && !win.isDestroyed()) {
      rendererRecoveryAttempted = true;
      setTimeout(() => {
        if (!win.isDestroyed()) win.reload();
      }, 300);
      return;
    }

    dialog.showErrorBox(
      'Settings Connect stopped unexpectedly',
      'The interface stopped twice and could not be recovered. Restart the app or use Settings Connect Web.'
    );
  });

  win.on('closed', () => {
    if (mainWindow === win) mainWindow = null;
  });

  win.loadURL('settings-connect://app/index.html').catch(failStartup);
}

app.whenReady().then(async () => {
  protocol.handle('settings-connect', async (request) => {
    const requestUrl = new URL(request.url);
    if (requestUrl.hostname !== 'app' || !['/', '/index.html'].includes(requestUrl.pathname)) {
      return new Response('Not found', { status: 404 });
    }

    const page = await fs.promises.readFile(path.join(__dirname, 'index.html'));
    return new Response(page, {
      status: 200,
      headers: {
        'Content-Type': 'text/html; charset=utf-8',
        'Cache-Control': 'no-store'
      }
    });
  });

  logEvent('App ready', `Electron ${process.versions.electron}; Windows ${os.release()}`);
  createWindow();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
}).catch(failStartup);

process.on('uncaughtException', failStartup);
process.on('unhandledRejection', failStartup);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});
