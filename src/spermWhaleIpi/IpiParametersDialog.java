package spermWhaleIpi;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamUtils.PamUtils;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import clickDetector.ClickDetection;


/**
 * A dialog box used to control the IPI computation parameters. Mostly copied 
 * and pasted from the WorkShopProcessParametersDialog.
 * @author Brian Miller
 *
 */
public class IpiParametersDialog extends PamDialog {

	private static final long serialVersionUID = 1;

	/*
	 * Make the dialog a singleton - saves time recreating it every time it's
	 * used and will also leave the same tab showing for multi tab dialogs
	 * (doesn't really make any difference for this simple dialog)
	 */
	static private IpiParametersDialog singleInstance;

	/*
	 * local copy of parameters
	 */
	IpiParameters ipiParameters;

	/*
	 * source panel is a handy utility for listing available data sources.
	 */
	SourcePanel sourcePanel;

	/*
	 * reference for data fields
	 */
	JTextField duration, minIpiTime, maxIpiTime, peakWidthPercent;

	private IpiParametersDialog(Frame parentFrame) {
		super(parentFrame, "Sperm Whale IPI settings", true);

		/*
		 * Use the Java layout manager to constructs nesting panels of all the
		 * parameters.
		 */
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		/*
		 * put a sourcePanel in the top of the dialog panel. need to put it in
		 * an inner panel in order to add a titled border (appearance is
		 * everything)
		 */
		sourcePanel = new SourcePanel(this, ClickDetection.class, true, true);
		JPanel sourceSubPanel = new JPanel();
		sourceSubPanel.setLayout(new BorderLayout());
		sourceSubPanel
				.setBorder(new TitledBorder("IPI Data source"));
		sourceSubPanel.add(BorderLayout.CENTER, sourcePanel.getPanel());
		mainPanel.add(BorderLayout.NORTH, sourceSubPanel);

		// make another panel for the rest of the parameters.
		JPanel ipiPanel = new JPanel();
		ipiPanel.setBorder(new TitledBorder("IPI parameters"));
		// use the gridbaglaoyt - it's the most flexible
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		ipiPanel.setLayout(layout);
		constraints.anchor = GridBagConstraints.EAST;
		constraints.ipadx = 3;
		constraints.gridx = 0;
		constraints.gridy = 0;

		addComponent(ipiPanel, new JLabel("Cepstrum duration (ms) "), constraints);
		constraints.gridx++;
		addComponent(ipiPanel, duration = new JTextField(6), constraints);
		duration.setHorizontalAlignment(JTextField.RIGHT);
		constraints.gridx = 0;
		constraints.gridy++;
		addComponent(ipiPanel, new JLabel("Minimum IPI delay (ms) "), constraints);
		constraints.gridx++;
		addComponent(ipiPanel, minIpiTime = new JTextField(6), constraints);
		minIpiTime.setHorizontalAlignment(JTextField.RIGHT);
		constraints.gridx = 0;
		constraints.gridy++;
		addComponent(ipiPanel, new JLabel("Maximum IPI delay (ms) "), constraints);
		constraints.gridx++;
		addComponent(ipiPanel, maxIpiTime = new JTextField(6), constraints);
		maxIpiTime.setHorizontalAlignment(JTextField.RIGHT);
		constraints.gridx = 0;
		constraints.gridy++;
		addComponent(ipiPanel, new JLabel("Peak Height % for Width "), constraints);
		constraints.gridx++;
		addComponent(ipiPanel, peakWidthPercent = new JTextField(6),
				constraints);
		peakWidthPercent.setToolTipText("The percentage of the peak height at which the peak width will be measured.");
		peakWidthPercent.setHorizontalAlignment(JTextField.RIGHT);
		constraints.gridx = 0;
		constraints.gridy++;
		mainPanel.add(BorderLayout.CENTER, ipiPanel);

		setDialogComponent(mainPanel);
	}

	public static IpiParameters showDialog(Frame parentFrame,
			IpiParameters ipiProcessParameters) {
		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			singleInstance = new IpiParametersDialog(parentFrame);
		}
		singleInstance.ipiParameters = ipiProcessParameters.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.ipiParameters;
	}

	public void setParams() {
		/*
		 * set the parameters in the source list. including the channel list and
		 * the actual data source.
		 */
//		sourcePanel.setChannelList(ipiParameters.channelMap);
		ArrayList<PamDataBlock> ipiSources = PamController.getInstance()
				.getDetectorDataBlocks();
		sourcePanel.setSource(ipiSources
				.get(ipiParameters.clickDataBlock));
		sourcePanel.setChannelList(ipiParameters.channelMap);

		duration.setText(String.format("%3.2f",
				ipiParameters.ipiDuration));
		minIpiTime.setText(String.format("%3.2f",
				ipiParameters.minIpiTime));
		maxIpiTime.setText(String.format("%3.2f",
				ipiParameters.maxIpiTime));
		peakWidthPercent.setText(String.format("%2.0f",
				ipiParameters.ipiPeakWidthPercent));
	}

	@Override
	public void cancelButtonPressed() {
		ipiParameters = null;
	}

	@Override
	/**
	 * return true if all parameters are OK, otherwise, return false. 
	 */
	public boolean getParams() {
		/*
		 * get the source parameters
		 */
		ipiParameters.clickDataBlock = sourcePanel.getSourceIndex();
		/*
		 * TODO: IPI detector uses only one channel, so modify the parameter
		 * dialog to only allow selection of one channel.
		 */
		int channelList = sourcePanel.getChannelList();
		if (channelList == 0) {
			return false;
		}

		
		ipiParameters.channelMap = channelList;
		// will throw an exception if the number format of any of the parameters
		// is invalid,
		// so catch the exception and return false to prevent exit from the
		// dialog.
		try {
			ipiParameters.ipiDuration = Double.valueOf(duration
					.getText());
			ipiParameters.minIpiTime = Double.valueOf(minIpiTime
					.getText());
			ipiParameters.maxIpiTime = Double.valueOf(maxIpiTime
					.getText());
			ipiParameters.ipiPeakWidthPercent = Double
					.valueOf(peakWidthPercent.getText());

		} catch (NumberFormatException ex) {
			return false;
		}

		return true;
	}

	@Override
	public void restoreDefaultSettings() {

		ipiParameters = new IpiParameters();
		setParams();

	}

}
