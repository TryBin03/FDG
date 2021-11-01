package trybin.fdg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MyTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = new ThreadPoolExecutor(
                5,
                15,
                5L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(70),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        ExecutorService executorService1 = new ThreadPoolExecutor(
                5,
                15,
                5L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(70),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        final Semaphore semaphore = new Semaphore(10,true);
        List<Integer> sqls = new ArrayList<>(10);
        long start = System.currentTimeMillis();
        int count = 10000;
        CompletableFuture<Void> voidCompletableFuture1 = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < count; i++) {
                int finalI = i;
                executorService.execute(() -> {
                    try {
                        semaphore.acquire();
                        System.out.println("生成Sql: " + finalI);
                        sqls.add(finalI);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < count; i++) {
                int finalI = i;
                executorService1.execute(() -> {
                    try {
                        System.out.println("消费Sql: " + finalI);
                        sqls.remove(new Integer(finalI));
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        semaphore.release();
                    }
                });
            }
        });

        CompletableFuture.allOf(voidCompletableFuture,voidCompletableFuture1).get();
        System.out.println("耗时："+ (System.currentTimeMillis() - start) / 1000D);

        executorService.shutdown();
        executorService1.shutdown();

    }
}
