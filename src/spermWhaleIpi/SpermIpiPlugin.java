/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package spermWhaleIpi;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
import clickDetector.ClickDetection;


/**
 * @author SCANS
 *
 */
public class SpermIpiPlugin implements PamPluginInterface {

	/**
	 * The name of the jarfile this package is contained in
	 */
	private String jarFile;

	@Override
	public String getDefaultName() {
		return "Sperm whale IPI";
	}

	@Override
	public String getHelpSetName() {
		return "spermWhaleIpi/help/SpermWhaleIpi.hs";
	}

	@Override
	public String getClassName() {
		return "spermWhaleIpi.IpiController";
	}

	@Override
	public String getDescription() {
		return "Sperm whale IPI";
	}

	@Override
	public String getMenuGroup() {
		return "Sound Measurements";
	}

	@Override
	public String getToolTip() {
		return "Measures inter pulse interval (IPI) of sperm whale clicks from the click detector";
	}

	@Override
	public PamDependency getDependency() {
		return new PamDependency(ClickDetection.class, "clickDetector.ClickControl");
	}

	@Override
	public int getMinNumber() {
		return 0;
	}

	@Override
	public int getMaxNumber() {
		return 0;
	}

	@Override
	public int getNInstances() {
		return 1;
	}

	@Override
	public boolean isItHidden() {
		return false;
	}

	@Override
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String getJarFile() {
		return jarFile;
	}

	@Override
	public String getDeveloperName() {
		return "Brian Miller";
	}

	@Override
	public String getContactEmail() {
		return "Brian.Miller@aad.gov.au";
	}

	@Override
	public String getVersion() {
		return "2018.05.09";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "2.00.12f";
	}

	@Override
	public String getPamVerTestedOn() {
		return "2.02.09";
	}

	@Override
	public String getAboutText() {
		String desc = "This is a module for Sperm whale Inter-pulse interval (IPI) computation. It "+
				"uses cepstral analysis to compute the Inter-pulse interval of sperm whale "+
				"clicks.Two related signal processing methods are used to obtain an IPI: "+
				"Method 1 computes the IPI for each click and then averages these IPIs "+
				"together as described by Gordon (1991) - J. Zool. Lond. Vol 224 pp 301-314 "+
				"Method 2 computes the ensemble averaged cepstrum from all clicks and then "+
				"picks one IPI from this ensemble averaged signal similar to Teloni (2007) - J. "+
				"Cetacean Res. Manage. Vol 9(2), pp 127-136.";
		return desc;
	}

	/* (non-Javadoc)
	 * @see PamModel.PamPluginInterface#allowedModes()
	 */
	@Override
	public int allowedModes() {
		return PamPluginInterface.ALLMODES;
	}
}
