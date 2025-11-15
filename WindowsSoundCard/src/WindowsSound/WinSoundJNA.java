package WindowsSound;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;

import PamUtils.PlatformInfo;
import PamUtils.PlatformInfo.OSType;
import PamView.dialog.warn.WarnOnce;

/**
 * the class name for this is a bit of a Misnomer since we now also support Linux. 
 * windows systems need to link to the dll winmmsound.dll, linux to linuxsound.so.
 * Windows uses the windows Multimedia library to access the sound card, Linux uses
 * ALSA. both libraries have the same function calls, so this Java class will work with 
 * either. 
 */
public class WinSoundJNA {

	// see https://www.baeldung.com/java-jna-dynamic-libraries for basic JNA stuff
	// https://learn.microsoft.com/en-us/windows/win32/api/mmeapi/nf-mmeapi-mixergetnumdevs for mm basic functions. 
	
	public static final int MMSYSERR_NOERROR = 0;
	
	private static final String WINDLL = "winmmsound.dll";
	
	private static final String LINUXSO = "linuxsound.so";
	
	private MMA mmaLib;

	public WinSoundJNA() {
		if (loadLibrary()) {
			mmaLib.enumerateDevices();
		}
	}
	
	private boolean loadLibrary() {
		String jnaPath = System.getProperty("jna.library.path");
		if (jnaPath == null) {
			String javaPath = System.getProperty("java.library.path");
			if (javaPath != null) {
//				javaPath += File.pathSeparator+"C:\\Users\\dg50\\source\\repos\\WindowsSoundJNA\\x64\\Release";
				System.setProperty("jna.library.path", javaPath);
				System.setProperty("java.library.path", javaPath);
			}
			jnaPath = System.getProperty("jna.library.path");
		}
//		System.out.println("JNA library path is " + System.getProperty("jna.library.path"));
		//		String libPath = "C:\\Users\\dg50\\source\\repos\\WindowsSoundJNA\\Release\\WINDOWSSOUNDJNA.dll";
		String libPath;
		if (PlatformInfo.calculateOS() == OSType.WINDOWS) {
			libPath = WINDLL;
		}
		else {
			libPath = LINUXSO;
		}
		try {
			mmaLib = Native.load(libPath, MMA.class);
		}
		catch (Error e) {
			String msg = String.format("The %s sound acquisition plugin requires the libary file %s "
					+ "to be included in the library path: %s. "
					+ "note that this daq plugin is not suitable for non Windows platforms.",
					WinMMDaqSystem.SYSTEMTYPE, libPath, jnaPath);
			WarnOnce.showWarning("Missing Windows Library", msg, WarnOnce.WARNING_MESSAGE);
			return false;
		}
		return true;
	}

//	private void test() {
//		int nDev = mmaLib.getNumDevices();
//	
//		
//		System.out.printf("Discoverd %d sound devices on system\n", nDev);
//		for (int i = 0; i < nDev; i++) {
//			Pointer name2 = mmaLib.getDeviceName2(i);
////			String nn = name2.getString(0, StandardCharsets.UTF_16LE);
//			byte[] rawName = name2.getByteArray(0,64);
//			String devName2 = new String(rawName, StandardCharsets.UTF_16LE);
//			String devName = mmaLib.getDeviceName(i);
//			int formats = mmaLib.getDeviceFormats(i);
//			int chans = mmaLib.getDeviceChannels(i);
//			System.out.printf("Device %d name %s formats 0x%X, channels %d\n", i, devName, formats, chans);
//		}
////		// some of them had no name and don't appear on Java list, so assume they aren't real. Just take ones with a name. 
//		SoundCallback callBack = new SoundCallback();
//		int a = mmaLib.wavePrepare(1, 2,  384000, 16, callBack);
//		mmaLib.waveStart();
//		System.out.println("Start : "  + a);
//		try {
//			Thread.sleep(4000);
//		} catch (InterruptedException e) {
//		}
//		mmaLib.waveStop();
//		System.out.println("Stop");
//	}
	
	/**
	 * Get device name, using the full 16 bit characters now used by winmm. 
	 * @param iDevice
	 * @return
	 */
	public String getDeviceName2(int iDevice) {
		Pointer name2 = mmaLib.getDeviceName2(iDevice);
		byte[] rawName = name2.getByteArray(0,64);
		String devName2;
		if (PlatformInfo.calculateOS() == OSType.WINDOWS) {
			devName2 = new String(rawName, StandardCharsets.UTF_16LE);
		}
		else {
			// on Linux, this is still UTF8. 
			for (int i = 0; i < rawName.length; i++) {
				if (rawName[i] == 0) {
					rawName = Arrays.copyOf(rawName, i);
					break;
				}
			}
			devName2 = new String(rawName, StandardCharsets.UTF_8);
//			System.out.println(devName2);
		}
		return devName2;
	}
	
	public class SoundCallback implements MMA.WMMCallback {

		@Override
		public void wmmNewData(Pointer data, int dataLength) {
			System.out.println("Callback " + dataLength);
		}
		
	}
	
	public interface MMA extends Library {
		
		int enumerateDevices();
		
		int getNumDevices();
		
		String getDeviceName(int iDevice);
		
		Pointer getDeviceName2(int iDevice);

		int getDeviceFormats(int iDevice);

		int getDeviceChannels(int iDevice);
		
		int wavePrepare(int iDevice, int nChannels, int sampleRate, int bitDepth, WMMCallback callback);
		
		int waveStart();
		
		int waveStop();
		
		public interface WMMCallback extends Callback { 
			public void wmmNewData(Pointer data, int dataLength);
		}
	}

	/**
	 * @return the mmaLib
	 */
	public MMA getMmaLib() {
		return mmaLib;
	}
}
