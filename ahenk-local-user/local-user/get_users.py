#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author:Mine DOGAN <mine.dogan@agem.com.tr>

import json

from base.plugin.abstract_plugin import AbstractPlugin


class GetUsers(AbstractPlugin):
    def __init__(self, task, context):
        super(GetUsers, self).__init__()
        self.task = task
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

        self.command_users = 'awk -F: \'{print $1 ":" $6 ":" $7}\' /etc/passwd | grep /bin/bash'
        self.command_user_groups = 'groups {}'
        self.command_not_active = 'egrep \':\!\' /etc/shadow |awk -F: \'{print $1}\''

        self.logger.debug('[LOCAL-USER] Parameters were initialized.')

    def handle_task(self):

        try:
            user_list = []

            result_code, p_out, p_err = self.execute(self.command_users)
            lines = p_out.split('\n')
            lines.pop()

            for line in lines:
                detail = line.split(':')

                result_code, p_out, p_err = self.execute(self.command_user_groups.format(str(detail[0]).strip()))
                groups = p_out.split(':')
                groups[1] = str(groups[1]).strip()
                groups[1] = groups[1].replace("'", "").replace(" ", ", ")

                is_active = 'true'

                result_code, p_out, p_err = self.execute(self.command_not_active)
                users = p_out.split('\n')

                if str(detail[0]).strip() in users:
                    is_active = 'false'

                user = {'user':str(detail[0]).strip(), 'groups':groups[1], 'home':detail[1], 'is_active':is_active}
                user_list.append(user)

                self.logger.debug('user: {0}, groups: {1}, home: {2}, is_active: {3}'.format(str(detail[0]).strip(), groups[1], detail[1], is_active))


            self.logger.info('[LOCAL-USER] Local-User task is handled successfully')
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Kullanıcı listesi başarıyla getirildi.',
                                         data=json.dumps({'users':user_list}),
                                         content_type=self.get_content_type().APPLICATION_JSON.value)

        except Exception as e:
            self.logger.error('[LOCAL-USER] A problem occured while handling Local-User task: {0}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Local-User görevi çalıştırılırken bir hata oluştu.')


def handle_task(task, context):
    get_users = GetUsers(task, context)
    get_users.handle_task()
