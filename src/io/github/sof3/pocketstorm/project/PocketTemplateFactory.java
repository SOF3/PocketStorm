package io.github.sof3.pocketstorm.project;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import io.github.sof3.pocketstorm.PocketStormIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class PocketTemplateFactory extends ProjectTemplatesFactory{
	@NotNull
	@Override
	public String[] getGroups(){
		return new String[]{"PHP"};
	}

	@Override
	public Icon getGroupIcon(String group){
		return PocketStormIcon.NODE;
	}

	@NotNull
	@Override
	public ProjectTemplate[] createTemplates(@Nullable String group, WizardContext context){
		return new ProjectTemplate[]{
				new PluginProjectGenerator()
		};
	}
}
