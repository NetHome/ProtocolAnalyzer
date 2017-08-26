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

import nu.nethome.coders.decoders.*;
import nu.nethome.util.plugin.RawPluginScanner;
import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolMessage;
import nu.nethome.util.ps.RawProtocolMessage;
import nu.nethome.util.ps.impl.*;
import nu.nethome.util.ps.impl.AudioProtocolPort.Channel;
import org.eclipse.swt.widgets.Display;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;


public class Main implements ProtocolDecoderSink {

    private static final float STANDARD_SAMPLE_FREQUENCY = 44100.0F;
    public static final int AUDIO_SAMPLER = 0;
    public static final int CUL_SAMPLER = 1;
    protected int m_SignalHardware = 1;
	protected PulseProtocolPort m_ProtocolPorts[];
	private MainWindow m_View;
	private String m_ExportTemplate = "#PROTOCOL#: #NAME# = #BYTE0HEX#, #BYTE1HEX#";
	private AudioProtocolPort m_AudioSampler;
	private SimpleFlankDetector m_FlankDetector;
	private ProtocolDecoderGroup m_ProtocolDecoders = new ProtocolDecoderGroup();
	private ProtocolSamplerGroup m_Samplers = new ProtocolSamplerGroup();
	private RawDecoder m_Raw;
	private ArduinoProtocolPort pulsePort;
	private ProntoDecoder m_ProntoDecoder;
	private FIRFilter6000 m_Filter;
    private RawPluginScanner m_PluginProvider = new RawPluginScanner();
    private SignalInverter inverter;
	

	public void parsedMessage(ProtocolMessage message) {
		m_View.parsedMessage(message);
	}

	public void parsedRawMessage(RawProtocolMessage message) {
		m_View.parsedRawMessage(message);
	}

	public void partiallyParsedMessage(String protocol, int bits) {
		m_View.partiallyParsedMessage(protocol, bits);
	}
	
	public void reportLevel(int level) {
		m_View.reportLevel(level);
	}

    public Main outer() {
        return Main.this;
    }

	public void go(float sampleRate){

        // Load plugins
        handlePlugins();

		// Create the Protocol-Decoders and add them to the decoder group
		m_ProtocolDecoders.add(new SIRCDecoder());
		m_ProtocolDecoders.add(new RC6Decoder());
		m_ProtocolDecoders.add(new RC5Decoder());
		m_ProtocolDecoders.add(new JVCDecoder());
		m_ProtocolDecoders.add(new ViasatDecoder());
		m_ProtocolDecoders.add(new PioneerDecoder());
		m_ProtocolDecoders.add(new HKDecoder());
		m_ProtocolDecoders.add(new UPMDecoder());
		m_ProtocolDecoders.add(new NexaDecoder());
		m_ProtocolDecoders.add(new NexaLDecoder());
		m_ProtocolDecoders.add(new DeltronicDecoder());
		m_ProtocolDecoders.add(new X10Decoder());
		m_ProtocolDecoders.add(new WavemanDecoder());
		m_ProtocolDecoders.add(new RisingSunDecoder());
		m_ProtocolDecoders.add(new NexaFireDecoder());
		m_ProtocolDecoders.add(new EmotivaDecoder());
		m_ProtocolDecoders.add(new ZhejiangDecoder());
		m_ProtocolDecoders.add(new OregonDecoder());
		m_ProtocolDecoders.add(new FineOffsetDecoder());
		m_ProtocolDecoders.add(new RollerTrolDecoder());
		m_ProtocolDecoders.add(new RollerTrolGDecoder());
		m_ProntoDecoder = new ProntoDecoder(); // Has extra settings which needs to be exposed
		m_ProtocolDecoders.add(m_ProntoDecoder);
		
		// Activate/Deactivate decoders according to last settings
		Preferences p = Preferences.userNodeForPackage(this.getClass());
		for (ProtocolDecoder decoder : m_ProtocolDecoders.getAllDecoders()) {
			if(!p.getBoolean(decoder.getInfo().getName(), true)) {
                m_ProtocolDecoders.setActive(decoder, false);
            }
		}
		
        // Set the Sink - which is this class
        m_ProtocolDecoders.setTarget(this);

        // Create the raw decoder/sampler which also has a protocol decoder interface
        m_Raw = new RawDecoder();
        m_Raw.setTarget( new ProtocolDecoderSink() {
            @Override
            public void parsedMessage(ProtocolMessage message) {
                outer().parsedRawMessage((RawProtocolMessage)message);
            }

            @Override
            public void partiallyParsedMessage(String protocol, int bits) {
                // Not needed
            }

            @Override
            public void reportLevel(int level) {
                outer().reportLevel(level);
            }
        });
        m_ProtocolDecoders.add(m_Raw);

		// Create The Flank Detector and attach the decoders
		m_FlankDetector = new SimpleFlankDetector();
		m_FlankDetector.setProtocolDecoder(m_ProtocolDecoders);
		
		// Add the raw sampler and Flank detector to the samplers group
		m_Samplers.add(m_Raw);
		m_Samplers.add(m_FlankDetector);
		
		// Create the FIR-Filter and attach to the samplers
		m_Filter = new FIRFilter6000(m_Samplers);

        // Create signal Inverter
        inverter = new SignalInverter(m_Filter);
		
		// Create our sampler port and attach the Filter
		m_AudioSampler = new AudioProtocolPort(inverter);
		// Load last settings
		loadAudioPreferences();
        m_AudioSampler.setSampleRate(sampleRate);

        PulseFilter pulseFilter = new PulseFilter(m_ProtocolDecoders);
		pulseFilter.setTarget( new ProtocolDecoderSink() {
			@Override
			public void parsedMessage(ProtocolMessage message) {
				// Not needed
			}

			@Override
			public void partiallyParsedMessage(String protocol, int bits) {
				// Not needed
			}

			@Override
			public void reportLevel(int level) {
				outer().reportLevel(level);
			}
		});
		// Create our CUL-Port and attach the decoders directly to it.
		pulsePort = new ArduinoProtocolPort(pulseFilter);
		// Read configuration for our CUL port
		loadCULPreferences();
		
		m_Raw.setSampleRate((int)m_AudioSampler.getSampleRate());
		
		m_ProtocolPorts = new PulseProtocolPort[2];
		m_ProtocolPorts[AUDIO_SAMPLER] = m_AudioSampler;
		m_ProtocolPorts[CUL_SAMPLER] = pulsePort;
		m_SignalHardware = p.getInt("SignalHardware", 0);
		
	    // Create the display for the main window
		Display display = new Display();

		// Create the main window
		m_View = new MainWindow(display, this);
		
		// Open the main window
		m_View.open();

		// Start scanning for signals
		this.startScanning();
		
		// Start processing GUI events
		while(!m_View.isDisposed()){
			//m_View.updateWindowState(false);
			if(!display.readAndDispatch())
				display.sleep();
		}
		
		this.stopScanning();
		
		// Dispose the window
		display.dispose();
		
		p.putInt("SignalHardware", m_SignalHardware);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        float sampleRate = STANDARD_SAMPLE_FREQUENCY;
		Main application = new Main();
        if (args.length == 2 && args[0].equals("-rate")) {
            sampleRate = Float.parseFloat(args[1]);
        }
		application.go(sampleRate);
	}

    private void handlePlugins() {
        try {
            m_PluginProvider.scanForPlugins(new File("../plugins"));
            List<Class> decoderPlugins = m_PluginProvider.getPluginsForInterface(ProtocolDecoder.class);
            for (Class decoder : decoderPlugins) {
                m_ProtocolDecoders.add((ProtocolDecoder) decoder.newInstance());
            }
        } catch (IOException e) {
            // Failed to find plugins
        } catch (InstantiationException e) {
            // Failed to create plugin
        } catch (IllegalAccessException e) {
            // Failed to create plugin
        }
    }

	public int getSignalHardware() {
		return m_SignalHardware;
	}

	public void setSignalHardware(int signalHardware) {
		m_SignalHardware = signalHardware;
	}
	
	/**
	 * Start scanning for messages using the currently selected port
	 */
	public void startScanning() {
		if (!m_ProtocolPorts[m_SignalHardware].isOpen()) {
			m_ProtocolPorts[m_SignalHardware].open();
		}
	}
	
	/**
	 * Stop scanning for messages
	 */
	public void stopScanning() {
		if (m_ProtocolPorts[m_SignalHardware].isOpen()) {
			m_ProtocolPorts[m_SignalHardware].close();
		}		
	}
	
	/**
	 * Check if current selected port is scanning
	 */
	public boolean isScanning() {
		return m_ProtocolPorts[m_SignalHardware].isOpen();
	}

	/**
	 * Load settings for sampler and flank detector from registry/config file
	 */
	public void loadAudioPreferences() {
		Preferences p = Preferences.userNodeForPackage(this.getClass());
		m_AudioSampler.setSource(p.getInt("SourceNumber", 0) >= 0 ? p.getInt("SourceNumber", 0) : 0);
		if (p.getBoolean("Mono", true)) {
			m_AudioSampler.setChannel(Channel.MONO);
		} else {
			m_AudioSampler.setChannel(p.getBoolean("LeftChannel", true) ? Channel.LEFT : Channel.RIGHT);
		}
		m_FlankDetector.setFlankSwing(p.getInt("FlankSwing", 80));
		m_FlankDetector.setFlankLength(p.getInt("FlankLength", 3));
		m_FlankDetector.setFlankHoldoff(p.getInt("FlankHoldoff", 1));
		m_FlankDetector.setPulseWidthCompensation(p.getInt("PulseWidthCompensation", 0));
		m_Filter.setActive(p.getBoolean("UseFilter", false));
	}
	
	/**
	 * Save settings for sampler and flank detector to registry/config file
	 */
	public void saveAudioPreferences() {
		Preferences p = Preferences.userNodeForPackage(this.getClass());
		p.putInt("SourceNumber", m_AudioSampler.getSource());
		p.putBoolean("Mono", m_AudioSampler.getChannel() == Channel.MONO);
		p.putBoolean("LeftChannel", m_AudioSampler.getChannel() == Channel.LEFT ? true : false);
		p.putInt("FlankSwing", m_FlankDetector.getFlankSwing());
		p.putInt("FlankLength", m_FlankDetector.getFlankLength());
		p.putInt("FlankHoldoff", m_FlankDetector.getFlankHoldoff());
		p.putInt("PulseWidthCompensation", m_FlankDetector.getPulseWidthCompensation());
		p.putBoolean("UseFilter", m_Filter.isActive());
	}

	/**
	 * Load the last saved settings of the CUL port from registry/config file
	 */
	public void loadCULPreferences() {
		Preferences p = Preferences.userNodeForPackage(this.getClass());
		pulsePort.setSerialPort(p.get("CULPort", pulsePort.getSerialPort()));
	}
	
	/**
	 * Save the current settings of the CUL port to registry/config file
	 */
	public void saveCULPreferences() {
		Preferences p = Preferences.userNodeForPackage(this.getClass());
		p.put("CULPort", pulsePort.getSerialPort());
	}

	public AudioProtocolPort getAudioSampler() {
		return m_AudioSampler;
	}

	public SimpleFlankDetector getFlankDetector() {
		return m_FlankDetector;
	}

	public ProtocolDecoderGroup getProtocolDecoders() {
		return m_ProtocolDecoders;
	}

	public ProtocolSamplerGroup getProtocolSamplers() {
		return m_Samplers;
	}

	public String getExportTemplate() {
		return m_ExportTemplate;
	}

	public void setExportTemplate(String exportTemplate) {
		m_ExportTemplate = exportTemplate;
	}

	public RawDecoder getRawDecoder() {
		return m_Raw;
	}

	public ArduinoProtocolPort getPulsePort() {
		return pulsePort;
	}

	public ProntoDecoder getProntoDecoder() {
		return m_ProntoDecoder;
	}

	public FIRFilter6000 getFilter() {
		return m_Filter;
	}

    public SignalInverter getInverter() {
        return inverter;
    }
}
