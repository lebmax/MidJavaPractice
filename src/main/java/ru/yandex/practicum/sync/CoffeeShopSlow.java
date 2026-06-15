package ru.yandex.practicum.sync;

// https://codefile.io/f/h9wI6QPPTS

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Задача состоит в симуляции работы кофейни, где заказы клиентов создаются и обрабатываются последовательно.
 * Каждый заказ проходит этап генерации (клиент оформляет заказ) и приготовления (бариста готовит напиток),
 * с соответствующими временными задержками.
 * Программа демонстрирует, как последовательная обработка влияет на общую производительность.
 * <p>
 * ### Класс OrderProducer
 * <p>
 * Этот класс отвечает за генерацию заказов в кофейне.
 * Переменная orderNumber используется для присвоения уникального номера каждому заказу.
 * Метод getOrder симулирует задержку в 1 секунду, имитируя время на оформление заказа клиентом,
 * после чего возвращает строку с номером заказа и выводит её в консоль.
 * <p>
 * ### Класс OrderConsumer
 * <p>
 * Этот класс моделирует обработку заказа баристой.
 * Метод processOrder симулирует задержку в 5 секунд, представляющую время, необходимое для приготовления напитка.
 * После завершения обработки он выводит сообщение о готовности заказа в консоль.
 * <p>
 * ### Класс CoffeeShopSlow
 * <p>
 * Этот класс запускает основную логику приложения, моделируя работу кофейни с последовательной обработкой заказов.
 * В цикле создаются заказы через объект OrderProducer и обрабатываются с помощью объекта OrderConsumer.
 * После завершения работы программа выводит общее время выполнения всех операций,
 * что демонстрирует недостатки последовательного подхода в данной модели.
 */
public class CoffeeShopSlow {
	
	private static final Logger logger = LoggerFactory.getLogger(CoffeeShopSlow.class);
	
	public static void main(String[] args) {
		int orders = 10;
		
		OrderProducer producer = new OrderProducer();
		OrderConsumer consumer = new OrderConsumer();
		int i = 0;
		long start = System.currentTimeMillis();
		while (i < orders) {
			String order = producer.addOrder(i);
			consumer.processOrder(order);
			++i;
		}
		long end = System.currentTimeMillis();
		
		logger.info("Время выполнения: {} с!!!!",
					TimeUnit.SECONDS.convert(Duration.ofMillis(end).minus(Duration.ofMillis(start)))
		);
	}
	
	public static class OrderProducer {
		public String addOrder(int orderNumber) {
			try {
				TimeUnit.SECONDS.sleep(1L);
				String order = "Order #" + orderNumber;
				logger.info("Клиент добавил: {}", order);
				return order;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		}
	}
	
	public static class OrderConsumer {
		
		public void processOrder(String order) {
			try {
				TimeUnit.SECONDS.sleep(5L); // Симуляция времени приготовления напитка
				logger.info("Бариста завершил приготовление: {}", order);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
