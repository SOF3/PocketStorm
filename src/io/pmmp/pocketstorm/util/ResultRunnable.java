package io.pmmp.pocketstorm.util;

import lombok.Getter;

public abstract class ResultRunnable<R> implements Runnable{
	@Getter private R result = null;

	@Override
	public final void run(){
		result = execute();
	}

	protected abstract R execute();
}
