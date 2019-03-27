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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.val;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);
	private static Collection<File> tempDirs = new ConcurrentLinkedQueue<File>();
	private static volatile boolean shutdownRegistered;
	private static File tempDirParent;

	public static void setTempDirParent(final File tempDirParentPath) {
		tempDirParent = tempDirParentPath;
	}

	private static synchronized void addShutdownHook() {
		if (!shutdownRegistered) {
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					removeTempDirs();
				}
			}));
			shutdownRegistered = true;
		}
	}

	private static void removeTempDirs() {
		for (val dir : tempDirs) {
			FileUtils.removeTempDir(dir);
		}
	}

	public static File createTempDir(final String prefix) throws IOException {
		val tempDir = createTempDir(tempDirParent, prefix);
		tempDirs.add(tempDir);
		addShutdownHook();
		return tempDir;
	}

	private static File createTempDir(final File tempDirParent, final String prefix) throws IOException {
		File x;
		if (tempDirParent != null) {
			x = File.createTempFile(prefix, "", tempDirParent);
		} else {
			x = File.createTempFile(prefix, "");
		}
		x.delete();
		x.mkdir();
		return x;
	}

	public static void removeTempDir(final File tempDir) {
		try {
			tempDirs.remove(tempDir);
			org.apache.commons.io.FileUtils.forceDelete(tempDir);
		} catch (IOException e) {
			LOG.warn("Error removing temporary directory: {}", tempDir, e);
		}
	}

	public static List<File> getPaths(final File dir, final String glob) throws IOException {
		List<File> paths = new ArrayList<File>();
		for (val path : org.apache.commons.io.FileUtils.listFiles(dir, new RegexFileFilter(glob),
				TrueFileFilter.TRUE)) {
			paths.add(path);
		}
		Collections.sort(paths);
		return paths;
	}
}
