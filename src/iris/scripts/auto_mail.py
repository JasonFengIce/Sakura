__author__ = 'liuze'
import sys
import os
sys.path.append((os.path.realpath(sys.argv[0]).rpartition('/')[0] or '.') + '/../lib')
from django.core.management import setup_environ


import time
import datetime
from time import localtime,strftime
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
    def send_email_message(self):
        from django.core.mail import EmailMessage
        email = EmailMessage('Hello', 'Body goes here', 'git_cord@ismartv.cn',['to1@example.com', 'to2@example.com'], ['bcc@example.com'],headers = {'Reply-To': 'another@example.com'})
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
            html_content = '<table><tr><td bgcolor = red>cause</td><td bgcolor=Fuchsia>ip</td><td bgcolor =Blue>isp</td><td bgcolor = Green>speed</td><td bgcolor = Purple>description</td><td bgcolor = Teal>phone</td><td bgcolor = Maroon>mail</td><td bgcolor = Teal>create_date</td></tr>'
            from iris.customer.models import Point
            html_content +=  '<h1><a href=\"http://iris.tvxio.com/admin/\" target=\"_blank\">  Login  Iris</a><h1>'
            for log in logs:
                point = Point.objects.get(id = log.point)
                html_content +="<tr><td>"+point.name+"</td><td>"+log.ip+"</td><td>"+log.isp+"</td><td>"+log.speeds+"</td><td>"+log.description+"</td><td>"+log.phone+"</td><td>"+log.mail+"</td><td>"+ str(log.create_date) +"</td></tr>"
            html_content +=  '</table>'
            msg = EmailMessage(subject, html_content, from_email, tos)
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
                        print counter
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
#        logs = Pointlog.objects.filter().order_by('-create_date')[:5]
        logs = Pointlog.objects.filter(create_date__lte = args[1],create_date__gte = args[0] ).order_by('-create_date')
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