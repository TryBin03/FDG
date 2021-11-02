package trybin.fdg;

import java.util.List;
import java.util.concurrent.*;

public class MyTest1 {
    public static void main(String[] args) {
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
        long start = System.currentTimeMillis();

        int listSize = 20;
        int count = 100;
        List<Integer> list = new CopyOnWriteArrayList<>();

        for (int i = 0; i < count; i++) {
            int finalI = i;
            executorService.execute(() -> {
                list.add(finalI);
                System.out.println("生成sql "+ finalI);
            });
        }

        CompletableFuture.runAsync(() -> {
            for (Integer next : list) {
                System.out.println("消费sql " + next);
                list.remove(next);
            }
        },executorService1);

        executorService.shutdown();
        executorService1.shutdown();
        System.out.println("耗时："+ (System.currentTimeMillis() - start) / 1000D+" s");
    }
}
