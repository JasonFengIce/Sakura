from django.contrib import admin
import re
import json
import uuid
import pika
from . import models
from django import forms

def point(log):
    if log and log.point:
        p = models.Point.objects.get(id=int(log.point))
        if p:
            return p.name
    return log.point


def speeds(speeds):
    str = ''
    if speeds and speeds.speeds:
        resp = re.sub(r"(,?)(\w+?)\s+?:", r"\1'\2' :", speeds.speeds);
        resp = resp.replace("u'", "'").replace("'", "\"");
        try:
            decodejson = json.loads(resp)
            if decodejson:
                for log in decodejson:
                    url = models.Url.objects.get(id=int(log['pk']))
                    str += '%s:(%d%s)<br>' % (url.title, log['speed'], 'KB/S')
        except Exception:
            str = speeds.speeds
    return str

speeds.allow_tags = True


def logs(indemnity):
    temp = str()
    for log in indemnity.indemnityLog:
        if log.package:
            temp.join("%s,%s" % (log.package, log.error_desc))
        if log.item:
            temp.join("%s,%s" % (log.item, log.error_desc))
    return temp


def packages(indemnity):
    if indemnity:
        p = ''
        packages = indemnity.package.values()
        for package in packages:
            p += "%s," % ( package['title'],)
        return p
    else:
        return None;


def items(indemnity):
    if indemnity:
        i = ''
        items = indemnity.item.values()
        for item in items:
            i += '<<%s>>' % (item['title'])
        return i
    else:
        return None;


def clip(log):
    if log.clip:
        key = log.clip.key
        url = log.clip.url
        quality = log.clip.quality
        return "%s(%s)<br>%s" % (key, quality, url)
    return None

clip.allow_tags = True


class CordIrisRpcClient(object):
    def __init__(self):
        credential = pika.PlainCredentials(
            username='cord_iris_client',
            password='fcR9JsYSSol7xFBiHsXSWSVcZTvMO3x4ifqjUYoO9B8=')
        self.connection = pika.BlockingConnection(
            pika.ConnectionParameters(host='cord_rabbitmq', virtual_host='/cord/iris',
                credentials=credential))
        self.connection.add_timeout(10, self.callback_timeout)
        self.channel = self.connection.channel()
        self.channel.exchange_declare(
            exchange='exchange.cord_iris',
            type='direct')
        result = self.channel.queue_declare(exclusive=True)
        self.channel.queue_bind(
            exchange='exchange.cord_iris',
            queue=result.method.queue,
            routing_key=result.method.queue)

        self.callback_queue = result.method.queue
        self.channel.basic_consume(self.on_response, no_ack=True, queue=self.callback_queue)


    def on_response(self, ch, method, props, body):
        if(self.corr_id == props.correlation_id):
            ilogs = models.IndemnityLog.objects.filter(correlation_id=props.correlation_id)
            if ilogs:
                for ilog in ilogs:
                    res = json.loads(body)
                    if ilog and res:
                        if res['error_code'] == 0:
                            ilog.status = 1
                            ilog.error_code = 0
                            ilog.error_desc = res['error_desc']
                            ilog.save()
                        else:
                            ilog.status = 0
                            ilog.error_code = int(res['error_code'])
                            ilog.error_desc = res['error_desc']
                            ilog.save()
        self.response = body

    def compensate(self, user_pk, package_pk=None, item_pk=None, ilog=None, duration=None):
        self.response = None;
        self.corr_id = str(uuid.uuid4())
        ilog.correlation_id = self.corr_id
        ilog.save();
        self.channel.basic_publish(exchange='exchange.cord_iris',
            routing_key='cord.iris',
            properties=pika.BasicProperties(reply_to=self.callback_queue, correlation_id=self.corr_id),
            body=json.dumps(dict(method='compensate', user_pk=user_pk, package_pk=package_pk, item_pk=item_pk, duration=duration)))

        while self.response is None:
            self.connection.process_data_events()
        self.response = None;
        ilogs = models.IndemnityLog.objects.filter(correlation_id=self.corr_id)
        return ilogs[0]

    def callback_timeout(self):
        raise  Exception()


def mq(data):
    logs = list()
    rpc = CordIrisRpcClient()
    if data['item']:
    #            print data['item']
        for item in data['item']:
            ilog = models.IndemnityLog()
            ilog.customer = data['customer']
            ilog.item = item
            lg = rpc.compensate(data['customer'].key, None, item.key, ilog, data['duration'])
            #                print lg.status
            logs.append(lg)
        #                if lg.status !=1:
        #                    print item
        #                    data['item']._result_cache.remove(item)
    if data['package']:
    #            print data['package']
        for package in data['package']:
            plog = models.IndemnityLog()
            plog.customer = data['customer']
            plog.package = package
            pg = rpc.compensate(data['customer'].key, package.key, None, plog, data['duration'])
            #                print pg.status
            logs.append(pg)
        #                if pg.status !=1:
        #                    print package
        #                    data['package']._result_cache.remove(package)
    data['indemnityLog'] = logs
    return data


class IndemnityForm(forms.ModelForm):
    def clean(self):
        cdata = self.cleaned_data
        if 'matter' in cdata and 'customer' in cdata:
            if ("package" in cdata or "item" in cdata):
                try:
                    cdata = mq(cdata)
                except:
                    raise forms.ValidationError("AMQP  Connection Fail or Error ")
            else:
                raise forms.ValidationError("Choose at least item or package")
        else:
            raise forms.ValidationError("matter or  customer is  None")

        return cdata


class IspAdmin(admin.ModelAdmin):
    list_display = ('id', 'title', 'mark',)
    fields = ['title', 'mark', ]


class UrlAdmin(admin.ModelAdmin):
    list_display = ('id', 'title', 'url', 'length', 'is_show',)
    fields = ['title', 'url', 'length', 'isp', 'is_show', ]


class SpeedlogAdmin(admin.ModelAdmin):
    list_display = ('ip', '__unicode__', 'user_agent', 'location', 'isp', 'speed', 'create_date',)
    search_fields = ('user_agent', 'ip', 'location')
    raw_id_fields = ('url',)
    related_lookup_fields = {
        '__unicode__': ('url',),
        }
    readonly_fields = ('ip', '__unicode__', 'user_agent', 'location', 'isp', 'speed', 'create_date', 'url')


class PointAdmin(admin.ModelAdmin):
    list_display = ('name', 'hant_name', 'en_name',)


class PointlogAdmin(admin.ModelAdmin):
    list_display = ('id',
    point, 'user_agent', 'device', 'size', speeds, 'description','content', 'phone', 'mail', 'ip', 'location', 'isp', 'create_date', 'reply_time', 'status',
    'result', 'update_date', clip)
    list_editable = ('status',)
    search_fields = ('user_agent', 'phone', 'mail', 'ip', 'description', 'location',)
    radio_fields = {'status': admin.HORIZONTAL}
    readonly_fields = (
    point, 'user_agent', 'speeds', 'description', 'phone', 'mail', 'ip', 'location', 'isp', 'create_date', 'update_date'
    , clip,)


class VideotypeAdmin(admin.ModelAdmin):
    list_display = ('type_name', 'bit_rate', 'resolution', 'hant_resolution', 'en_resolution',)


class CustomerAdmin(admin.ModelAdmin):
    list_display = ('key', 'name',)
    search_fields = ('key', 'name',)


class PackageAdmin(admin.ModelAdmin):
    list_display = ('key', 'title', 'description',)
    search_fields = ('key', 'title', 'description',)


class ItemAdmin(admin.ModelAdmin):
    list_display = ('key', 'title',)
    search_fields = ('key', 'title',)


class IndemnityAdmin(admin.ModelAdmin):
    list_display = ('customer', packages, items, 'matter', 'duration', 'update_date',)
    filter_horizontal = ('package', 'item', 'indemnityLog')
    readonly_fields = ('indemnityLog',)
    search_fields = ('customer__name', 'package__title', 'item__title')
    raw_id_fields = ('customer',)

    form = IndemnityForm


class IndemnityLogAdmin(admin.ModelAdmin):
    list_display = (
    'customer', 'package', 'item', 'status', 'error_desc', 'error_code', 'correlation_id', 'create_date',
    'update_date',)
    readonly_fields = (
    'customer', 'package', 'item', 'correlation_id', 'error_desc', 'error_code', 'create_date', 'update_date', 'status'
    ,)
    search_fields = ('customer__name', 'package__title', 'item__title')


class ClipLogAdmin(admin.ModelAdmin):
    list_display = ('key', 'url', 'quality', 'create_date')
    readonly_fields = ('key', 'url', 'quality', 'create_date')


class QualityAdmin(admin.ModelAdmin):
    list_display = ('key', 'name',)

admin.site.register(models.Videotype, VideotypeAdmin)
admin.site.register(models.Url, UrlAdmin)
admin.site.register(models.Point, PointAdmin)
admin.site.register(models.Pointlog, PointlogAdmin)
admin.site.register(models.Speedlog, SpeedlogAdmin)
admin.site.register(models.Isp, IspAdmin)
admin.site.register(models.Customer, CustomerAdmin)
admin.site.register(models.Package, PackageAdmin)
admin.site.register(models.Item, ItemAdmin)
admin.site.register(models.Indemnity, IndemnityAdmin)
admin.site.register(models.IndemnityLog, IndemnityLogAdmin)
admin.site.register(models.ClipLog, ClipLogAdmin)
admin.site.register(models.Quality, QualityAdmin)