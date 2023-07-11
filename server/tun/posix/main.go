package main

import (
	"fmt"
	"log"
	"net"
	"os"
	"strconv"
	"time"
	"unsafe"

	"golang.org/x/sys/unix"
)

const (
	tunDevice = "/dev/net/tun"
	ifReqSize = unix.IFNAMSIZ + 64
)

var (
	addrClient *net.UDPAddr
	connServer *net.UDPConn
)

func checkErr(err error) {
	if err == nil {
		return
	}
	panic(err)
}

// https://www.kernel.org/doc/Documentation/networking/tuntap.txt
func main() {
	nfd, err := unix.Open(tunDevice, os.O_RDWR, 0)
	checkErr(err)

	var ifr [ifReqSize]byte
	var flags uint16 = unix.IFF_TUN | unix.IFF_NO_PI
	name := []byte("tun0")
	copy(ifr[:], name)
	*(*uint16)(unsafe.Pointer(&ifr[unix.IFNAMSIZ])) = flags
	fmt.Println(string(ifr[:unix.IFNAMSIZ]))

	_, _, errno := unix.Syscall(
		unix.SYS_IOCTL,
		uintptr(nfd),
		uintptr(unix.TUNSETIFF),
		uintptr(unsafe.Pointer(&ifr[0])),
	)
	if errno != 0 {
		checkErr(fmt.Errorf("ioctl errno: %d", errno))
	}
	err = unix.SetNonblock(nfd, true)
	checkErr(err)
	// os.NewFile(uintptr(nfd), tunDevice)
	fd := os.NewFile(uintptr(nfd), tunDevice)
	go func() {
		for {
			buf := make([]byte, 1500)
			bufLen, err := fd.Read(buf)
			if err != nil {
				fmt.Printf("read error: %v\n", err)
				continue
			}
			if bufLen > 0 {
				log.Println("i here ->", bufLen, addrClient, connServer)
				if addrClient != nil && connServer != nil {
					connServer.WriteToUDP(buf[:bufLen], addrClient)
				}
			} else {
				time.Sleep(100 * time.Millisecond)
			}

		}
	}()

	address, err := net.ResolveUDPAddr("udp", ":8000")
	if err != nil {
		fmt.Println("解析地址失败：", err)
		return
	}
	connServer, err = net.ListenUDP("udp", address)
	if err != nil {
		fmt.Println("创建UDP连接失败", err)
		return
	}
	defer connServer.Close()
	fmt.Println("UDP服务器已启动")
	// 定义接收数据的缓冲区
	buffer := make([]byte, 2048)
	for {
		// 读取数据
		n, addr, err := connServer.ReadFromUDP(buffer)
		if err != nil {
			fmt.Println("读取数据失败：", err)
			continue
		}
		if addr != addrClient {
			addrClient = addr
		}

		// connServer.WriteToUDP(buffer[:n], addrClient)

		fmt.Printf("接收到来自 %s 的数据：%s\n", addr.String(), strconv.Itoa(int(buffer[0])))
		fd.Write(buffer[:n])
	}

}
