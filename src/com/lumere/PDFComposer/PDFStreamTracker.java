package com.lumere.PDFComposer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

public class PDFStreamTracker {
	public PDDocument doc;
	public PDPage page;
	public PDPageContentStream content_stream;
	public float xPos;
	public float yPos;

	public PDFStreamTracker(PDDocument doc, PDPage page, PDPageContentStream content_stream, float xPos, float yPos) {
		this.doc = doc;
		this.page = page;
		this.content_stream = content_stream;
		this.xPos = xPos;
		this.yPos = yPos;
	}
}
