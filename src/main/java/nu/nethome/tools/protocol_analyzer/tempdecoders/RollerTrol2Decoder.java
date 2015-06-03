/**
 * Copyright (C) 2005-2015, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.tools.protocol_analyzer.tempdecoders;

import nu.nethome.coders.RollerTrol;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.util.ps.*;

@Plugin
public class RollerTrol2Decoder implements ProtocolDecoder {
    public static final String ROLLER_TROL_PROTOCOL_NAME = "RollerTrol2";
    public static final int PROTOCOL_BIT_LENGTH = 40;
    public static final ProtocolInfo ROLLERTROL_PROTOCOL_INFO = new ProtocolInfo(ROLLER_TROL_PROTOCOL_NAME, "Mark Length", "RollerTrol2", PROTOCOL_BIT_LENGTH, 1);

    public static final int COMMAND_STOP = 0x55;
    public static final int COMMAND_UP = 0x11;
    public static final int COMMAND_UP_END = 0x1E;
    public static final int COMMAND_DOWN = 0x33;
    public static final int COMMAND_DOWN_END = 0x3C;
    public static final int COMMAND_LEARN = 0xCC;

    public static final PulseLength LONG_PREAMBLE_MARK =
            new PulseLength(RollerTrol2Decoder.class, "LONG_PREAMBLE_MARK", 5170, 4800, 5900);
    public static final PulseLength LONG_PREAMBLE_SPACE =
            new PulseLength(RollerTrol2Decoder.class, "LONG_PREAMBLE_SPACE", 1665, 1000, 2000);
    public static final PulseLength SHORT =
            new PulseLength(RollerTrol2Decoder.class, "SHORT", 360, 200, 500);
    public static final PulseLength LONG =
            new PulseLength(RollerTrol2Decoder.class, "LONG", 770, 600, 900);
    public static final PulseLength REPEAT_SPACE =
            new PulseLength(RollerTrol2Decoder.class, "REPEAT_SPACE", 9100, 8000, 12000);

    public static final BitString.Field COMMAND = new BitString.Field(0, 8);
    public static final BitString.Field CHANNEL = new BitString.Field(32, 8);
    public static final BitString.Field ADDRESS = new BitString.Field(8, 32);

    protected static final int IDLE = 0;
    protected static final int READING_LONG_PREAMBLE_SPACE = 2;
    protected static final int READING_PREAMBLE_MARK = 3;
    protected static final int READING_MARK = 5;
    protected static final int READING_SHORT_SPACE = 6;
    protected static final int READING_LONG_SPACE = 7;
    protected static final int REPEAT_SCAN = 10;


    public static final BitString.Field BYTE4 = new BitString.Field(32, 8);
    public static final BitString.Field BYTE3 = new BitString.Field(24, 8);
    public static final BitString.Field BYTE2 = new BitString.Field(16, 8);
    public static final BitString.Field BYTE1 = new BitString.Field(8, 8);
    public static final BitString.Field BYTE0 = new BitString.Field(0, 8);


    protected ProtocolDecoderSink m_Sink = null;
    BitString data = new BitString();
    protected int state = IDLE;
    private int repeat = 0;

    public void setTarget(ProtocolDecoderSink sink) {
        m_Sink = sink;
    }

    public ProtocolInfo getInfo() {
        return RollerTrol2Decoder.ROLLERTROL_PROTOCOL_INFO;
    }

    protected void addBit(boolean b) {
        data.addLsb(!b);
        if (data.length() == PROTOCOL_BIT_LENGTH) {
            decodeMessage(data);
        }
    }

    public void decodeMessage(BitString binaryMessage) {
        int channel = binaryMessage.extractInt(RollerTrol2Decoder.CHANNEL);
        int address = binaryMessage.extractInt(RollerTrol2Decoder.ADDRESS);
        int command = binaryMessage.extractInt(RollerTrol2Decoder.COMMAND);
        int bytes[] = new int[5];
        bytes[0] = binaryMessage.extractInt(BYTE4);
        bytes[1] = binaryMessage.extractInt(BYTE3);
        bytes[2] = binaryMessage.extractInt(BYTE2);
        bytes[3] = binaryMessage.extractInt(BYTE1);
        bytes[4] = binaryMessage.extractInt(BYTE0);
        ProtocolMessage message = new ProtocolMessage(RollerTrol.ROLLER_TROL_PROTOCOL_NAME, command, channel, 5);
        message.setRawMessageByteAt(0, bytes[0]);
        message.setRawMessageByteAt(1, bytes[1]);
        message.setRawMessageByteAt(2, bytes[2]);
        message.setRawMessageByteAt(3, bytes[3]);
        message.setRawMessageByteAt(4, bytes[4]);

        message.addField(new FieldValue("Command", command));
        message.addField(new FieldValue("Channel", channel));
        message.addField(new FieldValue("Address", address));
        message.setRepeat(repeat);
        m_Sink.parsedMessage(message);
        state = REPEAT_SCAN;
    }

    public int parse(double pulse, boolean bitstate) {
        switch (state) {
            case IDLE: {
                if (RollerTrol2Decoder.LONG_PREAMBLE_MARK.matches(pulse) && bitstate) {
                    data.clear();
                    repeat = 0;
                    state = READING_LONG_PREAMBLE_SPACE;
                }
                break;
            }
            case READING_LONG_PREAMBLE_SPACE: {
                if (RollerTrol2Decoder.LONG_PREAMBLE_SPACE.matches(pulse)) {
                    state = READING_MARK;
                } else {
                    quitParsing(pulse);
                }
                break;
            }
            case READING_MARK: {
                if (RollerTrol2Decoder.SHORT.matches(pulse)) {
                    state = READING_LONG_SPACE;
                    addBit(true);
                } else if (RollerTrol2Decoder.LONG.matches(pulse)) {
                    state = READING_SHORT_SPACE;
                    addBit(false);
                } else {
                    quitParsing(pulse);
                }
                break;
            }
            case READING_SHORT_SPACE: {
                if (RollerTrol2Decoder.SHORT.matches(pulse)) {
                    state = READING_MARK;
                } else {
                    quitParsing(pulse);
                }
                break;
            }
            case READING_LONG_SPACE: {
                if (RollerTrol2Decoder.LONG.matches(pulse)) {
                    state = READING_MARK;
                } else {
                    quitParsing(pulse);
                }
                break;
            }
            case REPEAT_SCAN: {
                if (RollerTrol2Decoder.REPEAT_SPACE.matches(pulse)) {
                    state = READING_PREAMBLE_MARK;
                } else {
                    state = IDLE;
                }
                break;
            }
            case READING_PREAMBLE_MARK: {
                if (RollerTrol2Decoder.LONG_PREAMBLE_MARK.matches(pulse) && bitstate) {
                    state = READING_LONG_PREAMBLE_SPACE;
                    data.clear();
                    repeat++;
                } else {
                    state = IDLE;
                }
                break;
            }
        }
        return state;
    }

    private void quitParsing(double pulseLength) {
        if (data.length() > 5) {
            m_Sink.partiallyParsedMessage(String.format("RollerTrol Pulse: %g ms, State: %d", pulseLength, state), data.length());
        }
        state = IDLE;
    }
}
