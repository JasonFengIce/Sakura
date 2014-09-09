from django.shortcuts import get_list_or_404
from djangorestframework.views import *
from django.utils import simplejson as json
from django.http import HttpResponse
from . import models
import datetime
import httplib2

class PointsView(View):
    def get(self, request):
        points = get_list_or_404(models.Point)
        try:
            type_temp = request.META['HTTP_ACCEPT_LANGUAGE']
        except:
            return [{'point_id': point.id, 'point_name': point.name} for point in points]
        if type_temp:
            if  'zh_CN' == type_temp:
                return [{'point_id': point.id, 'point_name': point.name} for point in points]
            if  'en_US' == type_temp:
                return [{'point_id': point.id, 'point_name': point.en_name} for point in points]
            if  'zh_TW' == type_temp:
                return [{'point_id': point.id, 'point_name': point.hant_name} for point in points]
            return [{'point_id': point.id, 'point_name': point.name} for point in points]
        else:
            return [{'point_id': point.id, 'point_name': point.name} for point in points]


class UrlsView(View):
    result_list = []
    size = 3

    def get(self, request):
        self.result_list = []
        user_agent = request.META['HTTP_USER_AGENT']
        if " " in user_agent and ("a11" in user_agent or "A11" in user_agent):
            mark = user_agent.split(" ")[1][2:4]
            print("mark", mark)
            urls = models.Url.objects.filter(isp__mark=mark)[:self.size]
            self.set_result_list(urls)
            if not self.result_list or len(self.result_list) < self.size:
                urls = models.Url.objects.filter(isp__mark="00")[:self.size - len(self.result_list)]
                self.set_result_list(urls)
            if not self.result_list or len(self.result_list) < self.size:
                urls = models.Url.objects.filter(isp__mark="99")[:self.size - len(self.result_list)]
                self.set_result_list(urls)
        else:
            urls = models.Url.objects.filter(isp__mark="99")[:self.size]
            self.set_result_list(urls)
        return [{'pk': url.id, 'title': url.title, 'url': url.url, 'length': url.length, 'display': url.is_show} for url
                                                                                                                 in
                                                                                                                 self.result_list]

    def set_result_list(self, urls):
        if urls:
            for url in urls:
                self.result_list.append(url)


class Speedlogs(View):
    def post(self, request):
        q = request.POST["q"]
        user_agent = request.META['HTTP_USER_AGENT']
        j = json.loads(q)
        i = 0
        speed = 0
        for url in j.get('speed'):
            speedlog = models.Speedlog()
            speedlog.ip = j.get("ip")
            speedlog.isp = j.get('isp')
            speedlog.location = j.get('location')
            if user_agent and len(user_agent)<100:
                speedlog.user_agent = user_agent
            speedlog.url = models.Url.objects.get(id=url.get('pk'))
            speedlog.speed = url.get('speed')
            speedlog.save()
            if speedlog.url.is_show and speedlog.speed > 0:
                speed += speedlog.speed
                i += 1;
        if i > 0:
            videotype = models.Videotype.objects.filter(bit_rate__lte=(speed * 8 / i)).order_by('-bit_rate')[:1]
        else:
            videotype = models.Videotype.objects.filter(bit_rate__lte=0).order_by('-bit_rate')[:1]
        try:
            type_temp = request.META['HTTP_ACCEPT_LANGUAGE']
            if videotype:
                if  'zh_CN' == type_temp:
                    return HttpResponse(videotype[0].resolution)
                if  'en_US' == type_temp:
                    return HttpResponse(videotype[0].en_resolution)
                if  'zh_TW' == type_temp:
                    return HttpResponse(videotype[0].hant_resolution)
            return HttpResponse(videotype[0].resolution)
        except:
            return HttpResponse(videotype[0].resolution)


class Pointlogs(View):
    def post(self, request):
        q = request.POST['q']
        user_agent = request.META['HTTP_USER_AGENT']
        j = json.loads(q)
        pointlog = models.Pointlog()
        try:
            if user_agent  and len(user_agent.split(" ")) > 1:
                pointlog.sn = user_agent.split(" ")[1]
                if pointlog.sn:
                    res = self.getDevice(pointlog.sn)
                    if res:
                        pointlog.device = res.get("device")
                        if res.get("size"):
                            pointlog.size = int(res.get("size"))
        except Exception:
            pass
        pointlog.ip = j.get("ip")
        pointlog.isp = j.get('isp')
        pointlog.user_agent = user_agent
        if j.get("width"):
            pointlog.width = j.get('width')
        if j.get('location'):
            pointlog.location = j.get('location')
        if j.get('speed'):
            pointlog.speeds = j.get('speed')
        if j.get('description'):
            pointlog.description = j.get('description')
        if j.get('option') :
            try :
                po = models.Point.objects.get(id=int(j.get('option')))
                if po:
                    pointlog.point = j.get('option')
            except Exception:
                pass
        pointlog.phone = j.get('phone')
        pointlog.mail = j.get('mail')
        if  j.get('clip'):
            clip = j.get('clip')
            if clip.get('pk') and clip.get('url') and clip.get('quality'):
                q = models.Quality.objects.filter(key=clip.get('quality'))
                if q:
                    clip = models.ClipLog.objects.create(key=clip.get('pk'), url=clip.get('url'), quality=q[0])
                    pointlog.clip = clip
        if pointlog.ip and pointlog.ip != 0 :
            logs = models.Pointlog.objects.filter(ip=pointlog.ip, description=pointlog.description,
                user_agent=pointlog.user_agent, point=pointlog.point)
            if logs.count() == 0 or(logs.count() > 0 and (datetime.datetime.now() - logs[0].create_date) > 3600 ):
                pointlog.save()
        return HttpResponse("OK")

    #http://newdata.tvxio.com/public/sn_meta?sn=HJD00180
    #http://10.0.1.6:9000/public/sn_meta?sn=HJD00180
    #[{"device": "K91", "sn": "1a2544ee", "size": "55"}]
    def getDevice(self, sn):
        try:
            HEADER = {"User-Agent": "Iris/sn_meta 000000001", "Accept": "application/json"}
            http = httplib2.Http()
            urls = "http://10.0.1.6:9000/public/sn_meta?sn=" + sn
            h, res = http.request(urls, "GET", headers=HEADER)
            if res:
                re = json.loads(res)[0]
                if re:
                    return re
            return None
        except Exception:
            return None


class PostFeedback(View):

    def post(self, request):
        id = request.POST['id']
        content = request.POST['content']
        if id and content:
           plog =  models.Pointlog.objects.get(id=id)
           if plog:
                plog.content = content
                plog.reply_time = datetime.datetime.now()
                plog.save()
                return HttpResponse("Save Feedback Success")
        return HttpResponse("Missing  values  for sn")

class GetFeedback(View):
    def get(self, request):
        topn = 3
        sn = request.GET.get('sn')
        if request.GET.get('topn'):
            topn =  request.GET.get('topn')
        if sn :
            plogs =  models.Pointlog.objects.filter(sn=sn).order_by('-create_date')[:topn]
            if plogs:
                rs = {"count":len(plogs)}
                datas = []
                for p in plogs:
                    reply_time = ""
                    if p.reply_time:
                        reply_time = p.reply_time.strftime("%Y-%m-%d %H:%M:%S")
                    datas.append({"commont":self.point(p), "reply":p.content, "submit_time":p.create_date.strftime("%Y-%m-%d %H:%M:%S"), "reply_time":reply_time})
                rs["data"] = datas
                return HttpResponse(json.dumps(rs))
        return HttpResponse("Missing  values  for sn")

    def point(self,log):
        try:
            if log and log.point:
                p = models.Point.objects.get(id=int(log.point))
                if p:
                    return p.name
            return log.point
        except Exception:
            return None