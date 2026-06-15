package ru.yandex.practicum.sync.additional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PrinterPoolBarrier {
	
	private static final Logger log = LoggerFactory.getLogger(PrinterPoolBarrier.class);

	private static final int PRINTER_COUNT = 3;
	private static final CyclicBarrier barrier = new CyclicBarrier(PRINTER_COUNT);

	public static void main(String[] args) throws InterruptedException {
		String[] documents = {"Документ 1", "Документ 2", "Документ 3", "Документ 4", "Документ 5"};

		try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
			for (String document : documents) {
				executor.submit(() -> printDocument(document));
			}
		}
	}
	
	private static void printDocument(String document) {
		try {
			System.out.println(document + " ожидает принтер");
			barrier.await();
			
			System.out.println("Начало печати: " + document);
			int printTime = (int) (Math.random() * 5000 + 1000);
			TimeUnit.MILLISECONDS.sleep(printTime);
			System.out.println("Завершение печати: " + document);
		} catch (Exception e) {
			log.error("Печать документа {} прервана", document, e);
		} finally {
			try {
				barrier.await(10000, TimeUnit.MILLISECONDS);
			} catch (Exception ignored) {
				//
			}
		}
	}
}
