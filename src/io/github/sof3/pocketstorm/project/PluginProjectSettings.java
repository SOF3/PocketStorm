package io.github.sof3.pocketstorm.project;

import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import lombok.Builder;
import lombok.Value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.yaml.snakeyaml.Yaml;

@Value
@Builder(builderClassName = "Builder")
public class PluginProjectSettings{
	@NotNull String name; // configurable from generator peer
	@NotNull String namespace; // configurable from generator peer
	@NotNull String main; // configurable from generator peer
	@NotNull Set<String> api; // configurable from generator peer
	@Nullable Set<Integer> protocols = null;
	@NotNull Set<String> extensions = new HashSet<>();
	@NotNull String initialVersion; // configurable from generator peer
	@Nullable String description; // configurable from generator peer
	@NotNull List<String> authors; // configurable from generator peer
	@Nullable String website = null;
	@NotNull LoadOrder load = LoadOrder.POSTWORLD;

	public void dumpYaml(Writer os){
		Yaml yaml = new Yaml();
		Map<String, Object> data = new HashMap<>();
		data.put("name", name);
		data.put("main", namespace + "\\" + main);
		data.put("api", api.toArray());
		if(protocols != null){
			data.put("mcpe-protocol", protocols);
		}
		if(extensions.size() > 0){
			data.put("extensions", extensions);
		}
		data.put("version", initialVersion);
		if(description != null){
			data.put("description", description);
		}
		if(authors.size() > 1){
			data.put("authors", authors);
		}else if(authors.size() == 1){
			data.put("author", authors.get(0));
		}
		if(website != null){
			data.put("website", website);
		}
		if(load == LoadOrder.STARTUP){
			data.put("load", "STARTUP");
		}
		yaml.dump(data, os);
	}

	@Override
	public String toString(){
		StringWriter writer = new StringWriter();
		dumpYaml(writer);
		return writer.toString();
	}

	public enum LoadOrder{
		STARTUP,
		POSTWORLD
	}
}
