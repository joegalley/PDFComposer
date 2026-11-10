package com.joegalley.pdfcomposer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

class PDFComposerTest {

    private static Path testImagePath() throws URISyntaxException {
        return Paths.get(PDFComposerTest.class.getResource("/test-image.jpg").toURI());
    }

    private static PDDocument renderAndReload(PDFComposer composer) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        composer.save(out);
        return Loader.loadPDF(out.toByteArray());
    }

    @Test
    void newDocumentHasOnePage() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder().build()) {
            try (PDDocument reloaded = renderAndReload(composer)) {
                assertEquals(1, reloaded.getNumberOfPages());
            }
        }
    }

    @Test
    void drawStringAdvancesCursorByOneLineHeight() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder().lineHeight(15).build()) {
            float startY = composer.getYPos();
            composer.drawString("hello world");
            assertEquals(startY - 15, composer.getYPos(), 0.001);
        }
    }

    @Test
    void drawStringTextIsExtractableFromSavedDocument() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder().build()) {
            composer.drawString("hello from PDFComposer");
            try (PDDocument reloaded = renderAndReload(composer)) {
                String text = new PDFTextStripper().getText(reloaded);
                assertTrue(text.contains("hello from PDFComposer"), "expected drawn text in output, got: " + text);
            }
        }
    }

    @Test
    void manyLinesOfTextEventuallyStartANewPage() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder().lineHeight(14).build()) {
            // A Letter page (792pt tall) with default 36pt margins has ~720pt
            // of usable height, i.e. room for far fewer than 500 lines.
            for (int i = 0; i < 500; i++) {
                composer.drawString("line " + i);
            }
            try (PDDocument reloaded = renderAndReload(composer)) {
                assertTrue(reloaded.getNumberOfPages() > 1,
                        "expected pagination to add pages, got " + reloaded.getNumberOfPages());
            }
        }
    }

    @Test
    void singleShortLineOfTextStaysOnOnePage() throws IOException {
        // Regression test: checkIfNewPageNeeded() used to unconditionally
        // return true, forcing a new (blank) page on every drawString call.
        try (PDFComposer composer = new PDFComposer.Builder().build()) {
            composer.drawString("just one line");
            try (PDDocument reloaded = renderAndReload(composer)) {
                assertEquals(1, reloaded.getNumberOfPages());
            }
        }
    }

    @Test
    void drawTableWithTitleAndHeaderRendersAllContent() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder().build()) {
            List<List<String>> rows = new ArrayList<>();
            rows.add(Arrays.asList("one", "two", "three"));
            rows.add(Arrays.asList("four", "five", "six"));

            composer.drawTable(rows, "Test Title", new String[] { "Col A", "Col B", "Col C" });

            try (PDDocument reloaded = renderAndReload(composer)) {
                String text = new PDFTextStripper().getText(reloaded);
                assertTrue(text.contains("Test Title"));
                assertTrue(text.contains("Col A"));
                assertTrue(text.contains("four"));
            }
        }
    }

    @Test
    void drawTableWithoutTitleOrHeaderDoesNotThrow() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder().build()) {
            List<List<String>> rows = new ArrayList<>();
            rows.add(Arrays.asList("a", "b"));
            composer.drawTable(rows, null, null);
        }
    }

    @Test
    void drawTableWithLongCellContentWrapsAndSpansMultiplePagesIfNeeded() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder().build()) {
            String longText = "word ".repeat(60).trim();
            List<List<String>> rows = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                rows.add(Arrays.asList(longText, "short"));
            }

            composer.drawTable(rows, "Long Table", new String[] { "Left", "Right" });

            try (PDDocument reloaded = renderAndReload(composer)) {
                assertTrue(reloaded.getNumberOfPages() >= 1);
                String text = new PDFTextStripper().getText(reloaded);
                assertTrue(text.contains("word"));
            }
        }
    }

    @Test
    void drawImageDoesNotThrow() throws Exception {
        try (PDFComposer composer = new PDFComposer.Builder().build()) {
            String imagePath = testImagePath().toString();
            composer.drawImage(imagePath, composer.getXPos(), composer.getYPos(), 20, 20);
        }
    }

    @Test
    void moveHelpersUpdateCursorPosition() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder().build()) {
            float startX = composer.getXPos();
            float startY = composer.getYPos();

            composer.moveRight(10).moveUp(5);
            assertEquals(startX + 10, composer.getXPos(), 0.001);
            assertEquals(startY + 5, composer.getYPos(), 0.001);

            composer.moveLeft(10).moveDown(5);
            assertEquals(startX, composer.getXPos(), 0.001);
            assertEquals(startY, composer.getYPos(), 0.001);
        }
    }

    @Test
    void drawBlankLinesMovesCursorDownByThatManyLines() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder().lineHeight(12).build()) {
            float startY = composer.getYPos();
            composer.drawBlankLines(3);
            assertEquals(startY - 36, composer.getYPos(), 0.001);
        }
    }

    @Test
    void customBuilderSettingsAreHonored() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder()
                .horizontalMargin(50)
                .verticalMargin(60)
                .build()) {
            assertEquals(50, composer.getXPos(), 0.001);
            try (PDDocument reloaded = renderAndReload(composer)) {
                assertEquals(1, reloaded.getNumberOfPages());
            }
        }
    }

    @Test
    void savingTwiceProducesAValidDocumentBothTimes() throws IOException {
        try (PDFComposer composer = new PDFComposer.Builder().build()) {
            composer.drawString("saved twice");

            ByteArrayOutputStream first = new ByteArrayOutputStream();
            composer.save(first);
            ByteArrayOutputStream second = new ByteArrayOutputStream();
            composer.save(second);

            try (PDDocument a = Loader.loadPDF(first.toByteArray());
                 PDDocument b = Loader.loadPDF(second.toByteArray())) {
                assertEquals(1, a.getNumberOfPages());
                assertEquals(1, b.getNumberOfPages());
            }
        }
    }
}
