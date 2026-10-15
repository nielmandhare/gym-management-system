"""Package source and requested report artifacts; exclude machine-local state."""
from pathlib import Path
import hashlib,json,zipfile
ROOT=Path(__file__).resolve().parents[2];OUT=ROOT/'output';OUT.mkdir(exist_ok=True)
files=[]
private={'src/resources/db.properties','src/resources/mysql.cnf','PASSWORDS.txt'}
for directory in ['src','web','lib','nbproject','test']:
    for file in (ROOT/directory).rglob('*'):
        if file.is_file() and '__pycache__' not in file.parts and file.suffix!='.pyc' and file.relative_to(ROOT).as_posix() not in private and not file.relative_to(ROOT).as_posix().startswith('nbproject/private/'):files.append(file)
for name in ['.gitignore','AGENTS.md','README.md','PROJECT_DEMO_GUIDE.md','STQA Mini Project Report.pdf','Start-Demo.ps1','Build.ps1','Run.ps1','Setup-Database.ps1','Stop-Database.ps1','Verify.ps1','Run-Web.ps1','Stop-Web.ps1','Verify-Web.ps1','Verify-WebJava.ps1','Run-SeleniumIDE.ps1','build.xml','manifest.mf']:
    file=ROOT/name
    if file.exists():files.append(file)
for name in ['STQA_Submission/01_Report/Gym_Management_System_SRS_Final.docx','STQA_Submission/01_Report/Gym_Management_System_SRS_Final.pdf','STQA_Submission/05_Reference_PDFs/STQA_Lab_Manual.pdf']:
    files.append(ROOT/name)
for name in ['gms-database-init.sql','local-demo-seed.sql','user-accounts-migration.sql','three-role-migration.sql','role-demo-accounts.sql']:
    files.append(ROOT/'backups'/name)
reports=ROOT/'output/stqa'
for file in reports.iterdir():
    if file.is_file() and file.suffix in {'.pdf','.json','.csv','.html','.md'}:files.append(file)
for directory in ['screenshots','gym-reports']:
    files.extend(file for file in (reports/directory).rglob('*') if file.is_file())
metadata=json.loads((reports/'REPORT_METADATA.json').read_text(encoding='utf8'))
for name in metadata['browser_sources'].values():
    result=ROOT/name
    files.extend(result.with_name(filename) for filename in ['results.json','results.csv','report.html'])
    files.extend(file for file in (result.parent/'screenshots').glob('*.png'))
files.append(ROOT/metadata['ide']['source'])
manifest=[]
for file in sorted(set(files)):
    assert file.is_file(),file
    name=file.relative_to(ROOT).as_posix()
    assert not name.startswith(('.local/','logs/','build/','dist/','.git/')) and name not in private
    manifest.append({'path':name,'bytes':file.stat().st_size,'sha256':hashlib.sha256(file.read_bytes()).hexdigest()})
manifest_path=OUT/'SUBMISSION_MANIFEST.json';manifest_path.write_text(json.dumps({'files':manifest,'excludes':['local credentials','MySQL data/config/seed marker','generated SQL backups','application logs','build outputs','dependency caches','unrelated project folders']},indent=2),encoding='utf8')
archive=OUT/'Gym_Web_STQA_Submission.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
    for item in manifest:z.write(ROOT/item['path'],'Gym_Management_System/'+item['path'])
    z.write(manifest_path,'Gym_Management_System/SUBMISSION_MANIFEST.json')
with zipfile.ZipFile(archive) as z:
    assert z.testzip() is None
    names=z.namelist();assert not any(name.endswith('/db.properties') or name.endswith('/mysql.cnf') for name in names)
print(archive,len(manifest),'files',archive.stat().st_size,'bytes; archive integrity verified')
