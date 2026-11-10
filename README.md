# PDFComposer

A small, builder-configured Java library for composing simple PDF reports —
running text, titled/headered tables, and images — on top of
[Apache PDFBox](https://pdfbox.apache.org/), without having to manage
PDFBox's content streams and page breaks by hand.

```java
try (PDFComposer composer = new PDFComposer.Builder().build()) {
    composer.drawHeading("Quarterly Report");
    composer.drawString("Generated " + LocalDate.now());
    composer.drawBlankLine();

    composer.drawTable(
        List.of(
            List.of("Alpha", "A long description that wraps to fit its column", "42"),
            List.of("Beta", "Short", "7")),
        "Sample Table",
        new String[] { "Name", "Description", "Count" });

    composer.save("report.pdf");
}
```

## Features

- Fluent `Builder` for page margins, fonts, colors, and spacing
- `drawString` / `drawBoldString` / `drawItalicString` / `drawHeading` for running text
- `drawTable` with an optional title bar and header row, automatic cell text
  wrapping, and automatic pagination when content runs past the bottom margin
- `drawImage` for placing JPEG/PNG/etc. images
- A simple cursor (`moveLeft` / `moveRight` / `moveUp` / `moveDown`) for manual
  layout alongside the higher-level drawing methods
- Implements `AutoCloseable`, so a composer can be used in a try-with-resources
  block

## Requirements

- Java 17+
- Maven 3.6+

## Installation

This library isn't published to Maven Central. Clone the repo and install it
to your local Maven repository:

```bash
git clone https://github.com/joegalley/PDFComposer.git
cd PDFComposer
mvn install
```

Then depend on it from another Maven project:

```xml
<dependency>
    <groupId>com.joegalley</groupId>
    <artifactId>pdfcomposer</artifactId>
    <version>1.0.0</version>
</dependency>
```

Alternatively, if you'd rather not install it locally, [JitPack](https://jitpack.io)
can build a dependency directly from a GitHub tag without any publishing step
on your end — point it at `com.github.joegalley:PDFComposer`.

## Usage

### Building a composer

```java
PDFComposer composer = new PDFComposer.Builder()
        .horizontalMargin(36)      // left/right page margin, in points
        .verticalMargin(36)        // top/bottom page margin, in points
        .lineHeight(14)            // height of a line of text or a table row
        .fontSizeSmall(10)         // table cell font size
        .fontSizeMedium(12)        // body text / title / header font size
        .fontSizeLarge(14)         // drawHeading() font size
        .build();
```

All builder methods are optional; the defaults above produce a readable
Letter-sized report with black text on a white background.

### Text

```java
composer.drawString("Regular text");
composer.drawBoldString("Bold text");
composer.drawItalicString("Italic text");
composer.drawHeading("A section heading");
composer.drawBlankLine();
composer.drawBlankLines(3);
```

### Tables

```java
List<List<String>> rows = List.of(
    List.of("row 1, col 1", "row 1, col 2"),
    List.of("row 2, col 1", "row 2, col 2"));

composer.drawTable(rows, "Optional Title", new String[] { "Column A", "Column B" });
```

Pass `null` for `title` or `headers` to omit them. Cell text wraps to fit the
column width, and the table continues onto additional pages automatically
when a row doesn't fit above the bottom margin.

### Images

```java
// (x, y) is the image's top-left corner, unlike PDFBox's own drawImage().
composer.drawImage("logo.png", composer.getXPos(), composer.getYPos(), 100, 50);
```

### Saving

```java
composer.save("report.pdf");          // to a file path
composer.save(new File("report.pdf")); // to a File
composer.save(outputStream);           // to any OutputStream
```

`save` can be called more than once (e.g. to write both a file and an
in-memory copy). Call `close()` — or use try-with-resources, as in the
examples above — once you're done with the composer, to release the
underlying document's resources.

## Building and testing

```bash
mvn test    # run the test suite
mvn install # build and install to the local Maven repository
```

## License

[MIT](LICENSE)
