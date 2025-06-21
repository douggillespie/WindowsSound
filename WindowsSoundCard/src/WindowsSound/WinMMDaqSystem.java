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
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import WindowsSound.WinSoundJNA.MMA;
import wavFiles.ByteConverter;

public class WinMMDaqSystem extends DaqSystem implements PamSettings {
	
	private AcquisitionControl acquisitionControl;
	
	public static final String SYSTEMTYPE = "Windows MM Sound";
	
	private ArrayList<String> deviceNames;
	
	private WinSoundJNA winSoundJNA;
	
	protected SoundCardParameters soundCardParameters = new SoundCardParameters(SYSTEMTYPE);

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
	}

	@Override
	public String getSystemType() {
		return SYSTEMTYPE;
	}

	@Override
	public String getSystemName() {
		return acquisitionControl.getUnitName();
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

	protected ArrayList<String> getDeviceNames() {
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
		byteConverter = ByteConverter.createByteConverter(2, false, Encoding.PCM_SIGNED);
		int res = mmaLib.wavePrepare(soundCardParameters.deviceNumber, daqParams.nChannels, (int) daqParams.sampleRate, 16, dataCallback);
		return res == WinSoundJNA.MMSYSERR_NOERROR;
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
			int nSamples = dataLength / nChan / 2;
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
		return getSystemName();
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
		return SoundCardParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		soundCardParameters = (SoundCardParameters) pamControlledUnitSettings.getSettings();
		soundCardParameters = soundCardParameters.clone();
		return true;
	}


}
