package spermWhaleIpi.display;

import java.awt.Component;

import spermWhaleIpi.IpiController;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class IpiDisplayProvider implements UserDisplayProvider {

	private IpiController ipiControl;
	
	private IpiDisplayContainer currentContainer;
	
	public IpiDisplayProvider(IpiController ipiControl) {
		super();
		this.ipiControl = ipiControl;
	}

	@Override
	public String getName() {
		return ipiControl.getUnitName() + " Displays";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		if (currentContainer != null) {
			return null;
		}
		currentContainer = ipiControl.getIpiDisplayContainer();
		return currentContainer;
	}

	@Override
	public Class getComponentClass() {
		return IpiDisplayContainer.class;
	}

	@Override
	public int getMaxDisplays() {
		return 1;
	}

	@Override
	public boolean canCreate() {
		return (currentContainer == null);
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		currentContainer = null;
	}

}
