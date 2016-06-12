#!/usr/bin/env bash

set -e

[ "$DEBUG" == 'true' ] && set -x

DAEMON=sshd

echo | adduser -D $WALNUT_USERNAME
echo $WALNUT_SEID > /tmp/$WALNUT_SEID
echo $WALNUT_SEID >> /tmp/$WALNUT_SEID

passwd $WALNUT_USERNAME < /tmp/$WALNUT_SEID
passwd < /tmp/$WALNUT_SEID

/usr/sbin/sshd -f /etc/ssh/sshd_config

chown -R $WALNUT_USERNAME /opt/fuse

#rm -fr /home/$WALNUT_USERNAME
#ln -s /opt/fuse/home/$WALNUT_USERNAME /home/$WALNUT_USERNAME

python /opt/walnut/ess.py $WALNUT_SEID $WALNUT_HOST $WALNUT_PORT /opt/fuse 
#exec "$@"
#fg
