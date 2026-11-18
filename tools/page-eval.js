// Runs one JavaScript expression in the debug app on the connected device: node tools/page-eval.js "document.title"
const { execSync } = require('child_process');
const adb = process.env.ADB || 'adb';
const pid = execSync(`${adb} shell pidof nl.alina.tovertuin`).toString().trim();
execSync(`${adb} forward tcp:9333 localabstract:webview_devtools_remote_${pid}`);
setTimeout(() => { console.log('TIMEOUT'); process.exit(2); }, 60000);
(async () => {
  const page = (await (await fetch('http://127.0.0.1:9333/json')).json()).find(p => p.type === 'page');
  const ws = new WebSocket(page.webSocketDebuggerUrl);
  await new Promise(r => ws.onopen = r);
  ws.onmessage = m => { const d = JSON.parse(m.data); if (d.id === 1) { const r = d.result.result; console.log(r.value !== undefined ? r.value : JSON.stringify(d.result)); process.exit(0); } };
  ws.send(JSON.stringify({ id: 1, method: 'Runtime.evaluate', params: { expression: process.argv[2], awaitPromise: true, returnByValue: true, userGesture: true } }));
})();
