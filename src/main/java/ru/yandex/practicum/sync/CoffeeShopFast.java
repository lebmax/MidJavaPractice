package ru.yandex.practicum.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CoffeeShopFast {
	
	private static final Logger logger = LoggerFactory.getLogger(CoffeeShopSlow.class);

	public static void main(String[] args) throws InterruptedException {
		final int orders = 30;
		
		CoffeeShopSlow.OrderProducer producer = new CoffeeShopSlow.OrderProducer();
		CoffeeShopSlow.OrderConsumer consumer = new CoffeeShopSlow.OrderConsumer();
		
		BlockingQueue<String> orderQueue = new ArrayBlockingQueue<>(10);
		
		AtomicInteger ind = new AtomicInteger(0);
		
		Callable<Void> producerProcess = () -> {
			int i;
			while ((i = ind.getAndIncrement()) < orders) {
				String order = producer.addOrder(i);
				try {
					orderQueue.put(order);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			return null;
		};
		
		Callable<Void> consumerProcess = () -> {
			try {
				while (true) {
					String order = orderQueue.poll(3, TimeUnit.SECONDS);
					if (order == null) {
						break;
					}
					consumer.processOrder(order);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return null;
		};
		
		long start = System.currentTimeMillis();
		
		Collection<Callable<Void>> callables = new ArrayList<>();
		callables.add(producerProcess);
		callables.add(producerProcess);
		callables.add(producerProcess);
		
		callables.add(consumerProcess);
		callables.add(consumerProcess);
		
		try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
			executor.invokeAll(callables);
		}
		
//		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
//			executor.invokeAll(callables);
//		}

		long end = System.currentTimeMillis();
		
		logger.info("Время выполнения: {} с!!!!",
					TimeUnit.SECONDS.convert(Duration.ofMillis(end).minus(Duration.ofMillis(start)))
		);
	}
}
