package io.github.sof3.pocketstorm.pm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.jetbrains.annotations.NotNull;

import com.google.gson.stream.JsonReader;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ApiVersion implements Comparable<ApiVersion>{
	private final int number;
	private final String name;
	private List<String> description;
	private Set<String> php;
	private boolean incompatible;
	private boolean indev;

	public static ApiVersion fromJson(int number, String name, JsonReader reader) throws IOException{
		ApiVersion instance = new ApiVersion(number, name);
		reader.beginObject();
		while(reader.hasNext()){
			switch(reader.nextName()){
				case "description":
					instance.description = new ArrayList<>();
					reader.beginArray();
					while(reader.hasNext()){
						instance.description.add(reader.nextString());
					}
					reader.endArray();
					break;
				case "php":
					instance.php = new HashSet<>(1);
					reader.beginArray();
					while(reader.hasNext()){
						instance.php.add(reader.nextString());
					}
					reader.endArray();
					break;
				case "incompatible":
					instance.incompatible = reader.nextBoolean();
					break;
				case "indev":
					instance.indev = reader.nextBoolean();
					break;
			}
		}
		reader.endObject();
		return instance;
	}

	@Override
	public int compareTo(@NotNull ApiVersion o){
		return Integer.compare(number, o.number);
	}
}
