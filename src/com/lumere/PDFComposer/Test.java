package com.lumere.PDFComposer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {
	public static void main(String[] args) {

		PDFComposer composer = new PDFComposer.Builder().build();

		composer.drawImage("img.jpg", composer.getXPos(), composer.getYPos(), 50, 50);
		composer.moveRight(50);
		composer.drawString("1111");
		composer.drawString("2222");

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

		// composer.drawTable(table_data, "Test Title", new String[] {
		// "Column 1", "Column 2", "Column 3", "Column 4" });

		table_data = new ArrayList<List<String>>();
		for (int i = 0; i < 7; i++) {
			List<String> data = new ArrayList<String>();
			data.add("one");
			data.add("two");
			data.add("three");
			data.add("four");
			table_data.add(data);
		}
		// composer.drawTable(table_data, "Test Title", new String[] {
		// "Column 1", "Column 2", "Column 3", "Column 4" });

		composer.saveDocument("testing.pdf");

	}
}
