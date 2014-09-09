from django.db import models
from django.utils.translation import  ugettext_lazy as _


# Create your models here.

STATUS_CHOICES = (
    (0, 'Fail'),
    (1, 'Success'),
    (2, 'Wait'),
    )
STATUS_PROCESS = (
    (1, _("Processed")),
    (0, _("Waiting")),
    )

class Isp(models.Model):
    title = models.CharField(_("Title"), max_length=200)
    mark = models.CharField(_("Mark"), max_length=2)

    class Meta:
        verbose_name = _("Isp")
        verbose_name_plural = _("Isp")
        ordering = ('id',)

    def __unicode__(self):
        return self.title


class Url(models.Model):
    title = models.CharField(_("Title"), max_length=200)
    url = models.CharField(_("Url"), max_length=200)
    length = models.IntegerField(_("Start Time"), max_length=20)
    is_show = models.BooleanField(_("Is Show"), default=1)
    isp = models.ForeignKey(Isp, verbose_name=_('Isp'))

    class Meta:
        verbose_name = _("Url")
        verbose_name_plural = _("Url")
        ordering = ('id',)

    def __unicode__(self):
        return self.title


class Speedlog(models.Model):
    ip = models.CharField(_("Ip"), max_length=20, )
    url = models.ForeignKey(Url, verbose_name=_('Url'), )
    location = models.CharField(_("Location"), max_length=200, blank=True, null=True, )
    isp = models.CharField(_("isp"), max_length=30, )
    speed = models.FloatField(_("Speed"), max_length=20, )
    user_agent = models.CharField(_("User Agent"), max_length=100, blank=True, null=True, )
    create_date = models.DateTimeField(_("Created at"), auto_now_add=True, )

    class Meta:
        verbose_name = _("Speedlog")
        verbose_name_plural = _("Speedlog")
        ordering = ('-create_date',)

    def __unicode__(self):
        return self.url.title


class Point(models.Model):
    name = models.CharField(_("Name"), max_length=20)
    hant_name = models.CharField(_("Name") + "(hant)", max_length=20)
    en_name = models.CharField(_("Name") + "(en)", max_length=20)

    class Meta:
        verbose_name = _("Point")
        verbose_name_plural = _("Point")
        ordering = ('id',)

    def __unicode__(self):
        return self.name


class Quality(models.Model):
    key = models.CharField(_("Key"), max_length=30)
    name = models.CharField(_("Quality"), max_length=30)

    class Meta:
        verbose_name = _("Quality")
        verbose_name_plural = _("Quality")

    def __unicode__(self):
        return self.key


class ClipLog(models.Model):
    key = models.CharField(_("Clip Id"), max_length=100)
    url = models.CharField(_("Url"), max_length=1024)
    quality = models.ForeignKey(Quality, blank=True, null=True, )
    create_date = models.DateTimeField(_("Created at"), auto_now_add=True, )

    class Meta:
        verbose_name = _("ClipLog")
        verbose_name_plural = _("ClipLog")

    def __unicode__(self):
        return self.key


class Pointlog(models.Model):
    point = models.CharField(_("Point"), max_length=200, editable=False, default='No Data')
    speeds = models.CharField(_("Speed"), max_length=200, default='No Data')
    description = models.CharField(_("Description"), max_length=500, default='No Data')
    clip = models.ForeignKey(ClipLog, blank=True, null=True, editable=False, )
    phone = models.CharField(_("Phone"), max_length=20, blank=True, null=True, )
    mail = models.CharField(_("Mail"), max_length=20, blank=True, null=True, )
    ip = models.CharField(_("Ip"), max_length=20, blank=True, null=True, )
    location = models.CharField(_("Location"), max_length=200, blank=True, null=True, default='No Data')
    isp = models.CharField(_("Isp"), max_length=50, blank=True, null=True, )
    user_agent = models.CharField(_("User Agent"), max_length=100, blank=True, null=True, )
    create_date = models.DateTimeField(_("Created at"), auto_now_add=True, )
    status = models.IntegerField(verbose_name=_("Status"), default=0, choices=STATUS_PROCESS, max_length=1)
    result = models.CharField(_('result'), max_length=200, blank=True, null=True, )
    update_date = models.DateTimeField(_("Update at"), auto_now=True, )
    sn = models.CharField(_("SN"), max_length=50, editable=False, )
    device = models.CharField(_("Device"), max_length=50, editable=False, )
    size = models.CharField(_("Size"), max_length=50, editable=False, )
    content = models.CharField(_("Reply Content"), max_length=500, blank=True, null=True,)
    reply_time = models.DateTimeField(_("Reply at"),blank=True, null=True, editable=False,)
    width = models.CharField(_("Width"), max_length=50, editable=False, )
    class Meta:
        verbose_name = _("Pointlog")
        verbose_name_plural = _("Pointlog")
        ordering = ('-create_date',)


class Videotype(models.Model):
    type_name = models.CharField(_("type name"), max_length=20)
    bit_rate = models.FloatField(_("Bit Rate"), max_length=16)
    resolution = models.CharField(_("Resolution"), max_length=30)
    hant_resolution = models.CharField(_("Resolution") + "(HANT)", max_length=30)
    en_resolution = models.CharField(_("Resolution") + "(EN)", max_length=100)

    class Meta:
        verbose_name = _("Videotype")
        verbose_name_plural = _("Videotype")


class Customer(models.Model):
    key = models.IntegerField(_("pk"), max_length=20, editable=False)
    name = models.CharField(_("name"), max_length=30, editable=False)
    update_date = models.DateTimeField(_("Update at"), auto_now=True, editable=False)

    class Meta:
        verbose_name = _("Customer")
        verbose_name_plural = _("Customer")

    def __unicode__(self):
        return self.name


class Package(models.Model):
    key = models.IntegerField(_("pk"), max_length=20, editable=False)
    title = models.CharField(_("title"), max_length=30, editable=False)
    description = models.CharField(_("description"), max_length=300, editable=False)
    adlet_url = models.CharField(_("adlet_url"), max_length=1024, editable=False)
    poster_url = models.CharField(_("poster_url"), max_length=1024, editable=False)
    thumb_url = models.CharField(_("thumb_url"), max_length=1024, editable=False)
    update_date = models.DateTimeField(_("Update at"), auto_now=True, editable=False)

    class Meta:
        verbose_name = _("Package")
        verbose_name_plural = _("Package")

    def __unicode__(self):
        return self.title


class Item(models.Model):
    key = models.IntegerField(_("pk"), max_length=20, editable=False)
    title = models.CharField(_("title"), max_length=30, editable=False)
    adlet_url = models.CharField(_("adlet_url"), max_length=1024, editable=False)
    poster_url = models.CharField(_("poster_url"), max_length=1024, editable=False)
    thumb_url = models.CharField(_("thumb_url"), max_length=1024, editable=False)
    update_date = models.DateTimeField(_("Update at"), auto_now=True, editable=False)

    class Meta:
        verbose_name = _("Item")
        verbose_name_plural = _("Item")

    def __unicode__(self):
        return self.title


class IndemnityLog(models.Model):
    customer = models.ForeignKey(Customer, verbose_name=_('Customer'))
    package = models.ForeignKey(Package, verbose_name=_('Package'), blank=True, null=True)
    item = models.ForeignKey(Item, verbose_name=_('Item'), blank=True, null=True)
    correlation_id = models.CharField(_("Correlation id"), max_length=100)
    status = models.IntegerField(verbose_name=_("Status"), default=2, choices=STATUS_CHOICES)
    error_desc = models.CharField(_("Error_desc"), max_length=100, blank=True, null=True)
    error_code = models.IntegerField(_("Error_code"), max_length=1, blank=True, null=True)
    create_date = models.DateTimeField(_("Created at"), auto_now_add=True)
    update_date = models.DateTimeField(_("Update at"), auto_now=True)

    class Meta:
        verbose_name = _("IndemnityLog")
        verbose_name_plural = _("IndemnityLog")

    def __unicode__(self):
        if self.item:
            return str("%s(%s)") % (self.item.__unicode__(), self.error_desc)
        elif self.package:
            return str("%s(%s)") % (self.package.__unicode__(), self.error_desc)
        return None


class Indemnity(models.Model):
    customer = models.ForeignKey(Customer, verbose_name=_('Customer'))
    package = models.ManyToManyField(Package, verbose_name=_('Package'), blank=True, null=True)
    item = models.ManyToManyField(Item, verbose_name=_('Item'), blank=True, null=True)
    matter = models.CharField(_("matter"), max_length=300)
    update_date = models.DateTimeField(_("Update at"), auto_now=True, editable=False)
    indemnityLog = models.ManyToManyField(IndemnityLog, verbose_name=_('IndemnityLog'), blank=True, null=True)
    duration = models.IntegerField(_("days"))

    class Meta:
        verbose_name = _("Indemnity")
        verbose_name_plural = _("Indemnity")

    def __unicode__(self):
        return self.matter
