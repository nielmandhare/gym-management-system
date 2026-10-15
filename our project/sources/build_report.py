"""Build a printable A4 portrait PDF and a text-editable Word source.

Dependencies: Pillow, python-docx, reportlab, pypdfium2 (preview only).
All output locations resolve relative to this folder, not a user's Desktop.
"""
from pathlib import Path
from xml.sax.saxutils import escape
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.platypus import Paragraph
from reportlab.lib import colors
from PIL import Image
import pypdfium2 as pdfium
from report_content import PROBLEM, DIAGRAMS

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'report'
OUT.mkdir(exist_ok=True)
STEM='Gym_Management_System_OOMD_Report'
PW,PH=A4
BODY=ParagraphStyle('body',fontName='Times-Roman',fontSize=11.5,leading=16,spaceAfter=8)
SMALL=ParagraphStyle('small',parent=BODY,fontSize=10,leading=13)


def build_word():
    doc=Document();s=doc.sections[0]
    s.page_width=Inches(8.2677);s.page_height=Inches(11.6929)
    s.top_margin=s.bottom_margin=Inches(.55)
    s.left_margin=s.right_margin=Inches(.68)
    s.footer_distance=Inches(.25)
    for name,size in [('Normal',12),('Title',24),('Heading 1',16),('Heading 2',12),('Caption',10)]:
        st=doc.styles[name];st.font.name='Times New Roman';st.font.size=Pt(size);st.font.color.rgb=RGBColor(0,0,0)
        st.paragraph_format.space_after=Pt(8)
        st.paragraph_format.line_spacing=1.12
        if name in ['Heading 1','Heading 2']:st.font.bold=True;st.paragraph_format.space_before=Pt(10)
    fp=s.footer.paragraphs[0];fp.alignment=WD_ALIGN_PARAGRAPH.CENTER
    fld=OxmlElement('w:fldSimple');fld.set(qn('w:instr'),'PAGE');fp._p.append(fld)
    def p(txt='',style=None,center=False):
        a=doc.add_paragraph(txt,style)
        if center:a.alignment=WD_ALIGN_PARAGRAPH.CENTER
        return a
    for i in range(5):p()
    p('Gym Management System','Title',True)
    p('OOMD Mini Project Report',center=True)
    p('Object Oriented Modelling and Design',center=True)
    for i in range(5):p()
    for line in ['Submitted by __________________________','Class and Division _____________________','Roll Numbers __________________________','Guide ________________________________','Academic Year _________________________']:p(line,center=True)
    p();p('Java Swing   JDBC   MySQL',center=True)
    doc.add_page_break()
    p('1 Title','Heading 1')
    p('Gym Management System')
    p('2 Problem Statement','Heading 1')
    for title,body in PROBLEM:p(title,'Heading 2');p(body)
    for i,(title,stem,blocks) in enumerate(DIAGRAMS,1):
        doc.add_page_break();p(f'3.{i} {title}','Heading 1')
        a=p(center=True);pic=a.add_run().add_picture(str(ROOT/'diagrams'/(stem+'.png')),width=Inches(6.85))
        pic._inline.docPr.set('descr',f'{title} of the Gym Management System')
        p(f'Figure {i}  {title}','Caption',True)
        doc.add_page_break();p(f'Explanation of the {title}','Heading 1')
        for sub,txt in blocks:p(sub,'Heading 2');p(txt)
    doc.core_properties.title='Gym Management System OOMD Report'
    doc.core_properties.subject='Seven UML views of the Java Swing gym project'
    doc.core_properties.author=''
    doc.save(OUT/(STEM+'.docx'))


def build_pdf():
    path=OUT/(STEM+'.pdf');c=canvas.Canvas(str(path),pagesize=A4)
    c.setTitle('Gym Management System OOMD Report');c.setAuthor('')
    number=1
    def footer():
        c.setFont('Times-Roman',9);c.drawCentredString(PW/2,22,str(number))
    def nextpage():
        nonlocal number
        footer();c.showPage();number+=1
    def head(txt):
        c.setFont('Times-Bold',16);c.drawString(49,PH-52,txt)
    def blocks(items,start=PH-82,gap=16):
        y=start
        for title,txt in items:
            c.setFont('Times-Bold',12);c.drawString(49,y,title);y-=20
            p=Paragraph(escape(txt),BODY);ww,hh=p.wrap(PW-98,PH)
            if y-hh<46:raise ValueError(f'Text exceeds one page: {title} on page {number}')
            p.drawOn(c,49,y-hh);y-=hh+gap
    c.setFont('Times-Bold',26);c.drawCentredString(PW/2,PH-210,'Gym Management System')
    c.setFont('Times-Roman',16);c.drawCentredString(PW/2,PH-250,'OOMD Mini Project Report')
    c.setFont('Times-Roman',13);c.drawCentredString(PW/2,PH-280,'Object Oriented Modelling and Design')
    for i,line in enumerate(['Submitted by __________________________','Class and Division _____________________','Roll Numbers __________________________','Guide ________________________________','Academic Year _________________________']):
        c.drawCentredString(PW/2,PH-390-i*37,line)
    c.drawCentredString(PW/2,160,'Java Swing   JDBC   MySQL')
    nextpage();head('1 Title')
    c.setFont('Times-Roman',13);c.drawString(49,PH-84,'Gym Management System')
    c.setFont('Times-Bold',16);c.drawString(49,PH-120,'2 Problem Statement')
    blocks(PROBLEM,start=PH-145,gap=9)
    for i,(title,stem,items) in enumerate(DIAGRAMS,1):
        nextpage();head(f'3.{i} {title}')
        img=ROOT/'diagrams'/(stem+'.png');im=Image.open(img)
        w=6.85*72;h=w*im.height/im.width
        c.drawImage(str(img),(PW-w)/2,PH-74-h,width=w,height=h)
        c.setFont('Times-Italic',10);c.drawCentredString(PW/2,PH-91-h,f'Figure {i}  {title}')
        nextpage();head('Explanation of the '+title);blocks(items)
    footer();c.save()
    # Review every output PDF page at a resolution sufficient for print labels.
    qa=ROOT.parent/'work'/'new-report-preview';qa.mkdir(parents=True,exist_ok=True)
    d=pdfium.PdfDocument(str(path))
    for i,pg in enumerate(d):pg.render(scale=2).to_pil().save(qa/f'page-{i+1:02d}.png')
    print(f'{len(d)} portrait pages written to {path}')


if __name__=='__main__':
    build_word();build_pdf()
