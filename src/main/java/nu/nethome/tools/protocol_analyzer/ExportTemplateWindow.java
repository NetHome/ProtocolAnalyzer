/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome (http://www.nethome.nu).
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.tools.protocol_analyzer;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;


public class ExportTemplateWindow {
	protected Shell m_Shell;
	private Main m_Model;
	Text m_Template;
	private Button m_SaveButton;

	public ExportTemplateWindow(Display display, Main model) {
		m_Model = model;
		m_Shell = new Shell(display);
		m_Shell.setSize(400, 400);
		m_Shell.setLayout(new FillLayout());
		m_Shell.setText("Edit Export Template");
		Image image = new Image(display, this.getClass().getClassLoader().getResourceAsStream("nu/nethome/tools/protocol_analyzer/radar16.png"));
		m_Shell.setImage(image);
		
		// Create layot
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = 1;
		m_Shell.setLayout(shellLayout);
		
		Label label;
		label = new Label(m_Shell, SWT.NONE);
		label.setText("Defines the export format.\n#PROTOCOL# = Protocol name\n#NAME# = Code name\n" +
				"#NAMEUP# = Code name in upper case\n#BYTE0# = Raw byte 0 in decimal\n#BYTE0HEX# = Raw byte 0 in hex\n");


		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		
		m_Template = new Text(m_Shell, SWT.BORDER  | SWT.V_SCROLL );
		m_Template.setText(m_Model.getExportTemplate());
		// m_Template.setTextLimit(60);
		m_Template.setEditable(true);
		m_Template.setLayoutData(gridData);
		m_Template.setFocus();
		
		// Create button
		m_SaveButton = new Button(m_Shell, SWT.PUSH);
		m_SaveButton.setText("Save Template");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END | SWT.DEFAULT);
		gridData.widthHint = 100;
		gridData.grabExcessVerticalSpace = false;
		m_SaveButton.setLayoutData(gridData);
		m_Shell.setDefaultButton(m_SaveButton);
		m_SaveButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				saveButtonPressed();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				saveButtonPressed();
			}
		});
	}
	
	public void	saveButtonPressed(){
		m_Model.setExportTemplate(m_Template.getText());
		m_Shell.close();
	}
	
	public void open() {
		// Open the panel
		//m_Shell.pack();
		m_Shell.open();
	}
	
	public boolean isDisposed() {
		return m_Shell.isDisposed();
	}
}

