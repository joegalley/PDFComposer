package com.lumere.PDFComposer;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

public class PDFComposer {
	private PDFStreamTracker tracker;

	private float hMargin;
	private float vMargin;
	private float line_ht;
	private int font_size_sm;
	private int font_size_md;
	private int font_size_lg;
	private Color COLOR_TEXT_DEFAULT;
	private Color COLOR_BG_DEFAULT;
	private Color COLOR_TITLE_BG;
	private Color COLOR_TITLE_FG;
	private Color COLOR_HEADER_BG;
	private Color COLOR_HEADER_FG;
	private PDFont FONT_REGULAR;
	private PDFont FONT_BOLD;
	private PDFont FONT_ITALIC;
	private float cell_content_vert_offset;
	private float cell_content_horiz_offset;

	public static class Builder {
		private PDFStreamTracker tracker;
		private PDDocument doc;
		private PDPage page;
		private PDPageContentStream cos;

		// optional
		private float hMargin = 10;
		private float vMargin = 10;
		private float line_ht = 10;

		private int font_size_sm = 10;
		private int font_size_md = 12;
		private int font_size_lg = 14;

		private Color COLOR_TEXT_DEFAULT = Color.BLACK;
		private Color COLOR_BG_DEFAULT = Color.WHITE;
		private Color COLOR_TITLE_BG = Color.BLACK;
		private Color COLOR_TITLE_FG = Color.WHITE;
		private Color COLOR_HEADER_BG = Color.GRAY;
		private Color COLOR_HEADER_FG = Color.WHITE;

		private PDFont FONT_REGULAR = PDType1Font.HELVETICA;
		private PDFont FONT_BOLD = PDType1Font.HELVETICA_BOLD;
		private PDFont FONT_ITALIC = PDType1Font.HELVETICA_OBLIQUE;

		private float cell_content_vert_offset = 3;
		private float cell_content_horiz_offset = 3;

		public Builder() {
			doc = new PDDocument();
			page = new PDPage();
			doc.addPage(page);
			try {
				cos = new PDPageContentStream(doc, page);
			} catch (IOException e) {
				e.printStackTrace();
			}
			tracker = new PDFStreamTracker(doc, page, cos, hMargin, page.findMediaBox().getHeight()
					- vMargin);
		}

		public Builder horizontalMargin(float margin) {
			this.hMargin = margin;
			return this;
		}

		public Builder verticalMargin(float margin) {
			this.vMargin = margin;
			return this;
		}

		public Builder lineHeight(float height) {
			this.line_ht = height;
			return this;
		}

		public Builder fontSizeSmall(int size) {
			this.font_size_sm = size;
			return this;
		}

		public Builder fontSizeMedium(int size) {
			this.font_size_md = size;
			return this;
		}

		public Builder fontSizeLarge(int size) {
			this.font_size_lg = size;
			return this;
		}

		public Builder textColor(Color color) {
			this.COLOR_TEXT_DEFAULT = color;
			return this;
		}

		public Builder backgroundColor(Color color) {
			this.COLOR_BG_DEFAULT = color;
			return this;
		}

		public Builder tableTitleColor(Color color) {
			this.COLOR_TITLE_FG = color;
			return this;
		}

		public Builder tableTitleBackgroundColor(Color color) {
			this.COLOR_TITLE_BG = color;
			return this;
		}

		public Builder tableHeaderColor(Color color) {
			this.COLOR_HEADER_FG = color;
			return this;
		}

		public Builder tableHeaderBackgroundColor(Color color) {
			this.COLOR_HEADER_BG = color;
			return this;
		}

		public Builder fontRegular(PDType1Font font) {
			this.FONT_REGULAR = font;
			return this;
		}

		public Builder fontBold(PDType1Font font) {
			this.FONT_BOLD = font;
			return this;
		}

		public Builder fontItalic(PDType1Font font) {
			this.FONT_ITALIC = font;
			return this;
		}

		public Builder cellVerticalOffset(float offset) {
			this.cell_content_vert_offset = offset;
			return this;
		}

		public Builder cellHorizontalOffset(float offset) {
			this.cell_content_horiz_offset = offset;
			return this;
		}

		public PDFComposer build() {
			return new PDFComposer(this);
		}
	}

	private PDFComposer(Builder builder) {

		tracker = builder.tracker;
		hMargin = builder.hMargin;
		vMargin = builder.vMargin;
		line_ht = builder.line_ht;
		font_size_sm = builder.font_size_sm;
		font_size_md = builder.font_size_md;
		font_size_lg = builder.font_size_lg;
		COLOR_TEXT_DEFAULT = builder.COLOR_TEXT_DEFAULT;
		COLOR_BG_DEFAULT = builder.COLOR_BG_DEFAULT;
		COLOR_TITLE_BG = builder.COLOR_TITLE_BG;
		COLOR_TITLE_FG = builder.COLOR_TITLE_FG;
		COLOR_HEADER_BG = builder.COLOR_HEADER_BG;
		COLOR_HEADER_FG = builder.COLOR_HEADER_FG;
		FONT_REGULAR = builder.FONT_REGULAR;
		FONT_BOLD = builder.FONT_BOLD;
		FONT_ITALIC = builder.FONT_ITALIC;
		cell_content_vert_offset = builder.cell_content_vert_offset;
		cell_content_horiz_offset = builder.cell_content_horiz_offset;

	}

	public PDFStreamTracker drawString(String str) {
		try {
			tracker.content_stream.beginText();
			tracker.content_stream.setFont(this.FONT_BOLD, 12);
			tracker.content_stream.moveTextPositionByAmount(tracker.xPos, tracker.yPos);
			tracker.content_stream.drawString(str);
			tracker.yPos -= this.line_ht;
			tracker.content_stream.endText();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tracker;
	}

	public void drawBlankLine() {
		tracker.yPos -= this.line_ht;
	}

	public void drawBlankLines(int num_lines) {
		for (int i = 0; i < num_lines; i++) {
			this.drawBlankLine();
		}
	}

	public void closeStream() {
		try {
			tracker.content_stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeDocument() {
		System.out.println("closeDocument");
		try {
			tracker.doc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDocument(ByteArrayOutputStream baos) {
		this.closeStream();
		try {
			tracker.doc.save(baos);
			tracker.doc.close();
		} catch (COSVisitorException | IOException e) {
			e.printStackTrace();
		}

	}

	public void saveDocument(String filename) {
		this.closeStream();
		File file = new File(filename);
		try {
			tracker.doc.save(file);
			tracker.doc.close();
		} catch (COSVisitorException | IOException e) {
			e.printStackTrace();
		}
	}

	public PDFStreamTracker drawTable(List<List<String>> content, String title, String[] headers) {

		try {

			if (title != null) {
				this.drawTitle(title);
			}
			if (headers != null) {
				this.drawHeader(headers);
			}

			tracker.yPos -= this.line_ht;

			// draw rows
			for (int i = 0; i < content.size(); i++) {
				// draws a single horizontal line (top of row)
				/*-
				this.tracker.content_stream.drawLine(this.horiz_margin, this.tracker.yPos, this.tracker.page.findMediaBox()
						.getWidth() - this.horiz_margin, this.tracker.yPos);
				 */

				// keep track of the top-left corner (start) of the row
				float row_top_left_corner_x = hMargin;
				float row_top_left_corner_y = this.tracker.yPos;

				float cell_width = (this.tracker.page.findMediaBox().getWidth() - (this.hMargin * 2))
						/ content.get(i).size();

				// draws each of the vertical lines (columns) underneath the
				// current
				// horizontal line
				float row_height_needed = 0;

				int max_cell_height = 0;

				int sz = 0;
				for (int j = 0; j < content.get(i).size(); j++) {
					String cell_content = content.get(i).get(j);
					if (cell_content != null) {

						// for each cell_content, how tall would it be after
						// wrapping?
						int chars_avail_per_cell_row = (int) Math.floor(cell_width
								/ this.FONT_REGULAR.getAverageFontWidth());

						int cur_text_row_width = 0;
						int num_rows = 0;

						int running_content_width = 0;

						// If the cell content must be broken into rows.
						List<ArrayList<String>> content_rows = new ArrayList<ArrayList<String>>();

						ArrayList<String> curr_row = new ArrayList<String>();
						float row_len = 0;
						int content_word_count = cell_content.split(" ").length;

						int num_words = 0;

						for (String s : cell_content.split(" ")) {
							num_words++;
							row_len += this.getWordWidth(s, this.font_size_md, this.FONT_REGULAR);

							if (num_words == content_word_count) {
								curr_row.add(s);
								ArrayList<String> copy = new ArrayList<String>(curr_row);
								content_rows.add(copy);
								curr_row.clear();

							} else {
								if (row_len >= cell_width) {
									ArrayList<String> copy = new ArrayList<String>(curr_row);
									content_rows.add(copy);
									row_len = 0;
									curr_row.clear();
									curr_row.add(s);
								} else {
									curr_row.add(s);
								}
							}
						}

						for (ArrayList<String> a : content_rows) {
							num_rows++;
						}

						row_height_needed = num_rows * line_ht;

						if (row_height_needed > this.tracker.yPos) {
							// start a new page
						} else {

							// move to the start of the row
							this.tracker.content_stream.moveTo(row_top_left_corner_x, row_top_left_corner_y);

							int cell_content_rows = 0;
							// write cell content
							for (ArrayList<String> row : content_rows) {
								cell_content_rows++;

								this.tracker.content_stream.beginText();
								this.tracker.content_stream.setFont(this.FONT_REGULAR, 12);
								this.tracker.content_stream.moveTextPositionByAmount((j * cell_width) + hMargin,
										this.tracker.yPos);
								String line = "";
								for (String word : row) {
									line += " " + word;
								}
								if (line != null) {
									this.tracker.content_stream.drawString(line);
								}
								this.tracker.content_stream.endText();
								if (cell_content_rows > 1) {
									this.tracker.yPos -= line_ht;
								}

							}// cell drawn
							this.tracker.yPos = row_top_left_corner_y;

						}

						if (content_rows.size() > sz) {
							sz = content_rows.size();
						}

					}

				}// row drawn

				tracker.yPos = (row_top_left_corner_y - (sz * line_ht));

				// left lines of all cells in row
				int col;
				for (col = 0; col < content.get(i).size(); col++) {
					this.tracker.content_stream.drawLine((col * cell_width) + hMargin, row_top_left_corner_y
							+ this.line_ht, (col * cell_width) + hMargin,
							(row_top_left_corner_y - (sz * this.line_ht)) + this.line_ht);
				}
				col -= 1;

				this.tracker.content_stream.drawLine((col * cell_width) + hMargin + cell_width,
						row_top_left_corner_y + line_ht, (col * cell_width) + hMargin + cell_width,
						(row_top_left_corner_y - (sz * line_ht)) + line_ht);

				// right line of last cell in row
				this.tracker.content_stream.drawLine((col * cell_width) + hMargin + cell_width,
						row_top_left_corner_y
								+ line_ht, (col * cell_width) + hMargin + cell_width,
						(row_top_left_corner_y - (sz * line_ht)) + line_ht);

				// bottom of row
				this.tracker.content_stream.drawLine(hMargin, this.tracker.yPos + line_ht, this.tracker.page
						.findMediaBox()
						.getWidth() - hMargin, this.tracker.yPos + line_ht);

			}

		} catch (IOException e) {
		}
		return this.tracker;

	}

	private void drawTitle(String title) throws IOException {
		// top line
		this.tracker.content_stream.drawLine(hMargin, this.tracker.yPos, this.tracker.page.findMediaBox()
				.getWidth() - hMargin, this.tracker.yPos);

		float row_top_left_corner_x = hMargin;
		float row_top_left_corner_y = this.tracker.yPos;

		// left line
		this.tracker.content_stream.drawLine(hMargin, this.tracker.yPos, hMargin, this.tracker.yPos
				- line_ht);

		// move to the start of the row
		this.tracker.content_stream.moveTo(row_top_left_corner_x, row_top_left_corner_y);

		// fill title
		this.tracker.content_stream.setNonStrokingColor(this.COLOR_TITLE_BG);
		this.tracker.content_stream.fillRect(row_top_left_corner_x, row_top_left_corner_y, this.tracker.page
				.getMediaBox().getWidth() - (2 * hMargin), -line_ht);

		// write title
		this.tracker.content_stream.setNonStrokingColor(this.COLOR_TITLE_FG);
		this.tracker.content_stream.beginText();
		this.tracker.content_stream.setFont(this.FONT_REGULAR, this.font_size_md);
		this.tracker.content_stream.moveTextPositionByAmount(this.tracker.xPos + hMargin, this.tracker.yPos
				- line_ht);
		this.tracker.content_stream.drawString(title);
		this.tracker.content_stream.endText();
		this.tracker.content_stream.setNonStrokingColor(this.COLOR_TEXT_DEFAULT);

		// right line
		this.tracker.content_stream.drawLine(this.tracker.page.findMediaBox().getWidth() - hMargin,
				this.tracker.yPos, this.tracker.page.findMediaBox().getWidth() - hMargin, this.tracker.yPos
						- line_ht);

		this.tracker.yPos -= line_ht;

		// bottom line
		this.tracker.content_stream.drawLine(hMargin, this.tracker.yPos, this.tracker.page.findMediaBox()
				.getWidth() - hMargin, this.tracker.yPos);

	}

	private void drawHeader(String[] headers) throws IOException {

		// header top
		this.tracker.content_stream.drawLine(hMargin, this.tracker.yPos, this.tracker.page.findMediaBox()
				.getWidth() - hMargin, this.tracker.yPos);

		// start of row
		float row_top_left_corner_x = hMargin;
		float row_top_left_corner_y = this.tracker.yPos;

		float cell_width = (this.tracker.page.findMediaBox().getWidth() - (hMargin * 2)) / headers.length;

		// fill header row
		this.tracker.content_stream.setNonStrokingColor(this.COLOR_HEADER_BG);
		this.tracker.content_stream.fillRect(row_top_left_corner_x, row_top_left_corner_y, this.tracker.page
				.getMediaBox().getWidth() - (2 * hMargin), -line_ht);
		this.tracker.content_stream.setNonStrokingColor(this.COLOR_HEADER_FG);

		// cell separators
		for (int i = 0; i < headers.length; i++) {
			String cell_content = headers[i];

			// for each cell_content, how tall would it be after wrapping?
			int chars_avail_per_cell_row = (int) Math.floor(cell_width / this.FONT_REGULAR.getAverageFontWidth());
			int num_rows_cell_text = 0;
			int cur_text_row_width = 0;
			int num_rows = 0;

			for (char c : cell_content.toCharArray()) {
				cur_text_row_width += this.FONT_REGULAR.getFontWidth(c);
				if (cur_text_row_width >= chars_avail_per_cell_row) {
					num_rows++;
				}
			}

			float row_height_needed = num_rows * line_ht;

			if (row_height_needed > this.tracker.yPos) {
				// start a new page
			} else {
				// draw cell's left line
				this.tracker.content_stream.drawLine((i * cell_width) + hMargin, this.tracker.yPos,
						(i * cell_width) + hMargin, this.tracker.yPos - line_ht);

				// move to the start of the row
				this.tracker.content_stream.moveTo(row_top_left_corner_x, row_top_left_corner_y);

				// write cell content
				this.tracker.content_stream.beginText();
				this.tracker.content_stream.setFont(this.FONT_REGULAR, this.font_size_md);
				this.tracker.content_stream.moveTextPositionByAmount((i * cell_width) + hMargin,
						this.tracker.yPos - line_ht);
				this.tracker.content_stream.drawString(cell_content);
				this.tracker.content_stream.endText();

				// if last cell in row, draw the right line
				if (i == headers.length - 1) {
					this.tracker.content_stream.drawLine((i * cell_width) + hMargin + cell_width,
							this.tracker.yPos, (i * cell_width) + hMargin + cell_width, this.tracker.yPos
									- line_ht);

				}

			}
		}

		this.tracker.yPos -= line_ht;

		// bottom of row
		this.tracker.content_stream.drawLine(hMargin, this.tracker.yPos, this.tracker.page.findMediaBox()
				.getWidth() - hMargin, this.tracker.yPos);

		this.tracker.content_stream.setNonStrokingColor(this.COLOR_TEXT_DEFAULT);

	}

	private float getWordWidth(String s, int font_size, PDFont font) throws IOException {
		int fontSize = 16;
		float width = font.getStringWidth(s) / 1000 * fontSize;
		return width;
	}

	public void drawImage(String path_to_image, float width, float height) {
		try {
			PDXObjectImage img = new PDJpeg(tracker.doc, new FileInputStream(path_to_image));
			// tracker.content_stream.drawImage(img, tracker.xPos, tracker.yPos,
			// width, height);
			tracker.content_stream.drawXObject(img, tracker.xPos, tracker.yPos, width, height);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void moveRight(float pixels) {
		try {
			tracker.content_stream.moveTextPositionByAmount(tracker.xPos + pixels, tracker.yPos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
