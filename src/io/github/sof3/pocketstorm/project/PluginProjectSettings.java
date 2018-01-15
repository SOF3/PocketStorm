package io.github.sof3.pocketstorm.project;

import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
@Builder(builderClassName = "Builder")
public class PluginProjectSettings{
	@NotNull String name; // check
	@NotNull String namespace; // check
	@NotNull String main; // check
	@NotNull Set<String> api; // check
	@Nullable Set<Integer> protocols;
	@NotNull Set<String> extensions;
	@NotNull String initialVersion; // check
	@Nullable String description; // check
	@NotNull List<String> authors;
	@Nullable String website;
	@NotNull LoadOrder load;

	public void dumpYaml(Writer os){
		Yaml yaml = new Yaml();
		Map<String, Object> data = new HashMap<>();
		data.put("name", name);
		data.put("main", namespace + "\\" + main);
		data.put("api", api);
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
