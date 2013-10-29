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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class SettingsWindow {
	protected Shell m_Shell;
	protected Display m_Display;
	protected Table m_Table;
	protected Main m_Model;
	
	protected boolean m_ShowRaw = false;
	
	/**
	 * Creates the Main Window and its content
	 * 
	 * @param display The display used to open the window
	 * @param model
	 */
	public SettingsWindow(Display display, Main model) {
	    // Color white = display.getSystemColor(SWT.COLOR_WHITE);
		m_Display = display;
		m_Model = model;
		
		// Create the main window of the application
		m_Shell = new Shell(display);
		m_Shell.setText("Manage Decoders");

		// Decorate the window with a nice icon at the top
		Image image = new Image(display, this.getClass().getClassLoader().getResourceAsStream("nu/nethome/tools/protocol_analyzer/radar16.png"));
		m_Shell.setImage(image);
		
		// Create a grid layout for the window
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = 2;
		m_Shell.setLayout(shellLayout);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_Shell.setLayoutData(gridData);

		Label m_Label = new Label(m_Shell, SWT.NONE);
		m_Label.setText("Flank height");

		gridData = new GridData(GridData.FILL_HORIZONTAL/* .GRAB_HORIZONTAL*/);
		gridData.widthHint = 80;
		m_Label.setLayoutData(gridData);
		
		Text m_ValueField = new Text(m_Shell, SWT.BORDER /*| SWT.H_SCROLL | SWT.V_SCROLL */);
		m_ValueField.setText("<No Access>");

	}
	
	/**
	 * Opens the window
	 */
	public void open() {
		// Open the panel
		m_Shell.pack();
		m_Shell.setSize(350, 300);
		m_Shell.open();
	}
}

