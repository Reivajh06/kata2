# Kata 2 — Input, output and data loading

Software Engineering 2 · School of Computer Engineering · ULPGC

📹 **Explanatory video (≤ 7 min):** [video link](https://VIDEO-LINK)

## Goal

A program that **reads a small dataset from an `InputStream`**, turns each row into a domain entity, computes a **summary** and **writes it to an `OutputStream`**.

The dataset is a CSV of Minecraft mobs ([`minecraft_mobs.csv`](src/main/resources/minecraft_mobs.csv), 78 rows). Each row becomes a `Mob`, and the main summary is the **number of mobs per behavior type** (`PEACEFUL`, `HOSTILE`, `CONDITIONAL`).

The dataset comes from this [Kaggle collection](https://www.kaggle.com/datasets/madelinee/minecraft-blocks-items-mobs-biomes-etc?resource=download). The collection contains several CSV files; this kata uses one of them, called `Mobs.csv` inside the zip file.

## Requirements and dependencies

| Tool | Version | Use |
|---|---|---|
| JDK | **25** | The code uses a no-argument `static void main()` and the unnamed variable `_` |
| Maven | 3.9+ | Build and execution |
| JUnit Jupiter | 5.11.4 | Tests, downloaded by Maven |
| JUnit | 4.13.2 | Declared with test scope, downloaded by Maven |
| exec-maven-plugin | 3.6.4 | Runs `main` from Maven, downloaded by Maven |

The program has no runtime dependencies beyond the JDK.

## How to build and run

Maven must run on JDK 25, which `mvn -version` shows on its `Java version` line.

### 1. Get the code

```bash
git clone https://github.com/Reivajh06/kata2.git
```

```bash
cd kata2
```

### 2. Compile

```bash
mvn clean compile
```

### 3. Run the program

```bash
mvn compile exec:java
```

`Main` loads the CSV through a URL from the GitHub repository, so this step uses an internet connection.

Output:

```
Mobs loaded: 78
Mobs by behavior type: {PEACEFUL=34, HOSTILE=33, CONDITIONAL=11}
```

If a row is invalid, one message per error is written after the summary.

### 4. Run the tests

All tests:

```bash
mvn clean test
```

Only the end-to-end verification cases from the assignment:

```bash
mvn test -Dtest=Kata2VerificationTest
```

The tests work offline: URL tests use `file://` URLs and classpath resources.

### From IntelliJ IDEA

1. Open the project folder; IntelliJ detects it as a Maven project.
2. In *File → Project Structure → Project*, select a JDK 25 SDK.
3. Run `src/main/java/reivajh06/Kata2.java` with the ▶ icon next to `main`.
4. Right-click `src/test/java` → *Run 'All Tests'*.

## Project structure

### The idea: one process, interchangeable streams

The program keeps three responsibilities apart:

```
 Source (InputStream)          Transformation                 Destination (OutputStream)
┌─────────────────────┐   ┌──────────────────────────┐   ┌──────────────────────────┐
│ FileMobLoader       │   │ MobParser   → List<Mob>  │   │ MobWriter                │
│ URLMobLoader        │──▶│ MobsAnalyzer → summary   │──▶│  System.out / file /     │
│ (memory / resource) │   │                          │   │  ByteArrayOutputStream   │
└─────────────────────┘   └──────────────────────────┘   └──────────────────────────┘
```

Reading and writing are each **defined once**, in terms of the `InputStream` and `OutputStream` abstractions. What changes between one source or destination and another is only **which stream is supplied**, so the process itself is not duplicated.

**Loading.** The reading process lives in a default method of the `Loader` interface. It reads the lines of any `InputStream` and closes the stream when done:

```java
default List<String> load(InputStream sourceStream) throws IOException
```

Each adapter only says where its stream comes from:

```java
load(new FileInputStream(file));   // FileMobLoader
load(url.openStream());            // URLMobLoader
```

**Writing.** `MobWriter` holds the writing process and receives its destination in the constructor. The same class serves every destination:

```java
new MobWriter(System.out);
new MobWriter(new FileOutputStream(file));
new MobWriter(new ByteArrayOutputStream());
```

**Connecting the parts.** `Main.main` only wires the pieces together, so switching source or destination means changing the object that is created there. Parser and analyzer stay the same:

```java
Loader loader = new FileMobLoader(new File("src/main/resources/minecraft_mobs.csv"));
```

A new source is a new class implementing `Loader`. A new destination is a different `OutputStream` passed to `MobWriter`.

### The `reivajh06` package

All the code hangs from the root package `reivajh06`, which is the author's GitHub account name ([Reivajh06](https://github.com/Reivajh06)), written in lowercase as Java package names are. The Maven `groupId`, `org.reivajh06`, is based on the same name.

Using the account name as the root package gives the code a namespace that identifies its owner and keeps its classes apart from those of other projects. Inside it, sub-packages group classes by role: `loaders`, `model` and `exceptions`.

### Files

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
    │   │   ├── ParseError.java         # row number, content and error message
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
| `FileMobLoader` / `URLMobLoader` | Input adapters. They know the concrete source (path or URL) and drop the header. On failure they throw `LoaderException` with a message and the original cause. |
| `MobParser` | Turns each line into a `Mob`. The delimiter is passed in the constructor. Invalid rows do not stop the program: they are returned as `ParseError` with their row number, counted from the first data row, and their content. |
| `MobsAnalyzer` | Computes the summary from a `List<Mob>`: `groupBy`, `groupFrequencies` and `filterMobs`. It never opens files or URLs. |
| `MobWriter` | Receives an `OutputStream` and writes the result. It does not know whether it is the console, a file or memory. On failure it throws `WriterException`. |

### Design decisions

- **The summary logic knows neither the source nor the destination.** `MobsAnalyzer` only receives a collection of `Mob`.
- **The concrete source and the header belong to the input adapters,** and the delimiter is a constructor parameter of `MobParser`.
- **No `null` on errors.** The code uses `Optional` (`parseLine`), empty results (an empty file gives an empty list) and controlled exceptions (`LoaderException`, `WriterException`).
- **`main` connects, it does not parse.** Parsing lives in `MobParser` and the summary in `MobsAnalyzer`.

## Kata repetitions

The kata was repeated with the same goal: read the dataset through an `InputStream`, transform it and write a summary. Each repetition can be identified in the commit history of `master`, the branch that holds the final version:

| Repetition | Commits | What was done / variation |
|---|---|---|
| 1 | `dfa45ff` … `c14095c` | First version: `Mob` entity and enums, CSV read with `FileInputStream`, groupings in `MobStatistics`. Source: **file**. Destination: **console**, through `System.out.println`. |
| 2 | `506d429` … `008692d` | Parsing moved to `MobParser`, groupings generalised (`groupBy`, `groupFrequencies`, `filterMobs` in `MobsAnalyzer`) and the writer created. Variation: output through an **`OutputStream`**, without closing `System.out`. |
| 3 | `377b8d7` … `5ab87e7` | `InputStream`-based `Loader` interface with two adapters, `FileMobLoader` and `URLMobLoader`. Controlled errors with `LoaderException` and `WriterException`, plus automated tests. Variation: **URL** source; **file** and **memory** destinations. |

## Verification

Verification is automated with **JUnit 5**:

```
[INFO] Tests run: 54, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

[`MainVerificationTest`](src/test/java/reivajh06/Kata2VerificationTest.java) runs the whole pipeline and has one block per case of the assignment's *expected verification* list:

| Assignment case | How it is checked |
|---|---|
| Valid file | All 78 rows are parsed without errors and the summary is `{PEACEFUL=34, HOSTILE=33, CONDITIONAL=11}`. |
| URL, classpath resource or `ByteArrayInputStream` | Same summary when loading through a `file://` URL, from the classpath and from memory. |
| Writing to console and to a file or `ByteArrayOutputStream` | The summary is written to `System.out` (redirected), to a temporary file and to memory, and the content is compared. |
| Missing path or invalid URL | `LoaderException` for a missing path, a URL to a missing resource, an unknown protocol and a syntactically invalid URL. |
| Empty file | An empty file and a header-only file on disk produce an empty summary `{}`. |
| Malformed row | Rows with missing columns, invalid numbers or unknown values are reported as `ParseError` (row number + content) and the remaining rows are processed. |
| Summary unchanged when rows are reordered or the source changes | Rows are shuffled with several seeds, and file, URL, resource and memory sources are compared: the summary is always the same. |

There are also unit tests per component:

- [`LoaderTest`](src/test/java/reivajh06/LoaderTest.java): sources, CRLF line endings, stream closing and errors.
- [`MobParserTest`](src/test/java/reivajh06/MobParserTest.java): row conversion and invalid rows.
- [`MobsAnalyzerTest`](src/test/java/reivajh06/MobsAnalyzerTest.java): groupings, frequencies and empty collection.
- [`MobWriterTest`](src/test/java/reivajh06/MobWriterTest.java): destinations, closing and `WriterException`.
