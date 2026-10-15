import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[2]/'.local/report-tools'))
import ast,html,json,os,sys,time
ROOT=Path(__file__).resolve().parents[2];sys.path.insert(0,str(ROOT/'.local/selenium'));os.environ['SE_CACHE_PATH']=str(ROOT/'.local/selenium-cache')
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
OUT=ROOT/'output/stqa/screenshots';OUT.mkdir(parents=True,exist_ok=True)
opt=webdriver.ChromeOptions();opt.add_argument('--headless=new');opt.add_argument('--window-size=1440,1050');d=webdriver.Chrome(options=opt);w=WebDriverWait(d,15);base='http://127.0.0.1:8081'
def capture(name):assert d.save_screenshot(str(OUT/(name+'.png')))
def login(account):
    d.delete_all_cookies();d.get(base);w.until(lambda x:d.find_element(By.ID,'login').is_displayed());d.find_element(By.ID,'login-username').send_keys(account);d.find_element(By.ID,'login-password').send_keys('123');d.find_element(By.ID,'sign-in').click();w.until(lambda x:d.find_elements(By.CSS_SELECTOR,'.stat-card'))
def page(name):
    d.find_element(By.CSS_SELECTOR,'[data-page="'+name+'"]').click();w.until(lambda x:not d.find_elements(By.CSS_SELECTOR,'.loading'))
try:
    d.get(base);w.until(lambda x:d.find_element(By.ID,'login').is_displayed());capture('01-login')
    login('admin');capture('02-admin-overview');page('members');capture('03-member-management');page('reports');capture('04-reports')
    login('trainer');page('progress');capture('05-trainer-progress')
    login('user1');page('profile');capture('06-member-profile')
    d.execute_cdp_cmd('Emulation.setDeviceMetricsOverride',{'width':390,'height':844,'deviceScaleFactor':1,'mobile':True})
    w.until(lambda x:d.execute_script("return document.querySelector('#sidebar').getBoundingClientRect().right<=1"))
    capture('07-mobile-profile');d.execute_cdp_cmd('Emulation.clearDeviceMetricsOverride',{})
    passing=[]
    for p in sorted((ROOT/'output/stqa/runs').glob('*/results.json')):
        obj=json.loads(p.read_text(encoding='utf-8'))
        if obj['metadata']['failed']==0:passing.append(obj)
    by_browser={obj['metadata']['browser']:obj for obj in passing}
    source=(ROOT/'web/tests/selenium_suite.py').read_text(encoding='utf8');parsed=ast.parse(source)
    sample='\n\n'.join(ast.get_source_segment(source,node) for node in ast.walk(parsed) if isinstance(node,ast.FunctionDef) and node.name in {'create_member','report_download'})
    summary={browser:obj['metadata'] for browser,obj in by_browser.items()}
    evidence=ROOT/'output/stqa/code-and-output.html';evidence.write_text('<html><head><meta charset="utf-8"><title>Selenium code and output</title><style>body{font:18px Segoe UI;margin:36px;background:#f5f7f2;color:#203c2a}pre{font:15px/1.55 Consolas;background:white;border:1px solid #dae5d0;padding:24px;white-space:pre-wrap}h1{font-size:28px}h2{font-size:20px}</style></head><body><h1>Selenium WebDriver — code and actual output</h1><h2>Regression code from selenium_suite.py</h2><pre>'+html.escape(sample)+'</pre><h2>Saved execution metadata</h2><pre>'+html.escape(json.dumps(summary,indent=2))+'</pre></body></html>',encoding='utf8')
    d.get(evidence.as_uri());d.set_window_size(1300,1600);d.set_window_size(1300,d.execute_script('return document.documentElement.scrollHeight')+120);capture('08-code-and-output')
    ide_files=[]
    for p in (ROOT/'output/stqa/ide').glob('results-*.json'):
        try:obj=json.loads(p.read_text());
        except:continue
        if obj.get('success'):ide_files.append((p,obj))
    if ide_files:
        p,obj=sorted(ide_files)[-1];ide_summary={key:obj[key] for key in ['numTotalTests','numPassedTests','numFailedTests','success']};names=[a['fullName'] for t in obj['testResults'] for a in t['assertionResults']]
        file=ROOT/'output/stqa/ide-output.html';file.write_text('<html><head><meta charset="utf-8"><title>Selenium IDE output</title><style>body{font:18px Segoe UI;margin:40px;color:#203c2a}pre{font:18px/1.5 Consolas;background:#edf4e7;padding:24px}li{font-size:16px;padding:3px}</style></head><body><h1>Selenium IDE — official runner results</h1><pre>'+html.escape(json.dumps(ide_summary,indent=2))+'</pre><ol>'+''.join('<li>PASS · '+html.escape(n)+'</li>' for n in names)+'</ol></body></html>',encoding='utf8');d.get(file.as_uri());d.set_window_size(1200,1400);capture('09-selenium-ide-output')
finally:d.quit()
print('Evidence captured in',OUT)
