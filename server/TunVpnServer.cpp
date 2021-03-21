#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <errno.h>
#include <fcntl.h>

#ifdef __linux__

#include <net/if.h>
#include <linux/if_tun.h>

static int get_interface(char *name)
{
    int interface = open("/dev/net/tun", O_RDWR | O_NONBLOCK);

    ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    ifr.ifr_flags = IFF_TUN | IFF_NO_PI;
    strncpy(ifr.ifr_name, name, sizeof(ifr.ifr_name));

    if (ioctl(interface, TUNSETIFF, &ifr)) {
        perror("Cannot get TUN interface");
        exit(1);
    }

    return interface;
}

#else

#error Sorry, you have to implement this part by yourself.

#endif

static int get_tunnel(char *port, char * packet,ssize_t * n )
{
    // We use an IPv6 socket to cover both IPv4 and IPv6.
    int tunnel = socket(AF_INET6, SOCK_DGRAM, 0);
    int flag = 1;
    setsockopt(tunnel, SOL_SOCKET, SO_REUSEADDR, &flag, sizeof(flag));
    flag = 0;
    setsockopt(tunnel, IPPROTO_IPV6, IPV6_V6ONLY, &flag, sizeof(flag));

    // Accept packets received on any local address.
    sockaddr_in6 addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin6_family = AF_INET6;
    addr.sin6_port = htons(atoi(port));

    // Call bind(2) in a loop since Linux does not have SO_REUSEPORT.
    while (bind(tunnel, (sockaddr *)&addr, sizeof(addr))) {
        if (errno != EADDRINUSE) {
            return -1;
        }
        usleep(100000);
    }

    // Receive packets till the secret matches.
    // char packet[1024];
    socklen_t addrlen;
    // do {
        addrlen = sizeof(addr);
        *n = recvfrom(tunnel, packet, sizeof(packet), 0,
                (sockaddr *)&addr, &addrlen);
        if (n <= 0) {
            return -1;
        }
    // } while (packet[0] != 0 || strcmp(secret, &packet[1]));

    // Connect to the client as we only handle one client at a time.
    connect(tunnel, (sockaddr *)&addr, addrlen);
    return tunnel;
}


int main(int argc, char **argv)
{



    // Get TUN interface.
    int interface = get_interface(argv[1]);

    // Wait for a tunnel.
    int tunnel;
    char parameters[1024];
    ssize_t parametersNum = 0;
    while ((tunnel = get_tunnel(argv[2], parameters,&parametersNum)) != -1) {
        printf(" sizeof parameters [%ld] ; parametersNum [%ld] \n", sizeof(parameters),parametersNum);

        fcntl(tunnel, F_SETFL, O_NONBLOCK);

        char packet[32767];
        int timer = 0;

        while (true) {
            bool idle = true;
            int length = read(interface, packet, sizeof(packet));
            if (length > 0) {
                printf("read to sizeof [%d] \n",length);
                send(tunnel, packet, length, MSG_NOSIGNAL);
                idle = false;
                if (timer < 1) {
                    timer = 1;
                }
            }

            if (parametersNum != 0)
            {
                printf("write to sizeof [%ld] \n",parametersNum);
                parameters[parametersNum] = 0;
                write(interface, parameters, parametersNum);
                parametersNum = 0;
            }

            length = recv(tunnel, packet, sizeof(packet), 0);
            if (length == 0) {
                break;
            }
            if (length > 0) {
                printf("write to sizeof [%d] \n",length);
                write(interface, packet, length);
                idle = false;
                if (timer > 0) {
                    timer = 0;
                }
            }

            if (idle) {
                usleep(100000);
                timer += (timer > 0) ? 100 : -100;
                if (timer > 20000) {
                    break;
                }
            }
        }
        printf("%s: The tunnel is broken\n", argv[1]);
        close(tunnel);
    }
    perror("Cannot create tunnels");
    exit(1);
}
