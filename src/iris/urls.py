from django.conf import settings
from django.views.generic.simple import direct_to_template
from django.conf.urls.defaults import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'service.views.home', name='home'),
    url("^$", direct_to_template, {"template": "index.html"}, name="home"),
    url(r'^customer/', include('iris.customer.urls')),
    url(r'^', include('djangorestframework.urls'),name='djangorestframework'),
    # Uncomment the admin/doc line below to enable admin documentation:
    url(r'^admin/doc/', include('django.contrib.admindocs.urls')),
    url(r'^grappelli/', include('grappelli.urls')),
    # Uncomment the next line to enable the admin:
    url(r'^admin/', include(admin.site.urls)),

)
# Serve static files when developing
if settings.DEBUG:
    from django.contrib.staticfiles.urls import staticfiles_urlpatterns
    from django.conf.urls.static import static
    urlpatterns += staticfiles_urlpatterns()
    urlpatterns += static('/media/', document_root=settings.MEDIA_ROOT)
    urlpatterns += static('/static/', document_root=settings.STATIC_ROOT)