//go:build linux
// +build linux

package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net"
	"os"
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

func readTunDevice(fd *os.File, ls *[]*Conn) {
	for {
		buf := make([]byte, 2048)
		bufLen, err := fd.Read(buf)
		if err != nil {
			panic(err)
		}
		if bufLen > 0 {
			currentConn := (*ls)[0]
			currentConn.server.WriteToUDP(buf[:bufLen], currentConn.address)
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

func (c *Conn) New(fd *os.File) {
	for buf := range c.message {
		// 处理认证包。用户名密码。
		if buf[0] == 0 {
			log.Println(string(buf[1:]))
			message := struct {
				Code int    `json:"code"`
				Mtu  int    `json:"mtu"`
				Ip   string `json:"ip"`
				Dns  string `json:"dns"`
			}{
				Code: 1,
				Mtu:  1500,
				Ip:   "10.0.0.2",
				Dns:  "8.8.8.8",
			}
			msgJson, err := json.Marshal(message)
			if err != nil {
				panic(err)
			}
			c.server.WriteToUDP(append([]byte{0}, msgJson...), c.address)
		} else {
			// 发送认证失败，重新认证。
			// 解析为用户名密码。
			// print(buf[0])
			// 认证正确返回 ip dns mtu
			fd.Write(buf)
		}

	}
}

func main() {
	// 处理 tun 设备
	nfd, err := getInterface("tun0")
	if err != nil {
		panic(err)
	}
	fd := os.NewFile(uintptr(nfd), "/dev/net/tun")

	// 处理udp srv
	address, err := net.ResolveUDPAddr("udp", ":8000")
	if err != nil {
		panic(err)
	}
	srv, err := net.ListenUDP("udp", address)
	if err != nil {
		panic(err)
	}
	defer srv.Close()

	// 客户队列
	currentConn := Conn{
		message: make(chan []byte),
		server:  srv,
	}

	ls := make([]*Conn, 1)
	ls[0] = &currentConn

	go currentConn.New(fd)
	go readTunDevice(fd, &ls)

	println("udp service readying...")

	// 处理连接队列。
	buffer := make([]byte, 2048)
	for {
		n, addr, err := srv.ReadFromUDP(buffer)
		if err != nil {
			fmt.Println("读取数据失败：", err)
			continue
		}

		if ls[0].address != addr {
			ls[0].address = addr
		}
		ls[0].message <- buffer[:n]
	}
}
