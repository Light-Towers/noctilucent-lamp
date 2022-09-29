package com.atguigu.asyncfilechannel;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

public class AsyncFileChannelDemo {


    @Test
    public void writeAsyncFileComplate() throws IOException {
        //1 创建AsynchronousFileChannel
        Path path = Paths.get("d:\\atguigu\\002.txt");
        AsynchronousFileChannel fileChannel =
                AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);

        //2 创建Buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //3 write方法
        buffer.put("atguigujavajava".getBytes());
        buffer.flip();

        fileChannel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                System.out.println("bytes written: " + result);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {

            }
        });

        System.out.println("write over");
    }

    @Test
    public void writeAsyncFileFuture() throws IOException {
        //1 创建AsynchronousFileChannel
        Path path = Paths.get("d:\\atguigu\\002.txt");
        AsynchronousFileChannel fileChannel =
                AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);

        //2 创建Buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //3 write方法
        buffer.put("atguigu ".getBytes());
        buffer.flip();
        Future<Integer> future = fileChannel.write(buffer, 0);

        while(!future.isDone());

        buffer.clear();
        System.out.println("write over");
    }

    @Test
    public void readAsyncFileChannelComplate() throws Exception {
        //1 创建AsynchronousFileChannel
        Path path = Paths.get("d:\\atguigu\\002.txt");
        AsynchronousFileChannel fileChannel =
                AsynchronousFileChannel.open(path, StandardOpenOption.READ);

        //2 创建Buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //3 调用channel的read方法得到Future
        fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                System.out.println("result: "+result);

                attachment.flip();
                byte[] data = new byte[attachment.limit()];
                attachment.get(data);
                System.out.println(new String(data));
                attachment.clear();
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {

            }
        });
    }


    @Test
    public void readAsyncFileChannelFuture() throws Exception {
        //1 创建AsynchronousFileChannel
        Path path = Paths.get("d:\\atguigu\\002.txt");
        AsynchronousFileChannel fileChannel =
                AsynchronousFileChannel.open(path, StandardOpenOption.READ);

        //2 创建Buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //3 调用channel的read方法得到Future
        Future<Integer> future = fileChannel.read(buffer, 0);

        //4 判断是否完成 isDone,返回true
        while(!future.isDone());

        //5 读取数据到buffer里面
        buffer.flip();
//        while(buffer.remaining()>0) {
//            System.out.println(buffer.get());
//        }
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        System.out.println(new String(data));
        buffer.clear();

    }


}
