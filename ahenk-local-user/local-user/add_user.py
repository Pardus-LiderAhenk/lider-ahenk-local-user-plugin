#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author:Mine DOGAN <mine.dogan@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin

class AddUser(AbstractPlugin):
    def __init__(self, task, context):
        super(AddUser, self).__init__()
        self.task = task
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

        self.username = self.task['username']
        self.password = self.task['password']
        self.home = self.task['home']
        self.active = self.task['active']
        self.groups = self.task['groups']

        self.add_user = 'useradd -d {0} {1}'
        self.check_home_owner = 'stat -c \'%U\' {}'
        self.enable_user = 'passwd -u {}'
        self.disable_user = 'passwd -l {}'
        self.add_user_to_groups = 'usermod -a -G {0} {1}'
        self.create_shadow_password = 'mkpasswd -m sha-512 {}'
        self.change_password = 'usermod -p {0} {1}'
        self.change_shell = 'usermod -s /bin/bash {}'
        self.change_owner = 'chown {0}.{0} {1}'
        self.change_permission = 'chmod 755 {}'

        self.logger.debug('[LOCAL-USER - ADD] Parameters were initialized.')

    def handle_task(self):
        try:
            if not self.is_exist(self.home):
                self.create_directory(self.home)
            self.execute(self.add_user.format(self.home, self.username))
            self.logger.debug('[LOCAL-USER - ADD] Added new user: {0}, home: {1}'.format(self.username, self.home))

            self.execute(self.change_owner.format(self.username, self.home))
            self.execute(self.change_permission.format(self.home))
            self.logger.debug('[LOCAL-USER - ADD] Changed owner and permission for home directory.')

            if self.active == "true":
                self.execute(self.enable_user.format(self.username))
                self.logger.debug('[LOCAL-USER - ADD] The user has been enabled.')
            elif self.active == "false":
                self.execute(self.disable_user.format(self.username))
                self.logger.debug('[LOCAL-USER - ADD] The user has been disabled.')

            if self.groups != "":
                self.execute(self.add_user_to_groups.format(self.groups, self.username))
                self.logger.debug('[LOCAL-USER - ADD] Added user to these groups: {}'.format(self.groups))

            if str(self.password).strip() != "":
                result_code, p_out, p_err = self.execute(self.create_shadow_password.format(self.password))
                shadow_password = p_out.strip()
                self.execute(self.change_password.format('\'{}\''.format(shadow_password), self.username))
                self.logger.debug('[LOCAL-USER - ADD] Changed password.')

            self.execute(self.change_shell.format(self.username))
            self.logger.debug('[LOCAL-USER - ADD] Changed user shell to /bin/bash')

            self.logger.info('[LOCAL-USER - ADD] User has been added successfully.')
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Kullanıcı başarıyla eklendi.')

        except Exception as e:
            self.logger.error('[LOCAL-USER - ADD] A problem occured while handling Local-User task: {0}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Local-User görevi çalıştırılırken bir hata oluştu.')

def handle_task(task, context):
    add_user = AddUser(task, context)
    add_user.handle_task()