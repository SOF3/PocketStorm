package io.github.sof3.pocketstorm.project;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import io.github.sof3.pocketstorm.PocketStormIcon;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

@Deprecated
public class PluginModuleBuilder extends ModuleBuilder{
	private final static String ID = "POCKETMINE_PLUGIN";

	@Getter private PluginProjectSettings.Builder settings = PluginProjectSettings.builder();

	@Override
	public Icon getNodeIcon(){
		return PocketStormIcon.NODE;
	}

	@Nullable
	@Override
	public String getBuilderId(){
		return ID;
	}

	@Override
	public String getGroupName(){
		return "PocketMine";
	}

	@Override
	public String getPresentableName(){
		return "PocketMine plugin module";
	}

	@Override
	public String getDescription(){
		return "A PocketMine plugin using the classic (DevTools-compatible) framework";
	}

	@Override
	public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider){
		return new ModuleWizardStep[]{};
	}

	@Override
	public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException{
	}

	@Override
	public ModuleType getModuleType(){
		return PluginModuleType.getInstance();
	}
}
