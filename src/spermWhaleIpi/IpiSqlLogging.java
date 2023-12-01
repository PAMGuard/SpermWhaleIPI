package spermWhaleIpi;

import java.sql.Types;

import GPS.GpsData;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * Database storage of DIFAR information. May as well write out the buoy lat and long. 
 * These aren't needed for Viewer operation, but will help with any further offline analysis
 * of the data that get's done. The only really interesting things in the data are the angle and 
 * the species selection. 
 * @author Doug Gillespie
 *
 */
public class IpiSqlLogging extends SQLLogging {

	private IpiController difarControl;
	private PamDataBlock<IpiAvgDataUnit> difarDataBlock;
	private PamTableItem endTime, ipiEnsemble, ipiMode, ipiMean, ipiLo, ipiHi, numIpi;

	protected IpiSqlLogging(IpiController difarControl, PamDataBlock difarDataBlock) {
		super(difarDataBlock);
		this.difarControl = difarControl;
		this.difarDataBlock = difarDataBlock;
		PamTableDefinition tableDef = new PamTableDefinition(difarControl.getUnitName(), UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(endTime = new PamTableItem("EndTime", Types.TIMESTAMP));
		tableDef.addTableItem(ipiEnsemble = new PamTableItem("EnsembleIpi", Types.DOUBLE));
		tableDef.addTableItem(ipiLo = new PamTableItem("EnsembleIpiLowerLimit", Types.DOUBLE));
		tableDef.addTableItem(ipiHi = new PamTableItem("EnsembleIpiUpperLimit", Types.DOUBLE));	
		tableDef.addTableItem(ipiMode = new PamTableItem("ModeIpi", Types.DOUBLE));
		tableDef.addTableItem(ipiMean = new PamTableItem("MeanIpi", Types.DOUBLE));		
		tableDef.addTableItem(numIpi = new PamTableItem("numClicks", Types.INTEGER));	
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		IpiAvgDataUnit ipiAvgUnit = (IpiAvgDataUnit) pamDataUnit;
		endTime.setValue(sqlTypes.getTimeStamp(ipiAvgUnit.endTime));
		ipiEnsemble.setValue(ipiAvgUnit.ipiEnsemble*1e3);
		ipiMode.setValue(ipiAvgUnit.ipiMode*1e3);
		ipiMean.setValue(ipiAvgUnit.ipiMean*1e3);
		ipiLo.setValue(ipiAvgUnit.ipiEnsembleLo*1e3);
		ipiHi.setValue(ipiAvgUnit.ipiEnsembleHi*1e3);
		numIpi.setValue(ipiAvgUnit.numIpi);
	}

	/**
	 * IPI data are not read from database in viewer mode (or ever)
	 *  
	 * @see generalDatabase.SQLLogging#createDataUnit(long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds,
			int databaseIndex) {
		return null;
	
	}

}
