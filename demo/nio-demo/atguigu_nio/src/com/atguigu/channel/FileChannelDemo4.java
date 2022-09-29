package com.atguigu.channel;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

//通道之间数据传输
public class FileChannelDemo4 {

    //transferTo()
    public static void main(String[] args) throws Exception {
        // 创建两个fileChannel
        RandomAccessFile aFile = new RandomAccessFile("/Users/yangjinhua/Downloads/尚硅谷Java NIO课程（2021最新版）/nio-code/atguigu_nio/src/com/atguigu/resource/01.txt","rw");
        FileChannel fromChannel = aFile.getChannel();

        RandomAccessFile bFile = new RandomAccessFile("/Users/yangjinhua/Downloads/尚硅谷Java NIO课程（2021最新版）/nio-code/atguigu_nio/src/com/atguigu/resource/03.txt","rw");
        FileChannel toChannel = bFile.getChannel();

        //fromChannel 传输到 toChannel
        long position = 0;
        long size = fromChannel.size();
        fromChannel.transferTo(0,size,toChannel);

        aFile.close();
        bFile.close();
        System.out.println("over!");
    }
}
