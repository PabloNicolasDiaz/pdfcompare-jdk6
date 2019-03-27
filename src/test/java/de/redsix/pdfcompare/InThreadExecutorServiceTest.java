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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import lombok.val;

import org.junit.jupiter.api.Test;

import de.redsix.pdfcompare.InThreadExecutorService.ImmediateFuture;

public class InThreadExecutorServiceTest {

	@Test
	public void run() throws ExecutionException, InterruptedException {
		final Future<String> future = new InThreadExecutorService().submit(() -> "Test");
		assertThat(future.get(), is("Test"));
	}

	@Test
	public void immediateFutureWithResult() throws ExecutionException, InterruptedException {
		val future = new ImmediateFuture<>("Test");
		assertThat(future.get(), is("Test"));
	}

	@Test
	public void immediateFutureWithException() throws ExecutionException, InterruptedException {
		val future = new ImmediateFuture<Object>(new Exception());
		assertThrows(Exception.class, () -> future.get());
	}
}
