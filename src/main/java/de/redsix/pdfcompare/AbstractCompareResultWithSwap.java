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

import static de.redsix.pdfcompare.Utilities.blockingExecutor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import lombok.Cleanup;
import lombok.val;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.redsix.pdfcompare.env.Environment;

/**
 * This CompareResult monitors the memory the JVM consumes through
 * Runtime.totalMemory() - Runtime.freeMemory() when a new page is added. When
 * the consumed memory crosses a threshold, images are swapped to disk and
 * removed from memory. The threshold defaults to 70% of Runtime.maxMemory() but
 * at least 200MB, which worked for me. After swapping, a System.gc() is
 * triggered.
 */
public abstract class AbstractCompareResultWithSwap extends CompareResultImpl {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractCompareResultWithSwap.class);
	private File tempDir;
	private boolean hasImages = false;
	private boolean swapped;
	private ExecutorService swapExecutor;

	@Override
	public boolean writeTo(final String filename) {
		if (!swapped) {
			return super.writeTo(filename);
		}
		val mergerUtility = new PDFMergerUtility();
		mergerUtility.setDestinationFileName(filename + ".pdf");
		return writeTo(mergerUtility);
	}

	@Override
	public boolean writeTo(final OutputStream outputStream) {
		if (!swapped) {
			return super.writeTo(outputStream);
		}
		final PDFMergerUtility mergerUtility = new PDFMergerUtility();
		mergerUtility.setDestinationStream(outputStream);
		return writeTo(mergerUtility);
	}

	private boolean writeTo(final PDFMergerUtility mergerUtility) {
		swapToDisk();
		Utilities.shutdownAndAwaitTermination(swapExecutor, "Swap");
		try {
			LOG.trace("Merging...");
			val start = Instant.now();
			for (val path : FileUtils.getPaths(getTempDir(), "partial_*")) {
				mergerUtility.addSource(path);
			}
			mergerUtility.mergeDocuments(Utilities.getMemorySettings(environment.getMergeCacheSize()));
			val end = Instant.now();
			LOG.trace("Merging took: " + new Duration(start, end).getMillis() + "ms");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (tempDir != null) {
				FileUtils.removeTempDir(tempDir);
			}
		}
		return isEqual;
	}

	@Override
	public synchronized void addPage(final PageDiffCalculator diffCalculator, final int pageIndex,
			final ImageWithDimension expectedImage, final ImageWithDimension actualImage,
			final ImageWithDimension diffImage) {
		super.addPage(diffCalculator, pageIndex, expectedImage, actualImage, diffImage);
		hasImages = true;
		if (needToSwap()) {
			swapToDisk();
			afterSwap();
		}
	}

	protected void afterSwap() {
	}

	protected abstract boolean needToSwap();

	private synchronized Executor getExecutor(Environment environment) {
		if (swapExecutor == null) {
			swapExecutor = blockingExecutor("Swap", 0, 2, 1, environment);
		}
		return swapExecutor;
	}

	private synchronized void swapToDisk() {
		if (!diffImages.isEmpty()) {
			val images = new TreeMap<Integer, ImageWithDimension>();
			val iterator = diffImages.entrySet().iterator();
			int previousPage = IterableUtils.get(diffImages.keySet(), 0).intValue();
			while (iterator.hasNext()) {
				val entry = iterator.next();
				if (entry.getKey() <= previousPage + 1) {
					images.put(entry.getKey(), entry.getValue());
					iterator.remove();
					previousPage = entry.getKey();
				}
			}
			if (!images.isEmpty()) {
				swapped = true;
				getExecutor(environment).execute(new Runnable() {
					@Override
					public void run() {
						LOG.trace("Swapping {} pages to disk", images.size());
						Instant start = Instant.now();

						final int minPageIndex = images.keySet().iterator().next();
						LOG.trace("minPageIndex: {}", minPageIndex);
						try {
							@Cleanup
							val document = new PDDocument(Utilities.getMemorySettings(environment.getSwapCacheSize()));
							document.setResourceCache(new ResourceCacheWithLimitedImages(environment));
							addImagesToDocument(document, images);
							val tempDir = getTempDir();
							val tempFile = tempDir
									.list(new NameFileFilter(String.format("partial_%06d.pdf", minPageIndex)));
							document.save(new File(tempFile[0]));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						Instant end = Instant.now();
						LOG.trace("Swapping took: {}ms", new Duration(start, end).getMillis());
					}
				});
			}
		}
	}

	@Override
	protected synchronized boolean hasImages() {
		return hasImages;
	}

	private synchronized File getTempDir() throws IOException {
		if (tempDir == null) {
			tempDir = FileUtils.createTempDir("PdfCompare");
		}
		return tempDir;
	}

	@Override
	protected void finalize() throws Throwable {
		if (swapExecutor != null) {
			swapExecutor.shutdown();
		}
	}
}
