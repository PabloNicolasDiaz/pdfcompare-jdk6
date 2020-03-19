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
package de.redsix.junitextensions;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import lombok.val;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TempDirectoryExtension implements AfterEachCallback, ParameterResolver {

	private static final String KEY = "tempDirectory";

	@Override
	public boolean supportsParameter(ParameterContext paramContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		final Parameter parameter = paramContext.getParameter();
		return parameter.<TempDirectory>getAnnotation(TempDirectory.class) != null
				&& Path.class.equals(parameter.getType());
	}

	@Override
	public Object resolveParameter(ParameterContext paramContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		val parameter = paramContext.getParameter();
		val parentPath = parameter.<TempDirectory>getAnnotation(TempDirectory.class).parentPath();
		return getLocalStore(extensionContext).getOrComputeIfAbsent(KEY,
				key -> createTempDirectory(parentPath, extensionContext));
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		Path tempDirectory = (Path) getLocalStore(context).get(KEY);
		if (tempDirectory != null) {
			delete(tempDirectory);
		}
	}

	private ExtensionContext.Store getLocalStore(ExtensionContext context) {
		return context.getStore(localNamespace(context));
	}

	private Namespace localNamespace(ExtensionContext context) {
		return Namespace.create(TempDirectoryExtension.class, context);
	}

	private Path createTempDirectory(final String parentPath, ExtensionContext context) {
		try {
			val dirName = getDirName(context);
			if (parentPath.length() > 0) {
				return Files.createTempDirectory(Paths.get(parentPath), dirName);
			} else {
				return Files.createTempDirectory(dirName);
			}
		} catch (IOException e) {
			throw new ParameterResolutionException("Could not create temp directory", e);
		}
	}

	private String getDirName(ExtensionContext context) {
		if (context.getTestMethod().isPresent()) {
			return context.getTestMethod().get().getName();
		} else if (context.getTestClass().isPresent()) {
			return context.getTestClass().get().getName();
		} else {
			return context.getDisplayName();
		}
	}

	private void delete(Path tempDirectory) throws IOException {
		Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				return deleteAndContinue(file);
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return deleteAndContinue(dir);
			}

			private FileVisitResult deleteAndContinue(Path path) throws IOException {
				Files.delete(path);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}