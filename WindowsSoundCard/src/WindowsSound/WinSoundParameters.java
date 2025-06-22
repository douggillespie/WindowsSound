package WindowsSound;

import Acquisition.SoundCardParameters;

public class WinSoundParameters extends SoundCardParameters implements Cloneable {

	public static final long serialVersionUID = 1L;
	
	public static final int[] BITDEPTHS = {16, 24};
	
	private int bitDepth = 16;

	public WinSoundParameters(String systemType) {
		super(systemType);
	}

	@Override
	public WinSoundParameters clone() {
		return (WinSoundParameters) super.clone();
	}

	/**
	 * @return the bitDepth
	 */
	public int getBitDepth() {
		if (bitDepth == 0) {
			bitDepth = 16;
		}
		return bitDepth;
	}

	/**
	 * @param bitDepth the bitDepth to set
	 */
	public void setBitDepth(int bitDepth) {
		this.bitDepth = bitDepth;
	}

}
