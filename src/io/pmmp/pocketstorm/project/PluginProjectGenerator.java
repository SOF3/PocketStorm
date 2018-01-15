package io.pmmp.pocketstorm.project;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.swing.Icon;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;

import io.pmmp.pocketstorm.MyUtil;
import io.pmmp.pocketstorm.PocketStormIcon;
import io.pmmp.pocketstorm.pm.PocketMine;
import io.pmmp.pocketstorm.project.ui.PluginGeneratorPeer;

import static io.pmmp.pocketstorm.SneakyException.s;

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

				Set<String> php = new HashSet<>();
				settings.getApi().forEach(api -> php.addAll(PocketMine.apiList.getValue().get(api).getPhp()));

				//noinspection ConstantConditions
				PhpProjectConfigurationFacade.getInstance(project).setLanguageLevel(php.stream().map(PhpLanguageLevel::from)
						.min(Comparator.comparingInt(PhpLanguageLevel::ordinal)).get());

				VirtualFile src = root.createChildDirectory(this, "src");
				ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
				model.addContentEntry(src.getParent()).addSourceFolder(src, false);
				model.commit();

				FileTemplate template = FileTemplateManager.getInstance(project).getInternalTemplate("PocketMine Plugin Main Class");
				Properties props = new Properties();

				FileTemplateUtil.createFromTemplate(template, settings.getMain() + ".php", props,
						PsiDirectoryFactory.getInstance(project).createDirectory(MyUtil.lazyCreateChildDir(this, src, settings.getNamespace())));
			}catch(Exception e){
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
