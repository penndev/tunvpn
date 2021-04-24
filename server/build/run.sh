#!/bin/bash
#add dir
mkdir -p /dev/net/
mknod /dev/net/tun c 10 200

# add tun0 device
ip tuntap add dev tun0 mode tun
# config tun0 device
ifconfig tun0 10.0.0.1 dstaddr 10.0.0.2 up
# use iptables to “10.0.0.0/8” net
iptables -t nat -A POSTROUTING -s 10.0.0.0/8 -o eth0 -j MASQUERADE

make

./TunVpnServer tun0 8000