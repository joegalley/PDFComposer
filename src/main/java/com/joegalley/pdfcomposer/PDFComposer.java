package com.joegalley.pdfcomposer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * A small, builder-configured helper for composing simple PDF reports
 * (running text, titled/headered tables, and images) on top of Apache
 * PDFBox, handling page breaks and basic layout so callers don't have to
 * manage PDFBox's content streams directly.
 * <p>
 * All coordinates are in PDF points, measured from the bottom-left corner of
 * the page (PDFBox's native coordinate system), except where a method's
 * documentation says otherwise.
 * <p>
 * A {@code PDFComposer} owns a {@link PDDocument} and is not thread-safe.
 * Call {@link #save(File)} (or one of its overloads) when the content is
 * ready, and {@link #close()} — ideally via try-with-resources — once the
 * composer is no longer needed, to release the underlying document's
 * resources.
 */
public class PDFComposer implements AutoCloseable {

    private final PDDocument doc;
    private PDFStreamTracker tracker;
    private boolean contentFinalized = false;

    private final float hMargin;
    private final float vMargin;
    private final float lineHeight;
    private final int fontSizeSmall;
    private final int fontSizeMedium;
    private final int fontSizeLarge;
    private final Color textColor;
    private final Color backgroundColor;
    private final Color titleBackgroundColor;
    private final Color titleTextColor;
    private final Color headerBackgroundColor;
    private final Color headerTextColor;
    private final PDFont fontRegular;
    private final PDFont fontBold;
    private final PDFont fontItalic;
    private final float cellVerticalOffset;
    private final float cellHorizontalOffset;

    /**
     * Fluent builder for {@link PDFComposer}. All setters are optional and
     * have sensible defaults for a basic report.
     */
    public static class Builder {
        private float hMargin = 36;
        private float vMargin = 36;
        private float lineHeight = 14;

        private int fontSizeSmall = 10;
        private int fontSizeMedium = 12;
        private int fontSizeLarge = 14;

        private Color textColor = Color.BLACK;
        private Color backgroundColor = Color.WHITE;
        private Color titleBackgroundColor = Color.BLACK;
        private Color titleTextColor = Color.WHITE;
        private Color headerBackgroundColor = Color.GRAY;
        private Color headerTextColor = Color.WHITE;

        private PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDFont fontItalic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

        private float cellVerticalOffset = 3;
        private float cellHorizontalOffset = 3;

        /** Left/right page margin, in points. Default 36 (0.5in). */
        public Builder horizontalMargin(float margin) {
            this.hMargin = margin;
            return this;
        }

        /** Top/bottom page margin, in points. Default 36 (0.5in). */
        public Builder verticalMargin(float margin) {
            this.vMargin = margin;
            return this;
        }

        /** Height of a single line of text or table row, in points. Default 14. */
        public Builder lineHeight(float height) {
            this.lineHeight = height;
            return this;
        }

        /** Font size used for table cell content. Default 10. */
        public Builder fontSizeSmall(int size) {
            this.fontSizeSmall = size;
            return this;
        }

        /** Font size used for body text, table titles, and headers. Default 12. */
        public Builder fontSizeMedium(int size) {
            this.fontSizeMedium = size;
            return this;
        }

        /** Reserved for larger headings. Default 14. */
        public Builder fontSizeLarge(int size) {
            this.fontSizeLarge = size;
            return this;
        }

        public Builder textColor(Color color) {
            this.textColor = color;
            return this;
        }

        public Builder backgroundColor(Color color) {
            this.backgroundColor = color;
            return this;
        }

        public Builder tableTitleColor(Color color) {
            this.titleTextColor = color;
            return this;
        }

        public Builder tableTitleBackgroundColor(Color color) {
            this.titleBackgroundColor = color;
            return this;
        }

        public Builder tableHeaderColor(Color color) {
            this.headerTextColor = color;
            return this;
        }

        public Builder tableHeaderBackgroundColor(Color color) {
            this.headerBackgroundColor = color;
            return this;
        }

        public Builder fontRegular(PDFont font) {
            this.fontRegular = font;
            return this;
        }

        public Builder fontBold(PDFont font) {
            this.fontBold = font;
            return this;
        }

        public Builder fontItalic(PDFont font) {
            this.fontItalic = font;
            return this;
        }

        /** Vertical padding between a table cell's border and its text, in points. Default 3. */
        public Builder cellVerticalOffset(float offset) {
            this.cellVerticalOffset = offset;
            return this;
        }

        /** Horizontal padding between a table cell's border and its text, in points. Default 3. */
        public Builder cellHorizontalOffset(float offset) {
            this.cellHorizontalOffset = offset;
            return this;
        }

        /**
         * Builds the composer, creating its underlying {@link PDDocument}
         * with a single, initially blank page.
         */
        public PDFComposer build() throws IOException {
            return new PDFComposer(this);
        }
    }

    private PDFComposer(Builder builder) throws IOException {
        hMargin = builder.hMargin;
        vMargin = builder.vMargin;
        lineHeight = builder.lineHeight;
        fontSizeSmall = builder.fontSizeSmall;
        fontSizeMedium = builder.fontSizeMedium;
        fontSizeLarge = builder.fontSizeLarge;
        textColor = builder.textColor;
        backgroundColor = builder.backgroundColor;
        titleBackgroundColor = builder.titleBackgroundColor;
        titleTextColor = builder.titleTextColor;
        headerBackgroundColor = builder.headerBackgroundColor;
        headerTextColor = builder.headerTextColor;
        fontRegular = builder.fontRegular;
        fontBold = builder.fontBold;
        fontItalic = builder.fontItalic;
        cellVerticalOffset = builder.cellVerticalOffset;
        cellHorizontalOffset = builder.cellHorizontalOffset;

        doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream cos = new PDPageContentStream(doc, page);
        fillPageBackground(cos, page);
        tracker = new PDFStreamTracker(doc, page, cos, hMargin, page.getMediaBox().getHeight() - vMargin);
    }

    /** Current horizontal drawing position, in points from the page's left edge. */
    public float getXPos() {
        return tracker.xPos;
    }

    /** Current vertical drawing position, in points from the page's bottom edge. */
    public float getYPos() {
        return tracker.yPos;
    }

    /** True if a block of the given height cannot fit above the bottom margin at the current position. */
    private boolean needsNewPage(float heightNeeded) {
        return (tracker.yPos - heightNeeded) < vMargin;
    }

    private void addPage() throws IOException {
        tracker.contentStream.close();
        PDPage newPage = new PDPage();
        doc.addPage(newPage);
        PDPageContentStream cos = new PDPageContentStream(doc, newPage);
        fillPageBackground(cos, newPage);
        tracker = new PDFStreamTracker(doc, newPage, cos, hMargin, newPage.getMediaBox().getHeight() - vMargin);
    }

    /** Fills a freshly created page with {@code backgroundColor}, when it isn't the PDF default white. */
    private void fillPageBackground(PDPageContentStream cos, PDPage page) throws IOException {
        if (!Color.WHITE.equals(backgroundColor)) {
            cos.setNonStrokingColor(backgroundColor);
            cos.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            cos.fill();
            cos.setNonStrokingColor(textColor);
        }
    }

    /** Draws a line of regular text at the current position, then advances the cursor down one line. */
    public PDFComposer drawString(String str) throws IOException {
        return drawText(str, fontRegular, fontSizeMedium);
    }

    /** Like {@link #drawString(String)}, but using the composer's bold font. */
    public PDFComposer drawBoldString(String str) throws IOException {
        return drawText(str, fontBold, fontSizeMedium);
    }

    /** Like {@link #drawString(String)}, but using the composer's italic font. */
    public PDFComposer drawItalicString(String str) throws IOException {
        return drawText(str, fontItalic, fontSizeMedium);
    }

    /** Draws a bold, large-font heading line, for section titles larger than a table title. */
    public PDFComposer drawHeading(String str) throws IOException {
        return drawText(str, fontBold, fontSizeLarge);
    }

    private PDFComposer drawText(String str, PDFont font, int fontSize) throws IOException {
        if (needsNewPage(lineHeight)) {
            addPage();
        }
        PDPageContentStream cos = tracker.contentStream;
        cos.beginText();
        cos.setFont(font, fontSize);
        cos.newLineAtOffset(tracker.xPos, tracker.yPos);
        cos.showText(str);
        cos.endText();
        tracker.yPos -= lineHeight;
        return this;
    }

    public PDFComposer drawBlankLine() {
        tracker.yPos -= lineHeight;
        return this;
    }

    public PDFComposer drawBlankLines(int numLines) {
        for (int i = 0; i < numLines; i++) {
            drawBlankLine();
        }
        return this;
    }

    /**
     * Draws a table starting at the current position: an optional title bar,
     * an optional header row, then one row per entry in {@code content}.
     * Cell text wraps to fit the column width, and the table automatically
     * continues onto new pages as needed.
     *
     * @param content rows of cell text; every row may have a different number of columns
     * @param title   optional title bar drawn above the table, or {@code null} to omit it
     * @param headers optional header row, or {@code null} to omit it
     */
    public PDFComposer drawTable(List<List<String>> content, String title, String[] headers) throws IOException {
        if (title != null) {
            drawTitle(title);
        }
        if (headers != null) {
            drawHeader(headers);
        }
        for (List<String> row : content) {
            drawTableRow(row);
        }
        return this;
    }

    private void drawTableRow(List<String> row) throws IOException {
        int numCols = row.size();
        if (numCols == 0) {
            return;
        }

        float tableWidth = tracker.page.getMediaBox().getWidth() - (2 * hMargin);
        float cellWidth = tableWidth / numCols;
        float textWidth = cellWidth - (2 * cellHorizontalOffset);

        List<List<String>> wrappedCells = new ArrayList<>(numCols);
        int maxLines = 1;
        for (String cellContent : row) {
            List<String> lines = wrapText(cellContent == null ? "" : cellContent, textWidth, fontRegular, fontSizeSmall);
            wrappedCells.add(lines);
            maxLines = Math.max(maxLines, lines.size());
        }

        float rowHeight = (maxLines * lineHeight) + (2 * cellVerticalOffset);
        if (needsNewPage(rowHeight)) {
            addPage();
        }

        float rowTop = tracker.yPos;
        float rowBottom = rowTop - rowHeight;
        PDPageContentStream cos = tracker.contentStream;

        for (int col = 0; col < numCols; col++) {
            float cellX = hMargin + (col * cellWidth);
            drawLine(cellX, rowTop, cellX, rowBottom);

            float textY = rowTop - cellVerticalOffset - fontSizeSmall;
            for (String line : wrappedCells.get(col)) {
                cos.beginText();
                cos.setFont(fontRegular, fontSizeSmall);
                cos.newLineAtOffset(cellX + cellHorizontalOffset, textY);
                cos.showText(line);
                cos.endText();
                textY -= lineHeight;
            }
        }

        float tableRight = hMargin + (numCols * cellWidth);
        drawLine(tableRight, rowTop, tableRight, rowBottom);
        drawLine(hMargin, rowTop, tableRight, rowTop);
        drawLine(hMargin, rowBottom, tableRight, rowBottom);

        tracker.yPos = rowBottom;
    }

    private void drawTitle(String title) throws IOException {
        float rowHeight = lineHeight + (2 * cellVerticalOffset);
        if (needsNewPage(rowHeight)) {
            addPage();
        }

        float top = tracker.yPos;
        float bottom = top - rowHeight;
        float width = tracker.page.getMediaBox().getWidth() - (2 * hMargin);
        PDPageContentStream cos = tracker.contentStream;

        cos.setNonStrokingColor(titleBackgroundColor);
        cos.addRect(hMargin, bottom, width, rowHeight);
        cos.fill();

        cos.setNonStrokingColor(titleTextColor);
        cos.beginText();
        cos.setFont(fontRegular, fontSizeMedium);
        cos.newLineAtOffset(hMargin + cellHorizontalOffset, bottom + cellVerticalOffset);
        cos.showText(title);
        cos.endText();
        cos.setNonStrokingColor(textColor);

        drawLine(hMargin, top, hMargin + width, top);
        drawLine(hMargin, top, hMargin, bottom);
        drawLine(hMargin + width, top, hMargin + width, bottom);
        drawLine(hMargin, bottom, hMargin + width, bottom);

        tracker.yPos = bottom;
    }

    private void drawHeader(String[] headers) throws IOException {
        int numCols = headers.length;
        if (numCols == 0) {
            return;
        }

        float tableWidth = tracker.page.getMediaBox().getWidth() - (2 * hMargin);
        float cellWidth = tableWidth / numCols;
        float rowHeight = lineHeight + (2 * cellVerticalOffset);

        if (needsNewPage(rowHeight)) {
            addPage();
        }

        float top = tracker.yPos;
        float bottom = top - rowHeight;
        PDPageContentStream cos = tracker.contentStream;

        cos.setNonStrokingColor(headerBackgroundColor);
        cos.addRect(hMargin, bottom, tableWidth, rowHeight);
        cos.fill();
        cos.setNonStrokingColor(headerTextColor);

        for (int col = 0; col < numCols; col++) {
            float cellX = hMargin + (col * cellWidth);
            cos.beginText();
            cos.setFont(fontRegular, fontSizeMedium);
            cos.newLineAtOffset(cellX + cellHorizontalOffset, bottom + cellVerticalOffset);
            cos.showText(headers[col]);
            cos.endText();
            drawLine(cellX, top, cellX, bottom);
        }

        float tableRight = hMargin + (numCols * cellWidth);
        drawLine(tableRight, top, tableRight, bottom);
        drawLine(hMargin, top, tableRight, top);
        drawLine(hMargin, bottom, tableRight, bottom);

        cos.setNonStrokingColor(textColor);
        tracker.yPos = bottom;
    }

    private void drawLine(float x1, float y1, float x2, float y2) throws IOException {
        PDPageContentStream cos = tracker.contentStream;
        cos.moveTo(x1, y1);
        cos.lineTo(x2, y2);
        cos.stroke();
    }

    /** Greedily wraps {@code text} into lines no wider than {@code maxWidth} at the given font/size. */
    private List<String> wrapText(String text, float maxWidth, PDFont font, int fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text.isEmpty()) {
            lines.add("");
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (currentLine.length() > 0 && textWidth(candidate, font, fontSize) > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(candidate);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    private float textWidth(String text, PDFont font, int fontSize) throws IOException {
        return font.getStringWidth(text) / 1000f * fontSize;
    }

    /**
     * Draws an image with its top-left corner at ({@code x}, {@code y}).
     * <p>
     * Note this differs from PDFBox's own {@code drawImage}, which places
     * the image's bottom-left corner at the given point.
     *
     * @param pathToImage path to a JPEG, PNG, or other ImageIO-readable image file
     * @param x           x-coordinate of the image's top-left corner
     * @param y           y-coordinate of the image's top-left corner
     * @param width       width to draw the image at (scaled if different from its natural width)
     * @param height      height to draw the image at (scaled if different from its natural height)
     */
    public PDFComposer drawImage(String pathToImage, float x, float y, float width, float height) throws IOException {
        PDImageXObject image = PDImageXObject.createFromFile(pathToImage, doc);
        tracker.contentStream.drawImage(image, x, y - height, width, height);
        return this;
    }

    public PDFComposer moveRight(float points) {
        tracker.xPos += points;
        return this;
    }

    public PDFComposer moveLeft(float points) {
        tracker.xPos -= points;
        return this;
    }

    public PDFComposer moveUp(float points) {
        tracker.yPos += points;
        return this;
    }

    public PDFComposer moveDown(float points) {
        tracker.yPos -= points;
        return this;
    }

    private void finalizeContent() throws IOException {
        if (!contentFinalized) {
            tracker.contentStream.close();
            contentFinalized = true;
        }
    }

    /** Finalizes the current page's content and writes the document to {@code filename}. */
    public void save(String filename) throws IOException {
        save(new File(filename));
    }

    /** Finalizes the current page's content and writes the document to {@code file}. */
    public void save(File file) throws IOException {
        finalizeContent();
        doc.save(file);
    }

    /** Finalizes the current page's content and writes the document to {@code out}. */
    public void save(OutputStream out) throws IOException {
        finalizeContent();
        doc.save(out);
    }

    /**
     * Finalizes the current page's content (if not already done by
     * {@link #save}) and releases the underlying document's resources.
     * Safe to call via try-with-resources.
     */
    @Override
    public void close() throws IOException {
        finalizeContent();
        doc.close();
    }
}
