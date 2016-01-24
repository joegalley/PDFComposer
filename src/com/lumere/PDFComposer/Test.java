package com.lumere.PDFComposer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {
	public static void main(String[] args) {

		PDFComposer composer = new PDFComposer.Builder().build();
		composer.drawString("1111111111");
		composer.drawString("222222222");

		List<List<String>> table_data;

		table_data = new ArrayList<List<String>>();
		for (int i = 0; i < 7; i++) {
			List<String> data = new ArrayList<String>();
			data.add("one");
			data.add("two");
			data.add("three");
			data.add("four");
			table_data.add(data);
		}

		composer.drawTable(table_data, "Test Title", new String[] { "Column 1", "Column 2", "Column 3",
				"Column 4" });
		
		composer.drawBlankLines(30);
		table_data = new ArrayList<List<String>>();
		for (int i = 0; i < 7; i++) {
			List<String> data = new ArrayList<String>();
			data.add("one");
			data.add("two");
			data.add("three");
			data.add("four");
			table_data.add(data);
		}

		composer.drawTable(table_data, "Test Title", new String[] { "Column 1", "Column 2", "Column 3",
				"Column 4" });
		
		composer.drawImage("img.jpg", 20, 200);
		composer.saveDocument("testing.pdf");


	}
}
