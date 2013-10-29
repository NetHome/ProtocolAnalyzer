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

import nu.nethome.util.plugin.Plugin;
import nu.nethome.util.ps.*;

/**
 * This is a very simple demo of how a protocol decoder is built with the nethome decoder framework.
 * The protocol decoders can be used with the ProtocolAnalyzer and the NethomeServer to decode
 * RF and IR protocols.
 * This DemoDecoder does not decode any real protocol, it simply interprets lengths of pulses and spaces
 * as 0 and 1. It serves to demonstrate how a real decoder would be written
 *
 * Note that the class is annotated as a "Plugin" which makes it possible for the ProtocolAnalyzer to load it
 * dynamically. All you have to do is to pack the class in a jar and place the jar in the "plugins" folder.
 *
 * @author Stefan Strömberg
 *
 */
@Plugin
public class DemoDecoder implements ProtocolDecoder {
	private static final int IDLE = 0;
	private static final int READING_MESSAGE = 1;

    private static final int MESSAGE_LENGTH = 16;

    private int state = IDLE;
	private long data = 0;
	private long lastData = 0;
	private int bitCounter = 0;
	private int repeatCount = 0;
    private ProtocolDecoderSink sink = null;
    private double lastPulse;

    /**
     * This is called by the framework to inform the decoder of the current sink
     * @param sink
     */
    public void setTarget(ProtocolDecoderSink sink) {
		this.sink = sink;
	}

    /**
     * This is called by the framework to get information about the decoder
     * @return information about the decoder
     */
	public ProtocolInfo getInfo() {
		return new ProtocolInfo("Demo", "Pulse Length", "Nethome", MESSAGE_LENGTH, 5);
	}

    /**
     * This is the method which the framework constantly feeds with pulses from the receiver. The decoder
     * has to implement a state machine which interprets the pulse train and decodes its messages.
     *
     * @param pulse Length of the pulse in micro seconds
     * @param state true for a mark pulse and false for a space pulse
     * @return the internal state of the decoder after decoding the pulse.
     */
	public int parse(double pulse, boolean state) {
		switch (this.state) {
			case IDLE: {
				if ((pulse > 200)  && (lastPulse > 20000) && state) {
					this.state = READING_MESSAGE;
				}
				break;
			}
			case READING_MESSAGE: {
				if ((pulse > 200) && (pulse < 1000)) {
                    addBit(0);
                } else if ((pulse > 1000) && (pulse < 8000)) {
                    addBit(1);
                } else {
					this.state = IDLE;
					data = 0;
					bitCounter = 0;
				}
				break;
			}
		}
        lastPulse = pulse;
        return this.state;
	}

    /**
     * An internal helper function which collects decoded bits and assembles messages
     *
     * @param b a decoded bit
     */
    private void addBit(int b) {
        data <<= 1;
        data |= b;
        bitCounter++;
        // Check if this is a complete message
        if (bitCounter == MESSAGE_LENGTH ){
            // It is, read the parameters
            int command = (int) data & 0xFF;
            int address = (int)(data >> 8);

            // Create the message
            ProtocolMessage message = new ProtocolMessage("Demo", command, address, 2);
            message.setRawMessageByteAt(0, command);
            message.setRawMessageByteAt(1, address);

            message.addField(new FieldValue("Command", command));
            message.addField(new FieldValue("Address", address));

            // Check if this is a repeat
            if (data == lastData) {
                repeatCount++;
                message.setRepeat(repeatCount);
            } else {
                repeatCount = 0;
            }

            // Report the parsed message
            sink.parsedMessage(message);
            lastData = data;
            data = 0;
            bitCounter = 0;
            state = IDLE;
        }
    }
}

