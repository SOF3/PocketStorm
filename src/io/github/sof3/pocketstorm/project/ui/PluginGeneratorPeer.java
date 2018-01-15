package io.github.sof3.pocketstorm.project.ui;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.DocumentEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.sof3.pocketstorm.pm.ApiVersion;
import io.github.sof3.pocketstorm.pm.PocketMine;
import io.github.sof3.pocketstorm.project.PluginProjectGenerator;
import io.github.sof3.pocketstorm.project.PluginProjectSettings;

public class PluginGeneratorPeer implements ProjectGeneratorPeer<PluginProjectSettings>{
	private JPanel ui_panel;
	private JTextField ui_edit_name;
	private JTextField ui_edit_main_ns;
	private JTextField ui_edit_main_class;
	private JTextField ui_edit_version;
	private JTextField ui_edit_desc;
	private JButton ui_button_apiEdit;
	private JLabel ui_label_apiList;
	private JLabel ui_label_warning;

	@Getter private boolean backgroundJobRunning = true;

	private void createUIComponents(){
	}

	private Set<ApiVersion> apiList;

	@Getter private final PluginProjectGenerator generator;

	@SuppressWarnings("unchecked") private final ThrowableRunnable<SettingsException>[] validators = new ThrowableRunnable[]{
			this::validateName,
			this::validateMain,
	};

	public PluginGeneratorPeer(PluginProjectGenerator generator){
		this.generator = generator;
		PocketMine.apiList.request(apis -> {
			//noinspection ConstantConditions
			ApiVersion max = apis.values().stream().max(Comparator.comparingInt(ApiVersion::getNumber)).get();
			Set<ApiVersion> set = new HashSet<>();
			set.add(max);
			setApiList(set);
			backgroundJobRunning = false;
		});

		RefreshWarningsListener rwListener = new RefreshWarningsListener();
		rwListener.listen(ui_edit_name);
		rwListener.listen(ui_edit_main_ns);
		rwListener.listen(ui_edit_main_class);
		rwListener.listen(ui_edit_version);
		rwListener.listen(ui_edit_desc);
		ui_button_apiEdit.addActionListener(e -> {
			ChooseApiDialogPanel.ChooseApiDialogWrapper dialog = new ChooseApiDialogPanel.ChooseApiDialogWrapper(ui_panel, apiList.stream().map(ApiVersion::getName).collect(Collectors.toSet()), this::setApiList);
			dialog.show();
			if(dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE){
				dialog.publishResult();
			}
		});
	}

	public void setApiList(Set<ApiVersion> versions){
		apiList = versions;
		ui_label_apiList.setText(versions.stream().sorted().map(ApiVersion::getName).collect(Collectors.joining(", ")));
	}

	private void validateName() throws SettingsException{
		String name = ui_edit_name.getText();
		if(!PocketMine.VALID_PLUGIN_NAME.matcher(name).matches()){
			throw new SettingsException("Invalid plugin name", "Plugin name must only contain alphabets, numbers, hyphens, underscores and periods.", Severity.ERROR);
		}
		for(String substring : PocketMine.PLUGIN_NAME_RESTRICTED_SUBSTRINGS){
			if(name.toLowerCase(Locale.ENGLISH).contains(substring)){
				throw new SettingsException("Invalid plugin name", "The substring \"" + substring + "\" is disallowed.", Severity.ERROR);
			}
		}
	}

	private void validateMain() throws SettingsException{
		String ns = ui_edit_main_ns.getText();
		String main = ui_edit_main_class.getText();
		if(ns.length() == 0){
			throw new SettingsException("Invalid namespace", "Namespace must not be empty", Severity.ERROR);
		}
		if(ns.equalsIgnoreCase("pocketmine")){
			for(String n : ns.split("\\\\")){
				if(!PocketMine.VALID_IDENTIFIER_NAME.matcher(n).matches()){
					throw new SettingsException("Invalid namespace", "Not a valid identifier", Severity.ERROR);
				}
			}
		}
		for(String n : main.split("\\\\")){
			if(!PocketMine.VALID_IDENTIFIER_NAME.matcher(n).matches()){
				throw new SettingsException("Invalid main class", "Not a valid identifier", Severity.ERROR);
			}
		}
		if(main.toLowerCase(Locale.ENGLISH).startsWith(ns.toLowerCase(Locale.ENGLISH) + '\\')){
			throw new SettingsException("Warning", "The main class does not need to contain the namespace", Severity.WARNING);
		}
	}


	public void refreshWarnings(){
		SettingsException warning = null;
		for(ThrowableRunnable<SettingsException> validator : validators){
			try{
				validator.run();
			}catch(SettingsException e){
				if(e.getSeverity() == Severity.ERROR){
					e.display();
					return;
				}
				warning = e;
			}
		}
		if(warning != null){
			warning.display();
		}else{
			ui_label_warning.setText("");
			ui_label_warning.setVisible(false);
		}
	}

	@NotNull
	@Override
	public PluginProjectSettings getSettings(){
		return PluginProjectSettings.builder()
				.name(ui_edit_name.getText())
				.namespace(ui_edit_main_ns.getText())
				.main(ui_edit_main_class.getText())
				.initialVersion(ui_edit_version.getText())
				.description(ui_edit_desc.getText().length() > 0 ? ui_edit_desc.getText() : null)
				.api(apiList.stream().map(ApiVersion::getName).collect(Collectors.toSet()))
				.build();
	}

	@Nullable
	@Override
	public ValidationInfo validate(){
		for(ThrowableRunnable<SettingsException> validator : validators){
			try{
				validator.run();
			}catch(SettingsException e){
				if(e.getSeverity() == Severity.ERROR){
					return new ValidationInfo(
							(e.getTitle().equals(SettingsException.DEFAULT_TITLE) ? "" : e.getTitle() + ": ")
									+ e.getMessage());
				}
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void addSettingsStateListener(@SuppressWarnings("deprecated") @NotNull WebProjectGenerator.SettingsStateListener listener){
		addSettingsListener(listener::stateChanged);
	}

	@Override
	public void addSettingsListener(@NotNull SettingsListener listener){

	}

	@NotNull
	@Override
	public JComponent getComponent(){
		return ui_panel;
	}

	@Override
	public void buildUI(@NotNull SettingsStep settingsStep){
		settingsStep.addSettingsComponent(ui_panel);
	}

	private class SettingsException extends ConfigurationException{
		@Getter private final Severity severity;

		public SettingsException(@Nls @Nullable String title, @Nls @NotNull String message, @NotNull Severity severity){
			super(message, title != null ? title : DEFAULT_TITLE);
			this.severity = severity;
		}

		public void display(){
			ui_label_warning.setIcon(severity.getIcon());
			ui_label_warning.setText(getFullMessage());
			ui_label_warning.setForeground(severity.getColor());
			ui_label_warning.setVisible(true);
		}

		private String getFullMessage(){
			return getTitle().equals(DEFAULT_TITLE) ? getMessage() : (getTitle() + ": " + getMessage());
		}
	}

	@RequiredArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	@Getter
	private enum Severity{
		ERROR(MessageType.ERROR.getTitleForeground(), MessageType.ERROR.getDefaultIcon()),
		WARNING(MessageType.WARNING.getTitleForeground(), MessageType.WARNING.getDefaultIcon());
		Color color;
		Icon icon;
	}

	private class RefreshWarningsListener extends DocumentAdapter{
		@Override
		protected void textChanged(DocumentEvent e){
			refreshWarnings();
		}

		public void listen(JTextField field){
			field.getDocument().addDocumentListener(this);
		}
	}

}
