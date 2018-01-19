package io.pmmp.pocketstorm.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PUBLIC)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Dirty2<A, B>{
	A first;
	B second;
}
