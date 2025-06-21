package WindowsSound;

import com.sun.jna.Structure;

public class WaveInCaps extends Structure {

	public short      wMid;
	public short      wPid;
	public int vDriverVersion; // is UINT in definition
	public short[]     szPname = new short[32]; // defo 32
	public int     dwFormats;
	public short      wChannels;
	public short      wReserved1;

	public WaveInCaps() {  
	}

}
