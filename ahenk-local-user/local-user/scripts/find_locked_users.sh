#!/bin/bash

cat /etc/xdg/xfce4/xfconf/xfce-perchannel-xml/xfce4-panel.xml | sed -n 's/^.*locked="\([A-Za-z; ]*\)".*$/\1/p'
