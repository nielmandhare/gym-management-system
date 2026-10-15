"""Seven portrait UML views of the existing Gym Management System.

Run with Python, Pillow installed. PNGs and editable SVGs are generated locally.
The white background, cyan headings and fine orthogonal connectors follow the
visual references in friends project; all content describes this gym project.
"""
from pathlib import Path
from html import escape
import math
import os
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'diagrams'
OUT.mkdir(exist_ok=True)
W, H = 1400, 1900
BLACK, GREY, CYAN = '#202020', '#626262', '#bedfe7'
BLUE, GREEN, ORANGE = '#668eae', '#8b9e62', '#bc8a52'
FONT_DIR = Path(os.environ.get('WINDIR', 'C:/Windows')) / 'Fonts'


class Canvas:
    def __init__(self):
        self.im = Image.new('RGB', (W, H), 'white')
        self.d = ImageDraw.Draw(self.im)
        self.svg = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">', '<rect width="100%" height="100%" fill="white"/>']

    def text(self, x, y, value, size=28, center=False, bold=False, color=BLACK, halo=False):
        font = ImageFont.truetype(str(FONT_DIR / ('arialbd.ttf' if bold else 'arial.ttf')), size)
        anchor = 'mm' if center else 'lm'
        if halo:
            bb = self.d.textbbox((x,y), value, font=font, anchor=anchor)
            self.d.rectangle((bb[0]-5,bb[1]-3,bb[2]+5,bb[3]+3),fill='white')
        self.d.text((x,y), value, font=font, fill=color, anchor=anchor)
        self.svg.append(f'<text x="{x}" y="{y}" text-anchor="{"middle" if center else "start"}" dominant-baseline="central" font-family="Arial,sans-serif" font-size="{size}" font-weight="{700 if bold else 400}" fill="{color}"'+(' stroke="white" stroke-width="7" paint-order="stroke"' if halo else '')+f'>{escape(value)}</text>')

    def lines(self, x, y, label, size=28, center=True, step=None, bold=False):
        vals=label.split('|'); step=step or size+5
        for i,v in enumerate(vals):self.text(x,y+(i-(len(vals)-1)/2)*step,v,size,center,bold)

    def rect(self, box, fill='white', radius=0, width=2, color=GREY):
        x,y,xx,yy=box
        self.d.rounded_rectangle(box,radius,fill=fill,outline=color,width=width)
        self.svg.append(f'<rect x="{x}" y="{y}" width="{xx-x}" height="{yy-y}" rx="{radius}" fill="{fill}" stroke="{color}" stroke-width="{width}"/>')

    def ellipse(self,box,fill='white',width=2,color=GREY):
        x,y,xx,yy=box
        self.d.ellipse(box,fill,outline=color,width=width)
        self.svg.append(f'<ellipse cx="{(x+xx)/2}" cy="{(y+yy)/2}" rx="{(xx-x)/2}" ry="{(yy-y)/2}" fill="{fill}" stroke="{color}" stroke-width="{width}"/>')

    def path(self,points,arrow=False,dash=False,color=GREY,width=2,filled=False):
        for (x,y),(xx,yy) in zip(points,points[1:]):
            if dash:
                n=max(1,int(math.dist((x,y),(xx,yy))/12))
                for i in range(0,n,2):
                    a=i/n;b=min(i+1,n)/n
                    self.d.line((x+(xx-x)*a,y+(yy-y)*a,x+(xx-x)*b,y+(yy-y)*b),fill=color,width=width)
            else:self.d.line((x,y,xx,yy),fill=color,width=width)
        self.svg.append('<polyline points="'+' '.join(f'{x},{y}' for x,y in points)+f'" fill="none" stroke="{color}" stroke-width="{width}"'+(' stroke-dasharray="8 6"' if dash else '')+'/>')
        if arrow:
            a,b=points[-2:]; ang=math.atan2(b[1]-a[1],b[0]-a[0]);r=14
            p=[(b[0]-r*math.cos(ang-.47),b[1]-r*math.sin(ang-.47)),b,(b[0]-r*math.cos(ang+.47),b[1]-r*math.sin(ang+.47))]
            if filled:self.polygon(p,fill=color,color=color)
            else:self.path(p,color=color,width=width)

    def polygon(self,p,fill='white',color=GREY,width=2):
        self.d.polygon(p,fill=fill);self.d.line(p+[p[0]],fill=color,width=width)
        self.svg.append('<polygon points="'+' '.join(f'{x},{y}' for x,y in p)+f'" fill="{fill}" stroke="{color}" stroke-width="{width}"/>')

    def actor(self,x,y,name):
        self.ellipse((x-16,y-60,x+16,y-28),color=BLACK)
        self.path([(x,y-28),(x,y+40),(x-29,y+85)],color=BLACK)
        self.path([(x,y+40),(x+29,y+85)],color=BLACK)
        self.path([(x-35,y),(x+35,y)],color=BLACK)
        self.text(x,y+119,name,28,True)

    def oval(self,x,y,w,label):
        self.ellipse((x-w/2,y-50,x+w/2,y+50))
        self.lines(x,y,label,26)

    def action(self,x,y,w,label,h=90):
        self.rect((x-w/2,y-h/2,x+w/2,y+h/2),radius=12)
        self.lines(x,y,label,29)

    def state(self,x,y,w,title,body,h=130):
        self.rect((x-w/2,y-h/2,x+w/2,y+h/2),radius=12)
        self.rect((x-w/2,y-h/2,x+w/2,y-h/2+43),CYAN,radius=10)
        self.path([(x-w/2,y-h/2+43),(x+w/2,y-h/2+43)])
        self.text(x,y-h/2+22,title,28,True)
        self.lines(x,y+20,body,25)

    def initial(self,x,y):self.ellipse((x-11,y-11,x+11,y+11),BLACK,color=BLACK)
    def final(self,x,y):
        self.ellipse((x-19,y-19,x+19,y+19),color=BLACK)
        self.ellipse((x-11,y-11,x+11,y+11),BLACK,color=BLACK)
    def decision(self,x,y,label,w=190,h=60):
        self.polygon([(x,y-h),(x+w,y),(x,y+h),(x-w,y)])
        self.text(x,y,label,28,True)

    def save(self,stem):
        self.im.save(OUT/(stem+'.png'),dpi=(220,220))
        (OUT/(stem+'.svg')).write_text('\n'.join(self.svg+['</svg>']),encoding='utf-8')
        print(stem)


def use_case():
    c=Canvas();c.rect((205,65,1195,1840),radius=7,color=BLACK)
    c.text(700,105,'Gym Management System',33,True)
    groups=[(95,390,'Admin',BLUE,205,[('Sign in','Manage user accounts'),('Manage members','Manage trainers'),('Membership and|payment status','Record attendance'),('Equipment and|products','View payments, sales,|progress and plans'),('Generate reports','Create SQL backup')]),
            (1305,1040,'Trainer',ORANGE,875,[('Sign in','View assigned|members and sessions'),('View assigned|attendance and progress','Record member|progress'),('Edit own profile','Change password|and log out')]),
            (95,1555,'Member',GREEN,1420,[('Sign in','View own profile|and membership'),('View own attendance|and progress','View own training'),('Edit own profile','Change password|and log out')])]
    for ax,ay,role,col,top,rows in groups:
        c.actor(ax,ay,role)
        for r,pair in enumerate(rows):
            for idx,label in enumerate(pair):
                x=455+idx*480;y=top+r*115;near=idx==0 if ax<700 else idx==1
                bus=180 if ax<700 else 1220
                if near:
                    c.path([(ax+(35 if ax<700 else -35),ay),(bus,ay),(bus,y),(x+(-180 if ax<700 else 180),y)],color=col)
                else:
                    c.path([(ax+(35 if ax<700 else -35),ay),(bus,ay),(bus,y-55),(x,y-55),(x,y-41)],color=col)
        for r,pair in enumerate(rows):
            for idx,label in enumerate(pair):c.oval(455+idx*480,top+r*115,400,label)
    c.save('01-use-case')


def class_diagram():
    c=Canvas()
    def card(x,y,title,attrs,method):
        w,h=390,205
        c.rect((x,y,x+w,y+h),radius=5)
        c.rect((x,y,x+w,y+42),CYAN,radius=5)
        c.text(x+w/2,y+22,title,28,True)
        for i,a in enumerate(attrs):c.text(x+12,y+63+i*29,a,25)
        c.path([(x,y+157),(x+w,y+157)])
        c.text(x+12,y+181,method,25)
    rows=[
        [('Admin',['-adminId: int','-name, mobile: String','-statusId: int'],'+manageGym()'),('AppUser',['-userId: int; role: String','-username, hash: String','-active: boolean'],'+authenticate()'),('Status',['-statusId: int','-active: boolean'],'+checkActive()')],
        [('Member',['-memberId: int','-name, mobile: String','-paid: boolean; statusId: int'],'+updateProfile()'),('Membership',['-membershipTypeId: int','-type: enum','-price: decimal'],'+setPrice()'),('Trainer',['-trainerId: int','-name, mobile: String','-statusId: int'],'+updateProfile()')],
        [('MemberAttendance',['-attendanceId: int','-date: Date','-checkInTime: Time'],'+recordCheckIn()'),('ProgressTrack',['-progressId: int; date: Date','-heightCm, weightKg: decimal','-bodyFat, bmi: decimal'],'+recordProgress()'),('TrainerAttendance',['-attendanceId: int','-date: Date','-checkIn, checkOut: Time'],'+viewAttendance()')],
        [('PaymentHistory',['-paymentId: int','-paidAmount: decimal','-method, reference: String'],'+viewPayments()'),('PersonalTraining',['-ptId: int; dates: Date','-total, completed: int','-status: enum'],'+viewSessions()'),('WorkoutPlan',['-planId: int','-level: enum','-day1 ... day7: String'],'+viewPlan()')],
        [('Product',['-productId: int','-name: String; price: decimal','-stock: int'],'+saveProduct()'),('OrderHistory',['-orderId: int; date: Date','-quantity: int; total: decimal','-method, reference: String'],'+viewSales()')],
        [('Equipment',['-equipmentId: int','-name, serialNo: String','-status: enum; dates: Date'],'+manageEquipment()'),('MaintenanceLog',['-logId: int; date: Date','-description: String','-cost: decimal'],'+viewMaintenance()')]
    ]
    # Association lines are laid out before the cards, along dedicated gutters.
    def assoc(points,label,x,y,mults=None):
        c.path(points)
        if label:c.text(x,y,label,24,True,halo=True)
        if mults:
            for xx,yy,txt in mults:c.text(xx,yy,txt,22,True,halo=True)
    assoc([(450,170),(505,170)],'',477,135,[(477,141,'0..1'),(477,195,'0..1')])
    # Role links: each trainer/member profile has at most one account.
    assoc([(505,220),(478,220),(478,485),(450,485)],'',0,0)
    c.text(475,336,'0..1',22,True,halo=True)
    assoc([(895,220),(923,220),(923,485),(950,485)],'',0,0)
    c.text(922,336,'0..1',22,True,halo=True)
    assoc([(450,420),(505,420)],'',0,0,[(467,400,'*'),(492,400,'1')])
    assoc([(255,560),(255,640)],'1     has     0..*',255,602)
    assoc([(450,525),(468,525),(468,742),(505,742)],'',0,0)
    c.text(470,627,'1:*',22,True,halo=True)
    assoc([(60,455),(30,455),(30,1027),(60,1027)],'',0,0)
    c.text(30,904,'1:*',22,True,halo=True)
    assoc([(450,555),(482,555),(482,1055),(505,1055)],'',0,0)
    c.text(483,900,'1:*',22,True,halo=True)
    assoc([(950,545),(924,545),(924,1055),(895,1055)],'',0,0)
    c.text(924,900,'1:*',22,True,halo=True)
    assoc([(1145,560),(1145,640)],'1     has     0..*',1145,601)
    assoc([(450,1315),(505,1315)],'',0,0,[(463,1290,'1'),(490,1290,'*')])
    assoc([(450,1600),(505,1600)],'',0,0,[(463,1577,'1'),(490,1577,'*')])
    for r,row in enumerate(rows):
        for col,data in enumerate(row):card(60+445*col,70+285*r,*data)
    # The two remaining associations share a lookup; state their multiplicity.
    c.text(1145,1350,'Status association',29,True)
    c.lines(1145,1440,'Status 1 : 0..* Admin|Status 1 : 0..* Member|Status 1 : 0..* Trainer',25,step=38)
    c.lines(1145,1690,'Conceptual record classes|Operations describe supported tasks|WorkoutPlan is a catalogue',24,step=37)
    c.lines(700,1825,'Each history row belongs to one parent. * means 0..*.|AppUser links to a Member or Trainer according to its role.',26,step=36)
    c.save('02-class')


def sequence_diagram():
    c=Canvas();xs=[110,345,580,815,1050,1285]
    for x,label in zip(xs,['User',':LoginForm',':Accounts',':Passwords',':Session',':MySQL']):
        if x==110:c.actor(x,100,'User')
        else:c.rect((x-103,75,x+103,180));c.text(x,128,label,27,True)
        c.path([(x,235 if x==110 else 180),(x,1840)],dash=True,color='#999999')
    def msg(y,a,b,label,ret=False):
        c.path([(xs[a],y),(xs[b],y)],arrow=True,dash=ret,color=BLACK,filled=not ret)
        c.text((xs[a]+xs[b])/2,y-28,label,27,True,halo=True)
    c.path([(xs[0],320),(xs[1],320)],arrow=True,color=BLACK,filled=True)
    c.lines(230,276,'Enter username / mobile|and password',26)
    msg(440,1,2,'login(identifier, password)')
    msg(560,2,5,'Read account and linked profile status')
    msg(680,5,2,'Account, role, hash, profile IDs',True)
    msg(800,2,3,'verify(password, hash)')
    msg(915,3,2,'Password matches?',True)
    c.rect((32,1000,1368,1810),color=BLACK)
    c.rect((32,1000,114,1045));c.text(73,1023,'alt',27,True)
    c.text(135,1040,'[password correct and account / profile active]',27)
    msg(1150,2,4,'start(trusted Identity)')
    msg(1270,2,1,'Return Identity with stored role',True)
    c.path([(345,1380),(445,1380),(445,1450),(345,1450)],arrow=True,color=BLACK)
    c.lines(830,1400,'Open MainInterface, TrainerDashboard|or MemberDashboard for the stored role',26)
    msg(1530,1,0,'Show matching dashboard',True)
    c.path([(32,1595),(1368,1595)],dash=True)
    c.text(135,1630,'[invalid credentials or inactive account / profile]',27)
    msg(1700,2,1,'Throw SecurityException',True)
    msg(1775,1,0,'Show login error',True)
    c.save('03-sequence')


def activity_diagram():
    c=Canvas();x=450
    c.initial(x,70);c.path([(x,81),(x,125)],arrow=True,filled=True)
    c.action(x,170,560,'Open Trainer Dashboard')
    c.path([(x,215),(x,280)],arrow=True,filled=True)
    c.action(x,325,560,'Open Record Progress')
    c.path([(x,370),(x,425)],arrow=True,filled=True)
    c.decision(x,485,'Assigned member?',185)
    c.path([(x,545),(x,620)],arrow=True,filled=True);c.text(500,576,'Yes',25)
    c.action(x,665,560,'Select member and enter values;|calculate BMI and validate')
    c.path([(x,710),(x,785)],arrow=True,filled=True)
    c.decision(x,845,'Valid values?',185)
    c.path([(x,905),(x,990)],arrow=True,filled=True);c.text(500,940,'Yes',25)
    c.action(x,1035,560,'Recheck active member and|assignment inside transaction')
    c.path([(x,1080),(x,1155)],arrow=True,filled=True)
    c.decision(x,1215,'Still authorized?',185)
    c.path([(x,1275),(x,1350)],arrow=True,filled=True);c.text(500,1310,'Yes',25)
    c.action(x,1395,560,'Insert dated progress record')
    c.path([(x,1440),(x,1520)],arrow=True,filled=True)
    c.action(x,1565,560,'Commit and refresh history')
    c.path([(x,1610),(x,1710)],arrow=True,filled=True);c.final(x,1730)
    for y,label in [(485,'Show no active|assignment message'),(845,'Show validation error|and correct the values'),(1215,'Reject save and|roll back transaction')]:
        c.path([(635,y),(855,y)],arrow=True,filled=True)
        c.text(741,y-26,'No',25,True);c.action(1100,y,490,label,h=110)
    c.path([(1100,790),(1100,665),(730,665)],arrow=True,filled=True)
    c.path([(1100,540),(1100,605)],arrow=True,filled=True);c.final(1100,625)
    c.path([(1100,1270),(1100,1360)],arrow=True,filled=True);c.final(1100,1380)
    c.lines(1100,1615,'BMI = weight (kg) /|height (m) squared',29)
    c.save('04-activity')


def state_diagram():
    c=Canvas();c.initial(700,50);c.path([(700,61),(700,100)],arrow=True)
    c.state(700,165,390,'Logged out','Login form is displayed')
    c.path([(700,230),(700,340)],arrow=True);c.text(770,284,'Submit credentials',25)
    c.state(700,405,390,'Authenticating','Verify hash and status')
    c.state(230,405,350,'Connection error','Display database error')
    c.state(1170,405,350,'Access denied','Invalid or inactive login')
    c.path([(505,405),(405,405)],arrow=True);c.text(450,500,'SQL error',24,True)
    c.path([(895,405),(995,405)],arrow=True);c.text(960,500,'Rejected',24,True)
    for x,dest in [(230,505),(1170,895)]:
        c.path([(x,340),(x,165),(dest,165)],arrow=True)
        c.text(x,277,'Retry',24,True,halo=True)
    c.path([(700,470),(700,580)],arrow=True);c.text(770,528,'Valid active login',25)
    c.decision(700,625,'Stored role',110,45)
    for x,role,body in [(240,'Admin session','Gym records and accounts'),(700,'Trainer session','Assigned member records'),(1160,'Member session','Personal records only')]:
        start=(590,625) if x<700 else ((810,625) if x>700 else (700,670))
        c.path([start,(x,625 if x!=700 else 720),(x,765)],arrow=True)
        c.text(x,717,'['+role.split()[0].upper()+']',25,True)
        c.state(x,830,400,role,body)
        task={'Admin session':('Managing records','Members, inventory, reports'),'Trainer session':('Recording progress','Check active assignment'),'Member session':('Viewing history','Membership, progress, training')}[role]
        c.path([(x,895),(x,1050)],arrow=True);c.text(x+38,970,'Open task',24)
        c.state(x,1115,400,*task)
        # Task return stays inside this role; no transition to another role.
        c.path([(x+200,1115),(x+220,1115),(x+220,925),(x+200,925),(x+200,865)],arrow=True)
        c.text(x+180,1000,'Return',23,True,halo=True)
        c.path([(x,1180),(x,1370),(700,1370),(700,1435)],arrow=True)
    c.text(700,1315,'Logout or failed active-account check',28,True,halo=True)
    c.state(700,1500,430,'Session cleared','No protected access remains')
    c.path([(485,1500),(20,1500),(20,165),(505,165)],arrow=True)
    c.text(250,1460,'Log in again',25,True)
    c.path([(700,1565),(700,1700)],arrow=True);c.text(785,1640,'Close application',25)
    c.final(700,1725)
    c.save('05-state-machine')


def component_diagram():
    c=Canvas();left,right=90,800;cw,ch=510,150
    def comp(x,y,title,body):
        c.rect((x,y,x+cw,y+ch),radius=4)
        c.rect((x+cw-50,y+15,x+cw-22,y+48))
        c.rect((x+cw-56,y+21,x+cw-40,y+29))
        c.rect((x+cw-56,y+35,x+cw-40,y+43))
        c.text(x+cw/2,y+57,title,31,True)
        c.lines(x+cw/2,y+109,body,27)
    nodes=[(left,60,'LoginForm','Shared sign in'),(right,60,'Role dashboards','Admin / Trainer / Member'),
           (left,350,'Accounts','Account and scoped operations'),(right,350,'Session and Passwords','Identity, roles and hashing'),
           (left,640,'Admin feature panels','Members, attendance, inventory'),(right,640,'UserManagementPanel','Accounts and profile links'),
           (left,930,'util and validation','Search, input, logging, UI'),(right,930,'connection.MySQL','JDBC and backup utility'),
           (left,1220,'JasperReports','Six report templates and viewer'),(right,1220,'MySQL server','gms database - 16 tables'),
           (right,1510,'mysqldump','Local SQL backup client')]
    # Required side is req; provided side is prov. The socket opens toward ball.
    def vertical(req,prov,cy,label,lx=None):
        x=req[0];r,b=23,12
        if req[1]<prov[1]:
            c.path([req,(x,cy-r)])
            pts=[(x+r*math.cos(t),cy+r*math.sin(t)) for t in [math.pi+i*math.pi/32 for i in range(33)]]
            c.path([(x,cy+b),prov])
        else:
            c.path([req,(x,cy+r)])
            pts=[(x+r*math.cos(t),cy+r*math.sin(t)) for t in [i*math.pi/32 for i in range(33)]]
            c.path([(x,cy-b),prov])
        c.path(pts);c.ellipse((x-b,cy-b,x+b,cy+b))
        c.text(lx if lx else x+45,cy,label,27)
    vertical((345,210),(345,350),280,'Authentication')
    vertical((1055,210),(1055,350),280,'Authorization')
    vertical((345,640),(345,500),570,'Scoped operations')
    vertical((345,790),(345,930),860,'Validation / UI')
    vertical((1055,1080),(1055,1220),1150,'JDBC / SQL')
    vertical((1055,1510),(1055,1370),1440,'SQL dump')
    c.path([(800,155),(730,155),(730,420),(600,420)],arrow=True,dash=True)
    c.path([(800,720),(690,720),(690,470),(600,470)],arrow=True,dash=True)
    c.path([(600,485),(630,485),(630,960),(800,960)],arrow=True,dash=True)
    c.text(644,880,'JDBC',26,True,halo=True)
    # Accounts requires the authorization and hash contract supplied at right.
    c.path([(600,445),(655,445)])
    c.path([(745,445),(800,445)])
    arc=[(690+23*math.cos(t),445+23*math.sin(t)) for t in [math.pi/2+i*math.pi/32 for i in range(33)]]
    c.path([(655,445),(667,445)]);c.path(arc)
    c.path([(702,445),(745,445)]);c.ellipse((678,433,702,457))
    c.text(700,522,'Identity / hash',25,True,halo=True)
    c.path([(90,750),(45,750),(45,1295),(90,1295)],arrow=True,dash=True)
    c.path([(600,1295),(740,1295),(740,1030),(800,1030)],arrow=True,dash=True)
    c.text(687,1260,'Report data',25,True,halo=True)
    c.path([(1055,1080),(1360,1080),(1360,1585),(1310,1585)],arrow=True,dash=True)
    c.text(1250,1480,'Backup call',25,True,halo=True)
    c.rect((left,1510,left+cw,1660));c.text(345,1550,'<<artifact>>',26,True)
    c.lines(345,1605,'Timestamped SQL files|backups/ folder',29)
    c.path([(800,1585),(600,1585)],arrow=True)
    c.text(700,1547,'Writes',26,True,halo=True)
    for n in nodes:comp(*n)
    c.path([(160,1780),(210,1780)]);c.ellipse((210,1768,234,1792));c.text(258,1780,'Provided interface',27)
    c.path([(790,1780),(835,1780)])
    c.path([(858+23*math.cos(t),1780+23*math.sin(t)) for t in [math.pi/2+i*math.pi/32 for i in range(33)]])
    c.text(910,1780,'Required socket',27)
    c.text(700,1850,'Dashed arrows show module dependencies.',26,True)
    c.save('06-component')


def deployment_diagram():
    c=Canvas()
    # UML node as a three-dimensional box, with contained execution environments.
    c.rect((65,75,1335,1810),color=BLACK)
    c.polygon([(65,75),(100,40),(1370,40),(1335,75)],color=BLACK)
    c.polygon([(1335,75),(1370,40),(1370,1775),(1335,1810)],color=BLACK)
    c.lines(700,137,'<<device>>|Windows desktop computer',31)
    c.rect((120,215,1280,770),radius=6)
    c.lines(700,274,'<<executionEnvironment>>|Java 17 JVM',30)
    def art(x,y,w,label):
        c.rect((x,y,x+w,y+155),radius=2)
        c.text(x+w/2,y+30,'<<artifact>>',25,True)
        c.lines(x+w/2,y+95,label,28)
    art(165,340,485,'Gym_Management_System.jar|Swing screens and security')
    art(750,340,485,'Runtime libraries|Connector/J, FlatLaf,|JasperReports')
    art(165,555,485,'Compiled report templates|src/report resources')
    art(750,555,485,'Database client settings|db.properties / mysql.cnf')
    c.path([(700,770),(700,950)],arrow=True)
    c.lines(1040,860,'JDBC over TCP/IP|127.0.0.1:3307',29)
    c.rect((120,950,1280,1335),radius=6)
    c.lines(700,1005,'<<executionEnvironment>>|MySQL Server 8',30)
    art(165,1110,485,'gms schema|16 persistent tables')
    art(750,1110,485,'Local data and configuration|.local/mysql/data and my.ini')
    c.rect((750,1480,1235,1635));c.lines(992,1558,'<<executionEnvironment>>|mysqldump process',29)
    c.path([(992,1480),(992,1335)],arrow=True)
    c.text(1068,1400,'SQL backup',27)
    art(165,1480,485,'Generated backup files|backups/ timestamp.sql')
    c.path([(750,1558),(650,1558)],arrow=True)
    c.text(700,1518,'Writes',25,True,halo=True)
    c.lines(700,1720,'<<artifact>> Run.ps1, Setup-Database.ps1, Build.ps1|Checkout contains dist/, lib/, resources, logs/ and backups/',29,step=43)
    c.save('07-deployment')


if __name__=='__main__':
    for f in [use_case,class_diagram,sequence_diagram,activity_diagram,state_diagram,component_diagram,deployment_diagram]:f()
