package io.pmmp.pocketstorm;

import java.util.ArrayList;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import io.pmmp.pocketstorm.inspections.perm.RegisteredPermissionCache;
import io.pmmp.pocketstorm.pm.PocketSdkManager;
import io.pmmp.pocketstorm.util.MyFileUtil;

public class PocketProjectComponent implements ProjectComponent{
	private final Project project;

	public PocketProjectComponent(Project project){
		this.project = project;
	}

	@Override
	public void projectOpened(){
		VirtualFile base = project.getBaseDir();
		ApplicationManager.getApplication().runReadAction(() -> {
			MyFileUtil.findFile(new ArrayList<>(), base, "plugin.yml")
					.forEach(file -> RegisteredPermissionCache.getInstance(file.getParent()));
		});
		VirtualFileManager.getInstance().addVirtualFileListener(new PluginYmlWatcher());
	}
}
