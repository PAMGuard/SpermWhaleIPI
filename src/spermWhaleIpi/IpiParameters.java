package spermWhaleIpi;

import java.io.Serializable;

public class IpiParameters implements Serializable, Cloneable {

	static final long serialVersionUID = 1;

	
	/**
	 * For now (2009-10-08) only the cepstrum is used to compute the IPI. In the
	 * future autocorrelation based methodscould be added.
	 */
	String ipiFunctionType = "cepstrum";

	/**
	 * Duration of the IPI function in milliseconds
	 */
	double ipiDuration = 10;

	/**
	 * use the first (0th) echo data block in the PamModel.
	 */
	int clickDataBlock = 0;

	/**
	 * IPI delays less than this value are not considered when picking the peak
	 * from the IPI function This value is in milliseconds.
	 */
	double minIpiTime = 3;

	/**
	 * IPI delays greater than this value are not considered when picking the
	 * peak from the IPI function. This value is in milliseconds.
	 */
	double maxIpiTime = 10;

	/**
	 * Percentage of the IPI peak value to consider when measuring the peak
	 * width.
	 */
	double ipiPeakWidthPercent = 75;

	/**
	 * When the detection stops, the summed IPI function will be written to this
	 * file.
	 */
	String outputFileName = "ipi.txt";

	public int channelMap = 1;

	public boolean resetOnStart = true;

	@Override
	/**
	 * overriding the clone function enables you to clone (copy) 
	 * these parameters easily in your code without having to 
	 * continually cast to (WorkshopProcessParameters) or handle
	 * the exception CloneNotSupportedException.
	 */
	protected IpiParameters clone() {
		try {
			return (IpiParameters) super.clone();
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
