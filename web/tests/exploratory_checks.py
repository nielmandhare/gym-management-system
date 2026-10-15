"""Focused exploratory checks for session isolation, dialog focus and boundary handling."""
from pathlib import Path
import json,os,sys,datetime as dt
ROOT=Path(__file__).resolve().parents[2];sys.path.insert(0,str(ROOT/'.local/selenium'));os.environ['SE_CACHE_PATH']=str(ROOT/'.local/selenium-cache')
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.support.ui import WebDriverWait
opt=webdriver.ChromeOptions();opt.add_argument('--headless=new');opt.add_argument('--window-size=1440,1050');drivers=[];results=[]
def browser(account):
    d=webdriver.Chrome(options=opt);drivers.append(d);w=WebDriverWait(d,15);d.get('http://127.0.0.1:8081');w.until(lambda x:d.find_element(By.ID,'login').is_displayed());d.find_element(By.ID,'login-username').send_keys(account);d.find_element(By.ID,'login-password').send_keys('123');d.find_element(By.ID,'sign-in').click();w.until(lambda x:d.find_elements(By.CSS_SELECTOR,'.stat-card'));return d,w
def request(d,path,body=None):return d.execute_async_script("const done=arguments[2];fetch('/api/'+arguments[0],{method:arguments[1]?'POST':'GET',headers:arguments[1]?{'Content-Type':'application/x-www-form-urlencoded'}:{},body:arguments[1]?new URLSearchParams(arguments[1]):undefined}).then(async r=>done({status:r.status,body:await r.json()}));",path,body)
def check(name,fn):
    try:actual=fn();results.append({'name':name,'status':'PASS','actual':actual or 'Assertions passed.'})
    except Exception as e:results.append({'name':name,'status':'FAIL','actual':str(e) or type(e).__name__})
try:
    admin,aw=browser('admin');member,mw=browser('user1')
    def isolation():assert request(admin,'me')['body']['role']=='ADMIN' and request(member,'me')['body']['role']=='MEMBER';return 'Two simultaneous browser sessions retained different roles.'
    check('Independent Admin and Member browser sessions',isolation)
    def totals():
        stats=request(admin,'dashboard')['body']['stats'];assert stats['members']==len(request(admin,'data?view=members')['body']);assert stats['trainers']==len(request(admin,'data?view=trainers')['body']);return 'Dashboard includes all members/trainers, matching the desktop COUNT(*) totals.'
    check('Dashboard totals match the desktop app',totals)
    admin.find_element(By.CSS_SELECTOR,'[data-page="members"]').click();aw.until(lambda x:admin.find_elements(By.ID,'add-record'));admin.find_element(By.ID,'add-record').click();aw.until(lambda x:admin.find_element(By.ID,'modal').is_displayed())
    def focus():
        for _ in range(12):
            admin.switch_to.active_element.send_keys(Keys.TAB)
            assert admin.execute_script("return document.querySelector('#modal').contains(document.activeElement)")
        return 'Twelve Tab transitions stayed inside the native modal.'
    check('Keyboard focus stays in dialog',focus)
    def escape():admin.switch_to.active_element.send_keys(Keys.ESCAPE);aw.until(lambda x:not admin.find_element(By.ID,'modal').is_displayed());return 'Escape closed the unsaved member form.'
    check('Dialog Escape dismissal',escape)
    member.execute_cdp_cmd('Emulation.setDeviceMetricsOverride',{'width':390,'height':844,'deviceScaleFactor':1,'mobile':True})
    def outside():
        member.find_element(By.ID,'menu-toggle').click();mw.until(lambda x:member.execute_script("return document.querySelector('#sidebar').getBoundingClientRect().left>=-1"));ActionChains(member).move_to_element_with_offset(member.find_element(By.TAG_NAME,'body'),170,-100).click().perform();mw.until(lambda x:member.execute_script("return document.querySelector('#sidebar').getBoundingClientRect().right<=1"));return 'Outside pointer click closed the mobile drawer.'
    check('Mobile drawer outside-click dismissal',outside)
    trainer,tw=browser('trainer');choices=request(trainer,'options')['body'].get('members',[]);mid=choices[0]['id'] if choices else 1
    def boundary(key,value):
        body={'action':'progress-add','member':mid,'date':dt.date.today().isoformat(),'height':175,'weight':70,'fat':18};body[key]=value;r=request(trainer,'action',body);assert r['status']==400,r;return r['body']['error']
    check('Height below minimum rejected',lambda:boundary('height',49.99))
    check('Body fat above maximum rejected',lambda:boundary('fat',70.01))
    check('Non-finite weight rejected',lambda:boundary('weight','NaN'))
finally:
    for d in drivers:d.quit()
    output=ROOT/'output/stqa/exploratory_results.json';output.write_text(json.dumps({'timestamp':dt.datetime.now().astimezone().isoformat(),'cases':results,'passed':sum(r['status']=='PASS' for r in results),'failed':sum(r['status']=='FAIL' for r in results)},indent=2),encoding='utf8')
    print(json.dumps(results,indent=2));assert all(r['status']=='PASS' for r in results)
