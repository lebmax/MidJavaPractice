package ru.yandex.practicum.virtual;

import java.util.LinkedList;
import java.util.List;
import java.util.SequencedCollection;
import java.util.stream.IntStream;

// https://codefile.io/f/Uin41yOge8
public class LogJournal {
	
	public static void main(String[] args) {
		var journal = new LogJournalEnt<String>();
		// Добавляем по очереди числа 1..99 а журнал
		
		IntStream.range(1, 100).boxed().map(Object::toString).forEach(journal::logEntry);
		// Последние десять записей должны быть - 90..99
		var expectedLastTenEntries = IntStream.range(90, 100).boxed().map(Object::toString).toList().reversed();
		
		int limit = 10;
		// Получаем фактические 10 последних записей
		var actualLastTenEntries = journal.getLastEntries(limit);
		
		// Проверяем соответствие последних 10 записей ожидаемым
		System.out.println("Actual - " + actualLastTenEntries + ", expected - " + expectedLastTenEntries);
		if (!actualLastTenEntries.equals(expectedLastTenEntries)) {
			throw new RuntimeException(
						"""
							Ой, записи не совпали. Ожидаемые - [99..90]. Фактические -
						""" + actualLastTenEntries);
		} else System.out.println("Ура-ура-ура");
		
	}
	
	/**
	 * Журнал для упорядоченной записи объектов в журнал
	 * @param <T> тип записываемого объекта
	 *           (при тестировании используйте {@link String}, представим что это журнал логов)
	 */
	static class LogJournalEnt<T> {
		
		// Для хранения и получения информации подберите подходящий тип коллекции
		private final SequencedCollection<T> entries = new LinkedList<>();
		
		/**
		 * Добавить в журнал новую запись (запись добавляется в конец)
		 *
		 * @param entry запись в журнал
		 */
		void logEntry(T entry) {
			entries.addLast(entry);
		}
		
		/**
		 * Получить последние актуальные записи в журнале
		 *
		 * @param limit число записей для получения
		 * @return последние записи в журнале, ограниченные limit
		 */
		List<T> getLastEntries(int limit) {
			return entries.reversed().stream().limit(limit).toList();
		}
	}
}
