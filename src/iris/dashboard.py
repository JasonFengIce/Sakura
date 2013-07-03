#-*- coding: utf-8 -*-

from django.utils.translation import ugettext_lazy as _
from django.core.urlresolvers import reverse

from grappelli.dashboard import modules, Dashboard
from grappelli.dashboard.utils import get_admin_site_name


class CustomIndexDashboard(Dashboard):

    def init_with_context(self, context):

        self.children.append(modules.ModelList(
            _("Messages Manager"),
            collapsible=True,
            column=1,
            css_classes=('collapse',),
            models=(
                'iris.customer.models.Pointlog',
                'iris.customer.models.Speedlog',
                'iris.customer.models.Url',
                'iris.customer.models.Point',
                'iris.customer.models.Videotype',
                'iris.customer.models.Isp',
                'iris.customer.models.ClipLog',
                'iris.customer.models.Quality',
                )
        ))
        self.children.append(modules.ModelList(
            _("Indemnity Manager"),
            collapsible=True,
            column=1,
            css_classes=('collapse',),
            models=(
                 'iris.customer.models.Indemnity',
                 'iris.customer.models.IndemnityLog',
                 'iris.customer.models.Customer',
                 'iris.customer.models.Item',
                 'iris.customer.models.Package',
                )
        ))
        self.children.append(
            modules.ModelList(
                _("System"),
                collapsible=True,
                column=2,
                css_classes=('collapse',),
                models=('django.contrib.*',)
            )
          )

        # append a recent actions module
        self.children.append(modules.RecentActions(
            _('Recent Actions'),
            limit=5,
            collapsible=False,
            column=3,
        ))


