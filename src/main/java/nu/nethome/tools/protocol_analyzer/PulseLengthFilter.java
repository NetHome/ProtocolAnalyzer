package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolInfo;

public class PulseLengthFilter implements ProtocolDecoder {

    private static final int BUFFER_SIZE = 26;
    private static final int PULSES_BACK = 20;
    private static final PulseLengthAnalyzer.PulseRequirements openReq =
            new PulseLengthAnalyzer.PulseRequirements(PULSES_BACK, 130.0, 2, 2, false);
    private static final PulseLengthAnalyzer.PulseRequirements closeReq =
            new PulseLengthAnalyzer.PulseRequirements(PULSES_BACK/2, 10.0, 3, 4, true);
    private final ProtocolDecoder downStream;
    private final PulseLevelReporter pulseLevelReporter = new PulseLevelReporter();
    private final PulseLengthAnalyzer analyzer = new PulseLengthAnalyzer(BUFFER_SIZE);
    private boolean isActive = true;
    private boolean filterIsOpen = false;

    public PulseLengthFilter(ProtocolDecoder downStream) {
        this.downStream = downStream;
    }

    @Override
    public int parse(double pulseLength, boolean state) {
        pulseLevelReporter.reportPulse();
        analyzer.addPulse(pulseLength, state);
        if (!isActive) {
            return downStream.parse(pulseLength, state);
        }
        int result = 0;
        if (!filterIsOpen) {
            if (analyzer.vetPulses(openReq)) {
                result = addBufferedPulses();
                filterIsOpen = true;
            }
        } else {
            if ((!analyzer.vetPulses(closeReq)) && !state) {
                pulseLength = 29000 + 1;
                analyzer.clear();
            }
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
        pulseLevelReporter.setTarget(sink);
    }

    private int addBufferedPulses() {
        boolean isFirst = true;
        while (analyzer.pulseCount() > 0) {
            PulseLengthAnalyzer.Pulse pulse = analyzer.getPulse();
            if (isFirst && pulse.isMark) {
                continue;
            }
            downStream.parse(pulse.length, pulse.isMark);
            isFirst = false;
        }
        return 0;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive)
    {
        this.isActive = isActive;
    }
}
