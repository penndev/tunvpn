## 在docker中配置后台

docker build -t test .

docker run --cap-add=NET_ADMIN -p 8000:8000/udp test