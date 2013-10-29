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

import nu.nethome.util.ps.FieldValue;
import nu.nethome.util.ps.ProtocolMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.Iterator;


public class MessageWindow {
	protected Shell m_Shell;
	private ProtocolMessage m_Message;
	protected Group m_AttributeGroup;
	Text m_Interpretation;
	private Button m_SaveButton;



	public MessageWindow(Display display, ProtocolMessage message) {
		m_Message = message;
		m_Shell = new Shell(display);
		// m_Shell.setSize(300, 400);
		m_Shell.setLayout(new FillLayout());
		m_Shell.setText(Messages.getString("MessageWindow.RawIRMessage")); //$NON-NLS-1$
		Image image = new Image(display, this.getClass().getClassLoader().getResourceAsStream("nu/nethome/tools/protocol_analyzer/radar16.png"));
		m_Shell.setImage(image);
		
		// Create layot
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = 1;
		m_Shell.setLayout(shellLayout);
		m_AttributeGroup = new Group(m_Shell, SWT.NONE);
		m_AttributeGroup.setText(Messages.getString("MessageWindow.Attributes")); //$NON-NLS-1$
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		m_AttributeGroup.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_AttributeGroup.setLayoutData(gridData);

		for (Iterator<FieldValue> i = m_Message.getFields().iterator(); i.hasNext();){
			FieldValue field = i.next();
			
			Label label;
			Text valueField;
			
			label = new Label(m_AttributeGroup, SWT.NONE);
			label.setText(field.getName());

			gridData = new GridData(GridData.FILL_HORIZONTAL/* .GRAB_HORIZONTAL*/);
			label.setLayoutData(gridData);
			
			valueField = new Text(m_AttributeGroup, SWT.BORDER | SWT.WRAP/*| SWT.H_SCROLL | SWT.V_SCROLL */);
			valueField.setEditable(false);
			gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);

			if (!field.isStringValue()) {
				valueField.setText(Integer.toHexString(field.getValue()));
				valueField.setTextLimit(60);
				gridData.widthHint = 90;
			}
			else {
				valueField.setText(field.getStringValue());
				gridData.widthHint = 300;
				gridData.heightHint = 100;
			}
			gridData.grabExcessHorizontalSpace = true;
			valueField.setLayoutData(gridData);
		}

		// Create the interpretation row
		Label ilabel = new Label(m_AttributeGroup, SWT.NONE);
		ilabel.setText(Messages.getString("MessageWindow.Name")); //$NON-NLS-1$
		gridData = new GridData(GridData.FILL_HORIZONTAL/* .GRAB_HORIZONTAL*/);
		ilabel.setLayoutData(gridData);
		
		m_Interpretation = new Text(m_AttributeGroup, SWT.BORDER /*| SWT.H_SCROLL | SWT.V_SCROLL */);
		m_Interpretation.setText(m_Message.getInterpretation());
		m_Interpretation.setTextLimit(60);
		m_Interpretation.setEditable(true);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.widthHint = 90;
		gridData.grabExcessHorizontalSpace = true;
		m_Interpretation.setLayoutData(gridData);
		m_Interpretation.setFocus();
		
		// Create button
		m_SaveButton = new Button(m_Shell, SWT.PUSH);
		m_SaveButton.setText(Messages.getString("MessageWindow.Save")); //$NON-NLS-1$
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END | SWT.DEFAULT);
		gridData.widthHint = 70;
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
		m_Message.setInterpretation(m_Interpretation.getText());
		m_Shell.close();
	}
	
	public void open() {
		// Open the panel
		m_Shell.pack();
		m_Shell.open();
	}
	
	public boolean isDisposed() {
		return m_Shell.isDisposed();
	}
}
