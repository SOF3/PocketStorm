package io.pmmp.pocketstorm.inspections.perm;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import lombok.Getter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.pmmp.pocketstorm.util.ExpiringMap;
import org.yaml.snakeyaml.Yaml;

public final class RegisteredPermissionCache{
	private static ExpiringMap<String, RegisteredPermissionCache> map = new ExpiringMap<>((long) 3600e+9);

	@Getter private final VirtualFile parent;
	@Getter @Nullable private final VirtualFile pluginYml;

	@Getter private Set<String> value;

	public static RegisteredPermissionCache getInstance(@NotNull VirtualFile parent){
		String hash = parent.getPath();
		if(map.contains(hash)){
			return map.get(hash);
		}
		RegisteredPermissionCache instance = new RegisteredPermissionCache(parent);
		map.put(hash, instance);
		return instance;
	}

	public static void refreshInstance(VirtualFile parent){
		String hash = parent.getPath();
		if(map.contains(hash)){
			RegisteredPermissionCache cache = map.get(hash);
			cache.refresh();
		}else{
			map.put(hash, new RegisteredPermissionCache(parent));
		}
	}

	private RegisteredPermissionCache(VirtualFile parent){
		this.parent = parent;
		pluginYml = parent.findChild("plugin.yml");
		refresh();
	}

	@SuppressWarnings("unchecked")
	public void refresh(){
		value = new HashSet<>();
		if(pluginYml == null){
			return;
		}
		ApplicationManager.getApplication().runReadAction(() -> {
			try(Reader reader = new InputStreamReader(pluginYml.getInputStream())){
				Yaml yaml = new Yaml();
				Object object = yaml.load(reader);
				if(!(object instanceof Map)){
					return;
				}
				Object permissions = ((Map) object).get("permissions");
				if(!(permissions instanceof Map)){
					return;
				}
				value.addAll(((Map<String, ?>) permissions).keySet());
			}catch(IOException e){
				e.printStackTrace();
			}
		});
	}

	public static Stream<RegisteredPermissionCache> all(){
		return map.valueStream();
	}
}
