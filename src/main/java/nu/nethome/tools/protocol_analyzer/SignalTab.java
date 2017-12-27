package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.RawProtocolMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;

public class SignalTab {

    private final static int PARSED_0 = -200;
    private final static int PARSED_1 = -150;
    private static final int SELECTION_MARK = -190;
    private static final int SELECTION_SPACE = -202;

    XYSeries selectedPulseSeries;
    XYSeriesCollection signalSeriesCollection;

    public SignalTab(RawProtocolMessage message, CTabFolder chartFolder) {
        // Check what kind of data we have, if it is only pulses, then just generate
        // the pulse series and if we have samples, then generate the sampleSeries as well
        if (messageHasOnlyPulseData(message)) {
            signalSeriesCollection = createPlotDataFromPulsesOnly(message.m_PulseLengths);
        } else {
            signalSeriesCollection = createPlotDataFromSamplesAndPulses(message);
        }
        selectedPulseSeries = new XYSeries("Selected Pulses", false);
        signalSeriesCollection.addSeries(selectedPulseSeries);

        // Create tab for signal
        CTabItem signalTab = new CTabItem(chartFolder, SWT.NONE);
        signalTab.setText("Signal");

        // Create a Chart and a panel for signal
        JFreeChart chart = ChartFactory.createXYLineChart("Signal", "ms", "Amplitude", signalSeriesCollection, PlotOrientation.VERTICAL, true, false, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 290));
        RawSignalWindow.configurePanelLooks(chart, 2);

        // Create a ChartComposite on our window
        ChartComposite frame = new ChartComposite(chartFolder, SWT.NONE, chart, true);
        frame.setHorizontalAxisTrace(false);
        frame.setVerticalAxisTrace(false);
        frame.setDisplayToolTips(true);
        GridData gridDatap = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
        gridDatap.grabExcessHorizontalSpace = true;
        gridDatap.grabExcessVerticalSpace = false;
        //gridDatap.heightHint = 270;
        frame.setLayoutData(gridDatap);
        signalTab.setControl(frame);
    }

    XYSeriesCollection createPlotDataFromSamplesAndPulses(RawProtocolMessage message) {
        double x = 0.0;
        boolean level = false;
        int sampleNumber = 0;
        Iterator<Integer> pulses = message.m_PulseList.iterator();
        int nextPulse = pulses.hasNext() ? pulses.next() : 0;
        XYSeriesCollection signalSeriesCollection = new XYSeriesCollection();
        XYSeries pulseSeries = new XYSeries("Parsed data");
        XYSeries sampleSeries = new XYSeries("Raw data");

        // Loop through the samples and pulses and generate coordinates for plotting
        for (double value : message.m_Samples) {
            sampleSeries.add(x, value);
            // Check if we have reached a pulse flank
            if (sampleNumber == nextPulse) {
                pulseSeries.add(x, level ? PARSED_1 : PARSED_0);
                level = !level;
                pulseSeries.add(x, level ? PARSED_1 : PARSED_0);
                nextPulse = pulses.hasNext() ? pulses.next() : 0;
            }
            x += 1000.0 / message.m_SampleFrequency;
            sampleNumber++;
        }
        signalSeriesCollection.addSeries(pulseSeries);
        signalSeriesCollection.addSeries(sampleSeries);
        return signalSeriesCollection;
    }

    XYSeriesCollection createPlotDataFromPulsesOnly(LinkedList<Double> pulseLengths) {
        double x1 = 0.0;
        boolean level1 = false;
        XYSeriesCollection signalSeriesCollection = new XYSeriesCollection();
        XYSeries pulseSeries = new XYSeries("Parsed data");

        for (double pulse : pulseLengths) {
            pulseSeries.add(x1, level1 ? PARSED_0 : PARSED_1);
            level1 = !level1;
            pulseSeries.add(x1, level1 ? PARSED_0 : PARSED_1);
            x1 += pulse / 1000.0;
            pulseSeries.add(x1, level1 ? PARSED_0 : PARSED_1);
        }
        signalSeriesCollection.addSeries(pulseSeries);
        return signalSeriesCollection;
    }

    boolean messageHasOnlyPulseData(RawProtocolMessage message) {
        return message.m_Samples.size() == 0;
    }

    void markSelectedPulses(double minLength, double maxLength, boolean isMark, RawProtocolMessage rawMessage) {
        Iterator<Integer> samples = rawMessage.m_Samples.iterator();
        Iterator<Integer> pulses = rawMessage.m_PulseList.iterator();

        double x = 0.0;
        double lastX = 0;
        int lastFlank = 0;
        boolean level = false;
        int sampleNumber = 0;
        int nextPulse = pulses.hasNext() ? pulses.next() : 0;

        // Remove the selection data series from the view to speed up handling
        signalSeriesCollection.removeSeries(selectedPulseSeries);

        // Clear them
        selectedPulseSeries.clear();

        // Check what kind of data we have, if it is only pulses, generate from them
        // and if we have samples, then generate from the samples
        if (messageHasOnlyPulseData(rawMessage)) {
            // Generate from pulse series
            for (double pulse : rawMessage.m_PulseLengths) {
                x += pulse;
                // Check if the pulse matches our interval
                if ((pulse >= (minLength - 0.5)) && (pulse <= (maxLength + 0.5)) && (isMark == level)) {
                    // If it does, plot the pulse
                    //m_SelectedPulseSeries.add(Double.NaN, Double.NaN);
                    selectedPulseSeries.add(lastX / 1000, SELECTION_MARK);
                    selectedPulseSeries.add(x / 1000, SELECTION_MARK);
                    selectedPulseSeries.add(Double.NaN, Double.NaN);
                }
                lastX = x;
                level = !level;
            }
        } else {
            // Loop through the samples and pulses and plot the pulses matching the length interval
            while (samples.hasNext()) {
                samples.next();

                // Check if we have reached a pulse flank
                if (sampleNumber == nextPulse) {
                    nextPulse = pulses.hasNext() ? pulses.next() : 0;

                    // calculate the pulse length
                    double length = (sampleNumber - lastFlank) * 1000000.0 / rawMessage.m_SampleFrequency;

                    // Check if the pulse matches our interval
                    if ((length >= (minLength - 0.5)) && (length <= (maxLength + 0.5)) && (isMark == level)) {
                        // If it does, plot the pulse
                        //m_SelectedPulseSeries.add(Double.NaN, Double.NaN);
                        selectedPulseSeries.add(lastX, SELECTION_MARK);
                        selectedPulseSeries.add(x, SELECTION_MARK);
                        selectedPulseSeries.add(Double.NaN, Double.NaN);
                    }
                    lastFlank = sampleNumber;
                    lastX = x;
                    level = !level;
                }
                x += 1000.0 / rawMessage.m_SampleFrequency;
                sampleNumber++;
            }
        }

        // Add the selection series to the graph again
        signalSeriesCollection.addSeries(selectedPulseSeries);
    }
}