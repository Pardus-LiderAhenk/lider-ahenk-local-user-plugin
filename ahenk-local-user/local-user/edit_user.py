#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author:Mine DOGAN <mine.dogan@agem.com.tr>

from base.plugin.abstract_plugin import AbstractPlugin

class EditUser(AbstractPlugin):
    def __init__(self, task, context):
        super(EditUser, self).__init__()
        self.task = task
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

        self.username = self.task['username']
        self.new_username = self.task['new_username']
        self.password = self.task['password']
        self.home = self.task['home']
        self.active = self.task['active']
        self.groups = self.task['groups']

        self.kill_processes = 'pkill -u {}'
        self.change_username = 'usermod -l {0} {1}'
        self.create_shadow_password = 'mkpasswd -m sha-512 {}'
        self.change_password = 'usermod -p {0} {1}'
        self.change_home = 'usermod -m -d {0} {1}'
        self.enable_user = 'passwd -u {}'
        self.disable_user = 'passwd -l {}'
        self.change_groups = 'usermod -G {0} {1}'
        self.change_owner = 'chown {0}.{0} {1}'
        self.change_permission = 'chmod 755 {}'
        self.logout_user = 'pkill -u {}'
        self.kill_all_process = 'killall -KILL -u {}'

        self.logger.debug('[LOCAL-USER - EDIT] Parameters were initialized.')

    def handle_task(self):
        try:
            self.execute(self.logout_user.format(self.username))
            self.execute(self.kill_all_process.format(self.username))
            self.logger.debug('[LOCAL-USER - DELETE] Killed all processes for {}'.format(self.username))

            if str(self.new_username).strip() != "":
                self.execute(self.kill_processes.format(self.username))
                self.execute(self.change_username.format(self.new_username, self.username))
                self.logger.debug('[LOCAL-USER - EDIT] Changed username {0} to {1}'.format(self.username, self.new_username))
                self.username = self.new_username

            if str(self.password).strip() != "":
                result_code, p_out, p_err = self.execute(self.create_shadow_password.format(self.password))
                shadow_password = p_out.strip()
                self.execute(self.change_password.format('\'{}\''.format(shadow_password), self.username))
                self.logger.debug('[LOCAL-USER - EDIT] Changed password.')

            if not self.is_exist(self.home):
                self.create_directory(self.home)

            self.execute(self.change_home.format(self.home, self.username))
            self.logger.debug('[LOCAL-USER - EDIT] Changed home directory to: {}'.format(self.home))

            self.execute(self.change_owner.format(self.username, self.home))
            self.execute(self.change_permission.format(self.home))
            self.logger.debug('[LOCAL-USER - ADD] Changed owner and permission for home directory.')

            if self.active == "true":
                self.execute(self.enable_user.format(self.username))
                self.logger.debug('[LOCAL-USER - EDIT] The user has been enabled.')
            elif self.active == "false":
                self.execute(self.disable_user.format(self.username))
                self.logger.debug('[LOCAL-USER - EDIT] The user has been disabled.')

            if self.groups != "":
                self.execute(self.change_groups.format(self.groups, self.username))
                self.logger.debug('[LOCAL-USER - EDIT] Added user to these groups: {}'.format(self.groups))


            self.logger.info('[LOCAL-USER - EDIT] User has been edited successfully.')
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Kullanıcı başarıyla düzenlendi.')

        except Exception as e:
            self.logger.error('[LOCAL-USER - EDIT] A problem occured while handling Local-User task: {0}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Local-User görevi çalıştırılırken bir hata oluştu.')

def handle_task(task, context):
    edit_user = EditUser(task, context)
    edit_user.handle_task()