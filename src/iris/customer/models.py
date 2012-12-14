from django.db import models
from django.utils.translation import ugettext, ugettext_lazy as _

# Create your models here.

class Isp(models.Model):
    title = models.CharField(_("Title"), max_length=200)
    mark =  models.CharField(_("Mark"), max_length=2)
    class Meta:
        verbose_name = _("Isp")
        verbose_name_plural = _("Isp")
        ordering = ('id',)
    def __unicode__(self):
        return self.title;

class Url(models.Model):
    title = models.CharField(_("Title"), max_length=200)
    url = models.CharField(_("Url"), max_length=200)
    length = models.IntegerField(_("Start Time"),max_length=20)
    is_show = models.BooleanField(_("Is Show"),default=1)
    isp  = models.ForeignKey(Isp, verbose_name=_('Isp'))
    class Meta:
        verbose_name = _("Url")
        verbose_name_plural = _("Url")
        ordering = ('id',)

    def __unicode__(self):
        return self.title;



class Speedlog(models.Model):
    ip = models.CharField(_("Ip"), max_length=20)
    url  = models.ForeignKey(Url, verbose_name=_('Url'))
    location = models.CharField(_("Location"), max_length=200,null=True)
    isp = models.CharField(_("isp"), max_length=30)
    speed = models.FloatField(_("Speed"),max_length=20)
    user_agent =  models.CharField(_("User Agent"), max_length=100,null=True)
    create_date = models.DateTimeField(_("Created at"), auto_now_add=True)
    class Meta:
        verbose_name = _("Speedlog")
        verbose_name_plural = _("Speedlog")
        ordering = ('-create_date',)

    def __unicode__(self):
        return self.url.title;

class Point(models.Model):
    name = models.CharField(_("Name"), max_length=20)
    hant_name =  models.CharField(_("Name")+"(hant)", max_length=20)
    en_name =  models.CharField(_("Name")+"(en)", max_length=20)
    class Meta:
        verbose_name = _("Point")
        verbose_name_plural = _("Point")
        ordering = ('id',)
    def __unicode__(self):
        return self.name

class Pointlog(models.Model):
    point =  models.CharField(_("Point"), max_length=200)
    speeds  = models.CharField(_("Speed"), max_length=200)
    description = models.CharField(_("Description"), max_length=500)
    phone =  models.CharField(_("Phone"), max_length=20,null=True)
    mail =  models.CharField(_("Mail"), max_length=20,null=True)
    ip = models.CharField(_("Ip"), max_length=20,null=True)
    location = models.CharField(_("Location"), max_length=200,null=True)
    isp = models.CharField(_("Isp"), max_length=50,null=True)
    user_agent =  models.CharField(_("User Agent"), max_length=100,null=True)
    create_date = models.DateTimeField(_("Created at"), auto_now_add=True)
    class Meta:
        verbose_name = _("Pointlog")
        verbose_name_plural = _("Pointlog")
        ordering = ('-create_date',)
    
class Videotype(models.Model):
    type_name = models.CharField(_("type name"),max_length=20 )
    bit_rate =  models.FloatField(_("Bit Rate"),max_length=16 )
    resolution = models.CharField(_("Resolution"),max_length=30 )
    hant_resolution =  models.CharField(_("Resolution") +"(HANT)", max_length=30)
    en_resolution =  models.CharField(_("Resolution")+"(EN)", max_length=100)
    class Meta:
        verbose_name = _("Videotype")
        verbose_name_plural = _("Videotype")
  
    
#class customer(models.Model):
#    ip = models.CharField(_("Ip"), max_length=20)
#    isp = models.CharField(_("isp"), max_length=20)
#    is_correct = models.BooleanField(_("Is Show"),default=1)
#    type =  models.CharField(_("Type"), max_length=20)
#    version = models.CharField(_("Version"), max_length=20)
#    serial = models.CharField(_("Serial"), max_length=20,null=True)
#    info =  models.CharField(_("Serial"), max_length=20,null=True)