package io.github.sof3.pocketstorm.project;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
import io.github.sof3.pocketstorm.PocketStormIcon;
import io.github.sof3.pocketstorm.project.ui.PluginGeneratorPeer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import static io.github.sof3.pocketstorm.SneakyException.s;

public class PluginProjectGenerator extends WebProjectTemplate<PluginProjectSettings>{
	@Nls
	@NotNull
	@Override
	public String getName(){
		return "PocketMine Plugin";
	}

	@Override
	public String getDescription(){
		return "Generates a standard PocketMine plugin";
	}

	@Nullable
	@Override
	public Icon getIcon(){
		return PocketStormIcon.NODE;
	}

	@Override
	public Icon getLogo(){
		return PocketStormIcon.NODE;
	}

	@Override
	public void generateProject(@NotNull Project project, @NotNull VirtualFile root, @NotNull PluginProjectSettings settings, @NotNull Module module){
		ApplicationManager.getApplication().runWriteAction(() -> {
			try(
					Writer writer = new OutputStreamWriter(root.createChildData(this, "plugin.yml").getOutputStream(this))
			){
				settings.dumpYaml(writer);
				writer.close();
				VirtualFile src = root.createChildDirectory(this, "src");
				ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
				model.addContentEntry(src.getParent()).addSourceFolder(src, false);
			}catch(IOException e){
				throw s(e);
			}
		});
	}

	@NotNull
	@Override
	public ProjectGeneratorPeer<PluginProjectSettings> createPeer(){
		return new PluginGeneratorPeer(this);
	}
}
