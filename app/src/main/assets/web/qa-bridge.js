/* Queen Alina: Android has no speechSynthesis in its web view, so the device voice goes through QAHost. */
(function () {
  if (!window.QAHost) return;
  var voices = [{ name: 'Android Nederlands', lang: 'nl-NL', default: true, localService: true, voiceURI: 'android-nl' }];
  function Utterance(text) { this.text = text == null ? '' : String(text); this.lang = 'nl-NL'; this.rate = 1; this.pitch = 1; this.volume = 1; this.voice = null; }
  var synth = {
    speaking: false, pending: false, paused: false, onvoiceschanged: null,
    getVoices: function () { return voices; },
    speak: function (u) { QAHost.speak(String(u.text), +u.rate || 1, +u.pitch || 1); },
    cancel: function () { QAHost.stop(); },
    pause: function () {}, resume: function () {},
    addEventListener: function () {}, removeEventListener: function () {}
  };
  window.SpeechSynthesisUtterance = Utterance;
  try { Object.defineProperty(window, 'speechSynthesis', { value: synth, configurable: true }); } catch (e) { window.speechSynthesis = synth; }
  try {
    if (localStorage.getItem('speeltuin-stars') == null) {
      var old = QAHost.legacyStars();
      if (old > 0) localStorage.setItem('speeltuin-stars', String(old));
    }
  } catch (e) {}
})();
