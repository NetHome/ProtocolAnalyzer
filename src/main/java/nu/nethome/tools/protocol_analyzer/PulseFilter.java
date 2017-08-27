package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolInfo;

import java.util.ArrayDeque;

public class PulseFilter implements ProtocolDecoder {

    private static final int REQUIRED_GOOD_COUNT = 20;
    private static final int ADDITIONAL_PULSES = 6;
    private static final double MIN_GOOD_PULSE_LENGTH = 100.0;
    private static final double MAX_GOOD_PULSE_LENGTH = 3000.0;

    private final ProtocolDecoder downStream;
    private ProtocolDecoderSink sink;
    private ArrayDeque<Double> queue = new ArrayDeque<Double>();
    private int goodCounter = 0;
    private boolean isVetting = true;
    private boolean filterIsOpen = false;

    public PulseFilter(ProtocolDecoder downStream) {
        this.downStream = downStream;
    }

    @Override
    public int parse(double pulseLength, boolean state) {
        int result;
        if (!filterIsOpen) {
            if (vet(pulseLength, state)) {
                addVettedPulses();
                filterIsOpen = true;
            }
            result = 0;
        } else {
            result = downStream.parse(pulseLength, state);
            if (pulseLength > 29000) {
                filterIsOpen = false;
            }
        }
        return result;
    }

    @Override
    public ProtocolInfo getInfo() {
        return null;
    }

    @Override
    public void setTarget(ProtocolDecoderSink sink) {
        this.sink = sink;
    }

    private void addVettedPulses() {
        if (queue.peekFirst()  < 0) {
            queue.removeFirst();
        }
        for (double pulse : queue) {
            downStream.parse(Math.abs(pulse), pulse < 0);
        }
    }

    private boolean vet(double pulse, boolean isMarkPulse) {
        if (!isVetting) {
            return true;
        }
        if (queue.size() >= REQUIRED_GOOD_COUNT + ADDITIONAL_PULSES) {
            queue.removeFirst();
        }
        queue.addLast(isMarkPulse ? -pulse : pulse);
        if ((pulse > MIN_GOOD_PULSE_LENGTH) && (pulse < MAX_GOOD_PULSE_LENGTH)) {
            goodCounter += 1;
            if (goodCounter >= REQUIRED_GOOD_COUNT) {
                goodCounter = REQUIRED_GOOD_COUNT;
                return true;
            }
        } else {
            goodCounter = 0;
        }
        return false;
    }
}
