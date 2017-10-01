/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolInfo;
import nu.nethome.util.ps.RawProtocolMessage;
import nu.nethome.util.ps.impl.ProtocolSampler;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * The RawDecoder acts as both a ProtocolDecoder and a ProtocolSampler. It is used to
 * save the raw sample data when a signal is detected in the sample stream.
 * When flanks are detected (the parse-method is called) the RawDecoder starts saving
 * all raw samples until the protocol message stops. The raw samples are then
 * reported as a special ProtocolMessage which contains all raw sample data.
 *
 * @author Stefan
 */
public class RawDecoder implements ProtocolDecoder, ProtocolSampler {

    private static final int IDLE = 0;
    private static final int READING_MESSAGE = 1;
    /**
     * Maximum size of a raw sampled message in milliseconds
     */
    private static final int MAX_MESSAGE_LENGTH_MS = 800;
    private static final int RAW_MESSAGE_END_GAP = 29000; // 11000
    private static final int LEVELTIMETOZERO = 1; // X Seconds without signal before level drops to zero
    private static final int REPORTSPERSECOND = 10; // Number of levelreports per second
    private static final int MIN_PULSES_PER_SAMPLE = 4;

    private int state = IDLE;
    private LinkedList<Integer> pulsePositions;
    private boolean isSampling = false;
    private ArrayList<Integer> samples;
    private int sampleCount = 0;
    private int sampleCountAtLastPulse = 0;
    private double lastPulseLength = 0;
    private ProtocolDecoderSink decoderSink = null;
    private int sampleFrequency;
    private int maxMessageLength;
    private boolean isFreeSampling = false;
    private int signalLevel = 0;
    private int signalLevelReportCount = 10;
    private LinkedList<Double> pulseLengths;
    private int maxSampleLength;
    private double totalMessageLength = 0;

    public void setTarget(ProtocolDecoderSink sink) {
        decoderSink = sink;
    }

    public RawDecoder() {
        setSampleRate(22000);
    }

    public ProtocolInfo getInfo() {
        return new ProtocolInfo("Raw", "Flank Length", "-", 0, 5);
    }

    public void setSampleRate(int frequency) {
        sampleFrequency = frequency;
        maxMessageLength = (int) (sampleFrequency * MAX_MESSAGE_LENGTH_MS * 0.001);
    }

    public int getSampleRate() {
        return sampleFrequency;
    }

    /**
     * Forces the class to start saving raw data for the specified number of samples.
     * When the samples are collected they are reported as usual via the
     * ProtocolDecoderSink.
     *
     * @param samples Number of samples to collect.
     */
    public void startFreeSampling(int samples) {
        isFreeSampling = true;
        restartSampler(samples);
    }

    private void restartSampler(int samples) {
        sampleCount = 0;
        totalMessageLength = 0;
        pulsePositions = new LinkedList<Integer>();
        this.samples = new ArrayList<Integer>(samples + 1);
        pulseLengths = new LinkedList<Double>();
        maxSampleLength = samples;
    }

    public void addSample(int sample) {
        calculateSignalLevel(sample);
        if (isSampling || isFreeSampling) {
            samples.add(sample);
            sampleCount++;
            if (sampleCount >= maxSampleLength) {
                boolean trimEnd = !isFreeSampling;
                ReportMessage(trimEnd);
                stopSampler();
            }
        }
    }

    private void calculateSignalLevel(int sample) {
        int absSample = Math.abs(sample);
        if (signalLevel < absSample) signalLevel = absSample;
        signalLevelReportCount--;
        if (signalLevelReportCount <= 0) {
            decoderSink.reportLevel(signalLevel);
            signalLevel -= 127 / (REPORTSPERSECOND * LEVELTIMETOZERO);
            signalLevelReportCount = sampleFrequency / REPORTSPERSECOND;
        }
    }

    public int parse(double pulseLength, boolean isMarkPulse) {
        switch (state) {
            case IDLE: {
                if ((pulseLength > 0.0) && (pulseLength < 200000.0) && !isMarkPulse) {
                    if (!isFreeSampling) {
                        restartSampler(maxMessageLength);
                    }
                    isSampling = true;
                    addPulse(pulseLength);
                    state = READING_MESSAGE;
                }
                break;
            }
            case READING_MESSAGE: {
                totalMessageLength += pulseLength;
                if ((pulseLength == 0) && isMarkPulse && (pulsePositions.size() > 0)) {
                    removeLastSpacePulseToBeReplacedByNext();
                } else if (((pulseLength < RAW_MESSAGE_END_GAP) && (totalMessageLength < 1000000.0)) || isFreeSampling) {
                    addPulse(pulseLength);
                } else {
                    // It has been a long space, so we got our message.
                    if (pulsePositions.size() >= MIN_PULSES_PER_SAMPLE && pulseLength > 0) {
                        ReportMessage(true);
                    }
                    stopSampler();
                }
            }
            break;
        }
        lastPulseLength = pulseLength;
        sampleCountAtLastPulse = sampleCount;
        return state;
    }

    private void removeLastSpacePulseToBeReplacedByNext() {
        pulsePositions.removeLast();
        pulseLengths.removeLast();
    }

    private void addPulse(double pulse) {
        pulsePositions.add(sampleCount);
        pulseLengths.add(pulse);
    }

    private void ReportMessage(boolean trimEnd) {
        // First we trim  off the space from the samples
        for (int i = samples.size() - 1; trimEnd && (i > sampleCountAtLastPulse); i--) {
            samples.remove(i);
        }
        RawProtocolMessage message = new RawProtocolMessage(pulsePositions, samples, sampleFrequency, pulseLengths);
        // Report the parsed message
        decoderSink.parsedMessage(message);
    }

    private void stopSampler() {
        isSampling = false;
        isFreeSampling = false;
        state = IDLE;
    }
}
