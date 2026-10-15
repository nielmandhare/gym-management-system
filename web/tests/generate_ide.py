"""Create an importable Selenium IDE project with 30 independent regression tests."""
from pathlib import Path
import json, uuid
ROOT=Path(__file__).resolve().parents[2]
def uid(name):return str(uuid.uuid5(uuid.NAMESPACE_URL,'gym-stqa:'+name))
def command(name,target='',value=''):
    return {'id':str(uuid.uuid4()),'comment':'','command':name,'target':target,'targets':[[target,'css:finder']] if target.startswith('css=') else [],'value':value}
def sign_in(role):
    account={'ADMIN':'admin','TRAINER':'trainer','MEMBER':'user1'}[role]
    return [command('open','/'),command('executeScript',"return fetch('/api/logout',{method:'POST'}).then(()=>true);"),command('open','/'),command('waitForElementVisible','id=login-username'),command('type','id=login-username',account),command('type','id=login-password','123'),command('click','id=sign-in'),command('waitForElementVisible','css=#app:not([hidden])'),command('waitForElementPresent','css=.stat-card')]
tests=[]
def add(name,commands):tests.append({'id':uid(name),'name':name,'commands':commands})
admin=[('overview','Overview'),('members','Members'),('trainers','Trainers'),('attendance','Attendance'),('equipment','Equipment'),('products','Products'),('payments','Payment history'),('sales','Product sales'),('workouts','Workout plans'),('progress','Progress tracking'),('sessions','Training sessions'),('memberships','Membership plans'),('users','User management'),('reports','Reports'),('backup','Database backup')]
for key,title in admin:
    add('Admin '+title,sign_in('ADMIN')+[command('click',f'css=[data-page="{key}"]'),command('assertText','id=page-title',title),command('waitForElementNotPresent','css=.loading')])
for role,views in [('TRAINER',[('profile','My profile'),('assigned-members','Assigned members'),('my-attendance','My attendance'),('member-attendance','Member attendance'),('progress','Progress tracking'),('sessions','Training sessions')]),('MEMBER',[('profile','My profile'),('my-attendance','My attendance'),('progress','My progress'),('sessions','My training')])]:
    for key,title in views:
        add(role.title()+' '+title,sign_in(role)+[command('click',f'css=[data-page="{key}"]'),command('assertText','id=page-title',title),command('waitForElementNotPresent','css=.loading'),command('assertElementNotPresent','css=[data-page="users"]')])
add('Unknown username is rejected',[command('open','/'),command('executeScript',"return fetch('/api/logout',{method:'POST'}).then(()=>true);"),command('open','/'),command('waitForElementVisible','id=login-username'),command('type','id=login-username','unknown_stqa_account'),command('type','id=login-password','123'),command('click','id=sign-in'),command('waitForElementVisible','css=#login-error:not(:empty)'),command('assertText','id=login-error',"We couldn't match that username or mobile number.")])
add('Admin incorrect password is rejected',[command('open','/'),command('waitForElementVisible','id=login-username'),command('type','id=login-username','admin'),command('type','id=login-password','incorrect_stqa_password'),command('click','id=sign-in'),command('waitForElementVisible','css=#login-error:not(:empty)'),command('assertText','id=login-error',"The password doesn't match. Try again.")])
add('Password visibility toggle',[command('open','/'),command('waitForElementVisible','id=show-password'),command('click','id=show-password'),command('assertElementPresent','css=#login-password[type="text"]'),command('click','id=show-password'),command('assertElementPresent','css=#login-password[type="password"]')])
add('Member cannot see administrative links',sign_in('MEMBER')+[command('assertElementNotPresent','css=[data-page="equipment"]'),command('assertElementNotPresent','css=[data-page="backup"]'),command('assertElementNotPresent','css=[data-page="reports"]')])
add('Admin sign out',sign_in('ADMIN')+[command('click','id=logout'),command('waitForElementVisible','id=confirm-action'),command('click','id=confirm-action'),command('waitForElementVisible','css=#login:not([hidden])')])
project={'id':uid('project'),'version':'2.0','name':'Gym Management System STQA','url':'http://127.0.0.1:8081','tests':tests,'suites':[{'id':uid('suite'),'name':'Three-role regression','persistSession':False,'parallel':False,'timeout':30000,'tests':[test['id'] for test in tests]}],'urls':['http://127.0.0.1:8081/'],'plugins':[]}
output=ROOT/'web/tests/Gym_STQA.side';output.write_text(json.dumps(project,indent=2),encoding='utf-8');print(str(output),len(tests),'IDE tests')
