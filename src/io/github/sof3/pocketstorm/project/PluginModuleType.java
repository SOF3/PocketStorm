package io.github.sof3.pocketstorm.project;

import com.intellij.openapi.module.ModuleType;
import io.github.sof3.pocketstorm.PocketStormIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

@Deprecated
public class PluginModuleType extends ModuleType<PluginModuleBuilder>{
	private final static String ID = "POCKETMINE_PLUGIN";

	private static PluginModuleType instance = null;

	public static PluginModuleType getInstance(){
		return instance;
	}

	public PluginModuleType(){
		super(ID);
		instance = this;
	}

	@NotNull
	@Override
	public PluginModuleBuilder createModuleBuilder(){
		return new PluginModuleBuilder();
	}

	@NotNull
	@Override
	public String getName(){
		return "PocketMine plugin";
	}

	@NotNull
	@Override
	public String getDescription(){
		return "PocketMine plugin";
	}

	@Override
	public Icon getNodeIcon(boolean isOpened){
		return PocketStormIcon.NODE;
	}
}
