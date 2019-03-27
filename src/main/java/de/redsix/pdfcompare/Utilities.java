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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Cleanup;
import lombok.val;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.redsix.pdfcompare.env.Environment;

public class Utilities {

	private static final Logger LOG = LoggerFactory.getLogger(Utilities.class);

	public static MemoryUsageSetting getMemorySettings(final int bytes) throws IOException {
		return MemoryUsageSetting.setupMixed(bytes).setTempDir(FileUtils.createTempDir("PdfBox"));
	}

	static class NamedThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		NamedThreadFactory(final String name) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = name + "-" + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement());
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}

	public static ExecutorService blockingExecutor(final String name, int coreThreads, int maxThreads,
			int queueCapacity, Environment environment) {
		if (environment.useParallelProcessing()) {
			return new ThreadPoolExecutor(coreThreads, maxThreads, 3, TimeUnit.MINUTES,
					new LinkedBlockingQueue<Runnable>(queueCapacity), new NamedThreadFactory(name),
					new BlockingHandler());
		} else {
			return new InThreadExecutorService();
		}
	}

	public static ExecutorService blockingExecutor(final String name, int threads, int queueCapacity,
			Environment environment) {
		if (environment.useParallelProcessing()) {
			return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MINUTES,
					new LinkedBlockingQueue<Runnable>(queueCapacity), new NamedThreadFactory(name),
					new BlockingHandler());
		} else {
			return new InThreadExecutorService();
		}
	}

	public static void shutdownAndAwaitTermination(final ExecutorService executor, final String executorName) {
		if (executor != null) {
			executor.shutdown();
			try {
				final int timeout = 15;
				final TimeUnit unit = TimeUnit.MINUTES;
				if (!executor.awaitTermination(timeout, unit)) {
					LOG.error("Awaiting Shutdown of Executor '{}' timed out after {} {}", executorName, timeout, unit);
				}
			} catch (InterruptedException e) {
				LOG.warn("Awaiting Shutdown of Executor '{}' was interrupted", executorName);
				Thread.currentThread().interrupt();
			}
		}
	}

	public static void await(final CountDownLatch latch, final String latchName, Environment environment) {
		try {
			final int timeout = environment.getOverallTimeout();
			final TimeUnit unit = TimeUnit.MINUTES;
			if (!latch.await(timeout, unit)) {
				LOG.error("Awaiting Latch '{}' timed out after {} {}", latchName, timeout, unit);
			}
		} catch (InterruptedException e) {
			LOG.warn("Awaiting Latch '{}' was interrupted", latchName);
			Thread.currentThread().interrupt();
		}
	}

	public static int getNumberOfPages(final File document, Environment environment) throws IOException {
		@Cleanup
		val documentIS = new FileInputStream(document);
		return getNumberOfPages(documentIS, environment);

	}

	private static int getNumberOfPages(final InputStream documentIS, Environment environment) throws IOException {
		@Cleanup
		val pdDocument = PDDocument.load(documentIS, Utilities.getMemorySettings(environment.getDocumentCacheSize()));
		return pdDocument.getNumberOfPages();
	}

	public static ImageWithDimension renderPage(final File document, final int page, Environment environment)
			throws IOException {
		@Cleanup
		val documentIS = new FileInputStream(document);
		return renderPage(documentIS, page, environment);
	}

	public static ImageWithDimension renderPage(final InputStream documentIS, final int page, Environment environment)
			throws IOException {
		@Cleanup
		val pdDocument = PDDocument.load(documentIS, Utilities.getMemorySettings(environment.getDocumentCacheSize()));
		if (page >= pdDocument.getNumberOfPages()) {
			throw new IllegalArgumentException("Page out of range. Last page is: " + pdDocument.getNumberOfPages());
		}
		pdDocument.setResourceCache(new ResourceCacheWithLimitedImages(environment));
		PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
		return PdfComparator.renderPageAsImage(pdDocument, pdfRenderer, page);
	}
}
