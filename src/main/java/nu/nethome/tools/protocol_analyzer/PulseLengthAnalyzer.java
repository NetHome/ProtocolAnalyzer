package nu.nethome.tools.protocol_analyzer;

/**
 *
 */
public class PulseLengthAnalyzer {
    private static final double MARGIN_FOR_SAME_PULSE_LENGTH = .1;
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
        if (count < size) {
            return size;
        }
        return groupCurrentPulses(pulsesBack);
    }

    private int groupCurrentPulses(int pulsesBack) {
        double minPulseLength = Double.MAX_VALUE;
        int markGroupCount = 0;
        double markGroups[] = new double[size];
        int spaceGroupCount = 0;
        double spaceGroups[] = new double[size];
        int currentPos = decrease(writePos);
        for (int i = 0; i < pulsesBack; i++) {
            double pulseLength = pulseLengths[currentPos];
            currentPos = decrease(currentPos);
            minPulseLength = Math.min(minPulseLength, Math.abs(pulseLength));
            if (isMarkPulse(pulseLength)) {
                markGroupCount = placePulseInGroup(markGroupCount, markGroups, pulseLength);
            } else {
                spaceGroupCount = placePulseInGroup(spaceGroupCount, spaceGroups, pulseLength);
            }
        }
        //if (minPulseLength < 120.0) {
        //    groupCount = size;
        //}
        return markGroupCount + 1;
    }

    public boolean vetPulses(PulseRequirements requirements) {
        if (count < size) {
            return requirements.resultOnNotFull;
        }
        double minPulseLength = Double.MAX_VALUE;
        int markGroupCount = 0;
        double markGroups[] = new double[size];
        int spaceGroupCount = 0;
        double spaceGroups[] = new double[size];
        int currentPos = decrease(writePos);
        for (int i = 0; i < requirements.pulsesBack; i++) {
            double pulseLength = pulseLengths[currentPos];
            if (Math.abs(pulseLength) < requirements.minPulseLength) {
                return false;
            }
            currentPos = decrease(currentPos);
            if (isMarkPulse(pulseLength)) {
                markGroupCount = placePulseInGroup(markGroupCount, markGroups, pulseLength);
                if (markGroupCount > requirements.maxMarkGroups) {
                    return false;
                }
            } else {
                spaceGroupCount = placePulseInGroup(spaceGroupCount, spaceGroups, pulseLength);
                if (spaceGroupCount > requirements.maxSpaceGroups) {
                    return false;
                }
            }
        }
        return true;
    }

    private int placePulseInGroup(int groupCount, double[] groups, double pulseLength) {
        int groupCount1 = groupCount;
        for (int j = 0; j < size; j++) {
            if (groups[j] == 0) {
                groups[j] = pulseLength;
                groupCount1 = j;
                break;
            } else if (isCloseTo(groups[j], pulseLength)) {
                break;
            }
        }
        return groupCount1;
    }

    private boolean isCloseTo(double pulseLength, double candidatePulseLength) {
        return Math.abs(pulseLength - candidatePulseLength) / pulseLength <= MARGIN_FOR_SAME_PULSE_LENGTH;
    }

    private boolean isMarkPulse(double pulseLength) {
        return pulseLength >= 0;
    }

    public static class Pulse {
        public final double length;
        public final boolean isMark;

        public Pulse(double length, boolean isMark) {
            this.length = length;
            this.isMark = isMark;
        }
    }

    public static class PulseRequirements {
        public final int pulsesBack;
        public final double minPulseLength;
        public final int maxMarkGroups;
        public final int maxSpaceGroups;
        public final boolean resultOnNotFull;

        public PulseRequirements(int pulsesBack, double minPulseLength, int maxMarkGroups, int maxSpaceGroups, boolean resultOnNotFull) {
            this.pulsesBack = pulsesBack;
            this.minPulseLength = minPulseLength;
            this.maxMarkGroups = maxMarkGroups;
            this.maxSpaceGroups = maxSpaceGroups;
            this.resultOnNotFull = resultOnNotFull;
        }
    }
}
