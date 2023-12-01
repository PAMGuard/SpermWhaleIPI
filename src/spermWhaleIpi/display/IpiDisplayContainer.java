package spermWhaleIpi.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import difar.DifarControl;
import difar.display.DisplaySouthPanel;
import spermWhaleIpi.IpiController;
import spermWhaleIpi.IpiPluginPanelProvider;
import spermWhaleIpi.IpiPluginPanelProvider.IpiPluginPanel;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayComponentAdapter;
import PamController.PamControllerInterface;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.panel.PamPanel;

/**
 * This is a main panel which will hold as many other panels as 
 * we need for the IPI display system. In principle, this can be included 
 * in either a panel for a user display or could very quickly adapt to go in 
 * it's own tab panel. 
 * @author BSM
 *
 */
public class IpiDisplayContainer extends UserDisplayComponentAdapter {

	private PamPanel outerDisplayPanel;

	private IpiController ipiControl;


	/**
	 * 
	 * @param ipiControl
	 */
	public IpiDisplayContainer(IpiController ipiControl) {
		super();
		this.ipiControl = ipiControl;

		outerDisplayPanel = new PamPanel(PamColor.BORDER);
		outerDisplayPanel.setLayout(new BorderLayout());

	}

	@Override
	public Component getComponent() {
		return outerDisplayPanel;
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyModelChanged(int changeType) {
		
	}

	@Override
	public String getFrameTitle() {
		return ipiControl.getUnitName();
	}

}
