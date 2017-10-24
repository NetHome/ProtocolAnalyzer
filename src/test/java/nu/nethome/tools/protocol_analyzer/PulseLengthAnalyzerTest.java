package nu.nethome.tools.protocol_analyzer;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 *
 */
public class PulseLengthAnalyzerTest {

    private static final int SIZE = 17;
    private PulseLengthAnalyzer pulseLengthAnalyzer;

    @Before
    public void setUp() throws Exception {
        pulseLengthAnalyzer = new PulseLengthAnalyzer(SIZE);
    }

    @Test
    public void createWithSize() throws Exception {
        assertThat(pulseLengthAnalyzer.size(), is(SIZE));
    }

    @Test
    public void initialCountIs0() throws Exception {
        assertThat(pulseLengthAnalyzer.pulseCount(), is(0));
    }

    @Test
    public void countIs2AfterAdding2Pulses() throws Exception {
        pulseLengthAnalyzer.addPulse(10.0, false);
        pulseLengthAnalyzer.addPulse(10.0, true);
        assertThat(pulseLengthAnalyzer.pulseCount(), is(2));
    }

    @Test
    public void countIsSIZEAfterAddingMorePulses() throws Exception {
        for (int i = 0; i < SIZE + 10; i++) {
            pulseLengthAnalyzer.addPulse(10.0, true);
        }
        assertThat(pulseLengthAnalyzer.pulseCount(), is(SIZE));
    }

    @Test
    public void getPulseDecresesCount() throws Exception {
        pulseLengthAnalyzer.addPulse(10.0, true);
        PulseLengthAnalyzer.Pulse pulse = pulseLengthAnalyzer.getPulse();
        assertThat(pulseLengthAnalyzer.pulseCount(), is(0));
    }

    @Test
    public void returnsAddedPulse() throws Exception {
        pulseLengthAnalyzer.addPulse(10.0, true);
        PulseLengthAnalyzer.Pulse pulse = pulseLengthAnalyzer.getPulse();
        assertThat(pulse.length, is(10.0));
        assertThat(pulse.isMark, is(true));
    }

    @Test
    public void returnsAddedPulsesInOrder() throws Exception {
        double firstLength = 10.0;
        double secondLength = 20.0;
        double thirdLength = 30.0;

        pulseLengthAnalyzer.addPulse(firstLength, false);
        pulseLengthAnalyzer.addPulse(secondLength, true);
        pulseLengthAnalyzer.addPulse(thirdLength, false);

        PulseLengthAnalyzer.Pulse pulse1 = pulseLengthAnalyzer.getPulse();
        assertThat(pulse1.length, is(firstLength));
        assertThat(pulse1.isMark, is(false));

        PulseLengthAnalyzer.Pulse pulse2 = pulseLengthAnalyzer.getPulse();
        assertThat(pulse2.length, is(secondLength));
        assertThat(pulse2.isMark, is(true));

        PulseLengthAnalyzer.Pulse pulse3 = pulseLengthAnalyzer.getPulse();
        assertThat(pulse3.length, is(thirdLength));
        assertThat(pulse3.isMark, is(false));
    }

    @Test
    public void getsTheLastSIZEPulses() throws Exception {
        for (int i = 0; i < SIZE + 1; i++) {
            pulseLengthAnalyzer.addPulse(i, true);
        }
        PulseLengthAnalyzer.Pulse pulse1 = pulseLengthAnalyzer.getPulse();
        assertThat(pulse1.length, is(1.0));
        PulseLengthAnalyzer.Pulse pulse2 = pulseLengthAnalyzer.getPulse();
        assertThat(pulse2.length, is(2.0));
    }

    @Test
    public void groupCountIsSIZEUntilSIZEPulsesHasBeenSeen() throws Exception {
        for (int i = 0; i < SIZE - 1; i++) {
            pulseLengthAnalyzer.addPulse(10.0, true);
            assertThat(pulseLengthAnalyzer.groupCount(SIZE), is(SIZE));
        }
    }

    @Test
    public void findsOneGroupWhenAllValuesAreSame() throws Exception {
        for (int i = 0; i < SIZE ; i++) {
            pulseLengthAnalyzer.addPulse(10.0, true);
        }
        assertThat(pulseLengthAnalyzer.groupCount(SIZE), is(1));
    }

    @Test
    public void finds3GroupsWhen3DifferentValuesAreAdded() throws Exception {
        for (int i = 0; i < SIZE ; i++) {
            pulseLengthAnalyzer.addPulse((i % 3) + 1, true);
        }
        assertThat(pulseLengthAnalyzer.groupCount(SIZE), is(3));
    }

    @Test
    public void countsOnlyMarkPulsesInGroups() throws Exception {
        for (int i = 0; i < SIZE ; i++) {
            pulseLengthAnalyzer.addPulse(i, false);
            pulseLengthAnalyzer.addPulse(i % 3, true);
        }
        assertThat(pulseLengthAnalyzer.groupCount(SIZE), is(3));
    }

    @Test
    public void countsOnlySpecifiedNumberOfPulsesInGroups() throws Exception {
        for (int i = 0; i < SIZE ; i++) {
            pulseLengthAnalyzer.addPulse(i, false);
            pulseLengthAnalyzer.addPulse((i % 3) + 1, true);
        }
        assertThat(pulseLengthAnalyzer.groupCount(1), is(1));
        assertThat(pulseLengthAnalyzer.groupCount(3), is(2));
        assertThat(pulseLengthAnalyzer.groupCount(5), is(3));
    }
}