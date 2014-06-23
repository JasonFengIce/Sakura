from django.conf.urls.defaults import patterns, url
from . import views

urlpatterns = patterns('',
    url(r'^speedlogs/$', views.Speedlogs.as_view(), name='speedlogs'),
    url(r'^pointlogs/$', views.Pointlogs.as_view(), name='pointlogs'),
    url(r'^points/$', views.PointsView.as_view(), name='points'),
    url(r'^urls/$', views.UrlsView.as_view(), name='urls'),
    url(r'^pointlogs/init/$', views.InitDevices.as_view(), name='init')
)
