from django.shortcuts import get_object_or_404, get_list_or_404
from djangorestframework.views import *
from django.utils import simplejson as json
from django.http import HttpResponse
from django.utils.translation import ugettext as _
from . import models


class PointsView(View):
    def get(self, request):
        points = get_list_or_404(models.Point)
        return [{'point_id':point.id,'point_name':point.name} for point in points]

class UrlsView(View):
     def get(self, request):
        urls = get_list_or_404(models.Url)
        return [{'pk': url.id,'title': url.title, 'url':url.url, 'length':url.length , 'display':url.is_show} for url in urls]
        
class Speedlogs(View):
#    def get(self, request):
#        q =  request.GET.get("q")
#        j = json.loads(q)
#        i = 0
#        speed = 0
#        for url in j['speed']:
#            speedlog  =  models.Speedlog()
#            speedlog.ip = j["ip"]
#            speedlog.isp = j['isp']
#            speedlog.location  = j['location']
#            speedlog.url =  models.Url.objects.get(id = url['pk'])
#            speedlog.speed  = url['speed']
#            speedlog.save()
#            if speedlog.url.is_show and speedlog.speed>0:
#              speed +=  speedlog.speed
#              i+=1;
#        if i>0:
#            videotype = models.Videotype.objects.filter(bit_rate__lte = (speed*8/i)).order_by('-bit_rate')[:1]
#        else:
#            videotype = models.Videotype.objects.filter(bit_rate__lte = 0).order_by('-bit_rate')[:1]
#        if videotype:
#            return HttpResponse(videotype[0].resolution)
#        
    def post(self,request):
        q =  request.POST["q"]
        user_agent =  request.META['HTTP_USER_AGENT']
        j = json.loads(q)
        i = 0
        speed = 0
        for url in j['speed']:
            speedlog  =  models.Speedlog()
            speedlog.ip = j["ip"]
            speedlog.isp = j['isp']
            speedlog.location  = j['location']
            speedlog.user_agent = user_agent
            speedlog.url =  models.Url.objects.get(id = url['pk'])
            speedlog.speed  = url['speed']
            speedlog.save()
            if speedlog.url.is_show and speedlog.speed>0:
              speed +=  speedlog.speed
              i+=1;
        if i>0:
            videotype = models.Videotype.objects.filter(bit_rate__lte = (speed*8/i)).order_by('-bit_rate')[:1]
        else:
            videotype = models.Videotype.objects.filter(bit_rate__lte = 0).order_by('-bit_rate')[:1]
        if videotype:
            return HttpResponse(videotype[0].resolution)
        
class Pointlogs(View):
#    def get (self, request):
#          q =  request.GET.get("q")
#          j = json.loads(q)
#          pointlog  =  models.Pointlog()
#          pointlog.ip = j["ip"]
#          pointlog.isp = j['isp']
#          pointlog.location  = j['location']
#          pointlog.speeds  = j['speed']
#          pointlog.point = j['option']
#          pointlog.description = j['description']
#          pointlog.phone = j['phone']
#          pointlog.mail= j['mail']
#          pointlog.save()
#          return HttpResponse("OK")
    def post(self,request):
          q =  request.POST['q']
          user_agent =  request.META['HTTP_USER_AGENT']
          j = json.loads(q)
          pointlog  =  models.Pointlog()
          pointlog.ip = j["ip"]
          pointlog.isp = j['isp']
          pointlog.user_agent = user_agent
          pointlog.location  = j['location']
          pointlog.speeds  = j['speed']
          pointlog.point = j['option']
          pointlog.description = j['description']
          pointlog.phone = j['phone']
          pointlog.mail= j['mail']
          if pointlog.ip!=0  and len(pointlog.ip)>0:
                logs =   models.Pointlog.objects.filter(ip = pointlog.ip,description = pointlog.description,user_agent = pointlog.user_agent,point = pointlog.point)
                if  logs.count() == 0:
                        pointlog.save()
          return HttpResponse("OK")


