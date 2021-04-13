IP='{{ip*}}'
NETMASK='{{netmask}}'
GATEWAY='{{gateway*}}'
HOSTNAME='{{hostname*}}'
DNS='{{dns}}'

sed -i "s|dhcp|static\n  address ${IP}\n  netmask ${NETMASK:-255.255.255.0}\n  gateway ${GATEWAY}|g" /etc/network/interfaces

hostnamectl --static set-hostname "${HOSTNAME}"

printf "127.0.0.1\tlocalhost\n\n${IP}\t${HOSTNAME}\n" > /etc/hosts

printf "nameserver ${DNS:-8.8.8.8}\n" > /etc/resolv.conf
