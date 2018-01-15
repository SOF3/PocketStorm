package io.github.sof3.pocketstorm.project.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import io.github.sof3.pocketstorm.pm.ApiVersion;
import io.github.sof3.pocketstorm.pm.PocketMine;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChooseApiDialogPanel extends JPanel{
	private DefaultListModel<ApiVersionHolder> model;
	private JPanel main;
	private JBList cbList;
	private CheckBoxList cbList0;
	private com.intellij.ui.components.JBLabel descArea;
	private ApiVersionHolder[] versions = null;
	private Set<ApiRange> ranges;

	@Value
	private static class ApiRange{
		@NonNull ApiVersionHolder from, to;
	}

	public ChooseApiDialogPanel(){
		PocketMine.apiList.request(apis -> {
			versions = new ApiVersionHolder[apis.size()];
			ApiVersionHolder lastIncompatible = null, last = null;
			for(ApiVersion version : apis.values()){
				ApiVersionHolder holder = versions[version.getNumber()] = new ApiVersionHolder(version);
				System.out.println(version.getName());
				if(version.isIncompatible()){
					lastIncompatible = holder;
				}
				last = holder;
			}

			Set<ApiRange> ranges = new HashSet<>();
			ranges.add(new ApiRange(lastIncompatible, last));
			setRanges(ranges);
			cbList0.setSelectedIndex(versions.length - 1);

			add(main);
		});
	}

	public void setValues(Set<String> starts){
		Set<ApiRange> tempRangeSet = new HashSet<>();
		ApiVersionHolder start = null;
		for(int i = 0; i < versions.length; ++i){
			ApiVersionHolder current = versions[i];
			if(start != null){
				// selection open
				if(current.version.isIncompatible()){
					// end selection at the previous version
					tempRangeSet.add(new ApiRange(start, versions[i - 1]));
					start = null;
				}
				// else expand the selection
			}
			if(start == null){
				// looking for new selections to open
				if(starts.contains(current.version.getName())){
					start = current;
				}
			}
		}
		if(start != null){
			tempRangeSet.add(new ApiRange(start, versions[versions.length - 1]));
		}
		setRanges(tempRangeSet);
	}

	public void setRanges(Set<ApiRange> ranges){
		for(ApiVersionHolder version : versions){
			version.cb.setSelected(false);
		}
		for(ApiRange range : ranges){
			for(int i = range.from.version.getNumber(); i <= range.to.version.getNumber(); ++i){
				versions[i].cb.setSelected(true);
			}
		}
		this.ranges = ranges;
	}

	public void showDescription(ApiVersion version){
		System.out.println("Show description: " + version.getName());
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
				System.out.println("Toggle " + version.getName());
				if(cb.isSelected()){
					cb.setSelected(false);
					cb.revalidate();
					// uncheck this checkbox
					// since it is incompatible with this version, all previous minor versions must be incompatible too
					// e.g. if incompatible with 1.3.0, 1.0.0-1.2.0 must be incompatible too
					for(int i = version.getNumber(); i >= 0; --i){
						versions[i].cb.setSelected(false);
						versions[i].cb.revalidate();
						if(versions[i].version.isIncompatible()){
							// inclusive, so put this after setSelected(false)
							break;
						}
					}
				}else{
					// check this checkbox
					// thus compatible with all following minor versions
					// e..g if compatible with 1.5.0, 1.y.0 where y >= 5 must be compatible too
					cb.setSelected(true);
					cb.revalidate();
					for(int i = version.getNumber() + 1; i < versions.length; ++i){
						if(versions[i].version.isIncompatible()){
							// exclusive, so put this before setSelected(true)
							break;
						}
						versions[i].cb.setSelected(true);
						versions[i].cb.revalidate();
					}
				}
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

	static class ChooseApiDialogWrapper extends DialogWrapper{
		private final Consumer<Set<ApiVersion>> setApiList;
		private ChooseApiDialogPanel panel;

		public ChooseApiDialogWrapper(JPanel parent, Consumer<Set<ApiVersion>> setApiList){
			super(parent, true);
			init();
			setTitle("Choose Supported API Versions");
			this.setApiList = setApiList;
		}

		@Override
		protected JComponent createCenterPanel(){
			panel = new ChooseApiDialogPanel();
			return panel;
		}

		public void publishResult(){
			setApiList.accept(panel.ranges.stream().map(range -> range.from.version).collect(Collectors.toSet())); // TODO implement
		}
	}
}
