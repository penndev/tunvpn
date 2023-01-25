#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>

#include <sys/types.h>
#include <sys/ioctl.h>

#include <net/if.h>
#include <netinet/in.h>

#include <linux/if_tun.h>

/**
 * @reutrn 返回tun fd 
 */
static int get_interface(){
    char *name = (char *)"tun0";
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

/**
 * @param argc 参数数量
 * @param argv 参数值
 */
int main(int argc, char *argv[]){
    char packet[32767]; 
    int sockfd, tunfd, readlen, scount;
    struct sockaddr_in servaddr, cliaddr;
    socklen_t clilen;

    sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    tunfd = get_interface();
    printf("open device tunfd : %d \n", tunfd);
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(8000);
    bind(sockfd, (struct sockaddr *)&servaddr, sizeof(servaddr));
    fcntl(sockfd, F_SETFL, O_NONBLOCK);
    clilen = sizeof(cliaddr);
    while (1){
        scount ++;
        
        readlen = recvfrom(sockfd, packet, 32767, 0, (struct sockaddr *)&cliaddr, &clilen);
        if(readlen > 0){
            scount = 0;
            // printf("recvfrom sockfd -> %d \n", readlen);
            write(tunfd,packet,readlen);
        }
        readlen = read(tunfd,packet,sizeof(packet));
        if(readlen > 0){
            scount = 0;
            // printf("read tunfd -> %d : addr %d \n", readlen, cliaddr.sin_addr.s_addr);
            sendto(sockfd, packet, readlen, 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
        }
        
        if(scount > 5){
            if(scount > 100){
                // printf("sleeping 3sec \n");
                sleep(3);
            }else{
                // printf("sleeping 100ms \n");
                usleep(100000);
            }
        }
    }

    return 0;
}