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

import nu.nethome.util.ps.RawProtocolMessage;
import nu.nethome.util.ps.impl.PulseLengthAnalyzer;
import nu.nethome.util.ps.impl.PulseLengthAnalyzer.PulseLengthGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import java.awt.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.List;


/**
 * @author Stefan
 * 
 * ToDo - Sort on frequency so the intervals are chose better interval  
 */
public class RawMessageDistributionWindow {

	class PulseLengthIntervalGroup implements Comparable<PulseLengthIntervalGroup>{
		
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
	
	/**
	 * The shell for an object edit panel
	 */
	protected Shell m_Shell;
	private final static int PARSED_0 = -200;
	private final static int PARSED_1 = -150;
	private static final int SELECTION_MARK = -190;
	private static final int SELECTION_SPACE = -202;
	private final static int NO_COLUMNS = 5;

	private RawProtocolMessage m_Message;
	protected PulseLengthAnalyzer m_PulseAnalyzer = new PulseLengthAnalyzer();
	protected List<PulseLengthAnalyzer.PulseLengthGroup> m_PulseFrequency;
	private XYSeriesCollection m_DistributionData;
	private XYSeries m_SelectedPulseSeries;
	private Table m_Table;
	private int m_MaxNumberOfPulses;
	private XYSeries m_SelectedIntervalSeries;
	private XYSeriesCollection m_SignalSeriesCollection;
	
	public  RawMessageDistributionWindow(Display display, RawProtocolMessage message) {
		m_Message = message;
		m_Shell = new Shell(display);
		m_Shell.setSize(1000, 500);
		m_Shell.setLayout(new GridLayout());
		m_Shell.setText("Undecoded Signal");
		Image image = new Image(display, this.getClass().getClassLoader().getResourceAsStream("nu/nethome/tools/protocol_analyzer/radar16.png"));
		m_Shell.setImage(image);

	    Iterator<Integer> samples = m_Message.m_Samples.iterator();
	    Iterator<Integer> pulses = m_Message.m_PulseList.iterator();
	    XYSeries markPulseSeries = new XYSeries("Mark pulses");
	    XYSeries spacePulseSeries = new XYSeries("Space pulses");
	    XYSeries peakSeries = new XYSeries("peak pulses");
	    m_SelectedIntervalSeries = new XYSeries("Selected Interval");
	    
	    XYSeries sampleSeries = new XYSeries("Raw data");
	    XYSeries pulseSeries = new XYSeries("Parsed data");
	    m_SelectedPulseSeries = new XYSeries("Selected Pulses", false);

	    double x = 0.0;
	    boolean level = false;

	    // Create a collection for plotting signal
	    m_SignalSeriesCollection = new XYSeriesCollection();

	    // Check what kind of data we have, if it is only pulses, then just generate
	    // the pulse series and if we have samples, then generate the sampleSeries as well
	    if (m_Message.m_Samples.size() == 0) {
	    	// Just generate pulse series
	    	for (double pulse : message.m_PulseLengths) {
	    		pulseSeries.add(x, level ? PARSED_0 : PARSED_1);
		    	level = !level;
		    	pulseSeries.add(x, level ? PARSED_0 : PARSED_1);
		    	x += pulse / 1000.0;
		    	pulseSeries.add(x, level ? PARSED_0 : PARSED_1);
	    	}
	    } else {
	    	// Both pulses and samples
		    int sampleNumber = 0;
		    int nextPulse = pulses.hasNext() ? pulses.next() : 0;

		    // Loop through the samples and pulses and generate coordinates for plotting
	    	while(samples.hasNext()){
	    		double value = (double)(samples.next());
	    		sampleSeries.add(x, value);
	    		// Check if we have reached a pulse flank
	    		if (sampleNumber == nextPulse){
	    			pulseSeries.add(x, level ? PARSED_1 : PARSED_0);
	    			level = !level;
	    			pulseSeries.add(x, level ? PARSED_1 : PARSED_0);
	    			nextPulse = pulses.hasNext() ? pulses.next() : 0;
	    		}
	    		x += 1000.0 / m_Message.m_SampleFrequency;
	    		sampleNumber++;
	    	}
	    	// Only add this series if we have samples
		    m_SignalSeriesCollection.addSeries(sampleSeries);
	    }
	    
	    m_SignalSeriesCollection.addSeries(pulseSeries);
	    m_SignalSeriesCollection.addSeries(m_SelectedPulseSeries);
	    
	    // Transform data to pulse distribution for mark and space flanks. We only count
	    // pulses under 10 ms and group them in 10 us wide groups counting how many
	    // pulses are within each 10 us group.
	    int markFrequency[] = new int[1000];
	    int spaceFrequency[] = new int[1000];
	    
	    boolean mark = false;
	    for (double length : message.m_PulseLengths) {
	    	if (length < 10000.0) {
	    		int lengthInterval = (int)(length / 10);
	    		if (mark) {
	    			markFrequency[lengthInterval]++;
	    			updateMax(markFrequency[lengthInterval]);
	    		}
	    		else {
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
	    List<PulseLengthIntervalGroup> peakPulseLengthIntervals = new LinkedList<PulseLengthIntervalGroup>();
	    findPeaks(markFrequency, true, peakPulseLengthIntervals);
	    findPeaks(spaceFrequency, false, peakPulseLengthIntervals);
	    Collections.sort(peakPulseLengthIntervals);
	    primePulseAnalyzer(peakPulseLengthIntervals);
		analyzePulsLengths2(m_Message.m_PulseLengths);
		
		int i1 = 0;
		for(PulseLengthIntervalGroup group : peakPulseLengthIntervals) {
			peakSeries.add(group.getCenterLength(), 0);
			peakSeries.add(group.getCenterLength(), m_MaxNumberOfPulses - i1 * m_MaxNumberOfPulses / 20);
			peakSeries.add(group.getCenterLength(), 0);
			i1++;
		}

	    // Create a collection for plotting pulse distribution
	    m_DistributionData = new XYSeriesCollection();
	    m_DistributionData.addSeries(markPulseSeries);
	    m_DistributionData.addSeries(spacePulseSeries);
	    //m_DistributionData.addSeries(peakSeries);
	    m_DistributionData.addSeries(m_SelectedIntervalSeries);
	    
        // Create Tab Folder for the charts
        CTabFolder chartFolder = new CTabFolder(m_Shell, SWT.NONE);
        GridData folderGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL| GridData.VERTICAL_ALIGN_BEGINNING);
        folderGridData.grabExcessHorizontalSpace = true;
        folderGridData.grabExcessVerticalSpace = false;
        folderGridData.heightHint = 280;
        chartFolder.setLayoutData(folderGridData);

        // Create tab for signal
        CTabItem signalTab = new CTabItem(chartFolder, SWT.NONE);
        signalTab.setText("Signal");
        
	    // Create a Chart and a panel for signal
	    JFreeChart chart = ChartFactory.createXYLineChart("Signal", "ms", "Amplitude", m_SignalSeriesCollection, PlotOrientation.VERTICAL, true, false, false);
	    ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 290));
        configurePanelLooks(chart, 2);

        // Create a ChartComposite on our window
        ChartComposite frame = new ChartComposite(chartFolder, SWT.NONE, chart, true);
        frame.setHorizontalAxisTrace(false);
        frame.setVerticalAxisTrace(false);
        frame.setDisplayToolTips(true);
        GridData gridDatap = new GridData(GridData.HORIZONTAL_ALIGN_FILL| GridData.VERTICAL_ALIGN_BEGINNING);
		gridDatap.grabExcessHorizontalSpace = true;
		gridDatap.grabExcessVerticalSpace = false;
		//gridDatap.heightHint = 270;
		frame.setLayoutData(gridDatap);
		signalTab.setControl(frame);
        
        // Create tab for pulse distribution
        CTabItem distributionTab = new CTabItem(chartFolder, SWT.NONE);
        distributionTab.setText("Pulse length Distribution");

	    // Create a Chart and a panel for pulse length distribution
	    JFreeChart distributionChart = ChartFactory.createXYLineChart("Pulse Length Distribution", "Pulse Length (us)", "# Pulses", m_DistributionData, PlotOrientation.VERTICAL, true, false, false);
	    ChartPanel distributionChartPanel = new ChartPanel(distributionChart);
        configurePanelLooks(distributionChart, 2);
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
        GridData distributionGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL| GridData.VERTICAL_ALIGN_BEGINNING);
		distributionGridData.grabExcessHorizontalSpace = true;
		distributionGridData.grabExcessVerticalSpace = false;
		distributionGridData.heightHint = 270;
		distributionFrame.setLayoutData(distributionGridData);
		distributionTab.setControl(distributionFrame);
        
		// Create the pulse group table
		m_Table = new Table(m_Shell, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_Table.setLayoutData(gridData);

		// Create the columns in the table
		TableColumn tc0 = new TableColumn(m_Table, SWT.CENTER);
		TableColumn tc1 = new TableColumn(m_Table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(m_Table, SWT.CENTER);
		TableColumn tc3 = new TableColumn(m_Table, SWT.CENTER);
		TableColumn tc4 = new TableColumn(m_Table, SWT.CENTER);
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
		m_Table.setHeaderVisible(true);
		
		Iterator<PulseLengthAnalyzer.PulseLengthGroup> spulses = m_PulseFrequency.iterator();
		while (spulses.hasNext()) {
			PulseLengthAnalyzer.PulseLengthGroup l = spulses.next();
			double avg = l.getAvarage();
			
			TableItem item1 = new TableItem(m_Table, SWT.NONE);
			String text[] = new String[NO_COLUMNS];
	        text[0] = l.m_IsMark ? "Mark" : "Space";
	        text[1] = String.format("%.0f uS", avg);
			text[2] = Integer.toString(l.getCount());
			text[3] = String.format("%.0f uS (%.1f%%)", l.m_Min, (l.m_Min/avg - 1)*100);
			text[4] = String.format("%.0f uS (+%.1f%%)", l.m_Max, (l.m_Max/avg - 1)*100);
			item1.setText(text);
		}
		
		m_Table.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
				
			}
			public void widgetSelected(SelectionEvent arg0) {
				//m_DistributionData.removeSeries(1);
				int selectedRow = m_Table.getSelectionIndex();
				PulseLengthGroup pl = m_PulseFrequency.get(selectedRow);
				markPulseInterval(pl.m_Min, pl.m_Max, pl.m_IsMark);
			}
			
		});

 	}

    private void configurePanelLooks(JFreeChart chart, int selectionSeries) {
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
		if (numberOfPulses > m_MaxNumberOfPulses) {
			m_MaxNumberOfPulses = numberOfPulses;
		}
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
		for (PulseLengthIntervalGroup interval : peakPulseLengthIntervals) {
			m_PulseAnalyzer.addPrimePulse(interval.m_CenterLength, interval.getIsMark());
		}
	}

	
	private void analyzePulsLengths2(List<Double> pulseList) {
		boolean isMark = false;
		for (Double pulse : pulseList) {
			m_PulseAnalyzer.addPulse(pulse, isMark); 
			isMark = !isMark;			
		}
		m_PulseFrequency = m_PulseAnalyzer.getPulses();
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
		Iterator<Integer> samples = m_Message.m_Samples.iterator();
		Iterator<Integer> pulses = m_Message.m_PulseList.iterator();

		double x = 0.0;
		double lastX = 0;
		int lastFlank = 0;
		boolean level = false;
		int sampleNumber = 0;
		int nextPulse = pulses.hasNext() ? pulses.next() : 0;

		// Remove the selection data series from the view to speed up handling
		m_SignalSeriesCollection.removeSeries(m_SelectedPulseSeries);
		m_DistributionData.removeSeries(m_SelectedIntervalSeries);

		// Clear them
		m_SelectedPulseSeries.clear();
		m_SelectedIntervalSeries.clear();
		
		// Mark the selected region in the pulse distribution graph
		m_SelectedIntervalSeries.add(((int)(minLength / 10)) * 10, 0);
		m_SelectedIntervalSeries.add(((int)(maxLength / 10)) * 10 + 10, 0);
		m_SelectedIntervalSeries.add(Float.NaN, Float.NaN);

		
		// Add the selection series to the graph again
		m_DistributionData.addSeries(m_SelectedIntervalSeries);
		
	    // Check what kind of data we have, if it is only pulses, generate from them
	    // and if we have samples, then generate from the samples
	    if (m_Message.m_Samples.size() == 0) {
	    	// Generate from pulse series
	    	for (double pulse : m_Message.m_PulseLengths) {
	    		x += pulse;
				// Check if the pulse matches our interval
				if ((pulse >= (minLength - 0.5)) && (pulse <= (maxLength + 0.5)) && (isMark == level)) {
					// If it does, plot the pulse
					//m_SelectedPulseSeries.add(Double.NaN, Double.NaN);
					m_SelectedPulseSeries.add(lastX, SELECTION_MARK);
					m_SelectedPulseSeries.add(x, SELECTION_MARK);
					m_SelectedPulseSeries.add(Double.NaN, Double.NaN);
				}
				lastX = x;
		    	level = !level;
	    	}
	    } else {
	    	// Loop through the samples and pulses and plot the pulses matching the length interval
	    	while(samples.hasNext()){
	    		samples.next();

	    		// Check if we have reached a pulse flank
	    		if (sampleNumber == nextPulse){
	    			nextPulse = pulses.hasNext() ? pulses.next() : 0;

	    			// calculate the pulse length
	    			double length = (sampleNumber - lastFlank) * 1000000.0 / m_Message.m_SampleFrequency;

	    			// Check if the pulse matches our interval
	    			if ((length >= (minLength - 0.5)) && (length <= (maxLength +  0.5)) && (isMark == level)) {
	    				// If it does, plot the pulse
	    				//m_SelectedPulseSeries.add(Double.NaN, Double.NaN);
	    				m_SelectedPulseSeries.add(lastX, SELECTION_MARK);
	    				m_SelectedPulseSeries.add(x, SELECTION_MARK);
	    				m_SelectedPulseSeries.add(Double.NaN, Double.NaN);
	    			}
	    			lastFlank = sampleNumber;
	    			lastX = x;
	    			level = !level;
	    		}
	    		x += 1000.0 / m_Message.m_SampleFrequency;
	    		sampleNumber++;
	    	}
	    }
		
		// Add the selection series to the graph again
		m_SignalSeriesCollection.addSeries(m_SelectedPulseSeries);
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
		int twoBack = 0;
		int oneBack = 0;
		
		for (int i = 0; i < pulseGroups.length; i++) {
			int current = pulseGroups[i];
			if ((oneBack > twoBack) && (oneBack >= current)) {
				result.add(new PulseLengthIntervalGroup((i - 1) * 10 + 5, oneBack, isMark));
			}
			twoBack = oneBack;
			oneBack = current;
		}
	}
	
	
	public void open() {
		// Open the panel
		m_Shell.open();
	}
	
	public boolean isDisposed() {
		return m_Shell.isDisposed();
	}

}
