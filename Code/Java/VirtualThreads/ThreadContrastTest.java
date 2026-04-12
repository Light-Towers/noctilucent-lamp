package virtualthreads;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Java 21 虚拟线程 (Virtual Threads) 对比测试
 *
 * 场景：模拟 1,000,000 个任务，每个任务模拟 100 毫秒的 I/O 阻塞。
 */
public class ThreadContrastTest {

    public static void main(String[] args) throws InterruptedException {
        // 1. 测试平台线程 (Platform Threads)
        // 注意：如果你尝试在这里开 1,000,000 个线程，由于操作系统限制，通常会抛出 OOM 错误。
        // 我们这里只测试 10,000 个，看看内存和耗时。
        testPlatformThreads(10_000);

        System.out.println("--------------------------------------------------");

        // 2. 测试虚拟线程 (Virtual Threads)
        // 挑战 1,000,000 个线程！
        testVirtualThreads(1_000_000);
    }

    private static void testPlatformThreads(int count) {
        System.out.printf(">>> 开始测试 [平台线程 (Platform Threads)]，任务数量: %d\n", count);
        long start = System.currentTimeMillis();
        AtomicInteger completedCount = new AtomicInteger(0);

        try (var executor = Executors.newFixedThreadPool(200)) { // 传统的固定线程池
            IntStream.range(0, count).forEach(i -> {
                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(100)); // 模拟 I/O 阻塞
                        completedCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            });
        }

        long end = System.currentTimeMillis();
        System.out.printf("<<< [平台线程] 完成 %d 个任务，耗时: %d 毫秒\n", completedCount.get(), (end - start));
    }

    private static void testVirtualThreads(int count) {
        System.out.printf(">>> 开始测试 [虚拟线程 (Virtual Threads)]，任务数量: %d\n", count);
        long start = System.currentTimeMillis();
        AtomicInteger completedCount = new AtomicInteger(0);

        // 使用 Java 21 推荐的新语法：ExecutorService 实现了 AutoCloseable
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, count).forEach(i -> {
                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(100)); // 模拟 I/O 阻塞
                        completedCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            });
        } // 自动等待所有任务结束

        long end = System.currentTimeMillis();
        System.out.printf("<<< [虚拟线程] 完成 %d 个任务，耗时: %d 毫秒\n", completedCount.get(), (end - start));
    }
}
