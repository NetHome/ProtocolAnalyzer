package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolInfo;

import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;

public class PulseFilter implements ProtocolDecoder {

    private static final int REQUIRED_GOOD_COUNT = 20;
    private static final int ADDITIONAL_PULSES = 6;
    private static final double MIN_GOOD_PULSE_LENGTH = 150.0;
    private static final double MAX_GOOD_PULSE_LENGTH = 3000.0;

    private final ProtocolDecoder downStream;
    private ProtocolDecoderSink sink;
    private ArrayDeque<Double> queue = new ArrayDeque<Double>();
    private int goodCounter = 0;
    private boolean isActive = true;
    private boolean filterIsOpen = false;
    private volatile int level = 0;
    private boolean zeroLevelReported = false;
    private double badLevel = 0;

    public PulseFilter(ProtocolDecoder downStream) {
        this.downStream = downStream;
        Timer levelTimer = new Timer("LevelTimer", true);
        levelTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if ((sink != null) && !((level == 0) && zeroLevelReported)){
                    sink.reportLevel(level);
                    zeroLevelReported = (level == 0);
                }
                level -= 12;
                if (level <= 0) {
                    level = 0;
                }
            }
        }, 1000, 100);
    }

    @Override
    public int parse(double pulseLength, boolean state) {
        level = 128;
        if (!isActive) {
            return downStream.parse(pulseLength, state);
        }
        int result;
        if (!filterIsOpen) {
            if (vet(pulseLength, state)) {
                addVettedPulses();
                filterIsOpen = true;
                badLevel = 0;
            }
            result = 0;
        } else {
            if (isaGoodPulse(pulseLength)) {
                badLevel -= 0.01;
                if (badLevel < 0) {
                    badLevel = 0;
                }
            } else {
                badLevel += 2.0;
            }
            if ((badLevel > 3.0) && !state) {
                pulseLength = 29000 + 1;
            }
            result = downStream.parse(pulseLength, state);
            if (pulseLength > 29000) {
                filterIsOpen = false;
                goodCounter = 0;
                queue.clear();
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
        if (queue.size() >= REQUIRED_GOOD_COUNT + ADDITIONAL_PULSES) {
            queue.removeFirst();
        }
        queue.addLast(isMarkPulse ? -pulse : pulse);
        if (isaGoodPulse(pulse)) {
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

    private boolean isaGoodPulse(double pulse) {
        return (pulse > MIN_GOOD_PULSE_LENGTH) && (pulse < MAX_GOOD_PULSE_LENGTH);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive)
    {
        this.isActive = isActive;
    }
}
