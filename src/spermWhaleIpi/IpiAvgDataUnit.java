package spermWhaleIpi;

import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;

/**
 * This data unit is for measuring the Inter-pulse interval of a single 
 * click (presumably from a sperm whale).  
 * @author Brian Miller
 */
public class IpiAvgDataUnit extends PamDataUnit {

	/**
	 * Time in milliseconds that this data unit ends
	 */
	long endTime;
	
	/**
	 * The echo detection signal: a cepstrum or autocorrelation function 
	 * (which is computed from the raw data). 
	 */
	double[] ensemble;
	
	/**
	 * A Histogram of peak-ipis. Each index is a time bin, and the value
	 * of the index is the count.
	 */
	int[] histogram;
	

	/**
	 * Echo delay is time of the highest peak of the
	 * echo detection function relative to the start of the 
	 * detection.
	 */
	double ipiEnsemble;

	/**
	 * The amplitude of the echo detection function at the time of the ipi
	 */
	double ipiEnsembleAmplitude;
	
	/**
	 * Mean of all IPIs used for the ensemble & histogram
	 */
	double ipiMean;
	
	/**
	 * The mode from the IPI histogram
	 */
	double ipiMode;
	
	/**
	 * Lower and upper times of peak
	 */
	double ipiEnsembleLo;
	double ipiEnsembleHi;
	
	int numIpi;

	/**
	 * Store sample rate in dataUnit for convenience
	 */
	private float sampleRate;
	

	public IpiAvgDataUnit(long avgStartTime, long endTime, 
			double[] ensemble, double ipiEnsemble, double ipiEnsembleAmplitude,
			int [] histogram, double ipiMean, double ipiMode, 
			double ipiEnsembleLo, double ipiEnsembleHi, int numIpi) {
		super(avgStartTime);
		setDurationInMilliseconds(endTime - avgStartTime);
		this.endTime = endTime;
		this.ensemble = ensemble;
		this.ipiEnsemble = ipiEnsemble;
		this.ipiEnsembleAmplitude = ipiEnsembleAmplitude;
		this.histogram = histogram;
		this.ipiMean = ipiMean;
		this.ipiMode = ipiMode;	
		this.ipiEnsembleLo = ipiEnsemble - ipiEnsembleLo;
		this.ipiEnsembleHi = ipiEnsemble + ipiEnsembleHi;
		this.numIpi = numIpi;
	}



	public double[] getEchoData(){
		return ensemble;
	}

	public double getEchoDelay(){
		return ipiEnsemble;
	}
	
	public double getEchoValue(){
		return ipiEnsembleAmplitude;
	}

	public float getSampleRate() {
		return sampleRate;
	}
	
}
