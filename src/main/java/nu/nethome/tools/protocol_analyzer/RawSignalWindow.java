/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 * <p>
 * This file is part of OpenNetHome (http://www.nethome.nu).
 * <p>
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.tools.protocol_analyzer;

import nu.nethome.util.ps.RawProtocolMessage;
import nu.nethome.util.ps.impl.PulseLengthAnalyzer;
import nu.nethome.util.ps.impl.PulseLengthAnalyzer.PulseLengthGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Stefan
 *
 * ToDo - Sort on frequency so the intervals are chose better interval  
 */
public class RawSignalWindow {

    private final static int NO_COLUMNS = 5;
    protected PulseDistributionTab pulseDistributionTab;
    private SignalTab signalTab;

    protected Shell shell;
    private RawProtocolMessage rawMessage;
    private Table table;

    public RawSignalWindow(Display display, RawProtocolMessage message) {
        rawMessage = message;
        shell = createShell(display);
        CTabFolder chartFolder = createTabFolder(this.shell);

        signalTab = new SignalTab(message, chartFolder);
        pulseDistributionTab = new PulseDistributionTab(message, chartFolder);

        // Create the pulse group table
        table = new Table(this.shell, SWT.SINGLE | SWT.BORDER
                | SWT.FULL_SELECTION);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        table.setLayoutData(gridData);

        // Create the columns in the table
        TableColumn tc0 = new TableColumn(table, SWT.CENTER);
        TableColumn tc1 = new TableColumn(table, SWT.CENTER);
        TableColumn tc2 = new TableColumn(table, SWT.CENTER);
        TableColumn tc3 = new TableColumn(table, SWT.CENTER);
        TableColumn tc4 = new TableColumn(table, SWT.CENTER);
        tc0.setText("Pulse Level");
        tc1.setText("Average Pulse Length");
        tc2.setText("Count");
        tc3.setText("Min Pulse Length");
        tc4.setText("Max Pulse Length");
        tc0.setWidth(80);
        tc1.setWidth(150);
        tc2.setWidth(70);
        tc3.setWidth(150);
        tc4.setWidth(150);
        table.setHeaderVisible(true);

        Iterator<PulseLengthAnalyzer.PulseLengthGroup> spulses = pulseDistributionTab.pulseFrequency.iterator();
        while (spulses.hasNext()) {
            PulseLengthAnalyzer.PulseLengthGroup l = spulses.next();
            double avg = l.getAvarage();

            TableItem item1 = new TableItem(table, SWT.NONE);
            String text[] = new String[NO_COLUMNS];
            text[0] = l.m_IsMark ? "Mark" : "Space";
            text[1] = String.format("%.0f uS", avg);
            text[2] = Integer.toString(l.getCount());
            text[3] = String.format("%.0f uS (%.1f%%)", l.m_Min, (l.m_Min / avg - 1) * 100);
            text[4] = String.format("%.0f uS (+%.1f%%)", l.m_Max, (l.m_Max / avg - 1) * 100);
            item1.setText(text);
        }

        table.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }

            public void widgetSelected(SelectionEvent arg0) {
                //m_DistributionData.removeSeries(1);
                int selectedRow = table.getSelectionIndex();
                PulseLengthGroup pl = pulseDistributionTab.pulseFrequency.get(selectedRow);
                markPulseInterval(pl.m_Min, pl.m_Max, pl.m_IsMark);
            }

        });

    }

    private Shell createShell(Display display) {
        Shell shell_x;
        shell_x = new Shell(display);
        shell_x.setSize(1000, 500);
        shell_x.setLayout(new GridLayout());
        shell_x.setText("Undecoded Signal");
        Image image = new Image(display, this.getClass().getClassLoader().getResourceAsStream("nu/nethome/tools/protocol_analyzer/radar16.png"));
        shell_x.setImage(image);
        return shell_x;
    }

    private CTabFolder createTabFolder(Shell shell1) {
        CTabFolder chartFolder = new CTabFolder(shell1, SWT.NONE);
        GridData folderGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
        folderGridData.grabExcessHorizontalSpace = true;
        folderGridData.grabExcessVerticalSpace = false;
        folderGridData.heightHint = 280;
        chartFolder.setLayoutData(folderGridData);
        return chartFolder;
    }

    /**
     Transform data to pulse distribution for mark and space flanks. We only count
     pulses under 10 ms and group them in 10 us wide groups counting how many
     pulses are within each 10 us group.
     */
    private XYSeriesCollection createPulseDistributionPlot(LinkedList<Double> pulseLengths) {
        //        XYSeries peakSeries = new XYSeries("peak pulses");

        // Loop through the pulse distribution groups and find "peaks", which are the centers
        // of pulse groups. Then we sort them to get the highest peaks first and "prime" the
        // pulse group analyzer with them, so the pulse groups get selected with correct centers.

//		int i1 = 0;
//		for(PulseLengthIntervalGroup group : peakPulseLengthIntervals) {
//			peakSeries.add(group.getCenterLength(), 0);
//			peakSeries.add(group.getCenterLength(), maxNumberOfPulses - i1 * maxNumberOfPulses / 20);
//			peakSeries.add(group.getCenterLength(), 0);
//			i1++;
//		}

        // Create a collection for plotting pulse distribution
        return pulseDistributionTab.createPulseDistributionPlot(pulseLengths);
    }

    private XYSeriesCollection createPlotDataFromSamplesAndPulses(RawProtocolMessage message) {

        // Loop through the samples and pulses and generate coordinates for plotting
        return signalTab.createPlotDataFromSamplesAndPulses(message);
    }

    private XYSeriesCollection createPlotDataFromPulsesOnly(LinkedList<Double> pulseLengths) {

        return signalTab.createPlotDataFromPulsesOnly(pulseLengths);
    }

    private boolean messageHasOnlyPulseData(RawProtocolMessage message) {
        return signalTab.messageHasOnlyPulseData(message);
    }

    public static void configurePanelLooks(JFreeChart chart, int selectionSeries) {
        TextTitle title = chart.getTitle(); // fix title
        Font titleFont = title.getFont();
        titleFont = titleFont.deriveFont(Font.PLAIN, (float) 14.0);
        title.setFont(titleFont);
        title.setPaint(Color.darkGray);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        XYLineAndShapeRenderer signalRenderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        signalRenderer.setSeriesStroke(selectionSeries, new BasicStroke(5f));
    }

    /**
     * Update the maximum number of pulses with same length
     * @param numberOfPulses
     */
    private void updateMax(int numberOfPulses) {
        pulseDistributionTab.updateMax(numberOfPulses);
    }

    /**
     * Prime the pulse length interval analyzer, so groups get created with centers where
     * there are peaks in the pulse length distribution
     *
     * @param peakPulseLengthIntervals a list of pulse length intervals which should be sorted
     * so the hintervals with the highest frequency of pulses are first
     *
     */
    private void primePulseAnalyzer(List<PulseLengthIntervalGroup> peakPulseLengthIntervals) {
        pulseDistributionTab.primePulseAnalyzer(peakPulseLengthIntervals);
    }


    private void analyzePulsLengths2(List<Double> pulseList) {
        pulseDistributionTab.analyzePulsLengths2(pulseList);
    }

/*	
	private void analyzePulsLengths(List<Integer> pulseList) {
		Iterator<Integer> pulses = pulseList.iterator();
		int lastPosition = 0;
		boolean isMark = false;
		while (pulses.hasNext()) {
			int currentPosition = pulses.next();
			double pusleLength = 1000000.0 * (currentPosition - lastPosition) / ((double)m_Message.m_SampleFrequency);
			m_PulseAnalyzer.addPulse(pusleLength, isMark); 
			lastPosition = currentPosition;
			isMark = !isMark;
		}
		m_PulseFrequency = m_PulseAnalyzer.getPulses();
	}
*/

    /**
     * Mark pulses within the specified pulse length interval in the signal graph and indicate the interval
     * in the pulse distribution graph.
     * @param minLength low end of the pulse length interval
     * @param maxLength high end of the pulse length interval
     * @param isMark true if we shall indicate mark pulses, false for space pulses
     */
    protected void markPulseInterval(double minLength, double maxLength, boolean isMark) {
        signalTab.markSelectedPulses(minLength, maxLength, isMark, this.rawMessage);
        pulseDistributionTab.markSelectedPulseLengthInterval(minLength, maxLength);
    }

    private void markSelectedPulses(double minLength, double maxLength, boolean isMark, RawProtocolMessage rawMessage) {

        // Remove the selection data series from the view to speed up handling

        // Clear them

        // Check what kind of data we have, if it is only pulses, generate from them
        // and if we have samples, then generate from the samples

        // Add the selection series to the graph again
        signalTab.markSelectedPulses(minLength, maxLength, isMark, rawMessage);
    }

    private void markSelectedPulseLengthInterval(double minLength, double maxLength) {

        // Mark the selected region in the pulse distribution graph


        // Add the selection series to the graph again
        pulseDistributionTab.markSelectedPulseLengthInterval(minLength, maxLength);
    }

    /**
     * Loop through pulse distribution curve and find all peaks. Add those peaks to the
     * result list.
     *
     * @param pulseGroups pulse distribution curve to analyze
     * @param isMark true if this is mark pulses
     * @param result list of the peak pulse groups
     */
    protected void findPeaks(int pulseGroups[], boolean isMark, List<PulseLengthIntervalGroup> result) {

        pulseDistributionTab.findPeaks(pulseGroups, isMark, result);
    }


    public void open() {
        // Open the panel
        shell.open();
    }

    public boolean isDisposed() {
        return shell.isDisposed();
    }

    public static class PulseLengthIntervalGroup implements Comparable<PulseLengthIntervalGroup> {

        protected int m_CenterLength;
        protected int m_Count;
        protected boolean m_IsMark;

        public PulseLengthIntervalGroup(int centerLength, int count, boolean isMark) {
            m_CenterLength = centerLength;
            m_Count = count;
            m_IsMark = isMark;
        }

        public int compareTo(PulseLengthIntervalGroup o) {
            // TODO Auto-generated method stub
            return (m_Count == o.m_Count) ? 0 : ((m_Count > o.m_Count) ? -1 : 1); // Reverse order
        }

        public int getCenterLength() {
            return m_CenterLength;
        }

        public int getCount() {
            return m_Count;
        }

        public boolean getIsMark() {
            return m_IsMark;
        }
    }


}
