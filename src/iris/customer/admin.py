from django.contrib import admin

from . import models

class UrlAdmin(admin.ModelAdmin):
  list_display = ('title', 'url', 'length', 'is_show',)

class SpeedlogAdmin(admin.ModelAdmin):
    list_display = ('ip', 'isp', 'speed','create_date',)

class PointAdmin(admin.ModelAdmin):
    list_display = ('name',)

class PointlogAdmin(admin.ModelAdmin):
    list_display = ('point', 'speeds' , 'description', 'phone', 'mail', 'ip', 'isp','create_date',)
    
class VideotypeAdmin(admin.ModelAdmin):
    
    list_display = ('type_name', 'bit_rate' , 'resolution',)

admin.site.register(models.Videotype,VideotypeAdmin)
admin.site.register(models.Url,UrlAdmin)
admin.site.register(models.Point,PointAdmin)
admin.site.register(models.Pointlog,PointlogAdmin)
admin.site.register(models.Speedlog,SpeedlogAdmin)
