import socket
import os
import sys
import struct


def sock_client_image(filePath):
    while True:
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            # s.connect(('172.20.159.255', 10060))  # 服务器和客户端在不同的系统或不同的主机下时使用的ip和端口，首先要查看服务器所在的系统网卡的ip
            s.connect(('192.168.42.28', 5000))  # 服务器和客户端都在一个系统下时使用的ip和端口
        except socket.error as msg:
            print(msg)
            print(sys.exit(1))
        # filepath = input('input the file: ')  # 输入当前目录下的图片名 xxx.jpg
        # filepath = '/storage/emulated/0/Android/data/com.example.myapplication/cache/output_image.jpg'
        filepath = filePath
        fhead = struct.pack(b'128sq', bytes(os.path.basename(filepath), encoding='utf-8'),
                            os.stat(filepath).st_size)  # 将xxx.jpg以128sq的格式打包
        s.send(fhead)

        fp = open(filepath, 'rb')  # 打开要传输的图片
        while True:
            data = fp.read(1024)  # 读入图片数据
            if not data:
                print('{0} send over...'.format(filepath))
                break
            s.send(data)  # 以二进制格式发送图片数据
            #server_reply = s.recv(1024)  # 接受服务端的返回结果
            #print(server_reply)
        s.close()
        break  # 循环发送



if __name__ == '__main__':
    sock_client_image(filePath)
