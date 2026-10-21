package ru.yandex.practicum.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CoffeeShopFaster {

	private static final Logger logger = LoggerFactory.getLogger(CoffeeShopFaster.class);

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		final int orders = 10;
		final int baristaCount = 2;

		CoffeeShopSlow.OrderProducer producer = new CoffeeShopSlow.OrderProducer();
		CoffeeShopSlow.OrderConsumer barista = new CoffeeShopSlow.OrderConsumer();
		BlockingQueue<Optional<String>> orderQueue = new ArrayBlockingQueue<>(orders);

		long start = System.nanoTime();
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			try {
				List<Future<Void>> baristas = new ArrayList<>(baristaCount);
				for (int i = 0; i < baristaCount; i++) {
					baristas.add(executor.submit(() -> {
						while (true) {
							Optional<String> next = orderQueue.take();
							if (next.isEmpty()) {
								return null;
							}
							barista.processOrder(next.orElseThrow());
						}
					}));
				}

				Future<Void> arrival = executor.submit(() -> {
					for (int orderNumber = 0; orderNumber < orders; orderNumber++) {
						orderQueue.put(Optional.of(producer.addOrder(orderNumber)));
					}
					return null;
				});

				arrival.get();
				for (int i = 0; i < baristaCount; i++) {
					orderQueue.put(Optional.empty());
				}
				for (Future<Void> task : baristas) {
					task.get();
				}
			} catch (InterruptedException | ExecutionException e) {
				executor.shutdownNow();
				throw e;
			}
		}

		long end = System.nanoTime();
		long elapsedSec = TimeUnit.SECONDS.convert(
				Duration.ofNanos(end).minus(Duration.ofNanos(start))
		);
		logger.info("Время выполнения: {} с!!!!", elapsedSec);
	}
}
