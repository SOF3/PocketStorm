package io.pmmp.pocketstorm.pm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.stream.JsonReader;
import org.apache.http.client.fluent.Request;

import static io.pmmp.pocketstorm.MyUtil.s;

public final class PocketMine{
	public final static Pattern VALID_PLUGIN_NAME = Pattern.compile("^[A-Za-z0-9_.-]+$");
	public final static String[] PLUGIN_NAME_RESTRICTED_SUBSTRINGS = new String[]{
			"pocketmine",
			"minecraft",
			"mojang"
	};
	public final static Pattern VALID_IDENTIFIER_NAME = Pattern.compile("^[A-za-z_][A-Za-z0-9_]*$");

	public final static Cache<Map<String, ApiVersion>> apiList = new Cache<>(() -> {
		try(
				InputStream is = Request.Get("https://poggit.pmmp.io/pmapis").execute().returnContent().asStream();
				JsonReader reader = new JsonReader(new InputStreamReader(is))
		){
			reader.beginObject();
			int i = 0;
			Map<String, ApiVersion> versions = new LinkedHashMap<>();
			while(reader.hasNext()){
				String name = reader.nextName();
				versions.put(name, ApiVersion.fromJson(i++, name, reader));
			}
			reader.endObject();
			reader.close();
			return versions;
		}catch(IOException e){
			throw s(e);
		}
	});

	public static void clonePocketMine(){
		// TODO implement
	}
}
