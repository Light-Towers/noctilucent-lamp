package com.atguigu.channel;

import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;

public class DatagramChannelDemo {

    //发送的实现
    @Test
    public void sendDatagram() throws Exception {
        //打开 DatagramChannel
        DatagramChannel sendChannel = DatagramChannel.open();
        InetSocketAddress sendAddress =
                new InetSocketAddress("127.0.0.1",9999);

        //发送
        while(true) {
            ByteBuffer buffer = ByteBuffer.wrap("发送atguigu".getBytes("UTF-8"));
            sendChannel.send(buffer,sendAddress);
            System.out.println("已经完成发送");
            Thread.sleep(1000);
        }
    }

    //接收的实现
    @Test
    public void receiveDatagram() throws Exception {
        //打开DatagramChannel
        DatagramChannel receiveChannel = DatagramChannel.open();
        InetSocketAddress receiveAddress = new InetSocketAddress(9999);
        //绑定
        receiveChannel.bind(receiveAddress);

        //buffer
        ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);

        //接收
        while(true) {
            receiveBuffer.clear();

            SocketAddress socketAddress = receiveChannel.receive(receiveBuffer);

            receiveBuffer.flip();

            System.out.println(socketAddress.toString());

            System.out.println(Charset.forName("UTF-8").decode(receiveBuffer));
        }
    }

    //连接  read  和 write
    @Test
    public void testConnect() throws Exception {
        //打开DatagramChannel
        DatagramChannel connChannel = DatagramChannel.open();
        //绑定
        connChannel.bind(new InetSocketAddress(9999));

        //连接
        connChannel.connect(new InetSocketAddress("127.0.0.1",9999));

        //write方法
        connChannel.write(ByteBuffer.wrap("发送atguigu".getBytes("UTF-8")));

        //buffer
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);

        while(true) {

            readBuffer.clear();

            connChannel.read(readBuffer);

            readBuffer.flip();
            System.out.println(Charset.forName("UTF-8").decode(readBuffer));

        }
    }

}
