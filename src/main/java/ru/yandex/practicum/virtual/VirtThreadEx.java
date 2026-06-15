package ru.yandex.practicum.virtual;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class VirtThreadEx {
	
	static final Logger logger = LoggerFactory.getLogger(VirtThreadEx.class);
	static Bathroom bathroom = new Bathroom();
	
	public static void main(String[] args) throws Exception {
		
//		viewCarrierThreadPoolSize();
		System.out.println("--------------------------------");
		
		// 1 case Различные варианты создания виртуальных потоков
//		concurrentMorningRoutine();
//		concurrentMorningRoutineUsingExecutors();
		
		/*
		jdk.virtualThreadScheduler.parallelism — количество доступных платформенных потоков
		для планирования виртуальных потоков. По умолчанию равно количеству доступных процессоров 4
		
		jdk.virtualThreadScheduler.maxPoolSize — максимальное количество доступных
		для планировщика платформенных потоков. По умолчанию равно 256
		
		jdk.virtualThreadScheduler.minRunnable — свойство, которое контролирует,
		когда временно добавлять платформенные потоки в пул.
		По умолчанию установлено на значение, равное максимуму из 1 и parallelism, разделённого на 2
		 */
		
		/**
		 * 2 case Принцип работы планировщика
		 * (проверяем поведение при блокирующих и неблокирующих операциях)
		 * 1) выставляем настройки
		 * -Djdk.virtualThreadScheduler.parallelism=1
		 * -Djdk.virtualThreadScheduler.maxPoolSize=1
		 * -Djdk.virtualThreadScheduler.minRunnable=1
		 * и комментируем sleep в методе workingHard - метод takeABreak не вызывается
		 * <p>
		 * 2) выставляем настройки
		 * -Djdk.virtualThreadScheduler.parallelism=1
		 * -Djdk.virtualThreadScheduler.maxPoolSize=1
		 * -Djdk.virtualThreadScheduler.minRunnable=1
		 * и убираем комментарий со sleep в методе workingHard - метод takeABreak вызывается
		 * <p>
		 * 3) выставляем настройки
		 * -Djdk.virtualThreadScheduler.parallelism=2
		 * -Djdk.virtualThreadScheduler.maxPoolSize=2
		 * -Djdk.virtualThreadScheduler.minRunnable=2
		 * и убираем комментарий sleep в методе workingHard - метод takeABreak вызывается
		 **/
//		workingHardRoutine();
		
		/**
		 * 3 case Pinned virtual thread
		 * В некоторых случаях блокирующая операция не отключает виртуальный поток от потока-носителя (carrier thread),
		 * блокируя базовый поток-носитель. В таких случаях мы говорим, что виртуальный поток прикреплён к потоку-носителю.
		 * Это не ошибка, а поведение, ограничивающее масштабируемость приложения.
		 * Таких ситуаций две - это synchronized и методы native (JNI)
		 * 1) Выставляем настройки
		 * -Djdk.virtualThreadScheduler.parallelism=1
		 * -Djdk.virtualThreadScheduler.maxPoolSize=1
		 * -Djdk.virtualThreadScheduler.minRunnable=1
		 * -Djdk.tracePinnedThreads=full
		 * В итоге код по сути стал последовательным
		 * <p>
		 * 2) Выставляем настройки
		 * -Djdk.virtualThreadScheduler.parallelism=1
		 * -Djdk.virtualThreadScheduler.maxPoolSize=2
		 * -Djdk.virtualThreadScheduler.minRunnable=1
		 * -Djdk.tracePinnedThreads=full
		 * Видим, что один поток запинился, а другой использует свободный carrier thread
		 * 2) Выставляем настройки
		 * -Djdk.virtualThreadScheduler.parallelism=1
		 * -Djdk.virtualThreadScheduler.maxPoolSize=2
		 * -Djdk.virtualThreadScheduler.minRunnable=1
		 * -Djdk.tracePinnedThreads=full
		 * И убираем комментарий с goToTheToiletWithLock, а goToTheToilet комментируем
		 * Видим, что уже потоки не пинятся, т.к. используется Lock вместо synchronized
		 **/
		twoEmployeesInTheOffice();
	}
	
	// ----- 3 case -------
	static void twoEmployeesInTheOffice() throws InterruptedException {
		var riccardo = goToTheToilet();
//		var riccardo = goToTheToiletWithLock();
		var daniel = takeABreak();
		riccardo.join();
		daniel.join();
	}
	
	static Thread goToTheToiletWithLock() {
		return virtualThread("Go to the toilet", () -> bathroom.useTheToiletWithLock());
	}
	
	static Thread goToTheToilet() {
		return virtualThread(
				"Go to the toilet",
				() -> bathroom.useTheToilet());
	}
	
	// ----- 2 case -------
	static void workingHardRoutine() throws InterruptedException {
		var workingHard = workingHard();
		var takeABreak = takeABreak();
		workingHard.join();
		takeABreak.join();
	}
	
	static Thread workingHard() {
		return virtualThread(
				"Working hard",
				() -> {
					log("I'm working hard");
					while (true) {
//						sleep(Duration.ofMillis(100L));
					}
				});
	}
	
	static Thread takeABreak() {
		return virtualThread(
				"Take a break",
				() -> {
					log("I'm going to take a break");
					sleep(Duration.ofSeconds(1L));
					log("I'm done with the break");
				});
	}
	
	// ----- 1 case -------
	static void concurrentMorningRoutine() throws InterruptedException {
		var bathTime = bathTime();
		var boilingWater = boilingWater();
		bathTime.join();
		boilingWater.join();
	}
	
	static void concurrentMorningRoutineUsingExecutors() throws ExecutionException, InterruptedException {
		final ThreadFactory factory = Thread.ofVirtual().name("routine-", 0).factory();
		try (var executor = Executors.newThreadPerTaskExecutor(factory)) {
			var bathTime =
					executor.submit(
							() -> {
								log("I'm going to take a bath");
								sleep(Duration.ofMillis(500L));
								log("I'm done with the bath");
							});
			var boilingWater =
					executor.submit(
							() -> {
								log("I'm going to boil some water");
								sleep(Duration.ofSeconds(1L));
								log("I'm done with the water");
							});
			bathTime.get();
			boilingWater.get();
		}
	}
	
	static Thread bathTime() {
		return virtualThread(
				"Bath time",
				() -> {
					log("I'm going to take a bath");
					sleep(Duration.ofMillis(500L));
					log("I'm done with the bath");
				});
	}
	
	static Thread boilingWater() {
		return virtualThread(
				"Boil some water",
				() -> {
					log("I'm going to boil some water");
					sleep(Duration.ofSeconds(1L));
					log("I'm done with the water");
				});
	}
	
	// ----------------------
	static class Bathroom {
		private final Lock lock = new ReentrantLock();
		
		synchronized void useTheToilet() {
			log("I'm going to use the toilet");
			sleep(Duration.ofSeconds(1L));
			log("I'm done with the toilet");
		}
		
		void useTheToiletWithLock() {
			try {
				if (lock.tryLock(10, TimeUnit.SECONDS)) {
					try {
						log("I'm going to use the toilet");
						sleep(Duration.ofSeconds(1L));
						log("I'm done with the toilet");
					} finally {
						lock.unlock();
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		}
	}
	
	static void viewCarrierThreadPoolSize() {
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			IntStream.range(0, numberOfCores() + 1)
					 .forEach(i -> executor.submit(() -> {
						 log("Hello, I'm a virtual thread number " + i);
						 sleep(Duration.ofSeconds(1L));
					 }));
		}
	}
	
	static int numberOfCores() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	static void log(String message) {
		logger.info("{} | {}", Thread.currentThread(), message);
	}
	
	private static Thread virtualThread(String name, Runnable runnable) {
		return Thread.ofVirtual()
					 .name(name)
					 .start(runnable);
	}
	
	private static void sleep(Duration duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}
}
