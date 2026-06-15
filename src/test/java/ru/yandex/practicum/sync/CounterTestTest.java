package ru.yandex.practicum.sync;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CounterTestTest {
	
	@Test
	public void testAtomicCounter() throws Exception {
		Counter counter = new CounterTest.AtomicCounter();
		runConcurrentIncrement(counter);
		assertEquals(2000, counter.getValue());
	}
	
	@Test
	public void testVolatileCounter() throws Exception {
		Counter counter = new CounterTest.VolatileCounter();
		runConcurrentIncrement(counter);
		assertNotEquals(2000, counter.getValue());
	}
	
	@Test
	public void testSynchronizedCounter() throws Exception {
		Counter counter = new CounterTest.SynchronizedCounter();
		runConcurrentIncrement(counter);
		assertEquals(2000, counter.getValue());
	}
	
	@Test
	public void testReentrantLockCounter() throws Exception {
		Counter counter = new CounterTest.LockCounter();
		runConcurrentIncrement(counter);
		assertEquals(2000, counter.getValue());
	}
	
	private void runConcurrentIncrement(Counter counter) {
		Thread t1 = new Thread(() -> {
			for (int i = 0; i < 1000; i++) {
				counter.increment();
			}
		});
		
		Thread t2 = new Thread(() -> {
			for (int i = 0; i < 1000; i++) {
				counter.increment();
			}
		});
		
		t1.start();
		t2.start();
		
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
