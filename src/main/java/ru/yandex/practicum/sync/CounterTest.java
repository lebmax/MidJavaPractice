package ru.yandex.practicum.sync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class CounterTest {
	
	public static void main(String[] args) {
		var counter = new SimpleCounter();
		var counterVolatile = new VolatileCounter();
		var counterAtomic = new AtomicCounter();
		var counterSynchronized = new SynchronizedCounter();
		var counterLock = new LockCounter();
		
		test(counter, "SimpleCounter");
		test(counterVolatile, "VolatileCounter");
		test(counterAtomic, "Atomic");
		test(counterSynchronized, "SynchronizedCounter");
		test(counterLock, "LockCounter");
	}
	
	private static void test(Counter counter, String name) {
		long start = System.currentTimeMillis();
		int expectedLikes = 1_000_000; //100 млн взято специально, чтобы долго выполнялся код и мы видели результаты
		int runnableThreads = 10;
		Runnable likeTask = () -> {
			for (int i = 0; i < expectedLikes; i++) {
				counter.increment();
			}
		};
		try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
			// Запускаем 10 потоков
			IntStream.range(0, runnableThreads).forEach(i -> executor.submit(likeTask));
		}
		long end = System.currentTimeMillis();
		
		System.out.printf("%s реализация, время выполнения: %d мс%n", name, (end - start));
		System.out.println("Итоговое количество лайков: " + counter.getValue());
		System.out.println("Ожидаемое количество лайков: " + expectedLikes * runnableThreads);
		System.out.println("------------------------");
	}
	
	public static class SimpleCounter implements Counter {
		
		private int count = 0;
		
		public void increment() {
			count++;
		}
		
		public long getValue() {
			return count;
		}
	}
	
	public static class AtomicCounter implements Counter {
		// AtomicLong для подсчёта лайков
		private final AtomicLong likeCount = new AtomicLong();
		
		// Метод для добавления лайка
		public void increment() {
			likeCount.incrementAndGet(); // Атомарное увеличение счётчика на 1
		}
		
		// Метод для получения текущего числа лайков
		public long getValue() {
			return likeCount.get(); // Атомарное получение текущего значения
		}
	}
	
	public static class SynchronizedCounter implements Counter {
		
		private long count = 0;
		
		public synchronized void increment() {
			count++;
		}
		
		public synchronized long getValue() {
			return count;
		}
	}
	
	public static class LockCounter implements Counter {
		
		private long count = 0;
		private final ReentrantLock lock = new ReentrantLock();
		
		public void increment() {
			lock.lock();
			count++;
			lock.unlock();
		}
		
		public long getValue() {
			lock.lock();
			try {
				return count;
			} finally {
				lock.unlock();
			}
		}
	}
	
	public static class VolatileCounter implements Counter {
		
		private volatile long count = 0;
		
		public void increment() {
			count++;
		}
		
		public long getValue() {
			return count;
		}
	}
}