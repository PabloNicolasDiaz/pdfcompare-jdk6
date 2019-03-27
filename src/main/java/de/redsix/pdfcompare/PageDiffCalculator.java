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

import org.apache.commons.lang3.ObjectUtils;

public class PageDiffCalculator {

	private final int totalPixels;
	private final double allowedDiffInPercent;
	private int diffsFound = 0;
	private int diffsFoundInExclusion = 0;
	private PageArea diffArea;

	public PageDiffCalculator(final int totalPixels, final double allowedDiffInPercent) {
		this.totalPixels = totalPixels;
		this.allowedDiffInPercent = allowedDiffInPercent;
	}

	public PageDiffCalculator(final boolean differencesFound, final boolean differencesFoundInExclusion) {
		totalPixels = 0;
		allowedDiffInPercent = 0;
		if (differencesFound)
			diffsFound = 1;
		if (differencesFoundInExclusion)
			diffsFoundInExclusion = 1;
	}

	public void diffFound() {
		++diffsFound;
	}

	public void diffFoundInExclusion() {
		++diffsFoundInExclusion;
	}

	public boolean differencesFound() {
		double allowedDiffInPixels = totalPixels == 0 ? 0 : totalPixels * allowedDiffInPercent / 100.0;
		return diffsFound > allowedDiffInPixels;
	}

	public boolean differencesFoundInExclusion() {
		return diffsFoundInExclusion > 0;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PageDiffCalculator)) {
			return false;
		}
		PageDiffCalculator that = (PageDiffCalculator) o;
		return diffsFound == that.diffsFound && diffsFoundInExclusion == that.diffsFoundInExclusion;
	}

	@SuppressWarnings("deprecation")
	@Override
	public int hashCode() {
		return ObjectUtils.hashCodeMulti(diffsFound, diffsFoundInExclusion);
	}

	public void addDiffArea(final PageArea diffArea) {
		this.diffArea = diffArea;
	}

	public PageArea getDiffArea() {
		return diffArea;
	}
}
