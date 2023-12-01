package spermWhaleIpi;

import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;

/**
 * This data unit is for measuring the Inter-pulse interval of a single 
 * click (presumably from a sperm whale).  
 * @author Brian Miller
 */
public class IpiDataUnit extends PamDataUnit {

	/**
	 * The echo detection signal: a cepstrum or autocorrelation function 
	 * (which is computed from the raw data). 
	 */
	double[] echoData;
	

	/**
	 * Echo delay is time of the highest peak of the
	 * echo detection function relative to the start of the 
	 * detection.
	 */
	double ipi;

	
	/**
	 * The amplitude of the echo detection function at the time of the ipi
	 */
	double ipiAmplitude;
	
	/**
	 * UID of original click detection that generated the echoData
	 */
	long parentUID;


	/**
	 * Store sample rate in dataUnit for convenience
	 */
	private float sampleRate;
	

	public IpiDataUnit(ClickDetection parent, double[] data, double ipi, double ipiAmplitude) {
		super(parent.getTimeMilliseconds(),parent.getChannelBitmap(),
				parent.getStartSample(),parent.getSampleDuration());
		this.echoData = data;
		this.ipi = ipi;
		this.ipiAmplitude = ipiAmplitude;
		this.parentUID = parent.getUID();
		this.sampleRate = parent.getParentDataBlock().getSampleRate();
		
	}

	/**
	 * Constructor used by binary data source 
	 * @param timeMillis
	 * @param parentUID
	 * @param echoData
	 * @param ipi
	 * @param ipiAmplitude
	 * @param sampleRate
	 */
	public IpiDataUnit(long timeMillis, long parentUID, double[] echoData, double ipi, double ipiAmplitude, float sampleRate) {
		super(timeMillis);
		this.parentUID = parentUID;
		this.echoData = echoData;
		this.ipi = ipi;
		this.ipiAmplitude = ipiAmplitude;
		this.sampleRate = sampleRate;
		
	}

	public double[] getEchoData(){
		return echoData;
	}

	public double getEchoDelay(){
		return ipi;
	}
	
	public double getEchoValue(){
		return ipiAmplitude;
	}

	public float getSampleRate() {
		return sampleRate;
	}

	public long getParentUID() {
		return parentUID;
	}
	
}
