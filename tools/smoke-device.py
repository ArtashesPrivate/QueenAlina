"""Exercise the actual native UI on our dedicated emulator; never grants free stars."""
import json
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'verification'
SERIAL = sys.argv[1] if len(sys.argv)>1 else 'emulator-5556'
ADB = ['adb','-s',SERIAL]
checks = []

def adb(*args):
    return subprocess.check_output(ADB+list(args), timeout=35).decode('utf-8',errors='replace')

def ui():
    for _ in range(5):
        try:
            adb('shell','uiautomator','dump','--compressed','/sdcard/alina-ui.xml')
            raw = adb('shell','cat','/sdcard/alina-ui.xml')
            return ET.fromstring(raw)
        except (ET.ParseError, subprocess.CalledProcessError):
            time.sleep(.4)
    raise AssertionError('Could not read native UI')

def texts(tree):
    return [n.get('text','') for n in tree.iter('node') if n.get('text')]

def tap_node(n):
    x1,y1,x2,y2=map(int,re.findall(r'\d+',n.get('bounds')))
    assert x2>x1 and y2>y1, n.attrib
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))
    time.sleep(.25)

def click(label, tree=None):
    tree=tree if tree is not None else ui()
    matches=[n for n in tree.iter('node') if n.get('text')==label]
    assert matches, f'{label!r} missing from {texts(tree)}'
    tap_node(matches[-1])

def state():
    raw=adb('shell','run-as','nl.alina.tovertuin','cat','shared_prefs/alina_progress_v1.xml')
    return json.loads(ET.fromstring(raw).find("string[@name='state']").text)

def screenshot(name):
    data=subprocess.check_output(ADB+['exec-out','screencap','-p'],timeout=30)
    (OUT/name).write_bytes(data)

def check(ok, label):
    assert ok, label
    checks.append(label)
    print('PASS:',label,flush=True)

def scroll_down():
    size=adb('shell','wm','size')
    w,h=map(int,re.findall(r'(\d+)x(\d+)',size)[-1])
    adb('shell','input','swipe',str(w//2),str(int(h*.72)),str(w//2),str(int(h*.3)),'250')
    time.sleep(.25)

def maths_round(wrong_first=False):
    click('Leren')
    tree=ui()
    play=[n for n in tree.iter('node') if n.get('text')=='Spelen']
    tap_node(play[0])
    for q in range(5):
        tree=ui()
        equation=next(t for t in texts(tree) if re.fullmatch(r'\d+ [−+] \d+ = \?',t))
        a,op,b=equation.split()[:3]
        answer=int(a)+int(b) if op=='+' else int(a)-int(b)
        if wrong_first and q==0:
            options=[n for n in tree.iter('node') if n.get('class')=='android.widget.Button' and n.get('text','').isdigit()]
            wrong=next(n for n in options if n.get('text')!=str(answer))
            tap_node(wrong)
            check('Bijna! Kijk nog eens rustig. Je mag opnieuw kiezen.' in texts(ui()),'Incorrect answer offers retry')
        click(str(answer),tree)
        if q<4:
            click('Volgende opdracht')
        else:
            check('+5 sterren' in texts(ui()),'Five questions complete a learning round')

try:
    adb('shell','am','force-stop','nl.alina.tovertuin')
    adb('shell','am','start','-n','nl.alina.tovertuin/.MainActivity')
    time.sleep(2)
    check('Hoi Alina!' in texts(ui()),'App launches to the native Dutch home screen')
    screenshot('phone-home.png')
    click('Winkel')
    check('Nog 20 sterren' in texts(ui()),'Shop shows affordable progress without charging money')
    screenshot('phone-shop.png')
    click('Nog 20 sterren')
    check('Nog even sparen' in texts(ui()),'Insufficient stars cannot buy an animal')
    click('Terug')
    start_stars=state()['stars']
    start_rounds=state()['rounds']
    maths_round(True)
    screenshot('phone-reward.png')
    check(state()['stars']==start_stars+5,'A complete UI round saves exactly five stars')
    for _ in range(3):maths_round()
    check(state()['stars']==start_stars+20,'Four UI rounds earn twenty stars')
    click('Winkel')
    tree=ui()
    buy=[n for n in tree.iter('node') if n.get('text')=='Welkom!']
    tap_node(buy[0])
    click('Ja, welkom!')
    s=state()
    check(s['pets'][4]['owned'] and s['active']==4 and s['stars']==start_stars,'Hamster purchase saves ownership, selection and exact balance')
    screenshot('phone-new-pet.png')
    # Care actions are below the illustration on compact screens.
    scroll_down()
    click('Eten geven')
    check(state()['pets'][4]['n0']>s['pets'][4]['n0'],'Feeding updates the selected animal')
    scroll_down()
    click('Slapen')
    screenshot('phone-sleep.png')
    check(state()['pets'][4]['n2']>s['pets'][4]['n2'],'Sleeping restores rest')
    adb('shell','am','force-stop','nl.alina.tovertuin')
    adb('shell','am','start','-n','nl.alina.tovertuin/.MainActivity')
    time.sleep(1)
    s=state()
    check(s['pets'][4]['owned'] and s['rounds']==start_rounds+4,'Restart restores earned progress and purchased pet')
    click('Leren')
    screenshot('phone-learning.png')
    tree=ui(); play=[n for n in tree.iter('node') if n.get('text')=='Spelen'];tap_node(play[1])
    tree=ui();check('Welk woord hoort bij dit dier?' in texts(tree),'Dutch reading game opens')
    screenshot('phone-words.png')
    click('Thuis')
    click('Leren')
    scroll_down()
    tree=ui(); play=[n for n in tree.iter('node') if n.get('text')=='Spelen'];tap_node(play[-1])
    check('Welke vorm komt hierna?' in texts(ui()),'Pattern game opens')
    screenshot('phone-pattern.png')
    click('Thuis')
    adb('shell','settings','put','system','font_scale','1.3')
    time.sleep(.5)
    screenshot('phone-large-text.png')
    check('Thuis' in texts(ui()),'Navigation remains available with larger system text')
    adb('shell','settings','put','system','font_scale','1.0')
    report={'result':'PASS','device':SERIAL,'checks':checks,'final_state':state()}
    (OUT/'device-smoke-results.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
except Exception:
    screenshot('device-test-failure.png')
    (OUT/'device-test-failure.xml').write_text(ET.tostring(ui(),encoding='unicode'),encoding='utf-8')
    raise
