package io.pmmp.pocketstorm.pm;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AsyncCache<T>{
	private final Supplier<T> supplier;

	private final List<Consumer<T>> consumers = new LinkedList<>();
	private volatile int state = 0;
	private T value;

	public AsyncCache(Supplier<T> supplier){
		this.supplier = supplier;
	}

	public synchronized void request(@Nullable Consumer<T> consumer){
		if(state == 0){
			if(consumer != null){
				consumers.add(consumer);
			}
			ApplicationManager.getApplication().executeOnPooledThread(() -> {
				value = supplier.get();
				synchronized(AsyncCache.this){
					state = 2;
					consumers.forEach(c -> c.accept(value));
				}
			});
			state = 1;
			return;
		}
		if(consumer == null){
			return;
		}
		if(state == 1){
			consumers.add(consumer);
			return;
		}
		assert state == 2;
		consumer.accept(value);
	}

	@NotNull
	public T getValue(){
		if(value == null){
			throw new IllegalStateException("Not ready yet");
		}
		return value;
	}
}
