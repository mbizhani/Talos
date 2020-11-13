package org.devocative.talos;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class Paraller<T> {
	private final List<WCallable> tasks = new ArrayList<>();
	private final int timeoutInSeconds;

	// ------------------------------

	public void addTask(String id, Callable<T> task) {
		tasks.add(new WCallable(id, task));
	}

	public void execute(BiConsumer<String, Optional<T>> response) {
		if (tasks.isEmpty()) {
			return;
		}

		final ExecutorService service = Executors.newFixedThreadPool(tasks.size());

		final List<Future<WResult>> futures;
		try {
			if (timeoutInSeconds > 0) {
				futures = service.invokeAll(tasks, timeoutInSeconds, TimeUnit.SECONDS);
			} else {
				futures = service.invokeAll(tasks);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		//TIP: it is necessary to call it or it waits forever!
		service.shutdown();

		for (int i = 0; i < futures.size(); i++) {
			final Future<WResult> future = futures.get(i);
			if (future.isCancelled()) {
				// TIP: due to calling invokeAll(), futures list are returned in order of submitted!
				response.accept(tasks.get(i).id, Optional.empty());
			} else {
				try {
					final WResult result = future.get();
					response.accept(result.id, Optional.of(result.result));
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ------------------------------

	@RequiredArgsConstructor
	private class WCallable implements Callable<WResult> {
		private final String id;
		private final Callable<T> callable;

		@Override
		public WResult call() throws Exception {
			final T result = callable.call();
			return new WResult(id, result);
		}
	}

	@AllArgsConstructor
	private class WResult {
		private String id;
		private T result;
	}
}
