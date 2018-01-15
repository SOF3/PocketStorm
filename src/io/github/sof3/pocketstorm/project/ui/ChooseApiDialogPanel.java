package io.github.sof3.pocketstorm.project.ui;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.*;

import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import io.github.sof3.pocketstorm.pm.ApiVersion;
import io.github.sof3.pocketstorm.pm.PocketMine;

public class ChooseApiDialogPanel extends JPanel{
	private DefaultListModel<ApiVersionHolder> model;
	private JPanel main;
	@SuppressWarnings("unused") private JBList cbList;
	private CheckBoxList cbList0;
	private JBLabel descArea;
	private ApiVersionHolder[] versions = null;
	private Set<ApiRange> ranges;

	@Value
	private static class ApiRange{
		@NonNull ApiVersionHolder from, to;

		public boolean includes(int number){
			return from.version.getNumber() <= number && number <= to.version.getNumber();
		}
	}

	public ChooseApiDialogPanel(@Nullable Set<String> initialRanges){
		PocketMine.apiList.request(apis -> {
			versions = new ApiVersionHolder[apis.size()];
			ApiVersionHolder lastIncompatible = null, last = null;
			for(ApiVersion version : apis.values()){
				ApiVersionHolder holder = versions[version.getNumber()] = new ApiVersionHolder(version);
				if(version.isIncompatible()){
					lastIncompatible = holder;
				}
				last = holder;
			}

			Set<ApiRange> ranges = new HashSet<>();
			if(initialRanges != null){
				ApiVersionHolder start = null;
				for(int i = 0; i < versions.length; ++i){
					ApiVersionHolder version = versions[i];
					for(String initialRange : initialRanges){
						if(start != null){
							if(version.version.isIncompatible()){
								ranges.add(new ApiRange(start, versions[i - 1]));
							}
						}
						if(start == null){
							if(version.version.getName().equals(initialRange)){
								start = version;
							}
						}
					}
				}
				if(start != null){
					ranges.add(new ApiRange(start, versions[versions.length - 1]));
				}
			}else{
				ranges.add(new ApiRange(lastIncompatible, last));
			}
			setRanges(ranges, true);
			cbList0.setSelectedIndex(versions.length - 1);

			add(main);
		});
	}

	public void recalculateRanges(){
		Set<ApiRange> tempRangeSet = new HashSet<>();
		ApiVersionHolder start = null;
		for(int i = 0; i < versions.length; ++i){
			if(start != null){
				if(versions[i].version.isIncompatible()){
					tempRangeSet.add(new ApiRange(start, versions[i - 1]));
					start = null;
				}
				// otherwise, it SHOULD continue to be selected
			}else{
				if(versions[i].cb.isSelected()){
					start = versions[i];
				}
			}
		}
		if(start != null){
			tempRangeSet.add(new ApiRange(start, versions[versions.length - 1]));
		}
		setRanges(tempRangeSet, false);
	}

	public void setRanges(Set<ApiRange> ranges, boolean updateCb){
		if(updateCb){
			for(ApiVersionHolder version : versions){
				version.cb.setSelected(false);
			}
			for(ApiRange range : ranges){
				for(int i = range.from.version.getNumber(); i <= range.to.version.getNumber(); ++i){
					versions[i].cb.setSelected(true);
				}
			}
		}
		this.ranges = ranges;
	}

	public void showDescription(ApiVersion version){
		descArea.setText("<html>" +
				"<h1>" + version.getName() + "</h1>" +
				"<p>Changes:</p>" +
				"<ul>" + version.getDescription().stream().map(s -> "<li>" + s + "</li>").collect(Collectors.joining()) + "</ul>" +
				"<p style='margin: 10px;'>PHP version: " + version.getPhp().stream().collect(Collectors.joining(", ")) + "</p>" +
				(version.isIndev() ? "<p>Warning: This version is still in development.</p>" : "")
		);
	}

	private void createUIComponents(){
		cbList = cbList0 = new CheckBoxList();
	}

	@ToString
	private class ApiVersionHolder{
		private final ApiVersion version;
		private final JBCheckBox cb;

		public ApiVersionHolder(ApiVersion version){
			this.version = version;
			cb = new JBCheckBox(version.getName());
			model.addElement(this);
		}

		public void onClick(boolean rightClick){
			showDescription(version);
			cbList0.setSelectedIndex(version.getNumber());
			if(!rightClick){
				if(cb.isSelected()){
					cb.setSelected(false);
					// uncheck this checkbox
					// since it is incompatible with this version, all previous minor versions must be incompatible too
					// e.g. if incompatible with 1.3.0, 1.0.0-1.2.0 must be incompatible too
					for(int i = version.getNumber(); i >= 0; --i){
						versions[i].cb.setSelected(false);
						if(versions[i].version.isIncompatible()){
							// inclusive, so put this after setSelected(false)
							break;
						}
					}
					// Poggit marks 1.0.0 as incompatible, so no need to check post-loop condition
				}else{
					// check this checkbox
					// thus compatible with all following minor versions
					// e..g if compatible with 1.5.0, 1.y.0 where y >= 5 must be compatible too
					cb.setSelected(true);
					for(int i = version.getNumber() + 1; i < versions.length; ++i){
						if(versions[i].version.isIncompatible()){
							// exclusive, so put this before setSelected(true)
							break;
						}
						versions[i].cb.setSelected(true);
					}
				}
				recalculateRanges();
				cbList0.repaint();
			}
		}
	}

	private class CheckBoxList extends JBList<ApiVersionHolder>{
		public CheckBoxList(){
			setCellRenderer(new CellRenderer());
			setModel(model = new DefaultListModel<>());
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			addMouseListener(new MouseAdapter(){
				@Override
				public void mouseClicked(MouseEvent e){
					int index = locationToIndex(e.getPoint());
					ApiVersionHolder version = versions[index];
					version.onClick(SwingUtilities.isRightMouseButton(e));
				}
			});
			addKeyListener(new KeyAdapter(){
				@Override
				public void keyPressed(KeyEvent e){
					if(e.getKeyCode() == KeyEvent.VK_SPACE){
						int index = getSelectedIndex();
						if(index >= 0 && index < versions.length){
							versions[index].onClick(false);
						}
					}
				}
			});
			addListSelectionListener(e -> {
				int index = getSelectedIndex();
				if(index >= 0 && index < versions.length){
					showDescription(versions[index].version);
				}
			});
		}

		private class CellRenderer implements ListCellRenderer<ApiVersionHolder>{
			@Override
			public Component getListCellRendererComponent(JList<? extends ApiVersionHolder> list, ApiVersionHolder value, int index, boolean isSelected, boolean cellHasFocus){
				value.cb.setForeground(isSelected ? getSelectionForeground() : getForeground());
				value.cb.setBackground(isSelected ? getSelectionBackground() : getBackground());
				return value.cb;
			}
		}
	}

	public static class ChooseApiDialogWrapper extends DialogWrapper{
		private final Consumer<Set<ApiVersion>> setApiList;
		private final Set<String> initialRanges;
		private ChooseApiDialogPanel panel;

		public ChooseApiDialogWrapper(JPanel parent, Set<String> initialRanges, Consumer<Set<ApiVersion>> setApiList){
			super(parent, true);
			this.initialRanges = initialRanges;
			init();
			setTitle("Choose Supported API Versions");
			this.setApiList = setApiList;
		}

		@Override
		protected JComponent createCenterPanel(){
			panel = new ChooseApiDialogPanel(initialRanges);
			return panel;
		}

		@Nullable
		@Override
		protected ValidationInfo doValidate(){
			if(panel.ranges.isEmpty()){
				return new ValidationInfo("No API versions selected");
			}
			return null;
		}

		public void publishResult(){
			setApiList.accept(panel.ranges.stream().map(range -> range.from.version).collect(Collectors.toSet()));
		}
	}
}
