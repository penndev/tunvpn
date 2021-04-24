# TunVpn 

###  Download apk

[Click here](../../releases)  to download the release apk file


### Configure the tunnel On the docker

1.  进入[部署目录](./server) 

    ```shell
    cd ./server
    ```
    
2.  编译docker镜像  

    ```shell
    docker build -t test .
    ```

3.  运行镜像 

    ```shell
     docker run --cap-add=NET_ADMIN -p 8000:8000/udp test
    ```

    

