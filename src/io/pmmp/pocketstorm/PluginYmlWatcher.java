package io.pmmp.pocketstorm;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileContentsChangedAdapter;
import org.jetbrains.annotations.NotNull;

import io.pmmp.pocketstorm.perm.RegisteredPermissionCache;

class PluginYmlWatcher extends VirtualFileContentsChangedAdapter{
	@Override
	protected void onFileChange(@NotNull VirtualFile file){
		if("plugin.yml".equals(file.getName())){
			RegisteredPermissionCache.refreshInstance(file.getParent());
		}
	}

	@Override
	protected void onBeforeFileChange(@NotNull VirtualFile file){

	}
}
