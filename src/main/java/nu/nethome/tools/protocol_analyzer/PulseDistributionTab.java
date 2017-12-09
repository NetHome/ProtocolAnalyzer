package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.RawProtocolMessage;
import nu.nethome.util.ps.impl.PulseLengthAnalyzer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PulseDistributionTab {
    protected PulseLengthAnalyzer pulseAnalyzer = new PulseLengthAnalyzer();
    protected List<PulseLengthAnalyzer.PulseLengthGroup> pulseFrequency;
    XYSeriesCollection distributionData;
    int maxNumberOfPulses;
    XYSeries selectedIntervalSeries;

    public PulseDistributionTab(RawProtocolMessage message, CTabFolder chartFolder) {
        distributionData = createPulseDistributionPlot(message.m_PulseLengths);
        selectedIntervalSeries = new XYSeries("Selected Interval");
        distributionData.addSeries(selectedIntervalSeries);

        CTabItem distributionTab = new CTabItem(chartFolder, SWT.NONE);
        distributionTab.setText("Pulse length Distribution");

        // Create a Chart and a panel for pulse length distribution
        JFreeChart distributionChart = ChartFactory.createXYLineChart("Pulse Length Distribution", "Pulse Length (us)", "# Pulses", distributionData, PlotOrientation.VERTICAL, true, false, false);
        ChartPanel distributionChartPanel = new ChartPanel(distributionChart);
        RawSignalWindow.configurePanelLooks(distributionChart, 2);
        distributionChartPanel.setPreferredSize(new Dimension(700, 270));// 270

        // Make the mark line dashed, so we can see the space line when they overlap
        float pattern[] = {5.0f, 5.0f};
        BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) distributionChart.getXYPlot().getRenderer();
        renderer.setSeriesStroke(0, stroke);

        // Create a ChartComposite on our tab for pulse distribution
        ChartComposite distributionFrame = new ChartComposite(chartFolder, SWT.NONE, distributionChart, true);
        distributionFrame.setHorizontalAxisTrace(false);
        distributionFrame.setVerticalAxisTrace(false);
        distributionFrame.setDisplayToolTips(true);
        GridData distributionGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
        distributionGridData.grabExcessHorizontalSpace = true;
        distributionGridData.grabExcessVerticalSpace = false;
        distributionGridData.heightHint = 270;
        distributionFrame.setLayoutData(distributionGridData);
        distributionTab.setControl(distributionFrame);
    }

    /**
     * Transform data to pulse distribution for mark and space flanks. We only count
     * pulses under 10 ms and group them in 10 us wide groups counting how many
     * pulses are within each 10 us group.
     */
    XYSeriesCollection createPulseDistributionPlot(LinkedList<Double> pulseLengths) {
        XYSeries markPulseSeries = new XYSeries("Mark pulses");
        XYSeries spacePulseSeries = new XYSeries("Space pulses");
//        XYSeries peakSeries = new XYSeries("peak pulses");
        int markFrequency[] = new int[1000];
        int spaceFrequency[] = new int[1000];

        boolean mark = false;
        for (double length : pulseLengths) {
            if (length < 10000.0) {
                int lengthInterval = (int) (length / 10);
                if (mark) {
                    markFrequency[lengthInterval]++;
                    updateMax(markFrequency[lengthInterval]);
                } else {
                    spaceFrequency[lengthInterval]++;
                    updateMax(spaceFrequency[lengthInterval]);
                }
            }
            mark = !mark;
        }

        for (int i = 0; i < 1000; i++) {
            markPulseSeries.add(i * 10, markFrequency[i]);
            markPulseSeries.add((i + 1) * 10, markFrequency[i]);
            spacePulseSeries.add(i * 10, spaceFrequency[i]);
            spacePulseSeries.add((i + 1) * 10, spaceFrequency[i]);
        }

        // Loop through the pulse distribution groups and find "peaks", which are the centers
        // of pulse groups. Then we sort them to get the highest peaks first and "prime" the
        // pulse group analyzer with them, so the pulse groups get selected with correct centers.
        List<RawSignalWindow.PulseLengthIntervalGroup> peakPulseLengthIntervals = new LinkedList<RawSignalWindow.PulseLengthIntervalGroup>();
        findPeaks(markFrequency, true, peakPulseLengthIntervals);
        findPeaks(spaceFrequency, false, peakPulseLengthIntervals);
        Collections.sort(peakPulseLengthIntervals);
        primePulseAnalyzer(peakPulseLengthIntervals);
        analyzePulsLengths2(pulseLengths);

//		int i1 = 0;
//		for(PulseLengthIntervalGroup group : peakPulseLengthIntervals) {
//			peakSeries.add(group.getCenterLength(), 0);
//			peakSeries.add(group.getCenterLength(), maxNumberOfPulses - i1 * maxNumberOfPulses / 20);
//			peakSeries.add(group.getCenterLength(), 0);
//			i1++;
//		}

        // Create a collection for plotting pulse distribution
        XYSeriesCollection distributionData = new XYSeriesCollection();
        distributionData.addSeries(markPulseSeries);
        distributionData.addSeries(spacePulseSeries);
        return distributionData;
    }

    /**
     * Update the maximum number of pulses with same length
     *
     * @param numberOfPulses
     */
    void updateMax(int numberOfPulses) {
        if (numberOfPulses > maxNumberOfPulses) {
            maxNumberOfPulses = numberOfPulses;
        }
    }

    /**
     * Prime the pulse length interval analyzer, so groups get created with centers where
     * there are peaks in the pulse length distribution
     *
     * @param peakPulseLengthIntervals a list of pulse length intervals which should be sorted
     *                                 so the hintervals with the highest frequency of pulses are first
     */
    void primePulseAnalyzer(List<RawSignalWindow.PulseLengthIntervalGroup> peakPulseLengthIntervals) {
        for (RawSignalWindow.PulseLengthIntervalGroup interval : peakPulseLengthIntervals) {
            pulseAnalyzer.addPrimePulse(interval.m_CenterLength, interval.getIsMark());
        }
    }

    void analyzePulsLengths2(List<Double> pulseList) {
        boolean isMark = false;
        for (Double pulse : pulseList) {
            pulseAnalyzer.addPulse(pulse, isMark);
            isMark = !isMark;
        }
        pulseFrequency = pulseAnalyzer.getPulses();
    }

    void markSelectedPulseLengthInterval(double minLength, double maxLength) {
        distributionData.removeSeries(selectedIntervalSeries);
        selectedIntervalSeries.clear();

        // Mark the selected region in the pulse distribution graph
        selectedIntervalSeries.add(((int) (minLength / 10)) * 10, 0);
        selectedIntervalSeries.add(((int) (maxLength / 10)) * 10 + 10, 0);
        selectedIntervalSeries.add(Float.NaN, Float.NaN);


        // Add the selection series to the graph again
        distributionData.addSeries(selectedIntervalSeries);
    }

    /**
     * Loop through pulse distribution curve and find all peaks. Add those peaks to the
     * result list.
     *
     * @param pulseGroups pulse distribution curve to analyze
     * @param isMark      true if this is mark pulses
     * @param result      list of the peak pulse groups
     */
    protected void findPeaks(int pulseGroups[], boolean isMark, List<RawSignalWindow.PulseLengthIntervalGroup> result) {
        int twoBack = 0;
        int oneBack = 0;

        for (int i = 0; i < pulseGroups.length; i++) {
            int current = pulseGroups[i];
            if ((oneBack > twoBack) && (oneBack >= current)) {
                result.add(new RawSignalWindow.PulseLengthIntervalGroup((i - 1) * 10 + 5, oneBack, isMark));
            }
            twoBack = oneBack;
            oneBack = current;
        }
    }
}