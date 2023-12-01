/**
 * 
 */
package spermWhaleIpi;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import Acquisition.AcquisitionProcess;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import difar.DifarBinaryDataSource;
import difar.DifarSqlLogging;
import fftManager.Complex;
import fftManager.FFT;
import fftManager.FastFFT;
import warnings.PamWarning;
import warnings.WarningSystem;
import PamController.PamController;

/**
 * This is a module for Sperm whale Inter-pulse interval (IPI) computation. It
 * uses cepstral analysis to compute the Inter-pulse interval of sperm whale
 * clicks.Two related signal processing methods are used to obtain an IPI:
 * <p>
 * Method 1 computes the IPI for each click and then averages these IPIs
 * together as described in Goold 1996 - J. Acoust. Soc. Am. Vol 100(5), pp
 * 3431-3441.
 * <p>
 * Method 2 computes the ensemble averaged cepstrum from all clicks and then
 * picks one IPI from this ensemble averaged signal similar to Teloni 2007 - J.
 * Cetacean Res. Manage. Vol 9(2), pp 127-136.
 * 
 * @author Brian Miller
 */
public class IpiProcess extends PamProcess {

	/**
	 * Address of the controlling process. Needed for accessing IPI parameters
	 * etc.
	 */
	IpiController ipiController;

	/**
	 * The number of clicks used to create the ensemble averaged functions
	 */
	private int numClicks;

	/**
	 * Number of samples required to achieve the ipiDuration
	 */
	private int ipiSamples;

	/**
	 * Store the IPI for each click as an element in this vector
	 */
	private Vector<Double> ipi;

	/**
	 * The value of the echoData at the ipiDelay time.
	 */
	private Vector<Double> ipiAmplitude;

	/**
	 * A histogram of the IPIs of each click.
	 * Indicies represent cepstrum time bins. Values hold the bin count.
	 */
	int[] ipiHistogram;
	
	/**
	 * The peak value of the histogam (count of the tallest bin
	 */
	int maxHistogramCount;
	
	/**
	 * The average IPI value from each of the echo detections
	 */
	private double ipiTimeMean;

	/**
	 * Store the ensemble average of each of the echo detection functions Then
	 * we can pick the peak of the ensemble averaged function This is how Teloni
	 * et al 2008 do it.
	 */
	private double[] summedIpiFunction;

	/**
	 * The computed IPI of the whale in milliseconds computed from the ensemble
	 * averaged cepstrum.
	 */
	private double ipiTimeEnsembleAvg;

	/**
	 * This value reflects the uncertainty in the measurement of the ensemble
	 * averaged IPI. The true IPI should be within ipiDelayEnsembleAvg -
	 * ipiDelayLowerLimit.
	 */
	private double ipiTimeLowerLimit;

	/**
	 * This value reflects the uncertainty in the measurement of the ensemble
	 * averaged IPI. The true IPI should be within ipiDelayEnsembleAvg +
	 * ipiDelayUpperLimit.
	 */
	private double ipiTimeUpperLimit;

	/**
	 * The height of the cepstrum at the time of the IPI peak. Used for scaling
	 * the ensemble average (when displayed as a plugin panel).
	 */
	private double ipiAmplitudeEnsembleAvg;
	
	/**
	 * The mode of all the individual IPIs 
	 * (Time bin of the maximum count from the IPI Histogram)
	 */
	private double ipiTimeMode;

	/**
	 * Index of the time bin of the ipiTimeMode
	 */
	private int ipiModeIx;
	
	long avgStartTime;

	private ChannelDetector[] channelDetectors;
	
	private PamDataBlock<ClickDetection> clickDetection;

	private PamDataBlock<IpiDataUnit> ipiDataBlock;
	
	private PamDataBlock<IpiAvgDataUnit> ipiAvgDataBlock;

	public IpiProcess(IpiController ipiController) {
		// you must call the constructor from the super class.
		super(ipiController, null);
		// Store a reference to the controller of this process
		this.ipiController = ipiController;
		ipiDataBlock = new PamDataBlock<IpiDataUnit>(IpiDataUnit.class, 
				ipiController.getUnitName(), this, ipiController.getIpiParameters().channelMap);
		ipiAvgDataBlock = new PamDataBlock<IpiAvgDataUnit>(IpiAvgDataUnit.class,
				ipiController.getUnitName() + " Average", this, ipiController.getIpiParameters().channelMap);
		addOutputDataBlock(ipiDataBlock);
		addOutputDataBlock(ipiAvgDataBlock);
		
		ipiAvgDataBlock.SetLogging(new IpiSqlLogging(ipiController, ipiAvgDataBlock));
		ipiDataBlock.setBinaryDataSource(new IpiBinaryDataSource(ipiController, ipiDataBlock));
		ipiDataBlock.setShouldLog(true);
	}

	@Override
	public void prepareProcess() {
		// TODO Auto-generated method stub
		super.prepareProcess();
		
		/*
		 * Need to hunt aroudn now in the Pamguard model and try to find the click detector
		 */
		ArrayList<PamDataBlock> clickDetectors = PamController.getInstance().getDataBlocks(ClickDetection.class, false);
		if (clickDetectors == null || 
				clickDetectors.size() <= ipiController.getIpiParameters().clickDataBlock) {
			setParentDataBlock(null);
			return;
		}
		/*
		 * If the detection data blocks exist, subscribe to the one in the parameters. 
		 */
		//clickDataBlock = (ClickDataBlock) clickDetectors.get(ipiController.ipiParameters.clickDetector);
		ClickDataBlock clickDataBlock = (ClickDataBlock) clickDetectors.get(ipiController.getIpiParameters().clickDataBlock);
		setParentDataBlock(clickDataBlock);
		
		float dataSampleRate = clickDataBlock.getSampleRate();
		
		/*
		 * usedChannels will be a combination of what we want and what's availble.
		 */
		int usedChannels = clickDataBlock.getChannelMap() & ipiController.getIpiParameters().channelMap;
		
		/*
		 * Tell the output data block which channels data may come from.
		 */
		ipiDataBlock.setChannelMap(usedChannels);
		
		/**
		 * allocate references to a list of detectors - one for each channel used. 
		 */
		channelDetectors = new ChannelDetector[PamUtils.getHighestChannel(usedChannels)+1];
		for (int i = 0; i <= PamUtils.getHighestChannel(usedChannels); i++) {
			if (((1<<i) & usedChannels) > 0) {
				channelDetectors[i] = new ChannelDetector(i);
			}
		}
		
		// Figure out how many samples are required to get the right IPI duration
		long targetSamples = (long) (ipiController.getIpiParameters().ipiDuration);
		
		// For faster FFT processing make this a power of 2
		int ipiDuration = PamUtils.getMinFftLength(targetSamples);

	
		// Figure out how many samples are required to get the right IPI
		// duration
		targetSamples = (long) (clickDataBlock.getSampleRate()
				* ipiController.getIpiParameters().ipiDuration / 1e3);
		

		int clickPreSamples = clickDataBlock.getClickControl().getClickParameters().preSample;
		int clickPostSamples = clickDataBlock.getClickControl().getClickParameters().postSample;
		int clickMinSamples = clickPreSamples + clickPostSamples;
		
		//TODO: Throw a warning if the click detector is not configured correctly.
		// 		Click pre and post samples need to be 4 times the maxIPI duration. 
		if (clickMinSamples < targetSamples) {
			String warnText  = "Warning the Click Detector is not configured correctly for the Sperm Whale IPI module.\n" + 
						"Minimum of " + targetSamples + " samples is required, but pre+post sample total is " +
						clickMinSamples + "\n" + 
						"Increase the pre and/or post samples of the Click Detector to fix this issue.";
			new PamWarning(this.processName, warnText, 1);
		}
		
		// For faster FFT processing make this a power of 2
		ipiSamples = PamUtils.getMinFftLength(targetSamples);

		summedIpiFunction = new double[ipiSamples];
		ipiHistogram = new int[ipiSamples];
		maxHistogramCount = 0;
		clearIpiData();
	}
	
	@Override
	public void pamStart() {
		if (avgStartTime == 0) {
			avgStartTime = PamCalendar.getSessionStartTime();
		}
		
		/**
		 * Clear the ensemble average and histogram on start if desired
		 */
		if (ipiController.getIpiParameters().resetOnStart){
			clearIpiData();
			ipiController.ipiSidePanel.fillData();
		}
		
		
	}

	/**
	 * Save the IPI ensemble average
	 */
	@Override
	public void pamStop() {
		saveIpiAvg();
	}



	public void saveIpiAvg() {
		long endTime = PamCalendar.getTimeInMillis();
		
		/**
		 * For some reason, pamStop() is called twice, so check and make sure we're 
		 * not double saving the same unit.
		 */
		IpiAvgDataUnit latest = ipiAvgDataBlock.getPreceedingUnit(endTime);
		if (latest != null && (latest.endTime == endTime | latest.getTimeMilliseconds() == avgStartTime) ) {
			return;
		}
		
		IpiAvgDataUnit ipiAvg = new IpiAvgDataUnit(avgStartTime, endTime, 
				summedIpiFunction, ipiTimeEnsembleAvg, ipiAmplitudeEnsembleAvg, 
				ipiHistogram, ipiTimeMean, ipiTimeMode,
				ipiTimeLowerLimit, ipiTimeUpperLimit, numClicks);
		ipiAvg.setChannelBitmap(ipiController.getIpiParameters().channelMap);
		ipiAvgDataBlock.addPamData(ipiAvg);
		
	}

	/**
	 * Most of the IPI computation is done here. It makes sense to only compute
	 * things when new data arrive. The new data should contain the cepstrum of
	 * a sperm whale click. We pick the peak and add the cepstrum to the 
	 * ensemble average. 
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		
		// see which channel it's from
		ClickDetection clickDetection = (ClickDetection) arg;
		int usedChannels = clickDetection.getChannelBitmap() & ipiController.getIpiParameters().channelMap;
		
		int chan;
		for (int i = 0; i <= PamUtils.getHighestChannel(usedChannels); i++) {
			int singleChanMap = (1<<i);
			chan = PamUtils.getSingleChannel(singleChanMap);

			// check that a detector has been instantiated for that detector
			if (channelDetectors == null || 
					channelDetectors.length < PamUtils.getNumChannels(usedChannels) || 
					channelDetectors[chan] == null) {
				continue;
			}
			if ((singleChanMap & usedChannels) > 0) {
				channelDetectors[chan].newData(o, clickDetection);
			}
		}
				
		
	
	}

	/**
	 * Since the detector may be running on several channels, make a sub class
	 * for the actual detector  code so that multiple instances may be created. 
	 * @author Doug
	 *
	 */
	class ChannelDetector {
		
		/*
		 * Which channel is this detector operating on
		 */
		int channel;
		
		public ChannelDetector(int channel) {
			this.channel = channel;
		}
		
		/**
		 * Performs the same function as newData in the outer class, but this
		 * time it should only ever get called with data for a single channel.
		 * Take the click detection and if appropriate, output an echoDataUnit
		 * @param o
		 * @param arg - a single channel of a click detection
		 */
		public void newData(PamObservable o, ClickDetection arg) {

			long availableSamples =  arg.getSampleDuration();
			float sampleRate = arg.getParentDataBlock().getSampleRate();
			int minIpiSample = (int) (ipiController.getIpiParameters().minIpiTime / 1e3 * sampleRate / 2);
			int maxIpiSample = (int) (ipiController.getIpiParameters().maxIpiTime / 1e3 * sampleRate / 2);
			
			// Make sure we have enough samples to do the echo detection
			if (availableSamples < ipiSamples) {
				String warnText = " The click with UID " + arg.getUID() //PamCalendar.formatDateTime2(ipiUnit.getTimeMilliseconds())
						+ " was discarded. " + ipiSamples 	+ " samples were required, but only "
						+ availableSamples  + " were available in this click detection.";
				PamWarning warn = new PamWarning("IPI Unit", warnText, 1);
				warn.setEndOfLife(1000);
				WarningSystem.getWarningSystem().addWarning(warn);
				return;
			}
			
			// Compute the cepstrum from the detection data
			double[] echoData = cepstrum(arg.getWaveData(channel), ipiSamples);
						
			maxIpiSample = Math.min(echoData.length, maxIpiSample);

			double ipiAmplitude = 0;
			int ipiIx = 0;
			for (int i = minIpiSample; i < maxIpiSample; i++) {
				if (echoData[i] > ipiAmplitude) {
					ipiAmplitude = echoData[i];
					ipiIx = i;
				}
			}
			/*
			 *  Don't forget the factor of 2 here. This is because the
			 *  bins in the cepstrum correspond to the 1/nyquist rate 
			 *  instead of the sampling period.
			 */
			double ipi = ipiIx / sampleRate * 2;
			
//			IpiDataUnit edu = new IpiDataUnit(arg.getTimeMilliseconds(), arg.getUID(), echoData, ipi, ipiAmplitude, arg.getParentDataBlock().getSampleRate());
			IpiDataUnit edu = new IpiDataUnit(arg, echoData, ipi, ipiAmplitude);

			edu.setChannelBitmap(1 << channel);
			/*
			 * put the unit back into the datablock, at which point all subscribers will
			 * be notified. 
			 */
			ipiDataBlock.addPamData(edu);	
			addToAverageIpi(edu);
		}
	}
	
	private void addToAverageIpi(IpiDataUnit ipiUnit) {
		double[] newIpiFunction = ipiUnit.getEchoData();
		double dataSampleRate = ipiUnit.getSampleRate();
		double peakWidthPercent = ipiController.getIpiParameters().ipiPeakWidthPercent;
		double ipiSum = 0;
		double ipiValue = 0;
		int minIpiIndex, maxIpiIndex, ipiEnsembleAvgIx = 0, ipiDelayIx = 0;

		int availableSamples = newIpiFunction.length;

		// This should never happen...
		if (availableSamples < ipiSamples) {
			String warnText = "Warning, the click with UID " + ipiUnit.getUID() //PamCalendar.formatDateTime2(ipiUnit.getTimeMilliseconds())
					+ " was discarded. " + ipiSamples 	+ " samples were required, but only "
					+ availableSamples  + " were available in this click detection.";
			PamWarning warn = new PamWarning("IPI Average", warnText, 1);
			warn.setEndOfLife(1000);
			warn.setWarningTip(warnText);
			WarningSystem.getWarningSystem().addWarning(warn);
			return;
		}

		// Add the new cepstrum to the ensemble average
		setNumClicks(getNumClicks() + 1);
		for (int i = 0; i < ipiSamples; i++) {
			summedIpiFunction[i] += newIpiFunction[i];
		}

		/*
		 * Work out the lower and upper indicies for searching for the cepstral
		 * peak. This should probably be done only once during pamStart...
		 */
		minIpiIndex = (int) (ipiController.getIpiParameters().minIpiTime
				/ 1e3 * dataSampleRate / 2);
		maxIpiIndex = (int) (ipiController.getIpiParameters().maxIpiTime
				/ 1e3 * dataSampleRate / 2);
		maxIpiIndex = Math.min(maxIpiIndex, availableSamples);

		/*
		 * Now pick the peak from both the new data and the ensemble averaged
		 * IPI function.
		 */
		double ensemblePeak = 0;
		for (int i = minIpiIndex; i < maxIpiIndex; i++) {
			if (summedIpiFunction[i] > ensemblePeak) {
				ensemblePeak = summedIpiFunction[i];
				ipiEnsembleAvgIx = i;
			}
			if (newIpiFunction[i] > ipiValue) {
				ipiValue = newIpiFunction[i];
				ipiDelayIx = i;
			}
		}
		ipiAmplitudeEnsembleAvg = ensemblePeak;

		/*
		 * Again remember the factors of 2 here are because we're dealing with
		 * the cepstral period = sample period/2
		 */
		ipiTimeEnsembleAvg = ipiEnsembleAvgIx / dataSampleRate * 2;

		int[] peakWidthIx = getPeakWidth(summedIpiFunction, ipiEnsembleAvgIx,
				peakWidthPercent);

		ipiTimeLowerLimit = ipiTimeEnsembleAvg - peakWidthIx[0]
				/ dataSampleRate * 2.0;

		ipiTimeUpperLimit = peakWidthIx[1] / dataSampleRate * 2.0
				- ipiTimeEnsembleAvg;

		ipi.addElement(ipiDelayIx / dataSampleRate * 2);
		
		ipiHistogram[ipiDelayIx]++;
		
		if(ipiHistogram[ipiDelayIx] >= maxHistogramCount){
			maxHistogramCount = ipiHistogram[ipiDelayIx];
			ipiModeIx = ipiDelayIx;
		}

		/*
		 * Compute the mean of all of the individual IPI delays This is similar
		 * to Goold 1996 and Rhinelander and Dawson 2004
		 */
		for (int i = 0; i < getNumClicks(); i++)
			ipiSum += ipi.get(i);

		ipiTimeMean = ipiSum / getNumClicks();
		
		ipiTimeMode = ipiModeIx / dataSampleRate * 2;
		
		// update the side panel
		ipiController.ipiSidePanel.fillData();
		
	}

	/**
	 * Used to measure the peak width of the IPI delay. Start at the
	 * peak position and iterate to the left until the sample amplitude is
	 * less than percent% of the IPI value. Then do the same thing but iterate
	 * to the right.
	 */
	private int[] getPeakWidth(double[] data, int peakPos, double percent) {
		
		double ipiValue, nextVal, testRatio;
		int i, lowIx, highIx, limits[];

		// Find the lower peak limit
		ipiValue = data[peakPos];
		i = peakPos;
		nextVal = data[i];
		testRatio = (nextVal / ipiValue * 100);
		while ((i > 0) && (testRatio > percent)) {
			i--;
			nextVal = data[i];
			testRatio = (nextVal / ipiValue * 100);
		}
		lowIx = i;

		// Find the upper peak limit
		i = peakPos;
		nextVal = data[i];
		testRatio = (nextVal / ipiValue * 100);
		while (i < data.length - 1 && testRatio > percent) {
			i++;
			nextVal = data[i];
			testRatio = (nextVal / ipiValue * 100);
		}
		highIx = i;

		limits = new int[2];
		limits[0] = lowIx;
		limits[1] = highIx;
		return limits;
	}


	
	public void clearIpiData() {
		/*
		 * Reset all of the IPI data before starting processing
		 */
		setNumClicks(0);
		ipi = new Vector<Double>(10);
		ipiAmplitude = new Vector<Double>(10);
		for (int i = 0; i < ipiSamples; i++)
			summedIpiFunction[i] = 0;

		ipiTimeMean = 0;
		
		ipiTimeMode = 0;
		
		for (int i = 0; i<ipiHistogram.length; i++) {
			ipiHistogram[i] = 0;
		}

		ipiTimeEnsembleAvg = 0;

		ipiAmplitudeEnsembleAvg = 0;

		ipiTimeLowerLimit = 0;

		ipiTimeUpperLimit = 0;
		
		avgStartTime = PamCalendar.getTimeInMillis();
	}

	/**
	 * This function computes the cepstrum of a signal.
	 * Cepstrum is defined as IFFT(log(abs(FFT(signal))));
	 * Here we simply take the inverse fourier transform of 
	 * the square root of the power spectrum for each detection.
	 */
	public double[] cepstrum(double[] data, int nSamples) {
		int duration; 
		int i;
		fftManager.FastFFT fastFFT = new fftManager.FastFFT();
		double[] output;
		Complex[] cepstrum;

		int minLength = Math.min(nSamples, data.length);

		duration = PamUtils.getMinFftLength(minLength);
		
		cepstrum = Complex.allocateComplexArray(duration);
		output = new double [duration];
		// put the actual audio data into the array
		for (i = 0; i < minLength; i++) {
			cepstrum[i].real = data[i];
			cepstrum[i].imag = 0;
		}

		cepstrum = fastFFT.rfft(data, cepstrum, FastFFT.log2(duration*2));
		//fft.recursiveFFT(cepstrum);
		
		for (i = 0; i < duration; i++) {
			cepstrum[i].real = Math.log(cepstrum[i].magsq());
			cepstrum[i].imag = 0;
		}

		fastFFT.ifft(cepstrum, FastFFT.log2(duration));
		//fft.recursiveIFFT(cepstrum);
					
		for (i = 0; i < output.length; i++) 
			output[i] = cepstrum[i].mag();
		
		return output;

	}
	
	

	public double getIpiDelayMode() {
		return ipiTimeMode;
	}

	public int getNumClicks() {
		return numClicks;
	}

	public void setNumClicks(int numClicks) {
		this.numClicks = numClicks;
	}
	

	public int getNumIpiSamples() {
		return ipiSamples;
	}

	public Vector<Double> getIpiDelays() {
		return ipi;
	}

	public Vector<Double> getIpiValues() {
		return ipiAmplitude;
	}
	
	public int getMaxHistogramCount() {
		return maxHistogramCount;
	}
	
	public int[] getIpiHistogram(){
		return ipiHistogram;
	}
	
	public double getIpiDelayMean() {
		return ipiTimeMean;
	}

	public double[] getSummedIpiFunction() {
		return summedIpiFunction;
	}

	public double getIpiDelayEnsembleAvg() {
		return ipiTimeEnsembleAvg;
	}

	public double getIpiValueEnsembleAvg() {
		return ipiAmplitudeEnsembleAvg;
	}

	public double getIpiDelayLowerLimit() {
		return ipiTimeLowerLimit;
	}

	public double getIpiDelayUpperLimit() {
		return ipiTimeUpperLimit;
	}
	
}