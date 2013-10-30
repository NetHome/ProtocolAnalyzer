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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class AboutWindow extends org.eclipse.swt.widgets.Dialog {

	private Shell dialogShell;
	private Composite composite1;
	private Label label2;
	private Label label5;
	private Label label4;
	private Label label3;
	private Label label1;

	/**
	* Auto-generated main method to display this 
	* org.eclipse.swt.widgets.Dialog inside a new Shell.
	*/
	public static void main(String[] args) {
		try {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			AboutWindow inst = new AboutWindow(shell, SWT.NULL);
			inst.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AboutWindow(Shell parent, int style) {
		super(parent, style);
	}

	public void open() {
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			dialogShell.setImage(SWTResourceManager.getImage("nu/nethome/tools/protocol_analyzer/radar16.png"));
			{
				//Register as a resource user - SWTResourceManager will
				//handle the obtaining and disposing of resources
				SWTResourceManager.registerResourceUser(dialogShell);
			}
			
			
			dialogShell.setLayout(new FormLayout());
			{
				FormData composite1LData = new FormData();
				composite1LData.width = 339;
				composite1LData.height = 225;
				composite1LData.left =  new FormAttachment(0, 1000, 0);
				composite1LData.top =  new FormAttachment(0, 1000, 0);
				composite1LData.right =  new FormAttachment(1000, 1000, 0);
				composite1LData.bottom =  new FormAttachment(1000, 1000, 0);
				composite1 = new Composite(dialogShell, SWT.NONE);
				composite1.setLayout(null);
				composite1.setLayoutData(composite1LData);
				composite1.setBackground(SWTResourceManager.getColor(255, 255, 255));
				{
					label1 = new Label(composite1, SWT.NONE);
					label1.setText("label1");
					label1.setImage(SWTResourceManager.getImage("nu/nethome/tools/protocol_analyzer/about.jpg"));
					label1.setBounds(3, 3, 151, 222);
				}
				{
					label2 = new Label(composite1, SWT.NONE);
					label2.setText("Protocol Analyzer");
					label2.setBounds(172, 21, 175, 24);
					label2.setFont(SWTResourceManager.getFont("Tahoma", 12, 1, false, false));
					label2.setBackground(SWTResourceManager.getColor(255, 255, 255));
				}
				{
					label3 = new Label(composite1, SWT.NONE);
					label3.setText("by");
					label3.setBounds(235, 47, 22, 17);
					label3.setBackground(SWTResourceManager.getColor(255, 255, 255));
				}
				{
					label4 = new Label(composite1, SWT.NONE);
					label4.setText("Stefan Strömberg (c) 2010-2013");
					label4.setBounds(170, 70, 200, 23);
					label4.setBackground(SWTResourceManager.getColor(255, 255, 255));
				}
				{
					label5 = new Label(composite1, SWT.NONE);
					label5.setText("http://www.nethome.nu");
					label5.setBounds(180, 99, 200, 30);
					label5.setBackground(SWTResourceManager.getColor(255, 255, 255));
				}
			}
			dialogShell.layout();
			dialogShell.pack();			
			dialogShell.setSize(400, 259);
			dialogShell.setText("About Protocol Analyzer");
			dialogShell.setLocation(getParent().toDisplay(100, 100));
			dialogShell.open();
			Display display = dialogShell.getDisplay();
			while (!dialogShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
