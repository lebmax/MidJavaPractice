# JFR: пининг и профилирование виртуальных потоков

Проект собирается для Java 21. Нужен JDK 21 с утилитами `java`, `jfr` и `jcmd`.
Пример `VirtThreadEx` на Java 21 вызывает `Thread.sleep` внутри `synchronized`:
виртуальный поток остаётся прикреплённым к потоку-носителю примерно на 1 с.
На JDK 24 и новее именно этот пример может не вызвать пининг: поведение
`synchronized` изменено в JEP 491.

## Запись на примере проекта

Из корня проекта соберите классы и скопируйте зависимости:

```bash
mvn -q -DskipTests compile dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
```

Запустите пример с конфигурацией JFR `profile`. На Windows в PowerShell:

```powershell
java '-XX:StartFlightRecording=filename=target/virtual-threads.jfr,settings=profile,dumponexit=true' '-Djdk.virtualThreadScheduler.parallelism=1' '-Djdk.virtualThreadScheduler.maxPoolSize=1' '-Djdk.virtualThreadScheduler.minRunnable=1' -cp 'target/classes;target/dependency/*' ru.yandex.practicum.virtual.VirtThreadEx
```

На Linux и macOS:

```bash
java '-XX:StartFlightRecording=filename=target/virtual-threads.jfr,settings=profile,dumponexit=true' '-Djdk.virtualThreadScheduler.parallelism=1' '-Djdk.virtualThreadScheduler.maxPoolSize=1' '-Djdk.virtualThreadScheduler.minRunnable=1' -cp 'target/classes:target/dependency/*' ru.yandex.practicum.virtual.VirtThreadEx
```

Настройки планировщика делают эффект блокировки потока-носителя заметным в
выводе примера. `dumponexit=true` сохраняет запись при завершении JVM.

Посмотрите состав записи и события пининга:

```bash
jfr summary target/virtual-threads.jfr
jfr view pinned-threads target/virtual-threads.jfr
jfr print --events jdk.VirtualThreadPinned --stack-depth 32 target/virtual-threads.jfr
```

На Java 21 ожидается событие `jdk.VirtualThreadPinned` длительностью около 1 с.
В его стеке видны `VirtThreadEx.sleep` и `Bathroom.useTheToilet`. Поле
`eventThread` указывает виртуальный поток. `jfr view pinned-threads` группирует
события по методу и показывает их число и суммарную длительность.

Конфигурация `profile` в JDK 21 включает `jdk.VirtualThreadPinned` со стеком
и порогом 20 мс. Отсутствие события не доказывает отсутствие пининга:
запись могла не захватить нужную нагрузку, а длительность могла не достичь
порога. Для коротких случаев создайте конфигурацию с меньшим порогом:

```bash
jfr configure --input profile.jfc --output target/virtual-threads-detailed.jfc 'jdk.VirtualThreadPinned#threshold=1ms' 'jdk.VirtualThreadStart#enabled=true' 'jdk.VirtualThreadEnd#enabled=true'
```

Затем повторите запуск, заменив `settings=profile` на
`settings=target/virtual-threads-detailed.jfc`. События начала и завершения
виртуальных потоков по умолчанию в `profile` выключены. Включайте их на
коротком интервале: при большом числе потоков запись быстро растёт.

Чтобы сравнить поведение с `ReentrantLock`, в `VirtThreadEx.twoEmployeesInTheOffice`
переключите вызов с `goToTheToilet()` на уже имеющийся
`goToTheToiletWithLock()`, сделайте новую запись и сравните количество и
длительность `jdk.VirtualThreadPinned`.

## Запись работающего приложения

Найдите идентификатор нужной JVM и замените `12345` в командах на него:

```bash
jcmd -l
jcmd 12345 JFR.start name=virtual-threads settings=profile
jcmd 12345 JFR.dump name=virtual-threads filename=virtual-threads-live.jfr
jcmd 12345 JFR.stop name=virtual-threads
```

Между `JFR.start` и `JFR.dump` выполните характерную нагрузку. Путь в
`filename` относится к рабочему каталогу целевой JVM; при необходимости
укажите абсолютный путь. Для длительного приложения достаточно обычно
интервала 30–60 с с нагрузкой, которую нужно исследовать.

## Что смотреть помимо пининга

```bash
jfr view hot-methods virtual-threads-live.jfr
jfr view latencies-by-type virtual-threads-live.jfr
jfr view contention-by-site virtual-threads-live.jfr
jfr view allocation-by-site virtual-threads-live.jfr
jfr print --events jdk.VirtualThreadSubmitFailed,jdk.ThreadPark,jdk.ThreadSleep --stack-depth 32 virtual-threads-live.jfr
```

| Данные | Что показывают |
| --- | --- |
| `jdk.ExecutionSample`, `hot-methods` | Где исполнялся Java-код во время выборок; для короткого примера выборок может не быть. |
| `jdk.ThreadPark`, `jdk.ThreadSleep`, `jdk.SocketRead`, `jdk.FileRead` | Ожидания и ввод-вывод дольше настроенного порога. Сами по себе эти события не доказывают пининг. |
| `jdk.JavaMonitorEnter`, `contention-by-site` | Ожидание входа в монитор и место конкуренции за него. |
| `jdk.ObjectAllocationSample`, `allocation-by-site` | Места интенсивных аллокаций; это выборка, а не полный учёт объектов. |
| `jdk.VirtualThreadSubmitFailed` | Сбой отправки виртуального потока планировщику. |
| `jdk.VirtualThreadStart` и `jdk.VirtualThreadEnd` | Жизненный цикл потоков, если события явно включены в конфигурации. |

Для поиска проблемы сначала проверьте `jfr summary`, затем агрегаты `jfr view`,
после этого откройте отдельные события и их стеки через `jfr print`. Запись
можно также открыть в JDK Mission Control, чтобы изучить временную шкалу и
стеки. Профилируйте характерную нагрузку; результаты измерения пропускной
способности с включённым JFR сравнивайте отдельно от запусков без записи.

## Документация JDK

- [Виртуальные потоки и события JFR в Java 21](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)
- [Команда `jfr`](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jfr.html)
- [Команда `jcmd` и операции JFR](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jcmd.html)
- [Изменение `synchronized` в JDK 24](https://docs.oracle.com/en/java/javase/24/migrate/significant-changes-jdk-24.html)
