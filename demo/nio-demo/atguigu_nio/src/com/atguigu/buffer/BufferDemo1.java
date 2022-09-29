package com.atguigu.buffer;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class BufferDemo1 {

    @Test
    public void buffer01() throws Exception {
        //FileChannel
        RandomAccessFile aFile =
                new RandomAccessFile("d:\\atguigu\\01.txt","rw");
        FileChannel channel = aFile.getChannel();

        //创建buffer，大小
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //读
        int bytesRead = channel.read(buffer);

        while(bytesRead != -1) {
            //read模式
            buffer.flip();

            while(buffer.hasRemaining()) {
                System.out.println((char)buffer.get());
            }
            buffer.clear();
            bytesRead = channel.read(buffer);
        }

        aFile.close();
    }


    @Test
    public void buffer02() throws Exception {

//        //创建buffer
//        IntBuffer buffer = IntBuffer.allocate(8);
//
//        //buffer放
//        for (int i = 0; i < buffer.capacity(); i++) {
//            int j = 2*(i+1);
//            buffer.put(j);
//        }
//
//        //重置缓冲区
//        buffer.flip();
//
//        //获取
//        while(buffer.hasRemaining()) {
//            int value = buffer.get();
//            System.out.println(value+" ");
//        }

        // 1、获取Selector选择器
        Selector selector = Selector.open();

        // 2、获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 3.设置为非阻塞
        serverSocketChannel.configureBlocking(false);

        // 4、绑定连接
        serverSocketChannel.bind(new InetSocketAddress(9999));

        // 5、将通道注册到选择器上,并制定监听事件为：“接收”事件
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);

    }

}
