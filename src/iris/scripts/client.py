#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Author: Peter Peng


import json
import pika
import uuid

import sys
import os
sys.path.append((os.path.realpath(sys.argv[0]).rpartition('/')[0] or '.') + '/../lib')
from django.core.management import setup_environ



def init():
    sys.path.append('../')
    sys.path.append('../../')
    print sys.path
    from iris import settings
    setup_environ(settings)

class CordIrisRpcClient(object):
    def __init__(self):
        credential = pika.PlainCredentials(
                username='cord_iris_client',
                password='fcR9JsYSSol7xFBiHsXSWSVcZTvMO3x4ifqjUYoO9B8=')
        self.connection = pika.BlockingConnection(
                pika.ConnectionParameters(host='cord_rabbitmq', virtual_host='/cord/iris',
                credentials=credential))
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
        if self.corr_id == props.correlation_id:
            self.response = body

    def list_users(self, page_num):
        self.response = None
        self.corr_id = str(uuid.uuid4())
        self.channel.basic_publish(exchange='exchange.cord_iris',
                routing_key='cord.iris',
                properties=pika.BasicProperties(reply_to=self.callback_queue,
                    correlation_id=self.corr_id),
                body=json.dumps(dict(method='list_users', page_num=page_num)))

        while self.response is None:
            self.connection.process_data_events()
        return json.loads(self.response)

    def list_packages(self, page_num):
        self.response = None
        self.corr_id = str(uuid.uuid4())
        self.channel.basic_publish(exchange='exchange.cord_iris',
                routing_key='cord.iris',
                properties=pika.BasicProperties(reply_to=self.callback_queue,
                    correlation_id=self.corr_id),
                body=json.dumps(dict(method='list_packages', page_num=page_num)))

        while self.response is None:
            self.connection.process_data_events()
        return json.loads(self.response)

    def list_expenses(self, page_num):
        self.response = None
        self.corr_id = str(uuid.uuid4())
        self.channel.basic_publish(exchange='exchange.cord_iris',
                routing_key='cord.iris',
                properties=pika.BasicProperties(reply_to=self.callback_queue,
                    correlation_id=self.corr_id),
                body=json.dumps(dict(method='list_expenses', page_num=page_num)))

        while self.response is None:
            self.connection.process_data_events()
        return json.loads(self.response)

   

def get_list_users(rpc):
    from iris.customer import models
    i = 0
    total = 0
    while True:
        i += 1
        print '[x] Request list_users(%d)' % i
        response = rpc.list_users(i)
        objects = response['objects']
        total += len(objects)
        print '[x] Got %s users' % (total, )
        for obj in  objects:
            customer  = models.Customer()
            customer.name = str(obj['username'])
            customer.pk = int(obj['pk'])
            customer.save()
        if response['has_more'] != True:
            break

def get_list_packages(rpc):
    from iris.customer import models
    i = 0
    total = 0
    while True:
        i += 1
        print '[x] Request list_packages(%d)' % i
        response = rpc.list_packages(i)
        total += len(response['objects'])
        for obj in  response['objects']:
            package  = models.Package()
            package.title = obj['title']
            package.pk = obj['pk']
            package.description = obj['description']
            package.adlet_url = obj['adlet_url']
            package.poster_url = obj['poster_url']
            package.thumb_url = obj['thumb_url']
            package.save()
        print '[x] Got %s packages' % (total, )
        if response['has_more'] != True:
            break

def get_list_expenses(rpc):
    from iris.customer import models
    i = 0
    total = 0
    while True:
        i += 1
        print '[x] Request list_expenses(%d)' % i
        response = rpc.list_expenses(i)
        total += len(response['objects'])
        for obj in  response['objects']:
            item  = models.Item()
            item.title = obj['title']
            item.pk = obj['pk']
            item.adlet_url = obj['adlet_url']
            item.poster_url = obj['poster_url']
            item.thumb_url = obj['thumb_url']
            item.save()
        print '[x] Got %s expenses' % (total, )
        if response['has_more'] != True:
            break




if __name__ == '__main__':
    init()
    os.environ['DJANGO_SETTINGS_MODULE'] = 'iris.settings'
    rpc = CordIrisRpcClient()
    get_list_users(rpc)
    get_list_packages(rpc)
    get_list_expenses(rpc)
    pass
