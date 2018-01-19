/*
 * This file was copied from intellij-community
 *
 * Copyright 2010-2016 Bas Leijdekkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import com.intellij.codeInspection.ui.ListTable;
import com.intellij.codeInspection.ui.ListWrappingTableModel;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.util.ui.JBUI;

@SuppressWarnings("SameParameterValue")
public class UiUtils{
	private UiUtils(){
	}

	public static JPanel createAddRemovePanel(final ListTable table){
		final JPanel panel = ToolbarDecorator.createDecorator(table)
				.setAddAction(button -> {
					final ListWrappingTableModel tableModel = table.getModel();
					tableModel.addRow();
					EventQueue.invokeLater(() -> {
						final int lastRowIndex = tableModel.getRowCount() - 1;
						editTableCell(table, lastRowIndex, 0);
					});
				})
				.setRemoveAction(button -> TableUtil.removeSelectedItems(table))
				.disableUpDownActions().createPanel();
		panel.setPreferredSize(JBUI.size(150, 100));
		return panel;
	}

	private static void editTableCell(final ListTable table, final int row, final int column){
		final ListSelectionModel selectionModel = table.getSelectionModel();
		selectionModel.setSelectionInterval(row, row);
		EventQueue.invokeLater(() -> {
			final ListWrappingTableModel tableModel = table.getModel();
			IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> {
				IdeFocusManager.getGlobalInstance().requestFocus(table, true);
			});
			final Rectangle rectangle = table.getCellRect(row, column, true);
			table.scrollRectToVisible(rectangle);
			table.editCellAt(row, column);
			final TableCellEditor editor = table.getCellEditor();
			final Component component = editor.getTableCellEditorComponent(table, tableModel.getValueAt(row, column), true, row, column);
			IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> {
				IdeFocusManager.getGlobalInstance().requestFocus(component, true);
			});
		});
	}
}
