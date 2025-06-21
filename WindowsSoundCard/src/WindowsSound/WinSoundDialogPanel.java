package WindowsSound;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.dialog.DialogComponent;

public class WinSoundDialogPanel implements DialogComponent {

	private WinMMDaqSystem winMMDaqSystem;
	
	private JPanel mainPanel;
	
	private JComboBox<String> deviceNames;

	public WinSoundDialogPanel(WinMMDaqSystem winMMDaqSystem) {
		this.winMMDaqSystem = winMMDaqSystem;
		mainPanel = new JPanel(new BorderLayout());
		deviceNames = new JComboBox<>();
		mainPanel.add(BorderLayout.CENTER, deviceNames);
		setParams();
	}

	@Override
	public JComponent getComponent(Window owner) {
		return mainPanel;
	}

	@Override
	public void setParams() {
		deviceNames.removeAllItems();
		ArrayList<String> currNames = winMMDaqSystem.getDeviceNames();
		for (int i = 0; i < currNames.size(); i++) {
			deviceNames.addItem(currNames.get(i).toString());
		}
		if (winMMDaqSystem.soundCardParameters.deviceNumber < currNames.size()) {
			deviceNames.setSelectedIndex(winMMDaqSystem.soundCardParameters.deviceNumber);
		}
	}

	@Override
	public boolean getParams() {
		winMMDaqSystem.soundCardParameters.deviceNumber = deviceNames.getSelectedIndex();
		return winMMDaqSystem.soundCardParameters.deviceNumber >= 0;
	}

}
