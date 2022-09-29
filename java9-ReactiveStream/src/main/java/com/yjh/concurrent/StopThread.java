package com.yjh.concurrent;

/**
 * @author osmondy
 * @create 2021/5/4 15:40
 */
public class StopThread {

    public static void main(String[] args) {
        //interrupt() 测试当前线程是否已经中断。线程的中断状态由该方法清除。
        Thread.currentThread().interrupt();
        System.out.println("是否停止1？" + Thread.interrupted());
        System.out.println("是否停止2？" + Thread.interrupted());
        System.out.println("是否停止3？" + Thread.interrupted());
        Thread.currentThread().interrupt();
        System.out.println("是否停止4？" + Thread.interrupted());
        System.out.println("是否停止5？" + Thread.interrupted());
        System.out.println("是否停止6？" + Thread.interrupted());
        System.out.println("end!");
    }

}
