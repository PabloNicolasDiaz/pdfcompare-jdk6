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
package de.redsix.pdfcompare.ui;

import java.awt.image.BufferedImage;

import de.redsix.pdfcompare.CompareResultWithExpectedAndActual;

public class ViewModel {

	private final CompareResultWithExpectedAndActual result;
	private int pageToShow = 0;
	private boolean showExpected = true;
	private final int maxPages;

	public ViewModel(final CompareResultWithExpectedAndActual compareResult) {
		this.maxPages = compareResult.getNumberOfPages();
		this.result = compareResult;
	}

	public int getPageToShow() {
		return pageToShow;
	}

	public boolean isShowExpected() {
		return showExpected;
	}

	public void showExpected() {
		showExpected = true;
	}

	public void showActual() {
		showExpected = false;
	}

	public boolean decreasePage() {
		if (pageToShow > 0) {
			--pageToShow;
			return true;
		}
		return false;
	}

	public boolean increasePage() {
		if (pageToShow < maxPages) {
			++pageToShow;
			return true;
		}
		return false;
	}

	public BufferedImage getLeftImage() {
		if (isShowExpected()) {
			return result.getExpectedImage(getPageToShow());
		} else {
			return result.getActualImage(getPageToShow());
		}
	}

	public BufferedImage getDiffImage() {
		return result.getDiffImage(getPageToShow());
	}
}
