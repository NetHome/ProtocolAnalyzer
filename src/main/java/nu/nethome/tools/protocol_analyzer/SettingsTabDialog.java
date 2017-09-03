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

import nu.nethome.util.ps.impl.AudioProtocolPort;
import nu.nethome.util.ps.impl.SimpleFlankDetector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

import javax.sound.sampled.Mixer;

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
public class SettingsTabDialog extends org.eclipse.swt.widgets.Dialog {

	private Shell dialogShell;
	private CTabItem generalSettingsTab;
	private CTabFolder settingsFolder;
	private Group prontoGroup;
	private Label label10;
	private Button right;
	private Label label7;
	private Group flank;
	private Combo combo1;
	private Label label1;
	private Button left;
	private Button mono;
	private Group Source;
	private Composite audioSettingsComposite;
	private Label labelx1;
	private Group group2;
	private Group group1;
	private Label label14;
	private Combo agcCombo;
	private Button invertSelection;
	private Label label13;
	private Label label5;
	private Combo sourceCombo;
	private Label frequenceLabel;
	private Text FrequencyText;
	private Label culSerialLabel;
	private Combo culPortCombo;
	private Composite culSettingsComposite;
	private CTabItem culTabItem;
	private Button selectCULSource;
	private Button selectAudioSource;
	private Group signalSourceGroup;
	private Label label12;
	private Text burstLengthComp;
	private Label label11;
	private Label label6;
	private Button useFilter;
	private Label label2;
	private Text swing;
	private Text length;
	private Label label3;
	private Label label4;
	private Text holdoff;
	private Button cancel;
	private Button ok;
	private Label frequency;
	private Text frequencyValue;
	private Label label8;
	private Label label9;
	private Text leadOutValue;
	private Composite generalSettingsComposite;
	private CTabItem audioSettingsTab;

	protected AudioProtocolPort m_Sampler = null;
	protected SimpleFlankDetector m_FlankDetector = null;
	private Main m_Model;

	public SettingsTabDialog(Shell parent, int style) {
		super(parent, style);
	}

	public void open(Main model) {
		m_Sampler = model.getAudioSampler();
		m_FlankDetector = model.getFlankDetector();
		m_Model = model;
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

			dialogShell.setLayout(new FormLayout());
			dialogShell.setText("Settings");
			dialogShell.layout();
			dialogShell.pack();			
			dialogShell.setSize(358, 417);
			{
			settingsFolder = new CTabFolder(dialogShell, SWT.NONE);
				FormData cTabFolder1LData = new FormData();
				cTabFolder1LData.width = 354;
				cTabFolder1LData.height = 281;
				cTabFolder1LData.left =  new FormAttachment(0, 1000, 0);
				cTabFolder1LData.top =  new FormAttachment(0, 1000, 0);
				cTabFolder1LData.right =  new FormAttachment(1000, 1000, 0);
				cTabFolder1LData.bottom =  new FormAttachment(1000, 1000, -50);
				settingsFolder.setLayoutData(cTabFolder1LData);
				{
				generalSettingsTab = new CTabItem(settingsFolder, SWT.NONE);
					generalSettingsTab.setText("General Settings");
					{
						generalSettingsComposite = new Composite(settingsFolder, SWT.NONE);
						FormLayout generalSettingsCompositeLayout = new FormLayout();
						generalSettingsComposite.setLayout(generalSettingsCompositeLayout);
						generalSettingsTab.setControl(generalSettingsComposite);
						{
							signalSourceGroup = new Group(generalSettingsComposite, SWT.NONE);
							FormLayout signalSourceGroupLayout = new FormLayout();
							signalSourceGroup.setLayout(signalSourceGroupLayout);
							signalSourceGroup.setText("Signal Source");
							FormData signalSourceGroupLData = new FormData();
							signalSourceGroupLData.width = 324;
							signalSourceGroupLData.height = 34;
							signalSourceGroupLData.left =  new FormAttachment(0, 1000, 12);
							signalSourceGroupLData.top =  new FormAttachment(0, 1000, 12);
							signalSourceGroup.setLayoutData(signalSourceGroupLData);
							{
								selectCULSource = new Button(signalSourceGroup, SWT.RADIO | SWT.LEFT);
								selectCULSource.setText("Arduino");
								FormData selectCULSourceLData = new FormData();
								selectCULSourceLData.width = 100;
								selectCULSourceLData.height = 16;
								selectCULSourceLData.left =  new FormAttachment(0, 1000, 139);
								selectCULSourceLData.top =  new FormAttachment(0, 1000, 9);
								selectCULSource.setLayoutData(selectCULSourceLData);
							}
							{
								selectAudioSource = new Button(signalSourceGroup, SWT.RADIO | SWT.LEFT);
								selectAudioSource.setText("Audio");
								FormData selectAudioSourceLData = new FormData();
								selectAudioSourceLData.width = 100;
								selectAudioSourceLData.height = 16;
								selectAudioSourceLData.left =  new FormAttachment(0, 1000, 9);
								selectAudioSourceLData.top =  new FormAttachment(0, 1000, 9);
								selectAudioSource.setLayoutData(selectAudioSourceLData);
							}
						}
						{
							prontoGroup = new Group(generalSettingsComposite, SWT.NONE);
							FormLayout prontoGroupLayout = new FormLayout();
							prontoGroup.setLayout(prontoGroupLayout);
							prontoGroup.setText("Pronto Settings");
							FormData prontoGroupLData = new FormData();
							prontoGroupLData.width = 324;
							prontoGroupLData.height = 70;
							prontoGroupLData.left =  new FormAttachment(0, 1000, 12);
							prontoGroupLData.top =  new FormAttachment(0, 1000, 74);
							prontoGroup.setLayoutData(prontoGroupLData);
							prontoGroup.setLayoutData(prontoGroupLData);
							{
								label10 = new Label(prontoGroup, SWT.NONE);
								label10.setText("uS");
								FormData label10LData = new FormData();
								label10LData.width = 47;
								label10LData.height = 15;
								label10LData.left =  new FormAttachment(0, 1000, 206);
								label10LData.top =  new FormAttachment(0, 1000, 38);
								label10.setLayoutData(label10LData);
							}
							{
								leadOutValue = new Text(prontoGroup, SWT.NONE);
								leadOutValue.setText("9000");
								FormData leadOutValueLData = new FormData();
								leadOutValueLData.width = 50;
								leadOutValueLData.height = 15;
								leadOutValueLData.left =  new FormAttachment(0, 1000, 138);
								leadOutValueLData.top =  new FormAttachment(0, 1000, 38);
								leadOutValue.setLayoutData(leadOutValueLData);
							}
							{
								label9 = new Label(prontoGroup, SWT.NONE);
								label9.setText("Lead Out Time");
								FormData label9LData = new FormData();
								label9LData.width = 100;
								label9LData.height = 15;
								label9LData.left =  new FormAttachment(0, 1000, 9);
								label9LData.top =  new FormAttachment(0, 1000, 38);
								label9.setLayoutData(label9LData);
							}
							{
								label8 = new Label(prontoGroup, SWT.NONE);
								label8.setText("Hz");
								FormData label8LData = new FormData();
								label8LData.width = 47;
								label8LData.height = 15;
								label8LData.left =  new FormAttachment(0, 1000, 206);
								label8LData.top =  new FormAttachment(0, 1000, 8);
								label8.setLayoutData(label8LData);
							}
							{
								frequencyValue = new Text(prontoGroup, SWT.NONE);
								frequencyValue.setText("40000");
								FormData frequencyValueLData = new FormData();
								frequencyValueLData.width = 50;
								frequencyValueLData.height = 15;
								frequencyValueLData.left =  new FormAttachment(0, 1000, 138);
								frequencyValueLData.top =  new FormAttachment(0, 1000, 8);
								frequencyValue.setLayoutData(frequencyValueLData);
							}
							{
								frequency = new Label(prontoGroup, SWT.NONE);
								frequency.setText("Frequency");
								FormData label8LData = new FormData();
								label8LData.width = 107;
								label8LData.height = 15;
								label8LData.left =  new FormAttachment(0, 1000, 9);
								label8LData.top =  new FormAttachment(0, 1000, 8);
								frequency.setLayoutData(label8LData);
							}
						}
					}
				}
				{
					audioSettingsTab = new CTabItem(settingsFolder, SWT.NONE);
					audioSettingsTab.setText("Audio Settings");
					{
						audioSettingsComposite = new Composite(settingsFolder, SWT.NONE);
						FormLayout audioSettingsCompositeLayout = new FormLayout();
						audioSettingsComposite.setLayout(audioSettingsCompositeLayout);
						audioSettingsTab.setControl(audioSettingsComposite);
						{
							Source = new Group(audioSettingsComposite, SWT.NONE);
							FormLayout SourceLayout = new FormLayout();
							Source.setLayout(SourceLayout);
							Source.setText("Audio Source");
							FormData SourceLData = new FormData();
							SourceLData.width = 308;
							SourceLData.height = 96;
							SourceLData.left =  new FormAttachment(0, 1000, 12);
							SourceLData.top =  new FormAttachment(0, 1000, 12);
							SourceLData.right =  new FormAttachment(1000, 1000, -12);
							Source.setLayoutData(SourceLData);
							{
								invertSelection = new Button(Source, SWT.CHECK | SWT.LEFT);
								FormData invertSelectionLData = new FormData();
								invertSelectionLData.left =  new FormAttachment(0, 1000, 9);
								invertSelectionLData.top =  new FormAttachment(0, 1000, 72);
								invertSelectionLData.width = 85;
								invertSelectionLData.height = 15;
								invertSelection.setLayoutData(invertSelectionLData);
								invertSelection.setText("Invert signal");
							}
							{
								mono = new Button(Source, SWT.RADIO | SWT.LEFT);
								mono.setSelection(m_Sampler.getChannel() == AudioProtocolPort.Channel.MONO);
								mono.setText("Mono");
								FormData monoLData = new FormData();
								monoLData.width = 79;
								monoLData.height = 16;
								monoLData.left =  new FormAttachment(0, 1000, 9);
								monoLData.top =  new FormAttachment(0, 1000, 44);
								mono.setLayoutData(monoLData);
							}
							{
								right = new Button(Source, SWT.RADIO | SWT.LEFT);
								right.setSelection(m_Sampler.getChannel() == AudioProtocolPort.Channel.RIGHT);
								right.setText("Right Channel");
								FormData rightLData = new FormData();
								rightLData.width = 98;
								rightLData.height = 16;
								rightLData.left =  new FormAttachment(0, 1000, 207);
								rightLData.top =  new FormAttachment(0, 1000, 44);
								right.setLayoutData(rightLData);
							}
							{
								left = new Button(Source, SWT.RADIO | SWT.LEFT);
								left.setSelection(m_Sampler.getChannel() == AudioProtocolPort.Channel.LEFT);
								left.setText("Left Channel");
								FormData leftLData = new FormData();
								leftLData.width = 107;
								leftLData.height = 16;
								leftLData.left =  new FormAttachment(0, 1000, 94);
								leftLData.top =  new FormAttachment(0, 1000, 44);
								left.setLayoutData(leftLData);
							}
							{
								label1 = new Label(Source, SWT.NONE);
								label1.setText("Source");
								FormData label1LData = new FormData();
								label1LData.width = 52;
								label1LData.height = 17;
								label1LData.left =  new FormAttachment(26, 1000, 0);
								label1LData.right =  new FormAttachment(191, 1000, 0);
								label1LData.top =  new FormAttachment(125, 1000, 0);
								label1.setLayoutData(label1LData);
							}
							{
								combo1 = new Combo(Source, SWT.NONE);
								FormData combo1LData = new FormData();
								combo1LData.width = 202;
								combo1LData.height = 21;
								combo1LData.left =  new FormAttachment(210, 1000, 0);
								combo1LData.right =  new FormAttachment(941, 1000, 0);
								combo1LData.top =  new FormAttachment(125, 1000, 0);
								combo1LData.bottom =  new FormAttachment(401, 1000, 0);
								combo1.setLayoutData(combo1LData);
							}
						}
						{
							flank = new Group(audioSettingsComposite, SWT.NONE);
							FormLayout flankLayout = new FormLayout();
							flank.setLayout(flankLayout);
							flank.setText("Flank Detection");
							FormData flankLData = new FormData();
							flankLData.width = 307;
							flankLData.height = 137;
							flankLData.left =  new FormAttachment(0, 1000, 12);
							flankLData.top =  new FormAttachment(0, 1000, 138);
							flankLData.right =  new FormAttachment(1000, 1000, -13);
							flank.setLayoutData(flankLData);
							{
								label7 = new Label(flank, SWT.NONE);
								label7.setText("Samples");
								FormData label7LData = new FormData();
								label7LData.width = 69;
								label7LData.height = 15;
								label7LData.left =  new FormAttachment(0, 1000, 235);
								label7LData.top =  new FormAttachment(0, 1000, 79);
								label7.setLayoutData(label7LData);
							}
							{
								holdoff = new Text(flank, SWT.NONE);
								holdoff.setText(Integer.toString(m_FlankDetector.getFlankHoldoff()));
								FormData holdoffLData = new FormData();
								holdoffLData.width = 36;
								holdoffLData.height = 15;
								holdoffLData.left =  new FormAttachment(0, 1000, 173);
								holdoffLData.top =  new FormAttachment(0, 1000, 79);
								holdoff.setLayoutData(holdoffLData);
							}
							{
								label4 = new Label(flank, SWT.NONE);
								label4.setText("Flank Holdoff (1-100)");
								FormData label4LData = new FormData();
								label4LData.width = 152;
								label4LData.height = 15;
								label4LData.left =  new FormAttachment(0, 1000, 9);
								label4LData.top =  new FormAttachment(0, 1000, 79);
								label4.setLayoutData(label4LData);
							}
							{
								label3 = new Label(flank, SWT.NONE);
								label3.setText("Samples");
								FormData label3LData = new FormData();
								label3LData.width = 69;
								label3LData.height = 15;
								label3LData.left =  new FormAttachment(0, 1000, 235);
								label3LData.top =  new FormAttachment(0, 1000, 57);
								label3.setLayoutData(label3LData);
							}
							{
								length = new Text(flank, SWT.NONE);
								length.setText(Integer.toString(m_FlankDetector.getFlankLength()));
								FormData lengthLData = new FormData();
								lengthLData.width = 36;
								lengthLData.height = 15;
								lengthLData.left =  new FormAttachment(0, 1000, 173);
								lengthLData.top =  new FormAttachment(0, 1000, 57);
								length.setLayoutData(lengthLData);
							}
							{
								labelx1 = new Label(flank, SWT.NONE);
								labelx1.setText("Flank Length (1-5)");
								FormData lengthLData = new FormData();
								lengthLData.width = 152;
								lengthLData.height = 15;
								lengthLData.left =  new FormAttachment(0, 1000, 9);
								lengthLData.top =  new FormAttachment(0, 1000, 57);
								labelx1.setLayoutData(lengthLData);
							}
							{
								swing = new Text(flank, SWT.NONE);
								swing.setText(Integer.toString(m_FlankDetector.getFlankSwing()));
								FormData swingLData = new FormData();
								swingLData.width = 36;
								swingLData.height = 15;
								swingLData.left =  new FormAttachment(0, 1000, 173);
								swingLData.top =  new FormAttachment(0, 1000, 35);
								swing.setLayoutData(swingLData);
							}
							{
								label2 = new Label(flank, SWT.NONE);
								label2.setText("Flank Swing (10 - 255)");
								FormData label2LData = new FormData();
								label2LData.width = 152;
								label2LData.height = 15;
								label2LData.left =  new FormAttachment(0, 1000, 9);
								label2LData.top =  new FormAttachment(0, 1000, 35);
								label2.setLayoutData(label2LData);
							}
							{
								useFilter = new Button(flank, SWT.CHECK | SWT.LEFT);
								useFilter.setSelection(m_Model.getFilter().isActive());
								useFilter.setText("Apply 6000Hz Low Pass FIR Filter");
								FormData useFilterLData = new FormData();
								useFilterLData.width = 231;
								useFilterLData.height = 15;
								useFilterLData.left =  new FormAttachment(0, 1000, 9);
								useFilterLData.top =  new FormAttachment(0, 1000, 8);
								useFilter.setLayoutData(useFilterLData);
							}
							{
								label6 = new Label(flank, SWT.NONE);
								label6.setText("Samples");
								FormData label6LData = new FormData();
								label6LData.width = 39;
								label6LData.height = 13;
								label6LData.left =  new FormAttachment(0, 1000, 173);
								label6LData.top =  new FormAttachment(0, 1000, -53);
								label6.setLayoutData(label6LData);
							}
							{
								label11 = new Label(flank, SWT.NONE);
								label11.setText("Burst Length comp.");
								FormData label11LData = new FormData();
								label11LData.width = 152;
								label11LData.height = 15;
								label11LData.left =  new FormAttachment(0, 1000, 9);
								label11LData.top =  new FormAttachment(0, 1000, 101);
								label11.setLayoutData(label11LData);
							}
							{
								burstLengthComp = new Text(flank, SWT.NONE);
								burstLengthComp.setText(Integer.toString(m_FlankDetector.getPulseWidthCompensation()));
								FormData burstLengthCompLData = new FormData();
								burstLengthCompLData.width = 36;
								burstLengthCompLData.height = 15;
								burstLengthCompLData.left =  new FormAttachment(0, 1000, 173);
								burstLengthCompLData.top =  new FormAttachment(0, 1000, 101);
								burstLengthComp.setLayoutData(burstLengthCompLData);
							}
							{
								label12 = new Label(flank, SWT.NONE);
								label12.setText("uS");
								FormData label12LData = new FormData();
								label12LData.width = 69;
								label12LData.height = 15;
								label12LData.left =  new FormAttachment(0, 1000, 235);
								label12LData.top =  new FormAttachment(0, 1000, 101);
								label12.setLayoutData(label12LData);
							}
						}
					}
				}
				{
					culTabItem = new CTabItem(settingsFolder, SWT.NONE);
					culTabItem.setText("Arduino Settings");
					{
						culSettingsComposite = new Composite(settingsFolder, SWT.NONE);
						culTabItem.setControl(culSettingsComposite);
						FormLayout culSettingsCompositeLayout = new FormLayout();
						culSettingsComposite.setLayout(culSettingsCompositeLayout);
						{
							group2 = new Group(culSettingsComposite, SWT.NONE);
							FormLayout group2Layout = new FormLayout();
							group2.setLayout(group2Layout);
							FormData group2LData = new FormData();
							group2LData.width = 323;
							group2LData.height = 41;
							group2LData.left =  new FormAttachment(0, 1000, 12);
							group2LData.top =  new FormAttachment(0, 1000, 12);
							group2.setLayoutData(group2LData);
							group2.setText("USB Serial port");
							{
								culPortCombo = new Combo(group2, SWT.NONE);
								culPortCombo.setText("Serial Port");
								FormData culPortComboLData = new FormData();
								culPortComboLData.left =  new FormAttachment(0, 1000, 116);
								culPortComboLData.top =  new FormAttachment(0, 1000, 8);
								culPortComboLData.width = 169;
								culPortComboLData.height = 21;
								culPortCombo.setLayoutData(culPortComboLData);
							}
							{
								culSerialLabel = new Label(group2, SWT.NONE);
								culSerialLabel.setText("Arduino Serial Port");
								FormData culSerialLabelLData = new FormData();
								culSerialLabelLData.left =  new FormAttachment(0, 1000, 9);
								culSerialLabelLData.top =  new FormAttachment(0, 1000, 8);
								culSerialLabelLData.width = 101;
								culSerialLabelLData.height = 15;
								culSerialLabel.setLayoutData(culSerialLabelLData);
							}
						}
						{
							group1 = new Group(culSettingsComposite, SWT.NONE);
							FormLayout group1Layout = new FormLayout();
							group1.setLayout(group1Layout);
							FormData group1LData = new FormData();
							group1LData.width = 326;
							group1LData.height = 106;
							group1LData.left =  new FormAttachment(0, 1000, 12);
							group1LData.top =  new FormAttachment(0, 1000, 81);
							group1.setLayoutData(group1LData);
							group1.setText("Input Settings");
							{
								sourceCombo = new Combo(group1, SWT.NONE);
								FormData bandWithComboLData = new FormData();
								bandWithComboLData.left =  new FormAttachment(0, 1000, 116);
								bandWithComboLData.top =  new FormAttachment(0, 1000, 40);
								bandWithComboLData.width = 169;
								bandWithComboLData.height = 21;
								sourceCombo.setLayoutData(bandWithComboLData);
							}
							{
								label13 = new Label(group1, SWT.NONE);
								label13.setText("Signal Source");
								FormData label13LData = new FormData();
								label13LData.left =  new FormAttachment(0, 1000, 9);
								label13LData.top =  new FormAttachment(0, 1000, 40);
								label13LData.width = 125;
								label13LData.height = 15;
								label13.setLayoutData(label13LData);
							}
						}
					}
				}
				settingsFolder.setSelection(0);
			}
			{
				ok = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
				ok.setText("Ok");
				FormData okLData = new FormData();
				okLData.width = 78;
				okLData.height = 23;
				okLData.bottom =  new FormAttachment(1000, 1000, -22);
				okLData.right =  new FormAttachment(1000, 1000, -92);
				ok.setLayoutData(okLData);
				ok.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						updateData();
						dialogShell.close();
					}
				});
			}
			{
				cancel = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
				cancel.setText("Cancel");
				FormData cancelLData = new FormData();
				cancelLData.width = 78;
				cancelLData.height = 23;
				cancelLData.right =  new FormAttachment(1000, 1000, -5);
				cancelLData.bottom =  new FormAttachment(1000, 1000, -22);
				cancel.setLayoutData(cancelLData);
				cancel.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						dialogShell.close();
					}
				});
			}
			fillInData();
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
	

	protected void fillInData() {
		if (m_Sampler == null) return;
		
		// Source
		Mixer.Info mixers[] = m_Sampler.getSourceList();
		for (int i = 0; i < mixers.length; i++) {
			combo1.add(mixers[i].getName());
		}
		combo1.select(m_Sampler.getSource());
		
		String[] ports = m_Model.getPulsePort().getPortNames();
		culPortCombo.add(m_Model.getPulsePort().getSerialPort());
		int selection = 0;
		for (int i = 0; i < ports.length; i++){
			culPortCombo.add(ports[i]);
			if (ports[i].equals(m_Model.getPulsePort().getSerialPort())) {
				selection = i + 1;
			}
		}
		culPortCombo.select(selection);
		
		// Channel
		left.setSelection(m_Sampler.getChannel() == AudioProtocolPort.Channel.LEFT);
		right.setSelection(m_Sampler.getChannel() == AudioProtocolPort.Channel.RIGHT);
		mono.setSelection(m_Sampler.getChannel() == AudioProtocolPort.Channel.MONO);
		
		// Filter
		useFilter.setSelection(m_Model.getFilter().isActive());

        // Inversion
        invertSelection.setSelection(m_Model.getInverter().isInverted());
		
		// Swing
		swing.setText(Integer.toString(m_FlankDetector.getFlankSwing()));

		// Length
		length.setText(Integer.toString(m_FlankDetector.getFlankLength()));

		// Holdoff
		holdoff.setText(Integer.toString(m_FlankDetector.getFlankHoldoff()));
		
		// Pulse length modification
		burstLengthComp.setText(Integer.toString(m_FlankDetector.getPulseWidthCompensation()));

		// Pronto Frequency
		frequencyValue.setText(Integer.toString((int)m_Model.getProntoDecoder().getModulationFrequency()));
		
		// Lead out length
		leadOutValue.setText(Integer.toString(m_Model.getProntoDecoder().getLeadOutTime()));	
		
		// Signal Hardware
		selectCULSource.setSelection(m_Model.getSignalHardware() == 1);
		selectAudioSource.setSelection(m_Model.getSignalHardware() == 0);
		
		// Bandwidth settings
		/*
		for (double bw : m_Model.getPulsePort().getBandwidths()) {
			bandWithCombo.add(String.format("%.2f KHz", bw / 1000));
		}
		bandWithCombo.select(m_Model.getPulsePort().getBandwidthOrdinal());
		*/
		sourceCombo.add("RF-Signal (Radio)");
		sourceCombo.add("IR-Signal");
		sourceCombo.select(m_Model.getArduinoChannel().number - 1);
		// AGC settings
		//agcCombo.add(String.format("%06X", m_Model.getPulsePort().getAGCSettings()));
		/*
		agcCombo.add("030091");
		agcCombo.add("040091");
		agcCombo.add("050091");
		agcCombo.add("060091");
		agcCombo.add("070091");
		agcCombo.add("030092");
		agcCombo.add("040092");
		agcCombo.add("050092");
		agcCombo.add("060092");
		agcCombo.add("070092");
		agcCombo.select(0);
		*/
		// FrequencyText.setText(Double.toString(m_Model.getPulsePort().getRadioFrequency()/1000000.0));
	}

	protected void updateData() {
		if (m_Sampler == null) return;
		
		try {
			// Source
			m_Sampler.setSource(combo1.getSelectionIndex());
			
			m_Model.getPulsePort().setSerialPort(culPortCombo.getItem(culPortCombo.getSelectionIndex()));

			// Channel
			m_Sampler.setChannel(mono.getSelection() ? AudioProtocolPort.Channel.MONO : 
								 	left.getSelection() ? AudioProtocolPort.Channel.LEFT :
								 		AudioProtocolPort.Channel.RIGHT);

			// Filter
			m_Model.getFilter().setActive(useFilter.getSelection());

            // Inversion
            m_Model.getInverter().setInverted(invertSelection.getSelection());

			// Swing
			m_FlankDetector.setFlankSwing(getInt(swing, 10, 255, m_FlankDetector.getFlankSwing()));

			// Length
			m_FlankDetector.setFlankLength(getInt(length, 1, 5, m_FlankDetector.getFlankLength()));

			// Holdoff
			m_FlankDetector.setFlankHoldoff(getInt(holdoff, 1, 100, m_FlankDetector.getFlankHoldoff()));
			
			// Pulse length modification
			m_FlankDetector.setPulseWidthCompensation(getInt(burstLengthComp, -1000, 1000, m_FlankDetector.getPulseWidthCompensation()));

			// Pronto Frequency
			m_Model.getProntoDecoder().setModulationFrequency(getDouble(frequencyValue, 30000.0, 50000.0, m_Model.getProntoDecoder().getModulationFrequency(), 1));
			
			// Lead out length
			m_Model.getProntoDecoder().setLeadOutTime(getInt(leadOutValue, 1000, 100000, m_Model.getProntoDecoder().getLeadOutTime()));
			
			// Signal hardware
			if (selectCULSource.getSelection()) {
				m_Model.setSignalHardware(1);
			}
			else {
				m_Model.setSignalHardware(0);
			}
			if (sourceCombo.getSelectionIndex() == 0) {
				m_Model.setArduinoChannel(ArduinoProtocolPort.InputChannel.RF);
			} else {
				m_Model.setArduinoChannel(ArduinoProtocolPort.InputChannel.IR);
			}
			
			//m_Model.getPulsePort().setBandwidthOrdinal(bandWithCombo.getSelectionIndex());
			
			//m_Model.getPulsePort().setAGCSettings(Integer.parseInt(agcCombo.getItem(agcCombo.getSelectionIndex()), 16));
			
			// m_Model.getPulsePort().setRadioFrequency(getDouble(FrequencyText, 0.0, 1000.0, m_Model.getPulsePort().getRadioFrequency(), 1000000));
		}
		catch (NumberFormatException n) {
			//NYI
		}
	}
	
	protected int getInt(Text source, int min, int max, int old) {
		int newValue = old;
		try {
			newValue = Integer.parseInt(source.getText());
			if ((newValue < min) || (newValue > max)) newValue = old;
		}
		catch (NumberFormatException n) {
		}
		return newValue;
	}
	
	protected Double getDouble(Text source, Double min, Double max, Double old, int factor) {
		Double newValue = old;
		try {
			newValue = Double.parseDouble(source.getText()) * factor;
			if ((newValue < min  * factor) || (newValue > max  * factor)) newValue = old;
		}
		catch (NumberFormatException n) {
		}
		return newValue;
	}
	
	public Text getFrequencyText() {
		return FrequencyText;
	}
	
	public Combo getSourceCombo() {
		return sourceCombo;
	}

}
