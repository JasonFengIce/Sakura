from django.utils import simplejson as json
from customer import models
import httplib2

def getDevices():
    ps = models.Pointlog.objects.all()
    for p in ps:
        user_agent = p.user_agent
        try:
            if user_agent and len(user_agent.split(" ")) > 1:
                p.sn = user_agent.split(" ")[1]
                if p.sn:
                    res = json.loads(getDevice(p.sn))[0]
                    print res
                    p.device = res.get("device")
                    if res.get("size"):
                        p.size = int(res.get("size"))
                p.save()
        except Exception:
            continue


def getDevice(sn):
    try:
        HEADER = {"User-Agent": "Iris/sn_meta 000000001", "Accept": "application/json"}
        http = httplib2.Http()
        urls = "http://10.0.1.6:9000/public/sn_meta?sn=%s" % sn
        h, res = http.request(urls, "GET", headers=HEADER)
        if res:
            return res
        else:
            return None
    except Exception:
        return None


getDevices();