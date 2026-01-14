package WindowsSound;

import java.io.Serializable;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat.Encoding;
import javax.swing.JComponent;

import com.sun.jna.Pointer;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.AcquisitionParameters;
import Acquisition.AudioDataQueue;
import Acquisition.DaqSystem;
import Acquisition.SoundCardParameters;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import WindowsSound.WinSoundJNA.MMA;
import WindowsSound.swing.WinSoundDialogPanel;
import wavFiles.ByteConverter;

public class WinMMDaqSystem extends DaqSystem implements PamSettings {
	
	private AcquisitionControl acquisitionControl;
	
//	public static final String SYSTEMTYPE = "Native Sound";
	public static final String SYSTEMTYPE = "Native Sound Card access";
	
	private ArrayList<String> deviceNames;
	
	private WinSoundJNA winSoundJNA;
	
	protected WinSoundParameters soundCardParameters = new WinSoundParameters(SYSTEMTYPE);

	private MMA mmaLib;
	
	private DataCallback dataCallback;

	private ByteConverter byteConverter;

	private long totalSamples;

	private AudioDataQueue newDataList;
	
	 private WinSoundDialogPanel dialogPanel;

	public WinMMDaqSystem(AcquisitionControl acquisitionControl) {
		super();
		this.acquisitionControl = acquisitionControl;
		winSoundJNA = new WinSoundJNA();
		mmaLib = winSoundJNA.getMmaLib();
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public String getSystemType() {
		return SYSTEMTYPE;
	}

	@Override
	public String getSystemName() {
		// should be the name of the selected card. 
		return getDeviceName();
//		return acquisitionControl.getUnitName();
	}

	public WinSoundDialogPanel getDialogPanel() {
		if (dialogPanel == null) {
			dialogPanel = new WinSoundDialogPanel(this);
		}
		return dialogPanel;
	}
	
	@Override
	public JComponent getDaqSpecificDialogComponent(AcquisitionDialog acquisitionDialog) {
		return getDialogPanel().getComponent(acquisitionDialog);
	}

	@Override
	public void dialogSetParams() {
		getDialogPanel().setParams();
	}
	
	@Override
	public boolean dialogGetParams() {
		return getDialogPanel().getParams();
	}

	public ArrayList<String> getDeviceNames() {
		if (deviceNames == null) {
			deviceNames = new ArrayList<>();
			int nDev = mmaLib.enumerateDevices();
			for (int i = 0; i < nDev; i++) {
				String name = winSoundJNA.getDeviceName2(i);
				deviceNames.add(name);
			}
		}
		return deviceNames;
	}

	@Override
	public int getMaxSampleRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxChannels() {
		return 2;
	}

	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		return PARAMETER_UNKNOWN;
	}

	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
		totalSamples = 0;
		newDataList = daqControl.getDaqProcess().getNewDataQueue();
		AcquisitionParameters daqParams = acquisitionControl.acquisitionParameters;
		if (dataCallback == null) {
			dataCallback = new DataCallback();
		}
		int deviceIndex = checkDiviceIndex();
		byteConverter = ByteConverter.createByteConverter(soundCardParameters.getBitDepth()/8, false, Encoding.PCM_SIGNED);
		int res = mmaLib.wavePrepare(deviceIndex, daqParams.nChannels, (int) daqParams.sampleRate, soundCardParameters.getBitDepth(), dataCallback);
		return res == WinSoundJNA.MMSYSERR_NOERROR;
	}
	
	/**
	 * Try to find the device by name. It that doesn't work, then use whatever the 
	 * device index was. 
	 * @return
	 */
	private int checkDiviceIndex() {
		deviceNames = getDeviceNames();
		if (deviceNames == null) {
			return 0;
		}
		String cardName = soundCardParameters.getCardName();
		if (cardName == null) {
			cardName = "";
		}
		for (int i = 0; i < deviceNames.size(); i++) {
			if (deviceNames.get(i).equals(cardName)) {
				return i;
			}
		}
		// gets here if it didn't find a named device. 
		return soundCardParameters.deviceNumber;
	}

	@Override
	public boolean startSystem(AcquisitionControl daqControl) {
		int res = mmaLib.waveStart();
		return res == WinSoundJNA.MMSYSERR_NOERROR;
	}

	private class DataCallback implements WinSoundJNA.MMA.WMMCallback {

		@Override
		public void wmmNewData(Pointer data, int dataLength) {
			byte[] byteData = data.getByteArray(0, dataLength);
			// now convert
			int nChan = acquisitionControl.acquisitionParameters.nChannels;
			int bytes = soundCardParameters.getBitDepth() / 8;
			int nSamples = dataLength / nChan / bytes;
			double[][] raw = new double[nChan][nSamples];
			byteConverter.bytesToDouble(byteData, raw, dataLength);
			long ms = acquisitionControl.getAcquisitionProcess().absSamplesToMilliseconds(totalSamples);
			for (int i = 0; i < nChan; i++) {
				RawDataUnit rawUnit = new RawDataUnit(ms, 1<<i, totalSamples, nSamples);
				rawUnit.setRawData(raw[i]);
				newDataList.addNewData(rawUnit);
			}
			totalSamples += nSamples;
		}
		
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		mmaLib.waveStop();
	}

	@Override
	public boolean isRealTime() {
		return true;
	}

	@Override
	public boolean canPlayBack(float sampleRate) {
		return false;
	}

	@Override
	public int getDataUnitSamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void daqHasEnded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDeviceName() {
		 ArrayList<String> names = getDeviceNames();
		int iN = soundCardParameters.deviceNumber;
		if (iN >=0 && iN < names.size()) {
			return names.get(iN);
		}
		return "Unknown";
	}

	@Override
	public String getUnitName() {
		return acquisitionControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return getSystemType();
	}

	@Override
	public Serializable getSettingsReference() {
		return soundCardParameters;
	}

	@Override
	public long getSettingsVersion() {
		return WinSoundParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		if (pamControlledUnitSettings.getSettings() instanceof WinSoundParameters) {
			soundCardParameters = (WinSoundParameters) pamControlledUnitSettings.getSettings();
			soundCardParameters = soundCardParameters.clone();
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int getSampleBits() {
		return soundCardParameters.getBitDepth();
	}

	/**
	 * @return the soundCardParameters
	 */
	public WinSoundParameters getSoundCardParameters() {
		return soundCardParameters;
	}


}
