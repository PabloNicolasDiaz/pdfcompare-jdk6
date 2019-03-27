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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InThreadExecutorService implements ExecutorService {

	private boolean shutdown = false;

	@Override
	public void shutdown() {
		shutdown = true;
	}

	@Override
	public List<Runnable> shutdownNow() {
		shutdown = true;
		return Collections.emptyList();
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

	@Override
	public boolean isTerminated() {
		return shutdown;
	}

	@Override
	public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
		return true;
	}

	@Override
	public <T> Future<T> submit(final Callable<T> task) {
		try {
			return new ImmediateFuture<T>(task.call());
		} catch (Exception e) {
			return new ImmediateFuture<T>(e);
		}
	}

	@Override
	public <T> Future<T> submit(final Runnable task, final T result) {
		try {
			task.run();
		} catch (Exception e) {
			return new ImmediateFuture<T>(e);
		}
		return new ImmediateFuture<T>(result);
	}

	@Override
	public Future<?> submit(final Runnable task) {
		return submit(task, null);
	}

	@Override
	public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
		List<Future<T>> result = new ArrayList<Future<T>>();
		for (Callable<T> task : tasks) {
			result.add(submit(task));
		}
		return result;
	}

	@Override
	public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout,
			final TimeUnit unit) throws InterruptedException {
		return invokeAll(tasks);
	}

	@Override
	public <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return null;
	}

	@Override
	public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return invokeAny(tasks);
	}

	@Override
	public void execute(final Runnable command) {
		command.run();
	}

	/* package for Testing */ static class ImmediateFuture<T> implements Future<T> {

		private final T result;
		private final Exception exception;

		public ImmediateFuture(final T result) {
			this.result = result;
			this.exception = null;
		}

		public ImmediateFuture(final Exception exeption) {
			this.result = null;
			this.exception = exeption;
		}

		@Override
		public boolean cancel(final boolean mayInterruptIfRunning) {
			return true;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return true;
		}

		@Override
		public T get() throws InterruptedException, ExecutionException {
			if (result != null) {
				return result;
			} else {
				throw new ExecutionException(exception);
			}
		}

		@Override
		public T get(final long timeout, final TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return get();
		}
	}
}
