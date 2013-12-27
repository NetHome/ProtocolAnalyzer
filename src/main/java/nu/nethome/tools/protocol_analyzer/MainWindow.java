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
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolMessage;
import nu.nethome.util.ps.RawProtocolMessage;
import nu.nethome.util.ps.impl.AudioProtocolPort.Channel;
import nu.nethome.util.ps.impl.AudioPulsePlayer;
import nu.nethome.util.ps.impl.PulseProtocolPort;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.prefs.Preferences;


/**
 * @author Stefan
 * 
 * This is the main window of the sampler application.
 */
public class MainWindow implements ProtocolDecoderSink {

	protected class ProtocolWindowMessage implements Runnable {
		ProtocolMessage m_Message;
		MainWindow m_Window;

		public ProtocolWindowMessage(MainWindow window, ProtocolMessage message) {
			m_Message = message;
			m_Window = window;
		}

		public void run() {
			m_Window.addTableRow(m_Message);
		}

	}

	protected class LevelWindowMessage implements Runnable {
		int m_Level;
		MainWindow m_Window;

		public LevelWindowMessage(MainWindow window, int level) {
			m_Level = level;
			m_Window = window;
		}

		public void run() {
			m_Window.displayLevel(m_Level);
		}
	}

	/**
	 * Implements the Level Meter LED Bar which displays the signal level
	 * @author Stefan
	 */
	protected class LevelMeter {
		protected Canvas m_Canvas;
		protected Color black;
		protected Color grey;
		protected Color green;
		protected Color yellow;
		protected GC m_Gc;
		protected int m_LastLevel = 0;

		public LevelMeter(Composite shell, GridData gridData) {
			m_LevelCanvas = new Canvas(shell, SWT.BORDER);
			m_LevelCanvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					e.gc.setBackground(black);
					e.gc.setForeground(yellow);
					// Fill with black background
					e.gc.fillRectangle(0, 0, LEVELWIDTH, LEVELHEIGHT);
					// Draw the "bar"
					for (int i = 0; i < 25; i++) {
						if (i == m_LastLevel) {
							e.gc.setForeground(grey);
						} else if ((i >= 17) && (i < m_LastLevel)) {
							m_Gc.setForeground(green);
						}
						e.gc.drawRectangle(i * 4, 1, 1, LEVELHEIGHT - 3);
					}
				}
			});
			m_Gc = new GC(m_LevelCanvas);
			m_LevelCanvas.setLayoutData(gridData);
			black = m_Display.getSystemColor(SWT.COLOR_BLACK);
			grey = m_Display.getSystemColor(SWT.COLOR_DARK_GRAY);
			green = m_Display.getSystemColor(SWT.COLOR_GREEN);
			yellow = m_Display.getSystemColor(SWT.COLOR_YELLOW);
		}

		public void drawLevel(int level) {
			if (level > m_LastLevel) {
				// If level has risen, draw colored bars up to new level
				for (int i = m_LastLevel; i < level; i++) {
					if (i < 17) {
						m_Gc.setForeground(yellow);
					} else {
						m_Gc.setForeground(green);
					}
					m_Gc.drawRectangle(i * 4, 1, 1, LEVELHEIGHT - 3);
				}
			} else if (level < m_LastLevel) {
				// If the level has decreased, draw grey bars down to new level
				m_Gc.setForeground(grey);
				for (int i = level; i < m_LastLevel; i++) {
					m_Gc.drawRectangle(i * 4, 1, 1, LEVELHEIGHT - 3);
				}
			}
			m_LastLevel = level;
		}
	}

	protected static final int LEVELWIDTH = 100;
	protected static final int LEVELHEIGHT = 10;
	protected static final int NO_COLUMNS = 6;
	protected static final String extensions[] = { "*.jir" }; //$NON-NLS-1$
	protected static final int UPDATE_INTERVAL = 500;

	protected Shell m_Shell;
	protected Display m_Display;
	protected Table m_Table;
	protected MenuItem m_SampleRawMenuItem;
	protected Main m_Model;
	protected String m_FileName = "Data.jir"; //$NON-NLS-1$
	protected Label m_StatusText;
	protected Canvas m_LevelCanvas;
	protected LevelMeter m_LevelMeter;
	protected boolean m_ShowRaw = true;

	AudioPulsePlayer player = new AudioPulsePlayer();
	private ToolItem m_StopScanningButton;
	private ToolItem m_StartScanningButton;
	private Label m_HWIcon;
	private ToolItem m_FreeSampleButton;

	/**
	 * Creates the Main Window and its content
	 * 
	 * @param display
	 *            The display used to open the window
	 * @param model
	 *            the model this is a view of
	 */
	public MainWindow(Display display, Main model) {
		m_Display = display;
		m_Model = model;
		m_ShowRaw = Preferences.userNodeForPackage(this.getClass()).getBoolean("ShowRaw", true); //$NON-NLS-1$

		// player.old_start();
		player.openLine();
		player.setSwing(100);
		
		// Create the main window of the application
		m_Shell = new Shell(display);
		m_Shell.setText(Messages.getString("MainWindow.IRSampler")); //$NON-NLS-1$

		// Decorate the window with a nice icon at the top
		m_Shell.setImage(getToolImage("radar16.png")); //$NON-NLS-1$

		// Create the window menues
		createMenus();

		// Create Toolbar
		createToolbar();

		// Create a grid layout for the window
		GridLayout shellLayout = new GridLayout();
		shellLayout.numColumns = 1;
		m_Shell.setLayout(shellLayout);

		// Create the Table
		createTable();

		GridData gridData;

		// Create status row.
		// Create a composite for the row, and give it a column grid layout
		Composite statusRowComposite = new Composite(m_Shell,
				SWT.NO_RADIO_GROUP);
		GridLayout statusRowLayout = new GridLayout();
		statusRowLayout.numColumns = 4;
		statusRowLayout.marginBottom = -6;
		statusRowLayout.marginTop = -4;
		statusRowComposite.setLayout(statusRowLayout);

		// Create hw icon
		m_HWIcon = new Label(statusRowComposite, 0);
		m_HWIcon.setImage(getToolImage("microphone16.png")); //$NON-NLS-1$
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.grabExcessVerticalSpace = false;
		gridData.heightHint = 22;
		m_HWIcon.setLayoutData(gridData);

		// Create the status text with layout
		m_StatusText = new Label(statusRowComposite, 0);
		String statusText = Messages.getString("MainWindow.SamplingAt") //$NON-NLS-1$
				+ Float.toString(m_Model.getAudioSampler().getSampleRate())
				+ Messages.getString("MainWindow.Hz"); //$NON-NLS-1$
		m_StatusText.setText(statusText);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.grabExcessVerticalSpace = false;
		gridData.widthHint = 180;
		m_StatusText.setLayoutData(gridData);

		// Create the Level meter with layout
		Label levelText = new Label(statusRowComposite, 0);
		levelText.setText(Messages.getString("MainWindow.Level")); //$NON-NLS-1$
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.grabExcessVerticalSpace = false;
		levelText.setLayoutData(gridData);
		
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = LEVELWIDTH;
		gridData.heightHint = LEVELHEIGHT;
		gridData.grabExcessVerticalSpace = false;
		m_LevelMeter = new LevelMeter(statusRowComposite, gridData);
		updateWindowState(true);
		
		// Create and start a timer to update status on button and status row
		Runnable timer = new Runnable() {
			public void run() {
				if (!m_Shell.isDisposed()) {
					updateWindowState(false);
				}
				m_Display.timerExec(UPDATE_INTERVAL, this);
			}
		};
		m_Display.timerExec(UPDATE_INTERVAL, timer);
	}

	/**
	 * Creates all items located in the popup menu and associates all the menu
	 * items with their appropriate functions.
	 * 
	 * @return Menu The created popup menu.
	 */
	private Menu createPopUpMenu() {
		Menu popUpMenu = new Menu(m_Shell, SWT.POP_UP);

		/**
		 * Adds a listener to handle enabling and disabling some items in the
		 * Edit submenu.
		 */
		popUpMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu menu = (Menu) e.widget;
				MenuItem[] items = menu.getItems();
				int count = m_Table.getSelectionCount();
				boolean isRaw = false;
				RawProtocolMessage mess = null;
				if (count == 1) {
					ProtocolMessage message = (ProtocolMessage) m_Table.getSelection()[0].getData();

					// Check that it is a raw sample
					if (message.getProtocol().equals("Raw")) {  //$NON-NLS-1$
						isRaw = true;
						mess = (RawProtocolMessage) message;

					}
				}
 				items[0].setEnabled(count != 0); // view
 				items[1].setEnabled(isRaw); // export pulse data
 				items[2].setEnabled(isRaw); // export pulse data
 				items[3].setEnabled(isRaw && mess.m_Samples.size() > 0); // export pulse data
			}
		});

		// New
		MenuItem item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(Messages.getString("MainWindow.ViewSampleMenu")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				viewSample();
			}
		});

		// new MenuItem(popUpMenu, SWT.SEPARATOR);

		// Edit
		item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(Messages.getString("MainWindow.ExportPulseData")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = m_Table.getSelection();
				if (items.length == 0)
					return;
				exportPulseData();
			}
		});
		
		// Re-analyze pulses
		item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(Messages.getString("MainWindow.ReanalyzePulseData")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = m_Table.getSelection();
				if (items.length == 0)
					return;
				reanalyzePulseData();
			}
		});

		// Re-analyze samples
		item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(Messages.getString("MainWindow.ReanalyzeSignalData")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = m_Table.getSelection();
				if (items.length == 0)
					return;
				reanalyzeSampleData();
			}
		});

		/*
		// Copy
		item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(resAddressBook.getString("Pop_up_copy"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = table.getSelection();
				if (items.length == 0)
					return;
				copyBuffer = new String[table.getColumnCount()];
				for (int i = 0; i < copyBuffer.length; i++) {
					copyBuffer[i] = items[0].getText(i);
				}
			}
		});

		// Paste
		item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(resAddressBook.getString("Pop_up_paste"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (copyBuffer == null)
					return;
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(copyBuffer);
				isModified = true;
			}
		});

		// Delete
		item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(resAddressBook.getString("Pop_up_delete"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = table.getSelection();
				if (items.length == 0)
					return;
				items[0].dispose();
				isModified = true;
			}
		});

		new MenuItem(popUpMenu, SWT.SEPARATOR);

		// Find...
		item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(resAddressBook.getString("Pop_up_find"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchDialog.open();
			}
		});
*/
		return popUpMenu;
	}

	/**
	 * Create the table view where the Samples are presented as nodes
	 */
	private void createTable() {
		m_Table = new Table(m_Shell, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_Table.setLayoutData(gridData);

		// Create the columns in the table
		TableColumn tc1 = new TableColumn(m_Table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(m_Table, SWT.CENTER);
		TableColumn tc3 = new TableColumn(m_Table, SWT.CENTER);
		TableColumn tc4 = new TableColumn(m_Table, SWT.CENTER);
		TableColumn tc5 = new TableColumn(m_Table, SWT.CENTER);
		TableColumn tc6 = new TableColumn(m_Table, SWT.CENTER);
		tc1.setText(Messages.getString("MainWindow.TimeStamp")); //$NON-NLS-1$
		tc2.setText(Messages.getString("MainWindow.Protocol")); //$NON-NLS-1$
		tc3.setText(Messages.getString("MainWindow.Command")); //$NON-NLS-1$
		tc4.setText(Messages.getString("MainWindow.Address")); //$NON-NLS-1$
		tc5.setText(Messages.getString("MainWindow.Data")); //$NON-NLS-1$
		tc6.setText(Messages.getString("MainWindow.Repeat")); //$NON-NLS-1$
		tc1.setWidth(80);
		tc2.setWidth(70);
		tc3.setWidth(70);
		tc4.setWidth(80);
		tc5.setWidth(100);
		tc6.setWidth(70);
		m_Table.setHeaderVisible(true);
		// Add a selection listener to the table which will open the message
		m_Table.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				viewSample();
			}
		});
		// Add keyboard handler
		m_Table.addKeyListener(new KeyAdapter() {	
			public void keyPressed(KeyEvent e) {
				handleKeyPress(e);
			}
		});
		m_Table.setMenu(createPopUpMenu());	
	}

	/**
	 * Handle keyboard commands
	 * @param e the KeyEvent to process
	 */
	protected void handleKeyPress(KeyEvent e) {
		if (e.character == SWT.DEL) {
			deleteSelectedRow((e.stateMask & SWT.SHIFT) != 0);
		}
		
	}

	Image getToolImage(String name) {
		return new Image(m_Display, this.getClass().getClassLoader()
				.getResourceAsStream("nu/nethome/tools/protocol_analyzer/" + name)); //$NON-NLS-1$
	}
	
	/**
	 * Create the Tool Bar with all buttons
	 */
	protected void createToolbar() {

		// http://help.eclipse.org/help31/index.jsp?topic=/org.eclipse.jdt.doc.user/reference/ref-156.htm
		ToolBar bar = new ToolBar(m_Shell, 0 /* | SWT.BORDER */| SWT.FLAT);
		bar.setSize(200, 48);
		ToolItem separator = new ToolItem(bar, SWT.SEPARATOR);
		separator.getData();

		// Open... button
		ToolItem item = new ToolItem(bar, 0);
		item.setImage(getToolImage("document_chart_into24.png")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("MainWindow.OpenSampledSession")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				loadData();
			}
		});

		// Save button
		item = new ToolItem(bar, 0);
		item.setImage(getToolImage("document_chart_floppy_disk24.png")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("MainWindow.SaveSampledSession")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				saveData();
			}
		});

		// Save As... button
		item = new ToolItem(bar, 0);
		item.setImage(getToolImage("document_chart_floppy_disk_edit24.png")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("MainWindow.SaveSampleAs")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				saveDataAs();
			}
		});

		separator = new ToolItem(bar, SWT.SEPARATOR);
		
		// Delete Selected
		item = new ToolItem(bar, 0);
		item.setImage(getToolImage("selection_delete24.png")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("MainWindow.DeleteSelectedSample")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteSelectedRow(false);
			}
		});

		// Delete Selected
		item = new ToolItem(bar, 0);
		item.setImage(getToolImage("delete24.png")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("MainWindow.DeleteAllSamples")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteAllRows();
			}
		});

		separator = new ToolItem(bar, SWT.SEPARATOR);
		
		// Edit template button
		item = new ToolItem(bar, 0);
		item.setImage(getToolImage("edit24.png")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("MainWindow.EditExportTemplate")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				ExportTemplateWindow w = new ExportTemplateWindow(m_Display,
						m_Model);
				w.open();
			}
		});

		// Export button
		item = new ToolItem(bar, 0);
		item.setImage(getToolImage("document_out24.png")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("MainWindow.ExportData")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				exportData();
			}
		});

		// Settings Button
		item = new ToolItem(bar, 0);
		item.setImage(getToolImage("preferences_edit24.png")); //$NON-NLS-1$
		item.setToolTipText(Messages.getString("MainWindow.Settings")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				editSettings();
			}
		});

		separator = new ToolItem(bar, SWT.SEPARATOR);

		// Start scanning button
		m_StartScanningButton = new ToolItem(bar, 0);
		m_StartScanningButton.setImage(getToolImage("radar_play24.png")); //$NON-NLS-1$
		m_StartScanningButton.setDisabledImage(getToolImage("radar_play_dis24.png")); //$NON-NLS-1$
		m_StartScanningButton.setToolTipText(Messages.getString("MainWindow.StartScanning")); //$NON-NLS-1$
		m_StartScanningButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				m_Model.startScanning();
				updateWindowState(false);
			}
		});
		
		// Stop scanning button
		m_StopScanningButton = new ToolItem(bar, 0);
		m_StopScanningButton.setDisabledImage(getToolImage("radar_stop_dis24.png")); //$NON-NLS-1$
		m_StopScanningButton.setImage(getToolImage("radar_stop24.png")); //$NON-NLS-1$
		m_StopScanningButton.setToolTipText(Messages.getString("MainWindow.StopScanning")); //$NON-NLS-1$
		m_StopScanningButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				m_Model.stopScanning();
				updateWindowState(false);
			}
		});

		// Free sample button
		m_FreeSampleButton = new ToolItem(bar, 0);
		m_FreeSampleButton.setImage(getToolImage("document_chart_clock24.png")); //$NON-NLS-1$
		m_FreeSampleButton.setDisabledImage(getToolImage("document_chart_clock_dis24.png")); //$NON-NLS-1$
		m_FreeSampleButton.setToolTipText(Messages.getString("MainWindow.Sample2Secs")); //$NON-NLS-1$
		m_FreeSampleButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				m_ShowRaw = true;
				m_SampleRawMenuItem.setSelection(true);
				m_Model.getRawDecoder().startFreeSampling(Math
						.round(m_Model.getAudioSampler().getSampleRate()) * 2);
			}
		});


		
//		 //This button was added temporarily for testing signal generation
//
//		item = new ToolItem (bar, 0); toolImage = new Image(m_Display,
//		this.getClass().getClassLoader().getResourceAsStream("nu/nethome/tools/protocol_analyzer/showraw.png"));
//		item.setImage (toolImage); item.setToolTipText("Play sound");
//		item.addSelectionListener(new SelectionListener() { public void
//		widgetDefaultSelected(SelectionEvent e) { widgetSelected(e); } public
//		void widgetSelected(SelectionEvent e) { 
//			X10Encoder encoder = new X10Encoder();
//			encoder.setHouseCode(7);
//			//encoder.setAddress('D');
//			encoder.setButton(0);
//			encoder.setCommand(m_Message);
//			encoder.setRepeatCount(3);
//			// encoder.houseCode = 0;
//			// encoder.deviceCode = 1;
//			// encoder.repeatCount = 6;
//			//encoder.command = 0x54;
//			// encoder.command = m_Message;
//			player.playMessage(encoder.encode());
//			//encoder.setSwing(75);
//			// player.start();
//			//encoder.encode(player);
//			// player.stop();
//			// m_Message ^= 0x40;
//			m_Message ^= 0x1;
//		}
//		}); 
	}

public void updateWindowState(boolean isConfigurationChange) {
	boolean isScanning = m_Model.m_ProtocolPorts[m_Model.getSignalHardware()].isOpen();
	m_StartScanningButton.setEnabled(!isScanning);
	m_StopScanningButton.setEnabled(isScanning);
	m_FreeSampleButton.setEnabled(isScanning && (m_Model.getSignalHardware() == 0));
    String statusText;
    if (!isScanning) {
        statusText = Messages.getString("MainWindow.Stopped");
    } else if (m_Model.getSignalHardware() == Main.AUDIO_SAMPLER) {
        statusText = Messages.getString("MainWindow.SamplingAt") //$NON-NLS-1$
                + Float.toString(m_Model.getAudioSampler().getSampleRate())
                + Messages.getString("MainWindow.Hz"); //$NON-NLS-1$
    } else {
        statusText = Messages.getString("MainWindow.Scanning");
    }
	m_StatusText.setText(statusText);
	if (isConfigurationChange) {
		m_HWIcon.setImage(getToolImage(m_Model.getSignalHardware() == 0 ? "microphone16.png" : "memorystick16.png" )); //$NON-NLS-1$ //$NON-NLS-2$
	}
}

//	protected int m_Message = 0x54;
//	protected int m_Message = 0x1;
	
	protected void createMenus() {

		// Create the menue bar
		Menu menuBar, fileMenu, viewMenu, helpMenu;
		MenuItem fileMenuHeader, viewMenuHeader, helpMenuHeader;
		MenuItem separatorItem, fileTemplateItem, fileExportItem, fileExitItem, fileLoadItem, fileSaveItem, fileSaveAsItem, helpGetHelpItem, decodersItem, settingsItem;
		menuBar = new Menu(m_Shell, SWT.BAR);

		// Create File menu
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText(Messages.getString("MainWindow.FileMenu")); //$NON-NLS-1$
		fileMenu = new Menu(m_Shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);

		// Open menu Item
		fileLoadItem = new MenuItem(fileMenu, SWT.PUSH);
		fileLoadItem.setText(Messages.getString("MainWindow.OpenMenu")); //$NON-NLS-1$
		fileLoadItem.setAccelerator(SWT.CTRL + 'O');
		fileLoadItem.setImage(getToolImage("open.png")); //$NON-NLS-1$
		fileLoadItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				loadData();
			}
		});

		// Save menu Item
		fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveItem.setText(Messages.getString("MainWindow.SaveMenu")); //$NON-NLS-1$
		fileLoadItem.setAccelerator(SWT.CTRL + 'S');

		fileSaveItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				saveData();
			}
		});

		// Save As... menu Item
		fileSaveAsItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveAsItem.setText(Messages.getString("MainWindow.SaveAsMenu")); //$NON-NLS-1$
		fileSaveAsItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				saveDataAs();
			}
		});

		separatorItem = new MenuItem(fileMenu, SWT.SEPARATOR);
		separatorItem.getSelection(); // Just to avoid Eclipse Warning

		// Export Template... menu Item
		fileTemplateItem = new MenuItem(fileMenu, SWT.PUSH);
		fileTemplateItem.setText(Messages.getString("MainWindow.EditExportTemplateMenu")); //$NON-NLS-1$
		fileTemplateItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				ExportTemplateWindow w = new ExportTemplateWindow(m_Display,
						m_Model);
				w.open();
			}
		});

		// Export menu Item
		fileExportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExportItem.setText(Messages.getString("MainWindow.ExportDataMenu")); //$NON-NLS-1$
		fileExportItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				exportData();
			}
		});

		separatorItem = new MenuItem(fileMenu, SWT.SEPARATOR);

		// Exit menu Item
		fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText(Messages.getString("MainWindow.ExitMenu")); //$NON-NLS-1$
		fileExitItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				m_Shell.close(); // calls dispose() - see note below
			}
		});
		
		// Create View menu
		viewMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		viewMenuHeader.setText(Messages.getString("MainWindow.ViewMenu")); //$NON-NLS-1$
		viewMenu = new Menu(m_Shell, SWT.DROP_DOWN);
		viewMenuHeader.setMenu(viewMenu);

		// Show Raw menu Item
		m_SampleRawMenuItem = new MenuItem(viewMenu, SWT.CHECK);
		m_SampleRawMenuItem.setText(Messages.getString("MainWindow.RawMenu")); //$NON-NLS-1$
		m_SampleRawMenuItem.setAccelerator(SWT.CTRL + 'R');
		m_SampleRawMenuItem.setSelection(m_ShowRaw);
		m_SampleRawMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MenuItem m = (MenuItem) e.widget;
				m_ShowRaw = m.getSelection();
				Preferences.userNodeForPackage(this.getClass()).putBoolean("ShowRaw", m_ShowRaw); //$NON-NLS-1$
			}
		});

		// Decoders... menu Item
		decodersItem = new MenuItem(viewMenu, SWT.PUSH);
		decodersItem.setText(Messages.getString("MainWindow.DecordersMenu")); //$NON-NLS-1$
		decodersItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				DecoderWindow win = new DecoderWindow(m_Display, m_Model);
				win.open();
			}
		});

		
		// Settings... menu Item
		settingsItem = new MenuItem(viewMenu, SWT.PUSH);
		settingsItem.setText(Messages.getString("MainWindow.Settingsxxx")); //$NON-NLS-1$
		settingsItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				editSettings();
			}
		});
		
		
		
		separatorItem = new MenuItem(viewMenu, SWT.SEPARATOR);

		// Sample... menu Item
		fileTemplateItem = new MenuItem(viewMenu, SWT.PUSH);
		fileTemplateItem.setText(Messages.getString("MainWindow.SampleMenu")); //$NON-NLS-1$
		fileTemplateItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				viewSample();
			}
		});
		
		
		// Create Action menu
		MenuItem actionMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		actionMenuHeader.setText(Messages.getString("MainWindow.ActionMenu")); //$NON-NLS-1$
		Menu actionMenu = new Menu(m_Shell, SWT.DROP_DOWN);
		actionMenuHeader.setMenu(actionMenu);

		// Add sample choices
		MenuItem sample2MenuItem = new MenuItem(actionMenu, SWT.PUSH);
		sample2MenuItem.setText(Messages.getString("MainWindow.Sample2SecsMenu")); //$NON-NLS-1$
		sample2MenuItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				m_ShowRaw = true;
				m_SampleRawMenuItem.setSelection(true);
				m_Model.getRawDecoder().startFreeSampling(Math
						.round(m_Model.getAudioSampler().getSampleRate()) * 2);
			}
		});

		// Add sample choices
		MenuItem sample5MenuItem = new MenuItem(actionMenu, SWT.PUSH);
		sample5MenuItem.setText(Messages.getString("MainWindow.Sample5SecsMenu")); //$NON-NLS-1$
		sample5MenuItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				m_ShowRaw = true;
				m_SampleRawMenuItem.setSelection(true);
				m_Model.getRawDecoder().startFreeSampling(Math
						.round(m_Model.getAudioSampler().getSampleRate()) * 5);
			}
		});

		// Create Help Menu
		helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText(Messages.getString("MainWindow.HelpMenu")); //$NON-NLS-1$
		helpMenu = new Menu(m_Shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(helpMenu);

		// About Menu Item
		helpGetHelpItem = new MenuItem(helpMenu, SWT.PUSH);
		helpGetHelpItem.setText(Messages.getString("MainWindow.AboutMenu")); //$NON-NLS-1$
		helpGetHelpItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				AboutWindow win = new AboutWindow(new Shell(m_Display), SWT.NULL);
				win.open();
			}
		});

		m_Shell.setMenuBar(menuBar);
	}

	public void parsedMessage(ProtocolMessage message) {
		// Because all window operations has to be done in the Window thread,
		// we have to leave this for later execusion. The IrWindowMEssage class
		// run-method will call the addTableRow method.
		if ((m_ShowRaw || !message.getProtocol().equalsIgnoreCase("raw"))) {  //$NON-NLS-1$
			Display.getDefault().asyncExec(new ProtocolWindowMessage(this, message));
		}
	}

	protected void addTableRow(ProtocolMessage message) {
		if (message.getRepeat() > 0) {
			// Ok, this is a repeated message, find the original message row
			for (int i = m_Table.getItemCount() - 1; i >= 0; i--) {
				TableItem item = m_Table.getItem(i);
				if (item.getText(1).compareTo(message.getProtocol()) == 0) {
					// Update it with the new repeat count
					item.setText(5, Integer.toString(message.getRepeat()));
					break;
				}
			}
		} else {
			// Create a new table row
			TableItem item1 = new TableItem(m_Table, SWT.NONE);
			String text[] = new String[NO_COLUMNS];
	        DateFormat df = new SimpleDateFormat(Messages.getString("MainWindow.HHmmssSSS")); //$NON-NLS-1$
	        Date now = new Date();
	        text[0] = df.format(now);
			text[1] = message.getProtocol();
			text[2] = Integer.toHexString(message.getCommand());
			text[3] = Integer.toHexString(message.getAddress());
			text[4] = "";  //$NON-NLS-1$
			for (int i = 0; i < message.getRawMessage().length; i++) {
				text[4] += Integer.toBinaryString(message.getRawMessage()[i]) + " ";  //$NON-NLS-1$
			}
			text[5] = Integer.toString(message.getRepeat());
			item1.setText(text);
			item1.setData(message);
		}
	}

	public void partiallyParsedMessage(String protocol, int bits) {
		System.out.print("\nPartially parsed " + Integer.toString(bits) //$NON-NLS-1$
				+ "bits of " + protocol); //$NON-NLS-1$
	}

	public void reportLevel(int level) {
		// Because all window operations has to be done in the Window thread,
		// we have to leave this for later execution.
		Display.getDefault().asyncExec(new LevelWindowMessage(this, level));
	}

	public void displayLevel(int level) {
		// Only draw level for Audio Sampling device 
		if (m_Model.getSignalHardware() == 0) {
			m_LevelMeter.drawLevel((25 * level) / 127);
		}
	}

	/**
	 * Opens the main window of the application
	 */
	public void open() {
		// Open the panel
		m_Shell.pack();
		m_Shell.setSize(500, 650);
		m_Shell.open();
	}

	public boolean isDisposed() {
		return m_Shell.isDisposed();
	}

	public void exportData() {
		int length = m_Table.getItems().length;
		FileDialog save = new FileDialog(m_Shell, SWT.SAVE);
		save.setText("Export Data"); //$NON-NLS-1$
		// Ask for export file name
		String fileName = save.open();
		if (fileName == null) {
			return;
		}
		try {
			PrintWriter out = new PrintWriter(new FileWriter(fileName)); //$NON-NLS-1$
			// Loop through all rows, except raw messages
			for (int i = 0; i < length; i++) {
				ProtocolMessage ir = (ProtocolMessage) (m_Table.getItems()[i].getData());
				if (ir.getProtocol().compareTo("Raw") != 0) {  //$NON-NLS-1$
					String output = m_Model.getExportTemplate();
					output = output.replaceAll("#PROTOCOL#", ir.getProtocol());  //$NON-NLS-1$
					output = output.replaceAll("#NAME#", ir.getInterpretation());  //$NON-NLS-1$
					output = output.replaceAll("#NAMEUP#", ir.getInterpretation()  //$NON-NLS-1$
							.toUpperCase());
					for (int j = 0; j < ir.getRawMessage().length; j++) {
						String hex = "0"  //$NON-NLS-1$
								+ Integer.toHexString(ir.getRawMessage()[j]);
						hex = hex.substring(hex.length() - 2);
						output = output.replaceAll("#BYTE"  //$NON-NLS-1$
								+ Integer.toString(j) + "HEX#", hex);  //$NON-NLS-1$
						output = output.replaceAll("#BYTE"  //$NON-NLS-1$
								+ Integer.toString(j) + "#", Integer  //$NON-NLS-1$
								.toString(ir.getRawMessage()[j]));
					}
					// Replace all attribute references with the attribute values
					for (FieldValue field : ir.getFields()) {
						String name = "#ATT:" + field.getName() + "#";
						output = output.replaceAll(name, field.getStringValue() != null ?
                                field.getStringValue() : Integer.toString(field.getValue()));
					}
					out.println(output);
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDataAs() {
		FileDialog save = new FileDialog(m_Shell, SWT.SAVE);
		save.setFilterExtensions(extensions);
		String fileName = save.open();
		if (fileName != null) {
			m_FileName = fileName;
		} else
			return;
		saveData();
	}

	public void saveData() {
		int length = m_Table.getItems().length;
		try {
			FileOutputStream fos = new FileOutputStream(m_FileName);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			// First write the number of messages
			oos.writeInt(length);
			// Then write each message
			for (int i = 0; i < length; i++) {
				oos.writeObject(m_Table.getItems()[i].getData());
			}
			oos.close();
			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadData() {
		Boolean oldShowRaw = m_ShowRaw;
		ObjectInputStream ois;
		FileDialog open = new FileDialog(m_Shell, SWT.OPEN);
		open.setFilterExtensions(extensions);
		String fileName = open.open();
		if (fileName != null) {
			m_FileName = fileName;
		} else
			return;
		try {
			FileInputStream fis = new FileInputStream(m_FileName);
			ois = new ObjectInputStream(fis);

			int length = ois.readInt();
			for (int i = 0; i < length; i++) {
				ProtocolMessage irm = (ProtocolMessage) ois.readObject();
				// If this is a raw sample from an older version, we have to recreate pulse lengths
				if (irm.getProtocol().compareTo("Raw") == 0) {  //$NON-NLS-1$
					RawProtocolMessage mess = (RawProtocolMessage) irm;
					if (mess.m_PulseLengths == null) {
						mess.m_PulseLengths = new LinkedList<Double>();
						int last = 0;
						for (int noSamples : mess.m_PulseList) {
							mess.m_PulseLengths.add((noSamples - last) * 1000000.0 / mess.m_SampleFrequency);
							last = noSamples;
						}
					}
				}
				addTableRow(irm);
			}
			ois.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		m_ShowRaw = oldShowRaw;
	}

	/**
	 * View the currently selected sample
	 */
	private void viewSample() {
		if (m_Table.getSelectionCount() == 0)
			return;
		ProtocolMessage message = (ProtocolMessage) m_Table.getSelection()[0]
				.getData();
		if (message.getProtocol().compareTo("Raw") == 0) {  //$NON-NLS-1$
			System.out.println(message.getProtocol());
			RawProtocolMessage mess = (RawProtocolMessage) message;
			RawMessageDistributionWindow GUI = new RawMessageDistributionWindow(m_Display, mess);
			GUI.open();
		} else {
			System.out.println(message.getProtocol());
			MessageWindow GUI = new MessageWindow(m_Display, message);
			GUI.open();
		}
	}
	
	/**
	 * Export the pulse data of the currently selected sample
	 */
	protected void exportPulseData() {
		
		// Make sure we have a line selected
		if (m_Table.getSelectionCount() == 0)
			return;
		ProtocolMessage message = (ProtocolMessage) m_Table.getSelection()[0]
				.getData();
		
		// Check that it is a raw sample
		if (!message.getProtocol().equals("Raw")) {  //$NON-NLS-1$
			return;
		}
		RawProtocolMessage mess = (RawProtocolMessage) message;

		FileDialog save = new FileDialog(m_Shell, SWT.SAVE);
		save.setText(Messages.getString("MainWindow.ExportData")); //$NON-NLS-1$
		
		// Ask for export file name
		String fileName = save.open();
		if (fileName == null) {
			return;
		}

		// Save all pulses
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(fileName));
			char pulseType = 's';
			// Loop through all pulses
			for (double pulse : mess.m_PulseLengths) {
				out.println(String.format("%c%.0f", pulseType, pulse));  //$NON-NLS-1$
				pulseType = (pulseType == 's') ? 'm' : 's';
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reanalyze the pulse data of the currently selected sample
	 */
	protected void reanalyzePulseData() {
		
		// Make sure we have a line selected
		if (m_Table.getSelectionCount() == 0)
			return;
		ProtocolMessage message = (ProtocolMessage) m_Table.getSelection()[0]
				.getData();
		
		// Check that it is a raw sample
		if (!message.getProtocol().equals("Raw")) {  //$NON-NLS-1$
			return;
		}
		RawProtocolMessage mess = (RawProtocolMessage) message;
		
		boolean currentlySampling = m_Model.isScanning();
		// Pause current sampler so we do not mix signals
		m_Model.stopScanning();

		// Loop through all pulses
		boolean state = false;
		//m_Model.getProtocolDecoders().parse(30000.0, false);
		for (double pulse : mess.m_PulseLengths) {
			// Feed pulses to decoders 
			m_Model.getProtocolDecoders().parse(pulse, state);
			state = !state;
		}
		// Add a long quiet period to end detection
		m_Model.getProtocolDecoders().parse(200000.0, false);
		
		// Restart port (if it was sampling)
		if (currentlySampling) {
			m_Model.startScanning();
		}
	}

	/**
	 * Export the pulse data of the currently selected sample
	 */
	protected void reanalyzeSampleData() {
		
		// Make sure we have a line selected
		if (m_Table.getSelectionCount() == 0)
			return;
		ProtocolMessage message = (ProtocolMessage) m_Table.getSelection()[0]
				.getData();
		
		// Check that it is a raw sample
		if (!message.getProtocol().equals("Raw")) {  //$NON-NLS-1$
			return;
		}
		RawProtocolMessage mess = (RawProtocolMessage) message;
		
		boolean currentlySampling = m_Model.isScanning();
		// Pause current sampler so we do not mix signals
		m_Model.stopScanning();

		// Loop through all samples
		for (int sample : mess.m_Samples) {
			// Feed pulses to decoders 
			m_Model.getProtocolSamplers().addSample(sample);
		}
		// Add a quiet period (200ms)to end detection
		int numberOfEndSamples = m_Model.getFlankDetector().getSampleRate() / 5;
		for (int i = 0; i < numberOfEndSamples; i++) {
			m_Model.getProtocolSamplers().addSample(0);
		}
		
		// Restart port (if it was sampling)
		if (currentlySampling) {
			m_Model.startScanning();
		}
	}

	/**
	 * Open settings dialog and apply the settings
	 */
	protected void editSettings() {
		PulseProtocolPort ports[] = {m_Model.getAudioSampler(), m_Model.getCULPort()};
		int previousHardware = m_Model.getSignalHardware();
		
		int lastSource = m_Model.getAudioSampler().getSource();
		
		Channel lastChannel = m_Model.getAudioSampler().getChannel();
		
		String lastCULPort = m_Model.getCULPort().getSerialPort();
		
		// Create settings dialog and open it
		SettingsTabDialog win = new SettingsTabDialog(m_Shell, 0);
		win.open(m_Model);
		
		// If source or selected hardware has changed, Stop previous port and start current, 
		// if it was not changed it means it is restarted 
		if ((lastSource != m_Model.getAudioSampler().getSource()) || 
				(previousHardware != m_Model.getSignalHardware()) ||
				(lastChannel != m_Model.getAudioSampler().getChannel()) ||
				(!lastCULPort.equals(m_Model.getCULPort().getSerialPort()))) {
			ports[previousHardware].close();
			ports[m_Model.getSignalHardware()].open();
		}
		m_Model.saveAudioPreferences();
		m_Model.saveCULPreferences();
		updateWindowState(true);
	}

	/**
	 * Delete the selected sample row, just exits if no row is selected
	 * @param force if false, requester ask if sure
	 */
	protected void deleteSelectedRow(boolean force) {
		if (m_Table.getSelectionCount() != 1) return;
		int response;
		if (force) {
			response = SWT.YES;
		} else {
			MessageBox box = new MessageBox(m_Shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			box.setMessage(Messages.getString("MainWindow.ReallyDeleteSelected")); //$NON-NLS-1$
			response = box.open();
		}
		if (response == SWT.YES) {
			m_Table.remove(m_Table.getSelectionIndex());
		}
	}
	
	/**
	 * Delete all sample rows
	 */
	protected void deleteAllRows() {
		MessageBox box = new MessageBox(m_Shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		box.setMessage(Messages.getString("MainWindow.ReallyDeleteAll")); //$NON-NLS-1$
		int response = box.open();
		if (response == SWT.YES) {
			m_Table.removeAll();
		}
	}
}
