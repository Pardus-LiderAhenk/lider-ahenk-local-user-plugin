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
        self.desktop_write_permission = self.task['desktop_write_permission']
        self.kiosk_mode = self.task['kiosk_mode']

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

        self.logger.debug('Parameters were initialized.')

    def handle_task(self):
        try:
            if not self.is_exist(self.home):
                self.create_directory(self.home)
            self.execute(self.add_user.format(self.home, self.username))
            self.logger.debug('Added new user: {0}, home: {1}'.format(self.username, self.home))

            self.execute(self.change_owner.format(self.username, self.home))
            self.execute(self.change_permission.format(self.home))
            self.logger.debug('Changed owner and permission for home directory.')

            if self.groups != "":
                self.execute(self.add_user_to_groups.format(self.groups, self.username))
                self.logger.debug('Added user to these groups: {}'.format(self.groups))

            if str(self.password).strip() != "":
                result_code, p_out, p_err = self.execute(self.create_shadow_password.format(self.password))
                shadow_password = p_out.strip()
                self.execute(self.change_password.format('\'{}\''.format(shadow_password), self.username))
                self.logger.debug('Changed password.')

            self.execute(self.change_shell.format(self.username))
            self.logger.debug('Changed user shell to /bin/bash')

            if self.active == "true":
                self.execute(self.enable_user.format(self.username))
                self.logger.debug('The user has been enabled.')
            elif self.active == "false":
                self.execute(self.disable_user.format(self.username))
                self.logger.debug('The user has been disabled.')

            if self.desktop_write_permission == "true":
                self.execute('chown -R {0}:{1} /home/{2}/Masaüstü'.format(self.username, self.username, self.username))
                self.logger.debug('chown -R {0}:{1} /home/{2}/Masaüstü'.format(self.username, self.username, self.username));

            elif self.desktop_write_permission == "false":
                self.execute('chown -R root:root /home/{0}/Masaüstü'.format(self.username))
                self.logger.debug('chown -R root:root /home/{0}/Masaüstü'.format(self.username))

            if self.kiosk_mode == "true":
                comm = "sed -i 's/^.*" + '<channel name="xfce4-panel"'+ ".*$/" + '<channel name="xfce4-panel" version="1.0" locked="*" unlocked="root">' + "/' /etc/xdg/xfce4/xfconf/xfce-perchannel-xml/xfce4-panel.xml"
                self.execute(comm)

            elif self.kiosk_mode == "false":
                comm = "sed -i 's/^.*" + '<channel name="xfce4-panel"' + ".*$/" + '<channel name="xfce4-panel" version="1.0">' + "/' /etc/xdg/xfce4/xfconf/xfce-perchannel-xml/xfce4-panel.xml"
                self.execute(comm)

            self.logger.info('User has been added successfully.')

            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='Kullanıcı başarıyla eklendi.')



        except Exception as e:
            self.logger.error('A problem occurred while handling Local-User task: {0}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='Local-User görevi çalıştırılırken bir hata oluştu.')


def handle_task(task, context):
    add_user = AddUser(task, context)
    add_user.handle_task()
