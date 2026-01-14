# Windows MM Sound: Windows Native Sound Card Access

This is a PAMGuard DAQ plugin to acquire sound card data through low level C functions from the Windows Multimedia API.

It provides better support for Windows sound cards than is available through the Javasound library built into PAMGuard.

In particular, it provides support for 24 bit sound cards and for sample rates \> 192kHz, if appropriate hardware is available.

Once installed, PAMGuard will automatically add this system to the list of available DAQ systems in the PAMGuard sound acquisition module.

## Installation

The plugin consists of two components:

1.  A jar file, typically called something like WindowsSoundCard_1_0.jar. This must be added to the plugins folder in your PAMGuard installation.

2.  A Windows dynamic Link Library winmmsound.dll. This must be added to the lib64 folder in your PAMGuard installation.

Releases from the gitHub site should contain a zip archive containing these two sub folders with the appropriate file in each folder. Do not delete existing jar files and dll files from the plugins and lib64 folders since these are needed either by the PAMGuard core, or other plugins, though you should delete any earlier versions of these particular files.

## Usage

Once both files are installed in your PAMGuard installation, restart PAMGuard and open the Sound Acquisition Dialog.

![PAMGuard Sound Acquisition Dialog](daqdialog.png)

At the bottom of the drop-down list of available Data Source Types, select "Windows MM Sound". You will then see a list of available sound card input devices (which will be very similar to the list available for normal sound card input). Select the device you want to use, and also the bit depth which can be 16 or 24.

Close the dialog and start PAMGuard.

Note that this is a new module and has only been tested on a limited number of Windows 11 computers. Please report any problems or errors to [support\@pamguard.org](mailto:support@pamguard.org){.email}, including the type of sound card you were using, the Windows version, PAMGuard version, and the error messages from the command window or [log files](https://www.pamguard.org/olhelp/overview/PamMasterHelp/docs/logfiles.html).

## Related Projects

C language code to build the dll are in the project <https://github.com/douggillespie/WindowsSoundJNA>.
