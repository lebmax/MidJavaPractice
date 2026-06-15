package ru.yandex.practicum.sync;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CounterTestNGTest {
	
	@DataProvider(name = "counters")
	public Object[][] counters() {
		return new Object[][]{
				{new CounterTest.SimpleCounter(), false},
				{new CounterTest.AtomicCounter(), true},
				{new CounterTest.SynchronizedCounter(), true},
				{new CounterTest.LockCounter(), true},
				{new CounterTest.VolatileCounter(), false}
		};
	}
	
	@Test(dataProvider = "counters")
	public void testCounter(Counter counter, boolean shouldBeAccurate) throws InterruptedException {
		int threads = 10;
		int incrementsPerThread = 100_000;
		try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
			CountDownLatch latch = new CountDownLatch(threads);
			
			for (int i = 0; i < threads; i++) {
				executor.submit(() -> {
					for (int j = 0; j < incrementsPerThread; j++) {
						counter.increment();
					}
					latch.countDown();
				});
			}
			
			latch.await();
		}
		
		long expected = (long) threads * incrementsPerThread;
		long actual = counter.getValue();
		
		if (shouldBeAccurate) {
			Assert.assertEquals(actual, expected, counter.getClass().getSimpleName() + " должен быть точным");
		} else {
			Assert.assertNotEquals(actual, expected, counter.getClass().getSimpleName() + " не должен быть точным");
		}
	}
}
