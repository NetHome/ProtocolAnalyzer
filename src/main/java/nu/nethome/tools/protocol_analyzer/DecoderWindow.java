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

import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.prefs.Preferences;

public class DecoderWindow {
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
    public DecoderWindow(Display display, Main model) {
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
        shellLayout.numColumns = 1;
        m_Shell.setLayout(shellLayout);

        // Create the table view where the Decoders are presented.
        m_Table = new Table(m_Shell, SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        m_Table.setLayoutData(gridData);

        // Create the columns in the table
        TableColumn tc1 = new TableColumn(m_Table, SWT.CENTER);
        TableColumn tc2 = new TableColumn(m_Table, SWT.CENTER);
        TableColumn tc3 = new TableColumn(m_Table, SWT.CENTER);
        TableColumn tc4 = new TableColumn(m_Table, SWT.CENTER);
        tc2.setText("Protocol");
        tc3.setText("Type");
        tc4.setText("Company");
        tc1.setWidth(20);
        tc2.setWidth(70);
        tc3.setWidth(90);
        tc4.setWidth(100);
        m_Table.setHeaderVisible(true);
        // Add a selection listener to the tree which will open a HomeItemGUI for selected nodes
        m_Table.addSelectionListener(
                new SelectionAdapter() {
                    public void widgetDefaultSelected(SelectionEvent e) {
                    }
                });
        m_Table.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    TableItem itemChanged = (TableItem) event.item;
                    ProtocolDecoder decoder = (ProtocolDecoder) itemChanged.getData();
                    if (decoder != null) {
                        m_Model.getProtocolDecoders().setActive(decoder, itemChanged.getChecked());
                        Preferences.userNodeForPackage(this.getClass()).
                                putBoolean(decoder.getInfo().getName(), itemChanged.getChecked());
                    }
                }
            }
        });
        m_Shell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event e) {
                System.out.println("Quit button selected");
            }
        });

        for (ProtocolDecoder decoder : m_Model.getProtocolDecoders().getAllDecoders()) {
            ProtocolInfo info = decoder.getInfo();
            TableItem item1 = new TableItem(m_Table, SWT.NONE);
            String text[] = new String[4];
            text[0] = "";
            text[1] = info.getName();
            text[2] = info.getType();
            text[3] = info.getCompany();
            item1.setText(text);
            item1.setData(decoder);
            item1.setChecked(m_Model.getProtocolDecoders().isActive(decoder));
        }
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
