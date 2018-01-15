package io.github.sof3.pocketstorm;

public class SneakyException extends RuntimeException{
	public SneakyException(Throwable cause){
		super(cause);
	}

	public static SneakyException s(Throwable delegate){
		return new SneakyException(delegate);
	}
}
