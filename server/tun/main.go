//go:build linux
// +build linux

package main

import (
	"fmt"
	"net"
	"os"
	"strconv"
	"time"
	"unsafe"

	"golang.org/x/sys/unix"
)

func getInterface(name string) (int, error) {
	fd, err := unix.Open("/dev/net/tun", unix.O_RDWR, 0)
	if err != nil {
		return -1, err
	}

	var ifr struct {
		name  [16]byte
		flags uint16
		_     [22]byte
	}

	copy(ifr.name[:], name)
	ifr.flags = unix.IFF_TUN | unix.IFF_NO_PI
	_, _, errno := unix.Syscall(
		unix.SYS_IOCTL,
		uintptr(fd),
		unix.TUNSETIFF,
		uintptr(unsafe.Pointer(&ifr)),
	)
	if errno != 0 {
		unix.Close(fd)
		return -1, errno
	}

	if err = unix.SetNonblock(fd, true); err != nil {
		unix.Close(fd)
		return -1, err
	}

	return fd, nil
}

func readTunDevice(fd *os.File) {
	for {
		buf := make([]byte, 2048)
		bufLen, err := fd.Read(buf)
		if err != nil {
			panic(err)
		}
		if bufLen > 0 {

		} else {
			time.Sleep(100 * time.Millisecond)
		}

	}
}

type Conn struct {
	address *net.UDPAddr
	message chan []byte
	server  *net.UDPConn
}

func (c *Conn) New() {
	for buf := range <-c.message {
		print(buf)
	}
}

func main() {
	// 处理 tun 设备
	nfd, err := getInterface("tun0")
	if err != nil {
		panic(err)
	}
	fd := os.NewFile(uintptr(nfd), "/dev/net/tun")
	go readTunDevice(fd)

	// 处理udp服务器队列
	address, err := net.ResolveUDPAddr("udp", ":8000")
	if err != nil {
		panic(err)
	}
	srv, err := net.ListenUDP("udp", address)
	if err != nil {
		panic(err)
	}
	defer srv.Close()
	buffer := make([]byte, 2048)
	for {
		n, addr, err := srv.ReadFromUDP(buffer)
		if err != nil {
			fmt.Println("读取数据失败：", err)
			continue
		}

		// 客户端队列。

		// 发送到客户端的chan

		fmt.Printf("接收到来自 %s 的数据：%s\n", addr.String(), strconv.Itoa(int(buffer[0])))
		fd.Write(buffer[:n])
	}

}
