// Pauses the debug app's page and prints where its JavaScript is busy (for hangs): node tools/page-pause.js
const { execSync } = require('child_process');
const adb = process.env.ADB || 'adb';
const pid = execSync(`${adb} shell pidof nl.alina.tovertuin`).toString().trim();
execSync(`${adb} forward tcp:9333 localabstract:webview_devtools_remote_${pid}`);
setTimeout(() => { console.log('TIMEOUT'); process.exit(2); }, 30000);
(async () => {
  const page = (await (await fetch('http://127.0.0.1:9333/json')).json()).find(p => p.type === 'page');
  const ws = new WebSocket(page.webSocketDebuggerUrl);
  let id = 0;
  const send = (method, params = {}) => ws.send(JSON.stringify({ id: ++id, method, params }));
  ws.onopen = () => { send('Debugger.enable'); send('Debugger.pause'); };
  ws.onmessage = m => {
    const d = JSON.parse(m.data);
    if (d.method === 'Debugger.paused') {
      for (const f of d.params.callFrames.slice(0, 12)) console.log(`${f.functionName || '(anon)'} line ${f.location.lineNumber + 1}:${f.location.columnNumber}`);
      send('Debugger.resume'); setTimeout(() => process.exit(0), 300);
    }
  };
})();
