package ru.yandex.practicum.sync.additional;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// https://codefile.io/f/XfCrnMLH7o

/**
 * Система печати документов
 * Условие:
 * В офисе есть 3 принтера, которые могут одновременно печатать документы.
 * Каждый документ должен быть напечатан полностью одним принтером.
 * Необходимо создать программу, которая будет управлять доступом к принтерам,
 * чтобы не возникало ситуации, когда больше 3 документов печатаются одновременно.
 */
public class PrinterPool {
	private static final int MAX_PRINTERS = 3;
	private final Semaphore semaphore;
	
	public PrinterPool() {
		semaphore = new Semaphore(MAX_PRINTERS);
	}
	
	public void printDocument(String documentName) {
		try {
			System.out.println(documentName + " ожидает принтер");
			semaphore.acquire();
			System.out.println(documentName + " начал печать");
			int printTime = (int) (Math.random() * 5000 + 1000);
			TimeUnit.MILLISECONDS.sleep(printTime);
			System.out.println(documentName + " завершил печать");
		} catch (InterruptedException e) {
			System.out.println("Печать документа " + documentName + " прервана");
		} finally {
			semaphore.release();
		}
	}
	
	public static void main(String[] args) {
		PrinterPool pool = new PrinterPool();
		
		for (int i = 1; i <= 5; i++) {
			final int docNumber = i;
			new Thread(() -> {
				pool.printDocument("Документ " + docNumber);
			}).start();
		}
	}
}
