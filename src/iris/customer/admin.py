from django.contrib import admin

from . import models


class UrlAdmin(admin.ModelAdmin):
    list_display = ('id','title', 'url', 'length', 'is_show',)
    fields  = ['title', 'url', 'length', 'is_show',]

class SpeedlogAdmin(admin.ModelAdmin):
    list_display = ('ip','__unicode__','user_agent','location','isp', 'speed','create_date',)
    search_fields = ('user_agent','ip','location')
    raw_id_fields = ('url',)
    related_lookup_fields = {
        '__unicode__': ('url',),
    }



class PointAdmin(admin.ModelAdmin):
    list_display = ('name','hant_name','en_name',)

class PointlogAdmin(admin.ModelAdmin):
    list_display = ('point','user_agent','speeds' , 'description', 'phone', 'mail', 'ip', 'location','isp','create_date',)
    search_fields = ('user_agent','phone','mail','ip','description','location')

class VideotypeAdmin(admin.ModelAdmin):
    list_display = ('type_name','bit_rate' , 'resolution', 'hant_resolution', 'en_resolution' ,)

admin.site.register(models.Videotype,VideotypeAdmin)
admin.site.register(models.Url,UrlAdmin)
admin.site.register(models.Point,PointAdmin)
admin.site.register(models.Pointlog,PointlogAdmin)
admin.site.register(models.Speedlog,SpeedlogAdmin)
