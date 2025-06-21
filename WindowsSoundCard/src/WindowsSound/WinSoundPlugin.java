package WindowsSound;

import Acquisition.AcquisitionControl;
import Acquisition.DaqSystem;
import Acquisition.DaqSystemInterface;

public class WinSoundPlugin implements DaqSystemInterface {

	private String jarFile;

	@Override
	public String getDefaultName() {
		return WinMMDaqSystem.SYSTEMTYPE;
	}

	@Override
	public String getHelpSetName() {
		return null;
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
		return "Doug Gillespie";
	}

	@Override
	public String getContactEmail() {
		return "pamguard@pamguard.org";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "V2.02.17";
	}

	@Override
	public String getPamVerTestedOn() {
		return "V2.02.17";
	}

	@Override
	public String getAboutText() {
		return "Acquisition using low level Windows sound drivers";
	}

	@Override
	public DaqSystem createDAQControl(AcquisitionControl acObject) {
		return new WinMMDaqSystem(acObject);
	}



}
