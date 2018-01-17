package io.pmmp.pocketstorm.project.ui;

import java.awt.Color;
import java.util.*;
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

import io.pmmp.pocketstorm.pm.ApiVersion;
import io.pmmp.pocketstorm.pm.PocketMine;
import io.pmmp.pocketstorm.project.PluginProjectGenerator;
import io.pmmp.pocketstorm.project.PluginProjectSettings;

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
	private JTextField ui_edit_author;
	private JTextField ui_edit_website;
	private JComboBox ui_select_load;
	private JCheckBox ui_check_initListener;

	@Getter private boolean backgroundJobRunning = true;

	@Getter private final PluginProjectGenerator generator;
	private Set<ApiVersion> apiList;

	private boolean nsChanged = false, mainChanged = false, userChange = true;

	@SuppressWarnings("unchecked") private final ThrowableRunnable<SettingsException>[] validators = new ThrowableRunnable[]{
			this::validateName,
			this::validateMain,
			this::validateVersion,
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

		ui_edit_main_class.setText("Main");
		ui_edit_author.setText(System.getProperty("user.name"));
		ui_edit_main_ns.getDocument().addDocumentListener(new DocumentAdapter(){
			@Override
			protected void textChanged(DocumentEvent e){
				nsChanged = nsChanged || userChange;
			}
		});
		ui_edit_main_class.getDocument().addDocumentListener(new DocumentAdapter(){
			@Override
			protected void textChanged(DocumentEvent e){
				mainChanged = mainChanged || userChange;
			}
		});
		DocumentAdapter listener = new DocumentAdapter(){
			@Override
			protected void textChanged(DocumentEvent e){
				if(!nsChanged || !mainChanged){
					String author = normalizeIdentifier(ui_edit_author.getText());
					String name = normalizeIdentifier(ui_edit_name.getText());
					if(author == null || name == null){
						return;
					}
					userChange = false;
					if(!nsChanged){
						ui_edit_main_ns.setText(author + "\\" + name);
					}
					if(!mainChanged){
						ui_edit_main_class.setText(name);
					}
					userChange = true;
					refreshWarnings();
				}
			}
		};
		ui_edit_name.getDocument().addDocumentListener(listener);
		ui_edit_author.getDocument().addDocumentListener(listener);

		ui_edit_version.setText("0.1.0");
		ui_select_load.setSelectedIndex(PluginProjectSettings.LoadOrder.POSTWORLD.ordinal());
	}

	@Nullable
	private static String normalizeIdentifier(String subject){
		StringBuilder builder = null;
		for(int i = 0; i < subject.length(); ++i){
			char ch = subject.charAt(i);
			if(ch == ' ' || ch == '-' || ch == '.'){
				ch = '_';
			}
			boolean digit = '0' <= ch && ch <= '9';
			boolean alpha = 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';
			boolean under = ch == '_';
			if(builder == null){
				if(digit || alpha || under){
					builder = new StringBuilder(subject.length());
					builder.append(digit ? "_" : "").append(ch);
				}
			}else{
				if(digit || alpha || under){
					builder.append(ch);
				}else{
					break; // should break?
				}
			}
		}
		return builder != null ? builder.toString().replaceAll("[_]{2,}", "_") : null;
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
		for(String n : ns.split("\\\\")){
			if(!PocketMine.VALID_IDENTIFIER_NAME.matcher(n).matches()){
				throw new SettingsException("Invalid namespace", "Not a valid identifier", Severity.ERROR);
			}
		}
		if(ns.startsWith("pocketmine")){
			throw new SettingsException("Invalid namespace", "Namespace cannot start with \"pocketmine\"", Severity.ERROR);
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

	private void validateVersion() throws SettingsException{
		String version = ui_edit_version.getText().trim();
		if(version.isEmpty()){
			throw new SettingsException("Invalid version", "The version must not be empty", Severity.ERROR);
		}
		if(version.length() >= 2 && version.charAt(0) == 'v' && Character.isDigit(version.charAt(1))){
			throw new SettingsException("Warning", "The version does not need to start with \"v\"", Severity.WARNING);
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
				.initialVersion(ui_edit_version.getText().trim())
				.description(ui_edit_desc.getText().trim().length() > 0 ? ui_edit_desc.getText().trim() : null)
				.api(apiList.stream().map(ApiVersion::getName).collect(Collectors.toSet()))
				.authors(Arrays.asList((String[]) (ui_edit_author.getText().trim().isEmpty() ? new String[0] : new String[]{ui_edit_author.getText().trim()})))
				.website(ui_edit_website.getText().trim().length() > 0 ? ui_edit_website.getText().trim() : null)
				.load(PluginProjectSettings.LoadOrder.values()[ui_select_load.getSelectedIndex()])
				.initListener(ui_check_initListener.isSelected())
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
