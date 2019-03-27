/*
 * Copyright 2016 Malte Finsterwalder
 * Copyright [2018] Pablo Nicolas Diaz Bilotto [https://github.com/PabloNicolasDiaz/]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.redsix.pdfcompare;

public class PageArea {

	final int page;
	private final int x1;
	private final int y1;
	private final int x2;
	private final int y2;

	public PageArea(final int page) {
		this.page = page;
		this.x1 = -1;
		this.y1 = -1;
		this.x2 = -1;
		this.y2 = -1;
	}

	public PageArea(final int x1, final int y1, final int x2, final int y2) {
		checkCoordinates(x1, y1, x2, y2);
		this.page = -1;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public PageArea(final int page, final int x1, final int y1, final int x2, final int y2) {
		checkCoordinates(x1, y1, x2, y2);
		if (page < 1) {
			throw new IllegalArgumentException("Page has to be greater or equal to 1");
		}
		this.page = page;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	private void checkCoordinates(final int x1, final int y1, final int x2, final int y2) {
		if (x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0) {
			throw new IllegalArgumentException("Coordinates have to be greater than 0");
		}
		if (x1 > x2 || y1 > y2) {
			throw new IllegalArgumentException(
					"x1 has to be smaller or equal to x2 and y1 has to be smaller or equal to y2");
		}
	}

	public boolean contains(int x, int y) {
		if (x1 == -1 && y1 == -1 && x2 == -1 && y2 == -1) {
			return true;
		}
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
	}

	public int getPage() {
		return page;
	}

	public int getX1() {
		return x1;
	}

	public int getY1() {
		return y1;
	}

	public int getX2() {
		return x2;
	}

	public int getY2() {
		return y2;
	}
}
