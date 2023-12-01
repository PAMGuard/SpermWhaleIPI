package spermWhaleIpi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import binaryFileStorage.PackedBinaryObject;
import difar.DifarControl;
import difar.DifarDataBlock;
import difar.DifarDataUnit;

/**
 * Class for storing IPI module cepstra to the binary store. 
 * @author Brian Miller
 *
 */
public class IpiBinaryDataSource extends BinaryDataSource {
	private static final int currentVersion = 1;
	private static final int IPI_DATA_ID = 0;
	private IpiController ipiControl;
	private PamDataBlock ipiDataBlock;
	
	public IpiBinaryDataSource(IpiController ipiControl, PamDataBlock<IpiDataUnit> ipiDataBlock) {
		super(ipiDataBlock);
		this.ipiDataBlock = ipiDataBlock;
		this.ipiControl = ipiControl;
	}

	@Override
	public String getStreamName() {
		return ipiControl.getUnitName();
	}

	@Override
	public int getStreamVersion() {
		return 0;
	}

	@Override
	public int getModuleVersion() {
		return currentVersion;
	}

	@Override
	public byte[] getModuleHeaderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);

		long timeMillis;
		long parentUID;
		double ipi;
		double ipiAmplitude;
		float sampleRate;
		float maxVal;
		int cepLength;
		double[] echoData = null;
		
		try {
			timeMillis = dis.readLong();
			parentUID = dis.readLong();
			ipi = (double) dis.readFloat();
			ipiAmplitude = (double) dis.readFloat();
			sampleRate = dis.readFloat();
			maxVal = dis.readFloat();
			cepLength = dis.readInt();
			if (cepLength>0) {
				echoData = new double[cepLength];
				for (int i = 0; i < echoData.length; i++) {
					echoData[i] = dis.readShort() * maxVal / 32767;
				}
			}
			
		bis.close();	
		}
		catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		
		IpiDataUnit du = new IpiDataUnit(timeMillis, parentUID, echoData, ipi, ipiAmplitude, sampleRate);
		return du;
		
		
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		IpiDataUnit du = (IpiDataUnit) pamDataUnit;
		long timeMillis = du.getTimeMilliseconds();
		long parentUID = du.getParentUID();
		double ipi = du.getEchoDelay();
		double ipiAmplitude = du.getEchoValue();
		float sampleRate = du.getSampleRate();
		double[] echoData = du.getEchoData();
		double maxVal = PamUtils.getAbsMax(PamUtils.getMinAndMax(echoData));		
		try {
			dos.writeLong(parentUID);
			dos.writeFloat((float) ipi);
			dos.writeFloat((float) ipiAmplitude);
			dos.writeFloat(sampleRate);
			dos.writeFloat((float) maxVal);
			int length = (int) Math.ceil(echoData.length/2);
			dos.writeInt(length);
			for (int i = 0; i < length; i++) {
				dos.writeShort((int) (echoData[i] * 32767 / maxVal)); 
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return new PackedBinaryObject(IPI_DATA_ID, bos.toByteArray());
	}
	
	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub
		
	}

}