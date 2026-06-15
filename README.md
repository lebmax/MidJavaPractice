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