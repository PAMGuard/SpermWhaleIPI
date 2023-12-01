package spermWhaleIpi.display;

//import ipiDemo.IpiPluginPanelProvider.IpiPluginPanel.CopyToClipboardListener;
//import ipiDemo.IpiPluginPanelProvider.IpiPluginPanel.RescaleListener;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.PamColors;
import PamView.PamSidePanel;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import spermWhaleIpi.IpiController;
import PamView.PamColors.PamColor;

/**
 * Displays summary information about the IPI modules.
 * @author Brian Miller
 */
public class IpiSidePanel implements PamSidePanel{

	IpiController ipiController;

	SidePanel sidePanel;

	TitledBorder titledBorder, ensembleBorder;

	JTextField ensembleIpi, modeIpi,  clickCount, ensembleWidth;


	public IpiSidePanel(IpiController ipiController) {

		this.ipiController = ipiController;

		sidePanel = new SidePanel();

	}

	private class SidePanel extends PamBorderPanel {

		private static final long serialVersionUID = 1L;

		public SidePanel() {
			super();

			setBorder(titledBorder = new TitledBorder(ipiController
					.getUnitName()));
			titledBorder.setTitleColor(PamColors.getInstance().getColor(
					PamColor.AXIS));
			
			GridBagLayout gb = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			setLayout(gb);
			ensembleIpi = new JTextField(5);
			ensembleWidth = new JTextField(5);
			clickCount = new JTextField(5);

			modeIpi = new JTextField(5);
			ensembleIpi.setEditable(false);

			clickCount.setEditable(false);
			modeIpi.setEditable(false);
			ensembleWidth.setEditable(false);

			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(new SaveIpiListener());
			JButton clearButton = new JButton("Clear");
			clearButton.addActionListener(new ResetIpiDataListener());
			clearButton.setToolTipText("Reset all IPI data");
			
			c.anchor = GridBagConstraints.EAST;
			c.ipadx = 1;
			c.ipady = 0;
			c.gridx = c.gridy = 0;
			c.gridwidth = c.RELATIVE;
			addComponent(this, new PamLabel("Clicks"), c);
			c.gridx++;
			addComponent(this, clickCount, c);
			c.gridx = 0; 
			c.gridy++;
			addComponent(this, new PamLabel("Histogram Mode"), c);
			c.gridx++;
			addComponent(this, modeIpi, c);
			c.gridx = 0;
			c.gridy++;
			addComponent(this, new PamLabel("Ensemble Peak"), c);
			c.gridx++;
			addComponent(this, ensembleIpi, c);
			c.gridx = 0;
			c.gridy++;
			addComponent(this, new PamLabel("Peak Width"), c);
			c.gridx++;
			addComponent(this, ensembleWidth, c);
			c.gridx = 0;
			c.gridy++;
			addComponent(this,saveButton,c);
			c.gridx++;
			addComponent(this,clearButton,c);
		}
		class ResetIpiDataListener implements ActionListener {
			public void actionPerformed(ActionEvent arg0) {
				ipiController.getIpiProcess().clearIpiData();
				fillData();
			}
			
		}
		
		class SaveIpiListener implements ActionListener, ClipboardOwner {
			public void actionPerformed(ActionEvent e){
				ipiController.getIpiProcess().saveIpiAvg();
			}
		
			@Override
			public void lostOwnership(Clipboard arg0, Transferable arg1) {

			}
		}
		
		@Override
		public void setBackground(Color bg) {
			super.setBackground(bg);
			if (titledBorder != null) {
				titledBorder.setTitleColor(PamColors.getInstance().getColor(
						PamColor.AXIS));
			}
		}

		private void fillData() {
			clickCount.setText(String.format("%d",
					ipiController.getIpiProcess().getNumClicks()));
			modeIpi.setText(String.format("%1.3f", ipiController.getIpiProcess()
					.getIpiDelayMode() * 1e3));
			ensembleIpi.setText(String.format("%1.3f", ipiController.getIpiProcess()
					.getIpiDelayEnsembleAvg() * 1e3));
			
			double width = (ipiController.getIpiProcess().getIpiDelayUpperLimit() + 
					ipiController.getIpiProcess().getIpiDelayLowerLimit() ) * 1e3;

			ensembleWidth.setText(String.format("%1.3f", width));
				
		}
	}

	public JComponent getPanel() {
		return sidePanel;
	}

	public void fillData(){
		sidePanel.fillData();
	}
	
	public void rename(String newName) {
		titledBorder.setTitle(newName);
		sidePanel.repaint();
	}
}
