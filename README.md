Для запуска JCStress тестов требуется выполнить

```bash
mvn clean verify
```

После этого в папке target будет создан файл jcstress.jar

Далее выполняем команду

```bash
java -jar ./target/jcstress.jar -v
```

Или если хотим выполнить какой-то конкретный тест
```bash
java -jar ./target/jcstress.jar -t PrinterPoolBarrierJCStressTest
```

## Профилирование виртуальных потоков через JFR

Пошаговая инструкция для примера `ru.yandex.practicum.virtual.VirtThreadEx`:
[docs/jfr-virtual-threads.md](docs/jfr-virtual-threads.md). В ней показаны запись
JFR, поиск пининга, подключение к работающей JVM и анализ других событий
виртуальных потоков.
