package nu.nethome.tools.protocol_analyzer;

/**
 *
 */
public class PulseLengthAnalyzer {
    private final int size;
    private int count;
    private double pulseLengths[];
    private int readPos;
    private int writePos;
    private long totalCount;

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
        totalCount++;
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
    private int decrease(int pos) {
        return (pos + size - 1) % size;
    }

    public Pulse getPulse() {
        Pulse pulse = new Pulse(Math.abs(pulseLengths[readPos]), isMarkPulse(pulseLengths[readPos]));
        readPos = increase(readPos);
        count--;
        return pulse;
    }

    public int groupCount(int pulsesBack) {
        if (totalCount < size) {
            return size;
        }
        return groupCurrentPulses(pulsesBack);
    }

    private int groupCurrentPulses(int pulsesBack) {
        int max = 0;
        double groups[] = new double[size];
        int currentPos = decrease(writePos);
        for (int i = 0; i < pulsesBack; i++) {
            double pulseLength = pulseLengths[currentPos];
            currentPos = decrease(currentPos);
            if (isMarkPulse(pulseLength)) {
                for (int j = 0; j < size; j++) {
                    if (groups[j] == 0) {
                        groups[j] = pulseLength;
                        max = j;
                        break;
                    } else if (groups[j] == pulseLength) {
                        break;
                    }
                }
            }
        }
        return max + 1;
    }

    private boolean isMarkPulse(double pulseLength) {
        return pulseLength >= 0;
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
