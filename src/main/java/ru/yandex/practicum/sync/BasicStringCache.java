package ru.yandex.practicum.sync;

import java.util.Map;
import java.util.function.Function;

public class BasicStringCache {
	
	private final Map<String, String> cache;
	
	public BasicStringCache(Map<String, String> cache) {
		this.cache = cache;
	}
	
	// Добавить запись в кеш
	public void put(String key, String value) {
		cache.put(key, value);
	}
	
	// Получить запись из кеша
	public String get(String key) {
		return cache.get(key);
	}
	
	// Если запись отсутствует в кеше, то получить её из БД, вызвав переданный метод
	public String get(String key, Function<String, String> valueFromDb) {
		return cache.computeIfAbsent(key, valueFromDb);
	}
	
	// Удалить запись из кеша
	public void remove(String key) {
		cache.remove(key);
	}
	
	// Очистить весь кеш
	public void clear() {
		cache.clear();
	}
	
	public int size() {
		return cache.size();
	}
}
