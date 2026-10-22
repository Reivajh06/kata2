# Kata 2 — Input, output and data loading

Software Engineering 2 · School of Computer Engineering · ULPGC

📹 **Explanatory video (≤ 7 min):** [video link](https://VIDEO-LINK)

## Goal

A program that **reads a small dataset from an `InputStream`**, turns each row into a domain entity, computes a **summary** and **writes it to an `OutputStream`**.

The dataset is a CSV of Minecraft mobs ([`minecraft_mobs.csv`](src/main/resources/minecraft_mobs.csv), 78 rows). Each row becomes a `Mob`, and the main summary is the **number of mobs per behavior type** (`PEACEFUL`, `HOSTILE`, `CONDITIONAL`).

The dataset used in this project was downloaded from here: [sources link](https://www.kaggle.com/datasets/madelinee/minecraft-blocks-items-mobs-biomes-etc?resource=download)

Note: The database contains a significant amount of csv. Due to the simplicity of this kata, only one csv is used (inside the zip file is called Mobs.csv)

The kata is about keeping three responsibilities apart:

```
 Source (InputStream)          Transformation                 Destination (OutputStream)
┌─────────────────────┐   ┌──────────────────────────┐   ┌──────────────────────────┐
│ FileMobLoader       │   │ MobParser   → List<Mob>  │   │ MobWriter                │
│ URLMobLoader        │──▶│ MobsAnalyzer → summary   │──▶│  System.out / file /     │
│ (memory / resource) │   │                          │   │  ByteArrayOutputStream   │
└─────────────────────┘   └──────────────────────────┘   └──────────────────────────┘
```

The same logic works with several sources (file, URL, classpath resource, memory) and several destinations (console, file, memory) without changing the transformation.

## Requirements and dependencies

| Tool | Version |
|---|---|
| JDK | **25** (the code uses an instance `void main()` and the unnamed variable `_`) |
| Maven | 3.9+ |
| JUnit Jupiter | 5.11.4 (tests only, downloaded by Maven) |
| exec-maven-plugin | 3.6.4 (runs `main` from Maven, downloaded by Maven) |

There are no other runtime dependencies.

## How to build and run

### 1. Get the code and check the JDK

```bash
git clone https://github.com/Reivajh06/kata2.git
```

```bash
cd kata2
```

Maven must run on JDK 25. Check it with:

```bash
mvn -version
```

If the `Java version` line is not 25, point `JAVA_HOME` to a JDK 25 installation before running the commands below.

### 2. Run the program (`Kata2.main`)

```bash
mvn compile exec:java
```

Expected output:

```
Mobs loaded: 78
Mobs by behavior type: {PEACEFUL=34, HOSTILE=33, CONDITIONAL=11}
```

`Kata2` loads the CSV, computes the frequencies per behavior type with `MobsAnalyzer` and writes them to `System.out` with `MobWriter`. If a row is invalid, one message per error (with its line number) is written after the summary.

> `Kata2` loads the CSV through a **URL** from the GitHub repository, so it needs an internet connection. To use the local file instead, change the `Loader`:
> ```java
> Loader loader = new FileMobLoader(new File("src/main/resources/minecraft_mobs.csv"));
> ```

### 3. Run the tests

All tests:

```bash
mvn clean test
```

Only the end-to-end verification cases from the assignment:

```bash
mvn test -Dtest=Kata2VerificationTest
```

Tests do not need an internet connection: URL tests use `file://` URLs and classpath resources.

### From IntelliJ IDEA

1. Open the project folder; IntelliJ detects it as a Maven project.
2. In *File → Project Structure → Project*, select a JDK 25 SDK.
3. Run `src/main/java/reivajh06/Kata2.java` with the ▶ icon next to `main`.
4. Right-click `src/test/java` → *Run 'All Tests'*.

## Project structure

```
kata2/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/reivajh06/
    │   │   ├── Kata2.java              # main: wires Loader → MobParser → MobsAnalyzer → MobWriter
    │   │   ├── MobParser.java          # text row → Mob
    │   │   ├── ParseResult.java        # valid mobs + parsing errors
    │   │   ├── ParseError.java         # line number, content and error message
    │   │   ├── MobsAnalyzer.java       # grouping, frequencies and filters
    │   │   ├── MobWriter.java          # writes to any OutputStream
    │   │   ├── CachedMobsCsv.java      # in-memory dataset
    │   │   ├── loaders/
    │   │   │   ├── Loader.java         # InputStream-based loading interface
    │   │   │   ├── FileMobLoader.java  # source: file (FileInputStream)
    │   │   │   └── URLMobLoader.java   # source: URL (URL.openStream)
    │   │   ├── exceptions/
    │   │   │   ├── LoaderException.java
    │   │   │   └── WriterException.java
    │   │   └── model/
    │   │       ├── Mob.java            # domain entity (record)
    │   │       ├── BehaviorType.java
    │   │       ├── SpawnBehavior.java
    │   │       └── ReproductiveRequirement.java
    │   └── resources/minecraft_mobs.csv
    └── test/
        ├── java/reivajh06/             # JUnit 5 tests
        └── resources/mobs-test.csv
```

### Main classes

| Class | Responsibility |
|---|---|
| `Mob` | Domain entity: one row of the dataset. |
| `Loader` | Loading interface. Its default method `load(InputStream)` reads the lines of **any** `InputStream` and closes it when done. |
| `FileMobLoader` / `URLMobLoader` | Input adapters. They know the concrete source (path or URL) and drop the header. On failure they throw `LoaderException` with a clear message and the original cause. |
| `MobParser` | Turns each line into a `Mob`. The delimiter is passed in the constructor. Invalid rows do not stop the program: they are returned as `ParseError` with line number and content. |
| `MobsAnalyzer` | Computes the summary from a `List<Mob>`: `groupBy`, `groupFrequencies` and `filterMobs`. It never opens files or URLs. |
| `MobWriter` | Receives an `OutputStream` and writes the result. It does not know whether it is the console, a file or memory. On failure it throws `WriterException`. |

### Design decisions

- **The summary logic knows neither the source nor the destination.** `MobsAnalyzer` only receives a collection of `Mob`.
- **Delimiter, header and concrete source belong to the input adapter.** Loaders drop the header and the delimiter is given to `MobParser`.
- **No `null` on errors.** The code uses `Optional` (`parseLine`), documented empty results (empty file → empty list) and controlled exceptions (`LoaderException`, `WriterException`).
- **Adding a new source or destination** only needs a new class implementing `Loader` (source) or a different `OutputStream` passed to `MobWriter` (destination). Parser and analyzer stay the same.

## Kata repetitions

The kata was repeated several times with the same goal and the same structure (loader, entity, transformation and writer). Each repetition can be identified in the commit history:

| Repetition | Commits | What was done / variation |
|---|---|---|
| 1 | `dfa45ff` … `c14095c` | First version: `Mob` entity and enums, CSV read with `FileInputStream`, groupings in `MobStatistics`. Source: **file**. Destination: **console**. |
| 2 | `506d429` … `008692d` | Parsing moved to `MobParser`, groupings generalised (`groupBy`, `groupFrequencies`, `filterMobs` in `MobsAnalyzer`) and the writer created. Variation: output through an **`OutputStream`** without closing `System.out`. |
| 3 | `377b8d7` … `80e690b` | `InputStream`-based `Loader` interface with two adapters, `FileMobLoader` and `URLMobLoader`. Controlled errors with `LoaderException` and `WriterException`, plus automated tests. Variation: **URL** source; **file** and **memory** destinations. |

## Verification

Verification is automated with **JUnit 5** (`mvn clean test`): **54 tests, 0 failures**.

[`Kata2VerificationTest`](src/test/java/reivajh06/Kata2VerificationTest.java) runs the whole pipeline and has one block per case of the assignment's *expected verification* list:

| Assignment case | How it is checked |
|---|---|
| Valid file | All 78 rows are parsed without errors and the summary is `{PEACEFUL=34, HOSTILE=33, CONDITIONAL=11}`. |
| URL, classpath resource or `ByteArrayInputStream` | Same summary when loading through a `file://` URL, from the classpath and from memory. |
| Writing to console and to a file or `ByteArrayOutputStream` | The summary is written to `System.out` (redirected), to a temporary file and to memory, and the content is compared. |
| Missing path or invalid URL | `LoaderException` with a clear message for a missing path, a URL to a missing resource, an unknown protocol and a syntactically invalid URL. |
| Empty file | An empty file and a header-only file produce an empty summary `{}`. |
| Malformed row | Rows with missing columns, invalid numbers or unknown values are reported as `ParseError` (line + content) and the remaining rows are processed. |
| Summary unchanged when rows are reordered or the source changes | Rows are shuffled with several seeds, and file, URL, resource and memory sources are compared: the summary is always the same. |

There are also unit tests per component:

- [`LoaderTest`](src/test/java/reivajh06/LoaderTest.java): sources, CRLF line endings, stream closing and errors.
- [`MobParserTest`](src/test/java/reivajh06/MobParserTest.java): row conversion and invalid rows.
- [`MobsAnalyzerTest`](src/test/java/reivajh06/MobsAnalyzerTest.java): groupings, frequencies and empty collection.
- [`MobWriterTest`](src/test/java/reivajh06/MobWriterTest.java): destinations, closing and `WriterException`.

```
[INFO] Tests run: 54, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
