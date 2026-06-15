package ru.yandex.practicum.sync;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class BasicStringCacheConcurrencyTest {

    @Test
    void testConcurrentComputeIfAbsent() throws InterruptedException {
        List<Map<String, String>> realizations = Arrays.asList(new ConcurrentHashMap<>(), new HashMap<>());
        for (Map<String, String> map : realizations) {
            BasicStringCache cache = new BasicStringCache(map);
            int threadCount = 100;
            AtomicInteger functionCallCount = new AtomicInteger(0);
            List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        cache.get("key", k -> {
                            functionCallCount.incrementAndGet();
                            try {
                                Thread.sleep(50); // увеличиваем окно гонки
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return "value";
                        });
                    } catch (Throwable t) {
                        exceptions.add(t);
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            boolean finished = endLatch.await(30, TimeUnit.SECONDS);
            assertTrue(finished, "Не все потоки завершились за отведённое время");

            if (map instanceof ConcurrentHashMap) {
                System.out.println("Concurrent hash map test");
                assertTrue(exceptions.isEmpty(),
                        "ConcurrentHashMap не должно быть исключений: " + exceptions);
                assertEquals(1, functionCallCount.get(),
                        "ConcurrentHashMap: функция должна выполниться только один раз");
            } else {
                System.out.println("Hash map test");
                boolean hasExceptions = !exceptions.isEmpty();
                boolean multipleCalls = functionCallCount.get() > 1;
                assertTrue(hasExceptions || multipleCalls,
                        "HashMap: ожидались исключения или множественные вызовы функции, " +
                                "но получено: исключения=" + exceptions +
                                ", вызовов=" + functionCallCount.get());
            }
        }
    }
}
