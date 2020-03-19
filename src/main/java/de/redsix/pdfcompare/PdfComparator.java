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
import static org.apache.commons.lang3.Validate.notNull;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import de.redsix.pdfcompare.env.DefaultEnvironment;
import de.redsix.pdfcompare.env.Environment;

@Slf4j
public class PdfComparator<T extends CompareResultImpl> {

	private static interface Supplier<T> {
		public T get();
	}

	public static final int DPI = 300;
	private static final int EXTRA_RGB = new Color(0, 160, 0).getRGB();
	private static final int MISSING_RGB = new Color(220, 0, 0).getRGB();
	public static final int MARKER_WIDTH = 20;
	private Environment environment;
	private Exclusions exclusions;
	private Supplier<InputStream> expectedStreamSupplier;
	private Supplier<InputStream> actualStreamSupplier;
	private ExecutorService drawExecutor;
	private ExecutorService parrallelDrawExecutor;
	private ExecutorService diffExecutor;
	private final T compareResult;
	private final int timeout = 3;
	private final TimeUnit unit = TimeUnit.MINUTES;
	private String expectedPassword = "";
	private String actualPassword = "";
	private boolean withIgnoreCalled = false;

	private PdfComparator(T compareResult) {
		notNull(compareResult, "compareResult is null");
		this.compareResult = compareResult;
		this.environment = DefaultEnvironment.create();
		this.exclusions = new Exclusions(environment);
	}

	@SuppressWarnings("unchecked")
	public PdfComparator(String expectedPdfFilename, String actualPdfFilename) throws IOException {
		this(expectedPdfFilename, actualPdfFilename, (T) new CompareResultImpl());
	}

	public PdfComparator(final String expectedPdfFilename, final String actualPdfFilename, T compareResult)
			throws IOException {
		this(compareResult);
		notNull(expectedPdfFilename, "expectedPdfFilename is null");
		notNull(actualPdfFilename, "actualPdfFilename is null");
		if (!expectedPdfFilename.equals(actualPdfFilename)) {
			this.expectedStreamSupplier = new Supplier<InputStream>() {
				@Override
				@SneakyThrows(FileNotFoundException.class)
				public InputStream get() {
					return new FileInputStream(new File(expectedPdfFilename));
				}
			};
			this.actualStreamSupplier = new Supplier<InputStream>() {
				@Override
				@SneakyThrows(FileNotFoundException.class)
				public InputStream get() {
					return new FileInputStream(new File(actualPdfFilename));
				}
			};
		}
	}

	@SuppressWarnings("unchecked")
	public PdfComparator(final File expectedFile, final File actualFile) throws IOException {
		this(expectedFile, actualFile, (T) new CompareResultImpl());
	}

	public PdfComparator(final File expectedFile, final File actualFile, final T compareResult) throws IOException {
		this(compareResult);
		notNull(expectedFile, "expectedFile is null");
		notNull(actualFile, "actualFile is null");
		if (!expectedFile.equals(actualFile)) {
			this.expectedStreamSupplier = new Supplier<InputStream>() {
				@Override
				@SneakyThrows(IOException.class)
				public InputStream get() {
					return new FileInputStream(expectedFile);
				}
			};
			this.actualStreamSupplier = new Supplier<InputStream>() {
				@Override
				@SneakyThrows(IOException.class)
				public InputStream get() {
					return new FileInputStream(actualFile);
				}
			};
		}
	}

	@SuppressWarnings("unchecked")
	public PdfComparator(final InputStream expectedPdfIS, final InputStream actualPdfIS) {
		this(expectedPdfIS, actualPdfIS, (T) new CompareResultImpl());
	}

	public PdfComparator(final InputStream expectedPdfIS, final InputStream actualPdfIS, final T compareResult) {
		this(compareResult);
		notNull(expectedPdfIS, "expectedPdfIS is null");
		notNull(actualPdfIS, "actualPdfIS is null");
		if (!expectedPdfIS.equals(actualPdfIS)) {
			this.expectedStreamSupplier = new Supplier<InputStream>() {
				@Override
				public InputStream get() {
					return expectedPdfIS;
				}
			};
			this.actualStreamSupplier = new Supplier<InputStream>() {
				@Override
				public InputStream get() {
					return actualPdfIS;
				}
			};
		}
	}

	/**
	 * @deprecated use {@link #withEnvironment(Environment)} instead.
	 */
	@Deprecated
	public void setEnvironment(Environment environment) {
		withEnvironment(environment);
	}

	/**
	 * Allows to inject an Environment that can override environment settings.
	 * {@link de.redsix.pdfcompare.env.SimpleEnvironment} is particularly useful if
	 * you want to override some properties.
	 *
	 * @param environment the environment so use
	 * @return this
	 * @throws IllegalStateException when withIgnore methods are called before this
	 * method.
	 */
	public PdfComparator<T> withEnvironment(Environment environment) {
		if (withIgnoreCalled) {
			throw new IllegalStateException(
					"withEnvironment(...) must be called before any withIgnore(...) methods are called.");
		}
		this.environment = environment;
		return this;
	}

	/**
	 * Reads a file with Exclusions.
	 *
	 * @param ignoreFilename The file to read
	 * @return this
	 * @see PdfComparator#withIgnore(InputStream)
	 */
	public PdfComparator<T> withIgnore(final String ignoreFilename) {
		notNull(ignoreFilename, "ignoreFilename is null");
		exclusions.readExclusions(ignoreFilename);
		return this;
	}

	/**
	 * Reads a file with Exclusions.
	 *
	 * @param ignoreFile The file to read
	 * @return this
	 * @see PdfComparator#withIgnore(InputStream)
	 */
	public PdfComparator<T> withIgnore(final File ignoreFile) {
		notNull(ignoreFile, "ignoreFile is null");
		withIgnoreCalled = true;
		exclusions.readExclusions(ignoreFile);
		return this;
	}

	/**
	 * Reads a file with Exclusions.
	 *
	 * It is possible to define rectangular areas that are ignored during
	 * comparison. For that, a file needs to be created, which defines areas to
	 * ignore. The file format is JSON (or actually a superset called
	 * <a href="https://github.com/lightbend/config/blob/master/HOCON.md">HOCON</a>)
	 * and has the following form:
	 *
	 * <pre>
	 * exclusions: [
	 *     {
	 *         page: 2
	 *         x1: 300 // entries without a unit are in pixels, when Pdf is rendered at 300DPI
	 *         y1: 1000
	 *         x2: 550
	 *         y2: 1300
	 *     },
	 *     {
	 *         // page is optional. When not given, the exclusion applies to all pages.
	 *         x1: 130.5mm // entries can also be given in units of cm, mm or pt (DTP-Point defined as 1/72 Inches)
	 *         y1: 3.3cm
	 *         x2: 190mm
	 *         y2: 3.7cm
	 *     },
	 *     {
	 *         page: 7
	 *         // coordinates are optional. When not given, the whole page is excluded.
	 *     }
	 * ]
	 * </pre>
	 *
	 * @param ignoreIS The inputStream to read
	 * @return this
	 */
	public PdfComparator<T> withIgnore(final InputStream ignoreIS) {
		notNull(ignoreIS, "ignoreIS is null");
		exclusions.readExclusions(ignoreIS);
		return this;
	}

	/**
	 * Allows to specify an area of a page that is excluded during the comparison.
	 *
	 * @param exclusion An area of the document, that shall be ignored.
	 * @return this
	 */
	public PdfComparator<T> withIgnore(final PageArea exclusion) {
		notNull(exclusion, "exclusion is null");
		withIgnoreCalled = true;
		exclusions.add(exclusion);
		return this;
	}

	/**
	 * Allows to specify an area of a page that is excluded during the comparison.
	 *
	 * @deprecated Use {@link PdfComparator#withIgnore(PageArea)} instead.
	 * @param exclusion An area of the document, that shall be ignored.
	 * @return this
	 */
	public PdfComparator<T> with(final PageArea exclusion) {
		notNull(exclusion, "exclusion is null");
		exclusions.add(exclusion);
		return this;
	}

	public PdfComparator<T> withExpectedPassword(final String password) {
		notNull(password, "password is null");
		expectedPassword = password;
		return this;
	}

	public PdfComparator<T> withActualPassword(final String password) {
		notNull(password, "password is null");
		actualPassword = password;
		return this;
	}

	private void buildEnvironment() {
		compareResult.setEnvironment(environment);
		drawExecutor = blockingExecutor("Draw", 1, 50, environment);
		parrallelDrawExecutor = blockingExecutor("ParallelDraw", 2, 4, environment);
		diffExecutor = blockingExecutor("Diff", 1, 2, environment);
	}

	public T compare() throws IOException {
		try {
			if (expectedStreamSupplier == null || actualStreamSupplier == null) {
				return compareResult;
			}
			buildEnvironment();
			try {
				@Cleanup
				val expectedStream = expectedStreamSupplier.get();
				try {
					@Cleanup
					val actualStream = actualStreamSupplier.get();
					@Cleanup
					val expectedDocument = PDDocument.load(expectedStream, expectedPassword,
							Utilities.getMemorySettings(environment.getDocumentCacheSize()));
					@Cleanup
					val actualDocument = PDDocument.load(actualStream, actualPassword,
							Utilities.getMemorySettings(environment.getDocumentCacheSize()));
					compare(expectedDocument, actualDocument);

				} catch (IOException ex) {
					addSingleDocumentToResult(expectedStream, MISSING_RGB);
					compareResult.expectedOnly();
				}
			} catch (IOException ex) {
				try {
					@Cleanup
					val actualStream = actualStreamSupplier.get();
					addSingleDocumentToResult(actualStream, EXTRA_RGB);
					compareResult.actualOnly();
				} catch (IOException innerEx) {
					// log.warn("No files found to compare. Tried Expected: '{}' and Actual: '{}'",
					// ex.getFile(),
					// innerEx.getFile());
					compareResult.noPagesFound();
				}
			}
		} finally {
			compareResult.done();
		}
		return compareResult;
	}

	private void compare(final PDDocument expectedDocument, final PDDocument actualDocument) throws IOException {
		expectedDocument.setResourceCache(new ResourceCacheWithLimitedImages(environment));
		val expectedPdfRenderer = new PDFRenderer(expectedDocument);
		actualDocument.setResourceCache(new ResourceCacheWithLimitedImages(environment));
		val actualPdfRenderer = new PDFRenderer(actualDocument);
		val minPageCount = Math.min(expectedDocument.getNumberOfPages(), actualDocument.getNumberOfPages());
		val latch = new CountDownLatch(minPageCount);
		for (int pageIndex = 0; pageIndex < minPageCount; pageIndex++) {
			drawImage(latch, pageIndex, expectedDocument, actualDocument, expectedPdfRenderer, actualPdfRenderer);
		}
		Utilities.await(latch, "FullCompare", environment);
		Utilities.shutdownAndAwaitTermination(drawExecutor, "Draw");
		Utilities.shutdownAndAwaitTermination(parrallelDrawExecutor, "Parallel Draw");
		Utilities.shutdownAndAwaitTermination(diffExecutor, "Diff");
		if (expectedDocument.getNumberOfPages() > minPageCount) {
			addExtraPages(expectedDocument, expectedPdfRenderer, minPageCount, MISSING_RGB, true);
		} else if (actualDocument.getNumberOfPages() > minPageCount) {
			addExtraPages(actualDocument, actualPdfRenderer, minPageCount, EXTRA_RGB, false);
		}
	}

	private void drawImage(final CountDownLatch latch, final int pageIndex, final PDDocument expectedDocument,
			final PDDocument actualDocument, final PDFRenderer expectedPdfRenderer,
			final PDFRenderer actualPdfRenderer) {
		drawExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					log.trace("Drawing page {}", pageIndex);
					val expectedImageFuture = parrallelDrawExecutor.submit(new Callable<ImageWithDimension>() {
						@Override
						public ImageWithDimension call() throws Exception {
							return renderPageAsImage(expectedDocument, expectedPdfRenderer, pageIndex, environment);
						}
					});
					val actualImageFuture = parrallelDrawExecutor.submit(new Callable<ImageWithDimension>() {
						@Override
						public ImageWithDimension call() throws Exception {
							return renderPageAsImage(actualDocument, actualPdfRenderer, pageIndex, environment);
						}
					});
					val expectedImage = getImage(expectedImageFuture, pageIndex, "expected document");
					val actualImage = getImage(actualImageFuture, pageIndex, "actual document");
					val diffImage = new DiffImage(expectedImage, actualImage, pageIndex, environment, exclusions,
							compareResult);
					log.trace("Enqueueing page {}.", pageIndex);
					diffExecutor.execute(new Runnable() {
						@Override
						public void run() {
							log.trace("Diffing page {}", diffImage);
							diffImage.diffImages();
							log.trace("DONE Diffing page {}", diffImage);
						}
					});
					log.trace("DONE drawing page {}", pageIndex);
				} catch (RenderingException e) {
				} finally {
					latch.countDown();
				}
			}
		});
	}

	private ImageWithDimension getImage(final Future<ImageWithDimension> imageFuture, final int pageIndex,
			final String type) {
		try {
			return imageFuture.get(timeout, unit);
		} catch (InterruptedException e) {
			log.warn("Waiting for Future was interrupted while rendering page {} for {}", pageIndex, type, e);
			Thread.currentThread().interrupt();
		} catch (TimeoutException e) {
			log.error("Waiting for Future timed out after {} {} while rendering page {} for {}", timeout, unit,
					pageIndex, type, e);
		} catch (ExecutionException e) {
			log.error("Error while rendering page {} for {}", pageIndex, type, e);
		}
		throw new RenderingException();
	}

	private void addSingleDocumentToResult(InputStream expectedPdfIS, int markerColor) throws IOException {
		@Cleanup
		val expectedDocument = PDDocument.load(expectedPdfIS);
		val expectedPdfRenderer = new PDFRenderer(expectedDocument);
		addExtraPages(expectedDocument, expectedPdfRenderer, 0, markerColor, true);
	}

	private void addExtraPages(final PDDocument document, final PDFRenderer pdfRenderer, final int minPageCount,
			final int color, final boolean expected) throws IOException {
		for (int pageIndex = minPageCount; pageIndex < document.getNumberOfPages(); pageIndex++) {
			val image = renderPageAsImage(document, pdfRenderer, pageIndex, environment);
			val dataBuffer = image.bufferedImage.getRaster().getDataBuffer();
			for (int i = 0; i < image.bufferedImage.getWidth() * MARKER_WIDTH; i++) {
				dataBuffer.setElem(i, color);
			}
			for (int i = 0; i < image.bufferedImage.getHeight(); i++) {
				for (int j = 0; j < MARKER_WIDTH; j++) {
					dataBuffer.setElem(i * image.bufferedImage.getWidth() + j, color);
				}
			}
			if (expected) {
				compareResult.addPage(new PageDiffCalculator(true, false), pageIndex, image, blank(image), image);
			} else {
				compareResult.addPage(new PageDiffCalculator(true, false), pageIndex, blank(image), image, image);
			}
		}
	}

	private static ImageWithDimension blank(final ImageWithDimension image) {
		return new ImageWithDimension(new BufferedImage(image.bufferedImage.getWidth(), image.bufferedImage.getHeight(),
				image.bufferedImage.getType()), image.width, image.height);
	}

	public static ImageWithDimension renderPageAsImage(final PDDocument document, final PDFRenderer expectedPdfRenderer,
			final int pageIndex, final Environment environment) throws IOException {
		val bufferedImage = expectedPdfRenderer.renderImageWithDPI(pageIndex, environment.getDPI());
		val page = document.getPage(pageIndex);
		val mediaBox = page.getMediaBox();
		if (page.getRotation() == 90 || page.getRotation() == 270)
			return new ImageWithDimension(bufferedImage, mediaBox.getHeight(), mediaBox.getWidth());
		else
			return new ImageWithDimension(bufferedImage, mediaBox.getWidth(), mediaBox.getHeight());
	}

	public T getResult() {
		return compareResult;
	}
}
