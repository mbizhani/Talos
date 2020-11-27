package org.devocative.talos.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;
import static org.devocative.talos.common.Util.nvl;

public class Tabular {
	private final String[] header;
	private final Integer[] width;
	private final List<String[]> rows = new ArrayList<>();

	// ------------------------------

	public Tabular(String... header) {
		this.header = header;

		width = new Integer[header.length];
		for (int i = 0; i < header.length; i++) {
			width[i] = header[i].length();
		}
	}

	// ------------------------------

	public void addRow(String... cells) {
		final String[] row = new String[header.length];

		if (cells.length == header.length) {
			for (int i = 0; i < cells.length; i++) {
				final String cell = nvl(cells[i], "");
				width[i] = max(width[i], cell.length());
				row[i] = cell;
			}
		} else {
			throw new RuntimeException("Invalid row size: " + Arrays.toString(cells));
		}

		rows.add(row);
	}

	public void print() {
		final StringBuilder builder = new StringBuilder();

		for (int i = 0; i < width.length - 1; i++) {
			builder.append("%-").append(width[i]).append("s  ");
		}
		builder.append("%s\n");

		final String format = builder.toString();
		System.out.printf(format, (Object[]) header);

		/*
		if (!rows.isEmpty()) {
			final String[] dummy = new String[header.length];
			for (int i = 0; i < header.length; i++) {
				dummy[i] = generate(width[i]);
			}
			rows.add(0, dummy);
		}
		*/

		for (String[] row : rows) {
			System.out.printf(format, (Object[]) row);
		}
	}

	// ------------------------------

	private String generate(int size) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < size; i++) {
			builder.append("-");
		}
		return builder.toString();
	}
}
