package io.pmmp.pocketstorm;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;

public class PocketProjectComponent implements ProjectComponent{
	private final Project project;

	public PocketProjectComponent(Project project){
		this.project = project;
	}

	@Override
	public void projectOpened(){
		VirtualFileManager.getInstance().addVirtualFileListener(new PluginYmlWatcher());
	}

}
