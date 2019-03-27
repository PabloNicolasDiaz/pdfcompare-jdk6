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
 * This CompareResult monitors the memory the JVM consumes through
 * Runtime.totalMemory() - Runtime.freeMemory() when a new page is added. When
 * the consumed memory crosses a threshold, images are swapped to disk and
 * removed from memory. The threshold defaults to 70% of Runtime.maxMemory() but
 * at least 200MB, which worked for me. After swapping, a System.gc() is
 * triggered.
 */
public class CompareResultWithMemoryOverflow extends AbstractCompareResultWithSwap {

	private long maxMemoryUsage = Math.min(Math.round(Runtime.getRuntime().maxMemory() * 0.7),
			Runtime.getRuntime().maxMemory() - 200 * 1024 * 1024);

	/**
	 * Defaults to 70% of the available maxMemory reported by the JVM.
	 */
	public CompareResultWithMemoryOverflow() {
	}

	/**
	 * Stores images to disk, when the used memory is higher than the given theshold
	 * in megabytes.
	 *
	 * @param approximateMaxMemoryUsageInMegaBytes the maximum memory to use in
	 * megabytes
	 */
	public CompareResultWithMemoryOverflow(final int approximateMaxMemoryUsageInMegaBytes) {
		this.maxMemoryUsage = approximateMaxMemoryUsageInMegaBytes * 1024 * 1024;
	}

	@Override
	protected boolean needToSwap() {
		long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		return usedMemory >= maxMemoryUsage;
	}

	@Override
	protected void afterSwap() {
		System.gc();
	}
}
