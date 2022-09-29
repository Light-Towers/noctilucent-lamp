package com.yjh.concurrent;

/**
 * @author osmondy
 * @create 2021/5/4 15:40
 */
public class StopThread2 {

    public static void main(String[] args) {
        try {
            MyThread thread = new MyThread();
            thread.start();
            Thread.sleep(100);
            thread.interrupt();

            System.out.println("是否停止1？" + thread.isInterrupted());
            System.out.println("是否停止2？" + thread.isInterrupted());
            System.out.println("是否停止3？" + thread.isInterrupted());
            thread.interrupt();
            System.out.println("是否停止4？" + thread.isInterrupted());
            System.out.println("是否停止5？" + thread.isInterrupted());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("end!");
    }

}
