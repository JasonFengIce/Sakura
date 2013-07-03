__author__ = 'liuze'
import sys
import os
sys.path.append((os.path.realpath(sys.argv[0]).rpartition('/')[0] or '.') + '/../lib')
from django.core.management import setup_environ

import time
from time import localtime
TIME = 3600

def init():
    sys.path.append('../')
    sys.path.append('../../')
    print sys.path
    from iris import settings
    setup_environ(settings)



class AutoMail(object):
    html_content = ''
    text_content = ''
    
#    def html_content(self):
##        print self.html_content
#    def text_content(self):
##        print self.text_content
    def send_mail(self):
        from django.core.mail import send_mail
        send_mail('Subject here', 'Here is the message.', 'git_cord@ismartv.cn',    ['liuze@ismartv.cn',], fail_silently=False)
#    def send_email_message(self):
#        from django.core.mail import EmailMessage
#        email = EmailMessage('Hello', 'Body goes here', 'git_cord@ismartv.cn',['to1@example.com', 'to2@example.com'], ['bcc@example.com'],headers = {'Reply-To': 'another@example.com'})
    def send_emailMultiAlternatives(self):
        from django.core.mail import EmailMultiAlternatives
        subject, from_email, to = 'hello', 'from@example.com', 'to@example.com'
        text_content = 'This is an important message.'
        html_content = '<p>This is an <strong>important</strong> message.</p>'
        msg = EmailMultiAlternatives(subject, text_content, from_email, [to])
        msg.attach_alternative(html_content, "text/html")
        msg.send()
    def send_EmailMessage(self,logs,start_date,end_date):
        if logs.count()>0:
            from django.core.mail import EmailMessage
            subject, from_email, tos = 'Iris Log '+str(start_date)+' - '+str(end_date), 'iris@ismartv.cn', ['cs@ismartv.cn',]
            html_content = '<table width="100%"><tr><td bgcolor = red>cause</td><td bgcolor = Green>user_agent</td><td bgcolor=Fuchsia>ip</td><td bgcolor =Blue>isp</td><td bgcolor = Green>speed</td><td bgcolor = Purple>description</td><td bgcolor = Teal>phone</td><td bgcolor = Maroon>mail</td><td bgcolor = Teal>create_date</td></tr>'
            html_content += '<h1><a href=\"http://iris.tvxio.com/admin/\" target=\"_blank\">  Login  Iris</a><h1>'
            from iris.customer.models import Point
            from iris.customer.models import Url
            n = 0
            phone = 0
            mail = 0
            phone_and_mail = 0
            count = len(logs)
            for log in logs:
                n+=1
                point = Point.objects.get(id = log.point)
                speeds = eval( log.speeds.replace('u',''))
                i=0
                str_speeds = ''
                speed_t = 0
                for speed in  speeds:
                    i+=1
                    pk = int(speed['pk'])
                    title = ''
                    try:
                        title = Url.objects.get(id=pk).title
                    except :
                        title = 'none'
                    speed_t += int(speed['speed'])
                    str_speeds += " NO."+ title +" : "+str(int(speed['speed']))+" KB/s <br>"
                str_speeds += " Average : "+str(speed_t/i)+" KB/s <br>"
                if n%2 == 0:
                    html_content +="<tr bgcolor=Silver ><td > "+point.name\
                                    +"</td ><td >"+log.user_agent\
                                   +"</td ><td >"+log.ip\
                                   +"</td><td >"+log.isp\
                                   +"</td><td >"+str_speeds\
                                   +"</td><td >"+log.description\
                                   +"</td><td >"+log.phone\
                                   +"</td><td >"+log.mail\
                                   +"</td><td >"+ str(log.create_date) \
                                   +"</td></tr>"
                else:
                     html_content +="<tr bgcolor=White ><td >"+point.name\
                                    +"</td ><td >"+log.user_agent\
                                   +"</td ><td >"+log.ip\
                                   +"</td><td >"+log.isp\
                                   +"</td><td >"+str_speeds\
                                   +"</td><td >"+log.description\
                                   +"</td><td >"+log.phone\
                                   +"</td><td >"+log.mail\
                                   +"</td><td >"+ str(log.create_date) \
                                   +"</td></tr>"
                if log.phone and log.mail:
                    phone_and_mail +=1
                elif log.phone:
                    phone+=1
                elif log.mail:
                    mail+=1
            html_content +=  '</table>'
            no_contact = count - phone_and_mail - phone - mail
            html='<table width="10%"><tr bgcolor = Red><td>PHONE_AND_MAIL</td><td >'+str(phone_and_mail)+'</td></tr>'
            html+='<tr bgcolor = Yellow><td>PHONE</td><td  >'+str(phone)+'</td></tr>'
            html+='<tr bgcolor = Green ><td>MAIL</td><td >'+str(mail)+'</td></tr>'
            html+='<tr bgcolor= Fuchsia ><td>NO_CONTACT</td><td >'+str(no_contact)+'</td></tr>'
            html+='<tr bgcolor = Teal  ><td>TOTAL</td><td >'+str(count)+'</td></tr></table><br>'
            html+=html_content
            msg = EmailMessage(subject, html, from_email, tos)
            msg.content_subtype = "html" # Main content is now text/html
            msg.send()

import threading
class Timer(threading.Thread):
        def __init__(self, seconds):
                self.runTime = seconds
                threading.Thread.__init__(self)
        def run(self):
                time.sleep(self.runTime)
            
class CountDownTimer(Timer):
        def run(self):
                counter = self.runTime
                for sec in range(self.runTime):
#                        print counter
                        time.sleep(1.0)
                        counter -= 1
class CountDownExec(CountDownTimer):
        def __init__(self, seconds, action, args=[]):
                self.args = args
                self.action = action
                CountDownTimer.__init__(self, seconds)
        def run(self):
                CountDownTimer.run(self)
                self.action(self.args)

def myAction(args=[]):
        from iris.customer.models import Pointlog
        logs = Pointlog.objects.filter(create_date__lte = args[1],create_date__gte = args[0] ).order_by('-create_date')
#        logs = Pointlog.objects.filter(create_date__lte = args[1])
        if logs.count()>0:
              mail = AutoMail();
              mail.send_EmailMessage(logs,args[0],args[1]);
        
if __name__ == '__main__':
        init()
        os.environ['DJANGO_SETTINGS_MODULE'] = 'iris.settings'
        start_date =  time.strftime("%Y-%m-%d %H:%M:%S",localtime(time.time()-3600))
        end_date =   time.strftime("%Y-%m-%d %H:%M:%S",localtime(time.time()))
        myAction([start_date,end_date])
        pass