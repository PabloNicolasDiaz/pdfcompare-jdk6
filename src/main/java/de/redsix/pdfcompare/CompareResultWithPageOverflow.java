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

/**
 * This CompareResult monitors the number of pages in the result. When a
 * threshold is reached, the pages are swapped to disk.
 */
public class CompareResultWithPageOverflow extends AbstractCompareResultWithSwap {

	private final int maxPages;

	/**
	 * Defaults to swap to disk, when more than 10 pages are stored.
	 */
	public CompareResultWithPageOverflow() {
		this.maxPages = 10;
	}

	/**
	 * Swaps to disk, when more than the given pages are in memory.
	 *
	 * @param maxPages the maximum amount of pages to keep in memory
	 */
	public CompareResultWithPageOverflow(final int maxPages) {
		this.maxPages = maxPages;
	}

	@Override
	protected synchronized boolean needToSwap() {
		return diffImages.size() >= maxPages;
	}
}
