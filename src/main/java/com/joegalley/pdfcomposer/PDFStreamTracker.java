package com.joegalley.pdfcomposer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

/**
 * Mutable cursor state for the page currently being written to: the document,
 * page, and open content stream, plus the current drawing position.
 */
class PDFStreamTracker {
    final PDDocument doc;
    final PDPage page;
    final PDPageContentStream contentStream;
    float xPos;
    float yPos;

    PDFStreamTracker(PDDocument doc, PDPage page, PDPageContentStream contentStream, float xPos, float yPos) {
        this.doc = doc;
        this.page = page;
        this.contentStream = contentStream;
        this.xPos = xPos;
        this.yPos = yPos;
    }
}
