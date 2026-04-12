package virtualthreads;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * Java 21 虚拟线程 (Virtual Threads) 锁固定 (Pinning) 实验
 *
 * 场景：模拟 1,000 个任务，每个任务都会进入同步块并阻塞 500 毫秒。
 * 注意：默认物理线程（Carrier Threads）数通常等于 CPU 核心数（你的机器是 8 核）。
 */
public class VirtualThreadPinningTest {

    private static final Object LOCK = new Object();
    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        // 1. 测试 synchronized (会导致 Pinning)
        testWithSynchronized(100); // 即使只有 100 个任务，也会非常慢

        System.out.println("--------------------------------------------------");

        // 2. 测试 ReentrantLock (不会导致 Pinning)
        testWithReentrantLock(1000); // 即使任务多 10 倍，也会飞快
    }

    private static void testWithSynchronized(int count) {
        System.out.printf(">>> 开始测试 [synchronized (会导致 Pinning)]，任务数量: %d\n", count);
        long start = System.currentTimeMillis();
        AtomicInteger completedCount = new AtomicInteger(0);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, count).forEach(i -> {
                executor.submit(() -> {
                    synchronized (LOCK) { // 进入同步块
                        try {
                            Thread.sleep(Duration.ofMillis(500)); // 模拟 I/O 阻塞
                            completedCount.incrementAndGet();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            });
        }

        long end = System.currentTimeMillis();
        System.out.printf("<<< [synchronized] 完成 %d 个任务，耗时: %d 毫秒\n", completedCount.get(), (end - start));
        System.out.println("提示：因为 Pinning 导致底层 8 个物理线程全被占用，任务只能 8 个一排队处理。");
    }

    private static void testWithReentrantLock(int count) {
        System.out.printf(">>> 开始测试 [ReentrantLock (正常挂起)]，任务数量: %d\n", count);
        long start = System.currentTimeMillis();
        AtomicInteger completedCount = new AtomicInteger(0);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, count).forEach(i -> {
                executor.submit(() -> {
                    REENTRANT_LOCK.lock(); // 使用 JUC 的锁
                    try {
                        try {
                            Thread.sleep(Duration.ofMillis(500)); // 模拟 I/O 阻塞
                            completedCount.incrementAndGet();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } finally {
                        REENTRANT_LOCK.unlock();
                    }
                });
            });
        }

        long end = System.currentTimeMillis();
        System.out.printf("<<< [ReentrantLock] 完成 %d 个任务，耗时: %d 毫秒\n", completedCount.get(), (end - start));
        System.out.println("提示：虽然有锁，但虚拟线程在 sleep 时会释放物理线程，整体吞吐量依然由锁的逻辑控制，而非物理线程数限制。");
    }
}
