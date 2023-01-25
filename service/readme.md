## tunvpn service

**仅支持 Linux**

`make tunvpn` 运行


### IP转发功能

    echo 1 > /proc/sys/net/ipv4/ip_forward
    iptables -t nat -A POSTROUTING -s 10.0.0.0/8 -o eth0 -j MASQUERADE

### 设置虚拟设备

    ip tuntap add dev tun0 mode tun
    ip link set dev tun0 up
    ip addr add 10.0.0.1/24 dev tun0


### 待实现功能
- 实现用户密码认证
- 实现根据IP包进行转发
- 实现DHCP功能
- 实现后台网页UI控制