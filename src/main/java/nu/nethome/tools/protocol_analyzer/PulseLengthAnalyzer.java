package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.impl.PulseProtocolPort;

/**
 *
 */
public class PulseLengthAnalyzer {
    private final int size;
    private int count;
    private double pulseLengths[];
    private int readPos;
    private int writePos;

    public PulseLengthAnalyzer(int size) {
        this.size = size;
        pulseLengths = new double[size];
    }

    public int size() {
        return size;
    }

    public int pulseCount() {
        return count;
    }

    public void addPulse(double pulseLength, boolean isMark) {
        if (count < size) {
            count++;
        } else {
            readPos = increase(readPos);
        }
        this.pulseLengths[writePos] = pulseLength * (isMark ? 1 : -1);
        writePos = increase(writePos);
    }

    private int increase(int pos) {
        return (pos + 1) % size;
    }

    public Pulse getPulse() {
        Pulse pulse = new Pulse(Math.abs(pulseLengths[readPos]), pulseLengths[readPos] >= 0);
        readPos = increase(readPos);
        count--;
        return pulse;
    }

    public class Pulse {
        public final double length;
        public final boolean isMark;

        public Pulse(double length, boolean isMark) {
            this.length = length;
            this.isMark = isMark;
        }
    }
}
