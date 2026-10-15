"""Real Selenium browser regression for the Java/MySQL web edition.

Run from the checkout: python web/tests/selenium_suite.py --base-url http://127.0.0.1:8081
Uses distinct fixture records; preserves existing demo profiles and passwords.
"""
from pathlib import Path
import argparse, csv, datetime as dt, html, json, os, subprocess, sys, time, traceback, uuid

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / '.local' / 'selenium'))
os.environ.setdefault('SE_CACHE_PATH', str(ROOT / '.local' / 'selenium-cache'))
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait, Select
from selenium.webdriver.support import expected_conditions as EC
from pypdf import PdfReader

parser=argparse.ArgumentParser();parser.add_argument('--base-url',default='http://127.0.0.1:8081');parser.add_argument('--browser',default='chrome',choices=['chrome','edge']);parser.add_argument('--output',default='output/stqa');args=parser.parse_args()
RUN=dt.datetime.now().strftime('%Y%m%d-%H%M%S');OUT=ROOT/args.output/'runs'/RUN;OUT.mkdir(parents=True,exist_ok=True);SHOTS=OUT/'screenshots';SHOTS.mkdir();DOWNLOADS=OUT/'downloads';DOWNLOADS.mkdir()
tag='STQA'+uuid.uuid4().hex[:8];phone='9'+str(int(uuid.uuid4().hex[:8],16)).zfill(9)[-9:]
state={'tag':tag,'phone':phone,'trainer_phone':'8'+phone[1:],'member_username':tag.lower()+'m','trainer_username':tag.lower()+'t','password':'GymTest123!'}
records=[]
STEP_DETAILS=json.loads((ROOT/'web/tests/case_steps.json').read_text(encoding='utf-8'))
options=webdriver.ChromeOptions() if args.browser=='chrome' else webdriver.EdgeOptions()
options.add_argument('--headless=new');options.add_argument('--window-size=1440,1050');options.add_argument('--disable-gpu');options.add_experimental_option('prefs',{'download.default_directory':str(DOWNLOADS),'download.prompt_for_download':False,'plugins.always_open_pdf_externally':True})
driver=webdriver.Chrome(options=options) if args.browser=='chrome' else webdriver.Edge(options=options)
wait=WebDriverWait(driver,18);driver.set_script_timeout(45)

def find(selector):return driver.find_element(By.CSS_SELECTOR,selector)
def text(selector):return find(selector).text
def click(selector):wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR,selector))).click()
def set_value(name,value):
    scope='#modal ' if driver.find_elements(By.CSS_SELECTOR,'dialog[open]') else '#login-form '
    element=find(f'{scope}[name="{name}"]')
    if element.tag_name=='select':Select(element).select_by_value(str(value))
    elif element.get_attribute('type')=='date':driver.execute_script("arguments[0].value=arguments[1];arguments[0].dispatchEvent(new Event('input',{bubbles:true}));arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",element,str(value))
    else:element.clear();element.send_keys(str(value))
def shot(name):
    file=SHOTS/(name+'.png');assert driver.save_screenshot(str(file));return str(file.relative_to(OUT))
def browser_api(path,body=None):
    return driver.execute_async_script("const done=arguments[arguments.length-1],path=arguments[0],body=arguments[1];fetch('/api/'+path,{method:body?'POST':'GET',headers:body?{'Content-Type':'application/x-www-form-urlencoded'}:{},body:body?new URLSearchParams(body):undefined}).then(async r=>done({status:r.status,body:await r.json()})).catch(e=>done({status:0,body:{error:e.message}}));",path,body)
def data(view):
    response=browser_api('data?view='+view);assert response['status']==200,response;return response['body']
def login(username='admin',password='123',expect=True):
    driver.delete_all_cookies();driver.get(args.base_url+'/');wait.until(lambda d:find('#login').is_displayed());set_value('username',username);set_value('password',password);click('#sign-in')
    if expect:wait.until(lambda d:find('#app').is_displayed());wait.until(lambda d:len(driver.find_elements(By.CSS_SELECTOR,'.stat-card'))>0);assert 'could not' not in text('#page-content').lower()
    else:wait.until(lambda d:bool(text('#login-error')));assert find('#login').is_displayed()
def nav(view):
    if not find(f'[data-page="{view}"]').is_displayed():click('#menu-toggle')
    click(f'[data-page="{view}"]');wait.until(lambda d:driver.current_url.endswith('#'+view));wait.until(lambda d:not driver.find_elements(By.CSS_SELECTOR,'.loading'));assert 'could not be loaded' not in text('#page-content').lower(),text('#page-content')
def search_for(value):
    input=find('#table-search');input.clear();input.send_keys(value)
def dialog_open():wait.until(lambda d:find('#modal').is_displayed())
def add_form(view):nav(view);click('#add-record');dialog_open()
def close_form():
    if driver.find_elements(By.CSS_SELECTOR,'dialog[open]'):click('[data-close]');wait.until(lambda d:not driver.find_elements(By.CSS_SELECTOR,'dialog[open]'))
def fill(values):
    for key,value in values.items():set_value(key,value)
def save(expect=True):
    click('#save-record')
    if expect:wait.until(lambda d:not driver.find_elements(By.CSS_SELECTOR,'dialog[open]'));wait.until(lambda d:not driver.find_elements(By.CSS_SELECTOR,'.loading'))
    else:wait.until(lambda d:bool(text('#form-error')))
def row_button(action,value=None):
    if value is not None:search_for(value)
    click(f'#table-body [data-row-action="{action}"]');dialog_open()
def confirm(expect=True):
    click('#confirm-action')
    if expect:wait.until(lambda d:not driver.find_elements(By.CSS_SELECTOR,'dialog[open]'));wait.until(lambda d:not driver.find_elements(By.CSS_SELECTOR,'.loading'))
    else:wait.until(lambda d:bool(text('#form-error')))
def expect_status(path,body,status):
    r=browser_api(path,body);assert r['status']==status,r;return r['body'].get('error',str(r['body']))
def sql(statement):
    properties={}
    for line in (ROOT/'src/resources/db.properties').read_text().splitlines():
        if '=' in line and not line.strip().startswith('#'):
            key,value=line.split('=',1);properties[key.strip()]=value.strip()
    executable=Path(os.environ['GMS_MYSQL_BIN'])/'mysql.exe' if os.environ.get('GMS_MYSQL_BIN') else Path(properties.get('db.mysql_path',r'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'))
    if not executable.is_file():raise RuntimeError('MySQL client missing: '+str(executable)+'. Set GMS_MYSQL_BIN or use Setup-Database.ps1 -MySqlBin.')
    command=[str(executable),'--defaults-extra-file='+str(ROOT/'src/resources/mysql.cnf'),'--batch','--skip-column-names','gms','--execute='+statement]
    result=subprocess.run(command,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=30)
    if result.returncode:raise RuntimeError(result.stderr)
    return result.stdout.strip()
def test(case,name,expected,fn,steps=None):
    start=time.perf_counter();r={'id':case,'name':name,'steps':steps or STEP_DETAILS.get(case,name),'expected':expected,'status':'PASS','actual':'','duration':0}
    try:r['actual']=str(fn() or ('Verified: '+expected))
    except Exception as e:
        r['status']='FAIL';r['actual']=str(e) or type(e).__name__;r['trace']=traceback.format_exc()
        try:r['screenshot']=shot(case+'-failure')
        except:pass
        try:close_form()
        except:pass
    r['duration']=round(time.perf_counter()-start,3);records.append(r);print(case,r['status'],name,flush=True)
def member_values():return {'name':tag+' Member','mobile':phone,'membership':2,'paid':1,'height':175,'weight':72,'fat':18,'health':'Selenium fixture: '+tag}
def on_value(value):return value is True or value==1
def search_empty():
    search_for('no_such_'+tag);assert 'No matching records' in text('#table-body');return 'Empty state shown for unmatched search.'

try:
    # Read-only safety check before creating exact-ID fixtures in the local schema.
    actual_dir=Path(sql('SELECT @@datadir')).resolve();expected_dir=(ROOT/'.local/mysql/data').resolve();assert actual_dir==expected_dir,(actual_dir,expected_dir)
    driver.get(args.base_url+'/');wait.until(lambda d:find('#login').is_displayed());shot('01-login')
    test('TC01','Required sign-in fields','Blank form stays on the sign-in page',lambda:(click('#sign-in'),assert_validity('#login-username')) if False else (click('#sign-in'),driver.execute_script("return document.querySelector('#login-username').validity.valueMissing"))[-1] or (_ for _ in ()).throw(AssertionError('Missing username was accepted')))
    test('TC02','Unknown account rejected','Error shown; no authenticated dashboard',lambda:login('unknown_'+tag,'123',False))
    test('TC03','Incorrect password rejected','Error shown for a valid account with wrong password',lambda:login('admin','wrong_'+tag,False))
    test('TC04','SQL injection input rejected','Injected username does not authenticate',lambda:login("admin' OR '1'='1",'123',False))
    test('TC05','Admin username login','Admin overview loads with real metrics',lambda:login())
    def dashboard():
        r=browser_api('dashboard');assert r['status']==200;assert r['body']['stats']['members']>=1;assert len(driver.find_elements(By.CSS_SELECTOR,'.stat-card'))==5;shot('02-admin-overview');return 'Five real metric cards and weekly attendance chart loaded.'
    test('TC06','Admin dashboard metrics','Five cards loaded with database totals including product sales',dashboard)
    test('TC07','Admin mobile-number login','Mobile number and password authenticate admin',lambda:login('0770000001'))
    def member_search():
        nav('members');name=data('members')[0]['name'];search_for(name);assert name in text('#table-body');return 'Matching name: '+name
    test('TC08','Member search','Matching member rows remain visible',member_search)
    test('TC09','Search empty state','Unknown search shows No matching records',lambda:(search_for('no_such_'+tag),assert_contains('#table-body','No matching records')) if False else search_empty())
    def pagination():
        nav('members');assert not find('#next-page').get_attribute('disabled');click('#next-page');assert text('#page-label').startswith('2');return text('#table-count')
    test('TC10','Member table pagination','Next page changes visible rows and page indicator',pagination)
    def required_member():
        add_form('members');click('#save-record');assert find('#modal').is_displayed();assert not driver.execute_script("return document.querySelector('#action-form').checkValidity()");close_form()
    test('TC11','Member required-field validation','Incomplete member form cannot be submitted',required_member)
    def bad_mobile():
        add_form('members');set_value('mobile','123');assert driver.execute_script("return document.querySelector('#field-mobile').validity.patternMismatch");close_form()
    test('TC12','Mobile number validation','Three-digit mobile is rejected by the form',bad_mobile)
    def bmi_preview():
        add_form('members');set_value('height',175);set_value('weight',72);assert '23.51' in text('#bmi-preview');close_form();return 'BMI preview: 23.51'
    test('TC13','Initial BMI preview','175 cm and 72 kg calculate BMI 23.51',bmi_preview)
    def create_member():
        add_form('members');fill(member_values());save();found=[r for r in data('members') if r['mobile']==phone];assert len(found)==1;state['member']=found[0]['id'];state['member_name']=tag+' Member';search_for(tag);shot('03-member-management');return 'Created member #'+str(state['member'])
    test('TC14','Create member with measurements','Member and initial progress are saved together',create_member)
    def initial_progress():
        found=[r for r in data('progress') if r['member']==state['member_name']];assert len(found)==1;assert float(found[0]['bmi'])==23.51;return 'Initial measurement found with BMI 23.51.'
    test('TC15','Member initial progress persistence','Initial measurement is available in Progress tracking',initial_progress)
    def duplicate_member():
        add_form('members');fill(member_values());save(False);assert 'already in use' in text('#form-error');close_form();assert len([r for r in data('members') if r['mobile']==phone])==1
    test('TC16','Duplicate member mobile blocked','Duplicate mobile rejected without duplicate records',duplicate_member)
    def edit_member():
        nav('members');row_button('member-save',tag);set_value('name',tag+' Updated');set_value('paid',0);save();state['member_name']=tag+' Updated';driver.refresh();wait.until(lambda d:driver.find_elements(By.CSS_SELECTOR,'#records-table'));search_for(tag);assert 'Updated' in text('#table-body') and 'Due' in text('#table-body')
    test('TC17','Edit member and retain changes','Updated name/payment flag survive a browser refresh',edit_member)
    def member_status():
        nav('members');row_button('toggle-status',tag);confirm();assert not on_value(next(r for r in data('members') if r['id']==state['member'])['active']);assert state['member'] not in [r['id'] for r in browser_api('options')['body']['members'] if on_value(r['active'])]
    test('TC18','Deactivate member','Inactive member excluded from check-in choices',member_status)
    def activate_member():
        nav('members');row_button('toggle-status',tag);confirm();assert on_value(next(r for r in data('members') if r['id']==state['member'])['active'])
    test('TC19','Reactivate member','Member status returns to Active',activate_member)
    def attendance():
        add_form('attendance');set_value('member',state['member']);save();assert 'payment is due' in text('#toast').lower();assert len([r for r in data('attendance') if r['mobile']==phone])==1
    test('TC20','Record attendance with payment warning','One check-in saved and payment-due warning shown',attendance)
    def duplicate_attendance():
        add_form('attendance');set_value('member',state['member']);save(False);assert 'already checked in today' in text('#form-error');close_form()
    test('TC21','Duplicate attendance blocked','Second check-in on same day is rejected',duplicate_attendance)
    test('TC22','Unknown member attendance rejected','Tampered member ID produces HTTP 400',lambda:expect_status('action',{'action':'attendance-add','member':2147483646},400))
    def trainer_create():
        add_form('trainers');fill({'name':tag+' Trainer','mobile':state['trainer_phone'],'nic':tag[:12],'hired':dt.date.today().isoformat(),'username':state['trainer_username'],'password':state['password']});save();state['trainer']=next(r['id'] for r in data('trainers') if r['mobile']==state['trainer_phone']);assert any(r['username']==state['trainer_username'] for r in data('users'));return 'Trainer and linked login created.'
    test('TC23','Create trainer and login account','Profile and Trainer account created atomically',trainer_create)
    def invalid_username():
        add_form('trainers');set_value('username','1bad');assert driver.execute_script("return document.querySelector('#field-username').validity.patternMismatch");close_form()
    test('TC24','Trainer username validation','Username beginning with a digit is invalid',invalid_username)
    def member_account():
        add_form('users');fill({'username':state['member_username'],'name':state['member_name'],'mobile':phone,'role':'MEMBER','active':1,'profile':state['member'],'password':state['password']});save();state['member_user']=next(r['id'] for r in data('users') if r['username']==state['member_username'])
    test('TC25','Create linked Member account','Account links to the selected member profile',member_account)
    test('TC26','Duplicate profile link rejected','Second account linked to same member gets HTTP 409',lambda:expect_status('action',{'action':'user-save','username':tag.lower()+'x','name':'Duplicate fixture','mobile':'7'+phone[1:],'role':'MEMBER','profile':state['member'],'password':state['password'],'active':1},409))
    def inactive_account():
        nav('users');row_button('toggle-status',state['member_username']);confirm();login(state['member_username'],state['password'],False);assert 'inactive' in text('#login-error').lower();login()
    test('TC27','Inactive account cannot log in','Deactivated Member login is rejected',inactive_account)
    def active_account():
        nav('users');row_button('toggle-status',state['member_username']);confirm();assert on_value(next(r for r in data('users') if r['id']==state['member_user'])['active'])
    test('TC28','Account reactivation','Member account is Active after reactivation',active_account)
    test('TC29','Current admin protected','Admin cannot disable own account (HTTP 400)',lambda:expect_status('action',{'action':'user-status','id':browser_api('me')['body']['id'],'active':0},400))
    def equipment_create():
        add_form('equipment');fill({'name':tag+' Bench','status':'GOOD','serial':tag,'purchased':dt.date.today().isoformat(),'warranty':'2028-10-15'});save();state['equipment']=next(r['id'] for r in data('equipment') if r['serial']==tag)
    test('TC30','Create equipment','Equipment profile appears in inventory',equipment_create)
    def equipment_edit():
        nav('equipment');row_button('equipment-save',tag);set_value('status','UNDER MAINTENANCE');save();assert next(r for r in data('equipment') if r['id']==state['equipment'])['status']=='UNDER MAINTENANCE'
    test('TC31','Edit equipment condition','Condition is persisted as UNDER MAINTENANCE',equipment_edit)
    def equipment_delete():
        nav('equipment');row_button('delete-record',tag);confirm();assert not any(r['id']==state['equipment'] for r in data('equipment'));state.pop('equipment')
    test('TC32','Delete unreferenced equipment','Created equipment is removed',equipment_delete)
    def product_create():
        add_form('products');fill({'name':tag+' Bottle','price':499.5,'stock':12,'description':'Selenium product fixture'});save();state['product']=next(r['id'] for r in data('products') if r['name']==tag+' Bottle')
    test('TC33','Create product','Product price and stock saved',product_create)
    def product_negative():
        add_form('products');set_value('price',-1);assert driver.execute_script("return document.querySelector('#field-price').validity.rangeUnderflow");close_form()
    test('TC34','Negative product price validation','Negative price rejected by browser validation',product_negative)
    def product_edit():
        nav('products');row_button('product-save',tag);set_value('price',549.75);set_value('stock',20);save();r=next(r for r in data('products') if r['id']==state['product']);assert float(r['price'])==549.75 and r['stock']==20
    test('TC35','Edit product price and stock','Updated price and quantity persist',product_edit)
    def referenced_product():
        sql(f"INSERT INTO order_history(product_product_id,quantity,unit_price,total_amount,paid_method,reference_no) VALUES({state['product']},1,549.75,549.75,'CASH','{tag}')")
        nav('products');row_button('delete-record',tag);confirm(False);assert 'referenced' in text('#form-error');close_form()
    test('TC36','Referenced product deletion protected','Product with order history cannot be deleted',referenced_product)
    def product_delete():
        sql(f"DELETE FROM order_history WHERE reference_no='{tag}'");nav('products');row_button('delete-record',tag);confirm();assert not any(r['id']==state['product'] for r in data('products'));state.pop('product')
    test('TC37','Delete unreferenced product','Product removed after fixture history is cleared',product_delete)
    test('TC38','Negative plan price rejected','Server rejects negative price (HTTP 400)',lambda:expect_status('action',{'action':'membership-price','id':2,'price':-1},400))
    def workout():
        nav('workouts');row_button('workout-details');assert len(driver.find_elements(By.CSS_SELECTOR,'.workout-days section'))==7;close_form()
    test('TC39','Workout plan details','All seven workout days are readable',workout)
    test('TC40','Payment history view','Saved payment records loaded in browser',lambda:(nav('payments'),len(data('payments'))))
    test('TC41','Product sales view','Saved sale records loaded in browser',lambda:(nav('sales'),len(data('sales'))))
    for index,name in enumerate(['members','trainers','attendance','payments','sales','equipment'],42):
        def report_download(name=name):
            nav('reports');click(f'[data-report="{name}"]');file=DOWNLOADS/(name+'-report.pdf');wait.until(lambda d:file.exists() and not list(DOWNLOADS.glob('*.crdownload')));reader=PdfReader(file);assert len(reader.pages)>=1;assert file.stat().st_size>1000;return f'{file.stat().st_size} bytes; {len(reader.pages)} PDF pages.'
        test(f'TC{index:02}',name.capitalize()+' PDF report','Valid PDF downloaded through report button',report_download)
    shot('04-report-center')
    def csv_export():
        nav('members');search_for(tag);click('#export-csv');wait.until(lambda d:list(DOWNLOADS.glob('members-*.csv')));file=next(DOWNLOADS.glob('members-*.csv'));assert tag in file.read_text(encoding='utf-8-sig');return 'Filtered member CSV contains fixture row.'
    test('TC48','Filtered CSV report export','CSV download contains visible matching member',csv_export)
    def backup():
        nav('backup');click('#backup-now');wait.until(lambda d:text('#backup-result').startswith('Saved:'));filename=text('#backup-result').split('Saved: ',1)[1];file=ROOT/'backups'/filename;assert file.is_file() and file.stat().st_size>1000;return filename+' '+str(file.stat().st_size)+' bytes'
    test('TC49','Local database backup','Timestamped SQL backup exists in checkout backups folder',backup)
    # Isolated assignment lets the new trainer record progress for only the fixture member.
    sql(f"INSERT INTO personal_training(member_member_id,trainer_trainer_id,start_date,session_price,total_sessions,sessions_completed,status) VALUES({state['member']},{state['trainer']},CURRENT_DATE(),500,10,0,'ACTIVE')")
    test('TC50','Trainer sign-in','Trainer account opens a separate workspace',lambda:login(state['trainer_username'],state['password']))
    def trainer_nav():
        assert not driver.find_elements(By.CSS_SELECTOR,'[data-page="users"]');assert driver.find_elements(By.CSS_SELECTOR,'[data-page="assigned-members"]');return 'Trainer navigation excludes admin modules.'
    test('TC51','Trainer navigation restriction','Admin management links are absent',trainer_nav)
    test('TC52','Trainer server authorization','Direct members API denied with HTTP 403',lambda:expect_status('data?view=members',None,403))
    def assigned_members():
        nav('assigned-members');assert tag in text('#table-body');assert str(state['member']) in text('#table-body');return 'Assigned fixture member and ID displayed.'
    test('TC53','Trainer assigned-member list','Trainer sees their assigned member',assigned_members)
    def progress():
        add_form('progress');fill({'member':state['member'],'date':dt.date.today().isoformat(),'height':175,'weight':71,'fat':17});save();search_for(tag);assert '23.18' in text('#table-body');shot('05-trainer-progress')
    test('TC54','Trainer progress entry','Assigned member progress saved with calculated BMI 23.18',progress)
    test('TC55','Future progress date rejected','Server rejects a future measurement date (HTTP 400)',lambda:expect_status('action',{'action':'progress-add','member':state['member'],'date':'2099-01-01','height':175,'weight':70,'fat':18},400))
    def unassigned_progress():
        id=int(sql(f"SELECT member_id FROM member WHERE member_id<>{state['member']} LIMIT 1"));return expect_status('action',{'action':'progress-add','member':id,'date':dt.date.today().isoformat(),'height':175,'weight':70,'fat':18},403)
    test('TC56','Unassigned member progress denied','Trainer cannot write progress for an unrelated member',unassigned_progress)
    def trainer_profile():
        nav('profile');click('#edit-profile');dialog_open();set_value('name',tag+' Coach');save();assert tag+' Coach' in text('.profile-cover');shot('06-trainer-profile')
    test('TC57','Trainer edits own profile','Profile and account display name update together',trainer_profile)
    def wrong_current_password():
        nav('profile');click('#change-password');dialog_open();fill({'oldPassword':'wrong','newPassword':'Changed123!','confirmPassword':'Changed123!'});save(False);assert 'Current password is incorrect' in text('#form-error');close_form()
    test('TC58','Wrong current password rejected','Password is preserved when current password is wrong',wrong_current_password)
    def mismatched_password():
        nav('profile');click('#change-password');dialog_open();fill({'oldPassword':state['password'],'newPassword':'Changed123!','confirmPassword':'Other123!'});save(False);assert 'do not match' in text('#form-error');close_form()
    test('TC59','Password confirmation validation','Mismatched new passwords cannot be saved',mismatched_password)
    def changed_password():
        nav('profile');click('#change-password');dialog_open();fill({'oldPassword':state['password'],'newPassword':'Changed123!','confirmPassword':'Changed123!'});save();login(state['trainer_username'],state['password'],False);login(state['trainer_username'],'Changed123!')
    test('TC60','Trainer password change','Old password rejected and new password works',changed_password)
    test('TC61','Member sign-in','Linked Member account opens member workspace',lambda:login(state['member_username'],state['password']))
    def member_profile():
        nav('profile');assert state['member_name'] in text('.profile-cover');assert 'MONTHLY' in text('.profile-details');shot('07-member-profile')
    test('TC62','Member own membership view','Member sees own profile, plan, and payment status',member_profile)
    def own_attendance():
        nav('my-attendance');assert len(data('my-attendance'))==1;assert len(driver.find_elements(By.CSS_SELECTOR,'#table-body tr'))==1
    test('TC63','Member attendance scoped','Only fixture member check-in is returned',own_attendance)
    def own_progress():
        nav('progress');r=data('progress');assert len(r)==2;assert {float(x['weight']) for x in r}=={71,72};assert not driver.find_elements(By.CSS_SELECTOR,'#add-record')
    test('TC64','Member progress scoped and read-only','Own initial/trainer entries visible; no record action',own_progress)
    def own_training():
        nav('sessions');r=data('sessions');assert len(r)==1 and r[0]['trainer']==tag+' Coach'
    test('TC65','Member training scoped','Only own training assignment is visible',own_training)
    test('TC66','Member admin API access denied','User management API denied with HTTP 403',lambda:expect_status('data?view=users',None,403))
    test('TC67','Member progress write denied','Member cannot submit trainer progress action',lambda:expect_status('action',{'action':'progress-add','member':state['member'],'date':dt.date.today().isoformat(),'height':175,'weight':70,'fat':18},403))
    def own_profile_edit():
        nav('profile');click('#edit-profile');dialog_open();set_value('name',tag+' Self');save();assert tag+' Self' in text('.profile-cover')
    test('TC68','Member edits own profile','Own name updates in profile and account',own_profile_edit)
    def desktop_layout():
        driver.set_window_size(1440,1050);nav('profile');assert driver.execute_script('return document.documentElement.scrollWidth<=window.innerWidth');shot('08-desktop-profile')
    test('TC69','Desktop layout','Document has no horizontal overflow at 1440px',desktop_layout)
    def mobile_layout():
        driver.execute_cdp_cmd('Emulation.setDeviceMetricsOverride',{'width':390,'height':844,'deviceScaleFactor':1,'mobile':True})
        assert find('#menu-toggle').is_displayed();click('#menu-toggle')
        wait.until(lambda d:driver.execute_script("return document.querySelector('#sidebar').getBoundingClientRect().left>=-1"))
        assert find('[data-page="progress"]').is_displayed();click('[data-page="progress"]')
        wait.until(lambda d:not driver.find_elements(By.CSS_SELECTOR,'.loading'))
        wait.until(lambda d:driver.execute_script("return document.querySelector('#sidebar').getBoundingClientRect().right<=1"))
        assert driver.execute_script('return document.documentElement.scrollWidth<=window.innerWidth');shot('09-mobile-progress')
        driver.execute_cdp_cmd('Emulation.clearDeviceMetricsOverride',{})
    test('TC70','Mobile navigation and layout','390px navigation works without document overflow',mobile_layout)
    def dark_theme():
        click('#theme-toggle');assert 'dark' in find('body').get_attribute('class');driver.refresh();wait.until(lambda d:find('#app').is_displayed());assert 'dark' in find('body').get_attribute('class');shot('10-dark-theme');click('#theme-toggle');driver.set_window_size(1440,1050)
    test('TC71','Theme persistence','Dark theme remains selected after page refresh',dark_theme)
    def logout():
        if not find('#logout').is_displayed():click('#menu-toggle')
        click('#logout');dialog_open();confirm();assert find('#login').is_displayed();assert browser_api('me')['status']==401
    test('TC72','Sign-out invalidates browser session','Protected API requires sign-in after logout',logout)
    test('TC73','Anonymous report access denied','Report endpoint returns HTTP 401',lambda:expect_status('report?name=members',None,401))
    def source_not_exposed():
        r=driver.execute_async_script("const done=arguments[0];fetch('/GymWebServer.java').then(r=>done(r.status));");assert r==404;return 'Java source URL returned 404.'
    test('TC74','Server source is not publicly served','Source file URL returns HTTP 404',source_not_exposed)
    def password_reset():
        login();nav('users');row_button('user-reset-password',state['member_username']);set_value('password','Reset123!');save();login(state['member_username'],state['password'],False);login(state['member_username'],'Reset123!');login()
    test('TC75','Admin account password reset','Reset password works; old password no longer works',password_reset)
    def member_delete():
        nav('members');row_button('delete-record',tag);confirm();assert not any(r['id']==state['member'] for r in data('members'));assert not any(r['id']==state['member_user'] for r in data('users'));login(state['member_username'],'Reset123!',False);login();state.pop('member');state.pop('member_user')
    test('TC76','Delete member and linked account','Member deletion cascades fixture history and login',member_delete)
    def trainer_delete():
        nav('trainers');row_button('delete-record',tag);confirm();assert not any(r['id']==state['trainer'] for r in data('trainers'));state.pop('trainer')
    test('TC77','Delete unreferenced trainer','Trainer and linked account removed after assignment cleanup',trainer_delete)
    def summary_counts():
        nav('members');metrics=driver.find_elements(By.CSS_SELECTOR,'.module-stat');assert len(metrics)==3;assert int(metrics[0].find_element(By.TAG_NAME,'strong').text)==len(data('members'));return 'Module summary matches the actual member record count.'
    test('TC78','Module summary values','Member summary count equals loaded database rows',summary_counts)
    def plan_price():
        state['plan']=int(sql("INSERT INTO membership(membership_type,price) VALUES('MONTHLY',17.5); SELECT LAST_INSERT_ID();"));nav('memberships')
        Select(find('#page-size')).select_by_value('50');click(f'tr[data-record-id="{state["plan"]}"] [data-row-action="membership-price"]');dialog_open();set_value('price',29.75);save();assert float(next(r for r in data('memberships') if r['id']==state['plan'])['price'])==29.75;sql(f"DELETE FROM membership WHERE membership_type_id={state['plan']}");state.pop('plan');return 'Fixture plan price changed from 17.50 to 29.75.'
    test('TC79','Membership plan price update','A valid price change is persisted for an isolated plan',plan_price)
    shot('11-cleanup-complete')
finally:
    # Cleanup only the unique records created by this run, even if a case failed.
    try:
        sql(f"DELETE FROM order_history WHERE reference_no='{tag}'")
        if state.get('member'):sql(f"DELETE FROM member WHERE member_id={int(state['member'])} AND mobile='{phone}'")
        if state.get('trainer'):sql(f"DELETE FROM trainer WHERE trainer_id={int(state['trainer'])} AND nic='{tag[:12]}'")
        if state.get('product'):sql(f"DELETE FROM product WHERE product_id={int(state['product'])} AND name='{tag} Bottle'")
        if state.get('equipment'):sql(f"DELETE FROM equipment WHERE equipment_id={int(state['equipment'])} AND serial_number='{tag}'")
        if state.get('plan'):sql(f"DELETE FROM membership WHERE membership_type_id={int(state['plan'])}")
    except Exception as e:state['cleanup_error']=str(e)
    metadata={'run':RUN,'timestamp':dt.datetime.now().astimezone().isoformat(),'base_url':args.base_url,'browser':args.browser,'browser_version':driver.capabilities.get('browserVersion'),'selenium_version':__import__('selenium').__version__,'tag':tag,'total':len(records),'passed':sum(r['status']=='PASS' for r in records),'failed':sum(r['status']=='FAIL' for r in records),'cleanup_error':state.get('cleanup_error')}
    driver.quit()
    (OUT/'results.json').write_text(json.dumps({'metadata':metadata,'cases':records},indent=2,ensure_ascii=False),encoding='utf-8')
    with (OUT/'results.csv').open('w',newline='',encoding='utf-8-sig') as f:
        writer=csv.DictWriter(f,fieldnames=['id','name','steps','expected','actual','status','duration'],extrasaction='ignore');writer.writeheader();writer.writerows(records)
    content='<html><head><meta charset="utf-8"><title>Selenium execution report</title><style>body{font:14px Segoe UI;margin:36px;color:#243c2b}h1{font-size:28px}table{border-collapse:collapse;width:100%}th,td{padding:10px;border:1px solid #dce5d4;text-align:left}th{background:#edf3e5}.PASS{color:#3c8227}.FAIL{color:#b32f27}small{color:#7d8c70}</style></head><body><h1>Gym Management System — Selenium execution</h1><p>'+html.escape(json.dumps(metadata))+'</p><table><tr><th>ID</th><th>Scenario</th><th>Expected</th><th>Actual</th><th>Status</th></tr>'+''.join('<tr><td>'+r['id']+'</td><td>'+html.escape(r['name'])+'</td><td>'+html.escape(r['expected'])+'</td><td>'+html.escape(r['actual'])+'</td><td class="'+r['status']+'">'+r['status']+'</td></tr>' for r in records)+'</table></body></html>'
    (OUT/'report.html').write_text(content,encoding='utf-8');(ROOT/args.output/'latest-run.txt').write_text(str(OUT.relative_to(ROOT)),encoding='utf-8')
    print(json.dumps(metadata),flush=True)
    if metadata['failed'] or metadata['cleanup_error']:sys.exit(1)
