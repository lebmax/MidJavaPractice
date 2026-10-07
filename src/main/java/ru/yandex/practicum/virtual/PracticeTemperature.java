package ru.yandex.practicum.virtual;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class PracticeTemperature {
	
	static final Logger logger = LoggerFactory.getLogger(PracticeTemperature.class);
	
	public static void main(String[] args) {
		var temperatures = generateTemperatures();
		
		// Используем виртуальные потоки
//		var timeSpentOnPlatformThreadsMs = handleTemperatures(temperatures, Executors.newVirtualThreadPerTaskExecutor());
//
		logger.info("-----");
//		// Используем обычные потоки, чтобы сравнить результат
//		var timeSpentOnPlatformThreadsMs = handleTemperatures(temperatures, Executors.newCachedThreadPool());
		var timeSpentOnPlatformThreadsMs = handleTemperatures(temperatures, Executors.newWorkStealingPool());
		
		logger.info("-----");
//		logger.info("Время затрачено при виртуальных потоках - {} мс", timeSpentOnVirtualThreadsMs);
		logger.info("Время затрачено при обычных потоках - {} мс", timeSpentOnPlatformThreadsMs);
	}
	
	// Каждое значение требуется обработать в отдельном потоке
	// Запуск обработки значений температур с использованием конкретного экзекутора
	private static long handleTemperatures(Collection<Double> temperatures, ExecutorService executor) {
		var startThreads = LocalDateTime.now();
		try (executor) {
			temperatures.forEach(temperature -> executor.submit(() -> handleTemperature(temperature)));
		}
		return startThreads.until(LocalDateTime.now(), ChronoUnit.MILLIS);
	}

	// Генерируем диапазон температур с шагом 0.1 от -273.1 до 5526.0 градусов.
	// Всего значений - 57992.
	private static Collection<Double> generateTemperatures() {
		return IntStream.range(-2731, 55261)
				.asDoubleStream()
				.map(it -> it / 10)
				.boxed()
				.toList();
//		return IntStream.range(-731, 2261)
//				.asDoubleStream()
//				.map(it -> it / 10)
//				.boxed()
//				.toList();
	}
	
	// Обрабатываем конкретное значение температуры - выводим описание
	// ВНИМАНИЕ: операция эмулирует блокировку потока на 5 секунд
	private static void handleTemperature(double temperature) {
		String prefixMessage = ("Температура " + temperature + " : ");
		if (temperature < -273.1) {
			throw new IllegalArgumentException(prefixMessage + "Не бывает такой низкой температуры");
		} else if (temperature == -273.1) {
			logger.info("{} | {}Самая низкая возможная температура", Thread.currentThread(), prefixMessage);
		} else if (temperature < 0.0) {
			logger.info("{} | {}Вода в твердом состоянии", Thread.currentThread(), prefixMessage);
		} else if (temperature < 100.0) {
			logger.info("{} | {}Вода в жидком состоянии", Thread.currentThread(), prefixMessage);
		} else if (temperature < 1669.0) {
			logger.info("{} | {}Горячо, но титан еще не плавится", Thread.currentThread(), prefixMessage);
		} else if (temperature < 5526.0) {
			logger.info("{} | {}Титан плавится, но пока холоднее чем на солнце", Thread.currentThread(), prefixMessage);
		} else {
			logger.info("{} | {}Температура как на солнце или выше", Thread.currentThread(), prefixMessage);
		}
		try {
			TimeUnit.SECONDS.sleep(5L);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
}
