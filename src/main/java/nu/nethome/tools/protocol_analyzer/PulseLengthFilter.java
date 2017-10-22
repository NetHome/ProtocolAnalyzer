package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolInfo;

import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;

public class PulseLengthFilter implements ProtocolDecoder {

    private static final int REQUIRED_GOOD_COUNT = 20;
    private static final int ADDITIONAL_PULSES = 6;


    private final ProtocolDecoder downStream;
    private ProtocolDecoderSink sink;
    private int goodCounter = 0;
    private boolean isActive = true;
    private boolean filterIsOpen = false;
    private volatile int level = 0;
    private boolean zeroLevelReported = false;

    public PulseLengthFilter(ProtocolDecoder downStream) {
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
        // NYI
    }

    private boolean vet(double pulse, boolean isMarkPulse) {
        // NYI
        return false;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive)
    {
        this.isActive = isActive;
    }
}
