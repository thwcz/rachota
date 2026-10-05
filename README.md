# Rachota (modified fork)

Rachota is a desktop time-tracking application written in Java/Swing. It lets you plan your day, track time spent on tasks and projects, and generate reports and invoices.

This is a modified fork of the original [Rachota](http://rachota.sourceforge.net/) by Jiri Kovalsky.

## Features

- Daily task planning and time tracking, with idle-time detection
- Regular (recurring) tasks, task filters and keyword/project grouping
- History and analytics views with charts
- Report and invoice generation
- Localisations: cs, de, en, es, fr, hu, it, ja, nl, pt_BR, ro, ru

## Changes compared to the original

- Updated to build and run on a current Java version
- Modern look and feel via [FlatLaf](https://www.formdev.com/flatlaf/)
- Workflow and user interface improvements
- General maintenance

## Building

Requirements: JDK and Maven.

```
mvn clean package
```

The build output goes to `target/` (runnable jar, `lib/` dependencies and, via launch4j, `Rachota.exe` on Windows).

## Running

```
java -jar target/rachota-2.7.jar
```

Settings (`settings.cfg`) and diary data (`diary_*.xml`, `db/`) are stored next to the application and are intentionally not part of this repository.

## License

Rachota is licensed under the [Common Development and Distribution License (CDDL) 1.0](LICENSE). Every source file keeps its original CDDL header, and modified files remain under the CDDL. See [NOTICE](NOTICE) for attribution and third-party information.

Not affiliated with or endorsed by the original authors.
