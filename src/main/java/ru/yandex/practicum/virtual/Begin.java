package ru.yandex.practicum.virtual;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;

// https://docs.google.com/presentation/d/1hEQFa50LUAM5e6jbvlXMQh14czbb1hQ2/edit?slide=id.p1#slide=id.p1
public class Begin {
	
	public static void main(String[] args) throws Exception {
		System.out.println("Example of virtual thread >");
		run();
		System.out.println("Example of virtual thread executor >");
		runExecutor();
		System.out.println("Example of executor factory of virtual thread >");
		runFactoryExecutor();
		System.out.println("Example of virtual thread blocking >");
		sleep();
	}
	
	static void run() throws Exception {
		Thread thread = Thread.startVirtualThread(() -> System.out.println("hello"));
		thread.join();
	}
	
	static void runExecutor() throws Exception {
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			Callable<String> task1 = () -> fetch("https://practicum.yandex.ru/catalog/programming/");
			Callable<String> task2 = () -> fetch("https://practicum.yandex.ru");
			
			String first = executor.invokeAny(List.of(task1, task2));
			System.out.println(first.length());
		}
	}
	
	static void runFactoryExecutor() throws Exception {
		final ThreadFactory factory = Thread.ofVirtual().factory();
		try (ExecutorService executor = Executors.newThreadPerTaskExecutor(factory)) {
			Callable<String> task1 = () -> fetch("https://practicum.yandex.ru/catalog/programming/");
			Callable<String> task2 = () -> fetch("https://practicum.yandex.ru");
			
			String first = executor.invokeAny(List.of(task1, task2));
			System.out.println(first.length());
		}
	}
	
	static String fetch(String url) throws IOException {
		try (InputStream in = URI.create(url).toURL().openStream()) {
			byte[] bytes = in.readAllBytes();
			return new String(bytes, StandardCharsets.ISO_8859_1);
		}
	}
	
	static void sleep() throws InterruptedException {
		Thread.startVirtualThread(() -> {
			Object lock = new Object();
			synchronized (lock) {
				try {
					TimeUnit.SECONDS.sleep(2L);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(e);
				}
			}
		}).join();
	}
}
