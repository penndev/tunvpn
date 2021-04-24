FROM ubuntu

RUN apt-get update \
	&& apt-get -y install net-tools iptables iproute2 make g++ 
	
WORKDIR  /home
EXPOSE  8000/udp
COPY  ./build  /home
WORKDIR  /home
CMD ./run.sh