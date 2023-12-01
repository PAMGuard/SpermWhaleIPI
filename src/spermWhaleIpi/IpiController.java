/**
 * This package contains files for the Inter-pulse-interval (IPI) computation plugin
 */
package spermWhaleIpi;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import difar.display.DifarDisplayContainer;
import difar.display.DifarDisplayProvider;
import difar.display.DifarDisplayProvider2;
import spermWhaleIpi.display.IpiDisplayContainer;
import spermWhaleIpi.display.IpiDisplayProvider;
import spermWhaleIpi.display.IpiSidePanel;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

/**
 * Controller for sperm whale IPI computation, parameters, and displays.
 * 
 * @author Brian Miller
 */
public class IpiController extends PamControlledUnit implements PamSettings {
	/**
	 * ipiProcess does all of the actual calculation of IPI
	 */
	private IpiProcess ipiProcess;

	/**
	 * User adjustable and fixed parameters for IPI stuff are stored here
	 */
	private IpiParameters ipiParameters;

	/**
	 * Displays summary information about IPI calculations in a panel on the 
	 * left hand side of the screen.
	 */
	protected IpiSidePanel ipiSidePanel;

	/**
	 * Displays IPI signal processing results beneath a user spectrogram
	 * display. 
	 */
	public IpiPluginPanelProvider ipiPluginPanelProvider;
	
	private IpiDisplayProvider displayProvider;
	
	private IpiDisplayContainer ipiDisplayContainer;
	

	public IpiController(String unitName) {

		super("Ipi module", unitName);

		/*
		 * create the parameters that will control the process. (do this before
		 * crating the process in case the process tries to access them from
		 * it's constructor).
		 */
		setIpiParameters(new IpiParameters());

		addPamProcess(setIpiProcess(new IpiProcess(this)));

		PamSettingManager.getInstance().registerSettings(this);

		setSidePanel(ipiSidePanel = new IpiSidePanel(this));

		ipiPluginPanelProvider = new IpiPluginPanelProvider(this);
		
//		displayProvider = new IpiDisplayProvider(this);

//		UserDisplayControl.addUserDisplayProvider(displayProvider);

	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		super.notifyModelChanged(changeType);

		/*
		 * This gets called every time a new module is added - make sure that
		 * the ipiProcess get's a chance to look around and see if there is data
		 * it wants to subscribe to.
		 */
		switch (changeType) {
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
		case PamControllerInterface.ADD_DATABLOCK:
		case PamControllerInterface.REMOVE_DATABLOCK:
			getIpiProcess().prepareProcess();
		}
	}

	/*
	 * Menu item and action for detection parameters... (non-Javadoc)
	 * 
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Parameters");
		menuItem.addActionListener(new SetParameters(parentFrame));
		return menuItem;
	}

	class SetParameters implements ActionListener {

		Frame parentFrame;

		public SetParameters(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent e) {

			IpiParameters newParams = IpiParametersDialog.showDialog(
					parentFrame, getIpiParameters());
			/*
			 * The dialog returns null if the cancel button was set. If it's not
			 * null, then clone the parameters onto the main parameters
			 * reference and call preparePRocess to make sure they get used !
			 */
			if (newParams != null) {
				setIpiParameters(newParams.clone());
				getIpiProcess().prepareProcess();
			}

		}

	}

	/**
	 * These next three functions are needed for the PamSettings interface which
	 * will enable Pamguard to save settings between runs
	 */
	public Serializable getSettingsReference() {
		return getIpiParameters();
	}

	public long getSettingsVersion() {
		return IpiParameters.serialVersionUID;
	}

	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		setIpiParameters((IpiParameters) pamControlledUnitSettings
				.getSettings());
		return true;
	}

	public IpiDisplayContainer getIpiDisplayContainer() {
		if (ipiDisplayContainer == null) {
			ipiDisplayContainer = new IpiDisplayContainer(this);
		}
		return ipiDisplayContainer;
	}

	public IpiProcess getIpiProcess() {
		return ipiProcess;
	}

	public IpiProcess setIpiProcess(IpiProcess ipiProcess) {
		this.ipiProcess = ipiProcess;
		return ipiProcess;
	}

	public IpiParameters getIpiParameters() {
		return ipiParameters;
	}

	public void setIpiParameters(IpiParameters ipiParameters) {
		this.ipiParameters = ipiParameters;
	}
}
