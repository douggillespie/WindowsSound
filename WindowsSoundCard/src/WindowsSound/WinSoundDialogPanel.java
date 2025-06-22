package WindowsSound;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.DialogComponent;
import PamView.panel.WestAlignedPanel;

public class WinSoundDialogPanel implements DialogComponent {

	private WinMMDaqSystem winMMDaqSystem;
	
	private JPanel mainPanel;
	
	private JComboBox<String> deviceNames;
	
	private JRadioButton bitButtons[];

	public WinSoundDialogPanel(WinMMDaqSystem winMMDaqSystem) {
		this.winMMDaqSystem = winMMDaqSystem;
		mainPanel = new JPanel(new BorderLayout());
		deviceNames = new JComboBox<>();
		mainPanel.add(BorderLayout.CENTER, deviceNames);
		mainPanel.setBorder(new TitledBorder("Windows sound card"));
		JPanel bitPanel = new JPanel(new FlowLayout());
		bitPanel.add(new JLabel("Bit depth: "));
		bitButtons = new JRadioButton[WinSoundParameters.BITDEPTHS.length];
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < WinSoundParameters.BITDEPTHS.length; i++) {
			bitButtons[i] = new JRadioButton(String.format("%d bit", WinSoundParameters.BITDEPTHS[i]));
			bg.add(bitButtons[i]);
			bitPanel.add(bitButtons[i]);
		}
		mainPanel.add(BorderLayout.SOUTH, new WestAlignedPanel(bitPanel));
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
		for (int i = 0; i < WinSoundParameters.BITDEPTHS.length; i++) {
			bitButtons[i].setSelected(winMMDaqSystem.soundCardParameters.getBitDepth() == WinSoundParameters.BITDEPTHS[i]);
		}
		
	}

	@Override
	public boolean getParams() {
		winMMDaqSystem.soundCardParameters.deviceNumber = deviceNames.getSelectedIndex();
		for (int i = 0; i < WinSoundParameters.BITDEPTHS.length; i++) {
			if (bitButtons[i].isSelected()) {
				winMMDaqSystem.soundCardParameters.setBitDepth(WinSoundParameters.BITDEPTHS[i]);
				break;
			}
		}
		return winMMDaqSystem.soundCardParameters.deviceNumber >= 0;
	}

}
