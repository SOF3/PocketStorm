package io.pmmp.pocketstorm.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import org.jetbrains.annotations.NotNull;

public class ExpiringMap<K, V>{
	@Value
	private static class Container<V>{
		long expiryTime;
		V value;

		boolean expired(){
			return expiryTime < System.nanoTime();
		}
	}

	private long nextExpiry = 0;

	private Map<K, Container<V>> internal = new HashMap<>();

	@Getter @Setter private long expiryLength; // in nanos

	public ExpiringMap(long expiryNanos){
		this.expiryLength = expiryNanos;
	}

	public void put(@NotNull K key, @NotNull V value){
		clearCache();
		long expiryAt = System.nanoTime() + expiryLength;
		if(nextExpiry > expiryAt){
			nextExpiry = expiryAt;
		}
		internal.put(key, new Container<>(expiryAt, value));
	}

	public boolean contains(K key){
		clearCache();
		return internal.containsKey(key);
	}

	public V get(K key){
		clearCache();
		if(internal.containsKey(key)){
			return internal.get(key).value;
		}
		return null;
	}

	public V remove(K key){
		clearCache();
		if(internal.containsKey(key)){
			Container<V> c = internal.remove(key);
			// nextExpiry will be recalculated eventually, no need to renew immediately in case remove() is called frequently.
//			if(nextExpiry == c.expiryTime){
//				renewNextExpiry();
//			}
			return c.value;
		}
		return null;
	}

	public Stream<V> valueStream(){
		return internal.values().stream().map(Container::getValue);
	}


	private void clearCache(){
		if(nextExpiry > System.nanoTime()){
			return;
		}
		internal.values().removeIf(Container::expired);
		renewNextExpiry();
	}

	private void renewNextExpiry(){
		nextExpiry = internal.values().stream().mapToLong(c -> c.expiryTime).min().orElse(Long.MAX_VALUE);
	}
}
