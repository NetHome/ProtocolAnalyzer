package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.ProtocolDecoderSink;

import java.util.Timer;
import java.util.TimerTask;

public class PulseLevelReporter {
    ProtocolDecoderSink sink;
    volatile int level = 0;
    boolean zeroLevelReported = false;

    public PulseLevelReporter() {
        Timer levelTimer = new Timer("LevelTimer", true);
        levelTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if ((sink != null) && !((level == 0) && zeroLevelReported)) {
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

    void reportPulse() {
        level = 128;
    }

    public void setTarget(ProtocolDecoderSink sink) {
        this.sink = sink;
    }
}