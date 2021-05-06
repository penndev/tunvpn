# TunVpn 

制作基于tun的隧道网络，已尝试可应用于简单场景的科学上网。

###  下载Apk

[Click here](../../releases)  to download the release apk file


### 配置代理服务器

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

    

