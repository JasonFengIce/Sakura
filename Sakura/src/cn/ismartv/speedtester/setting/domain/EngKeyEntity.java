package cn.ismartv.speedtester.setting.domain;

import android.os.SystemClock;
import android.view.KeyEvent;

public class EngKeyEntity {
	private final static int RC_SERIAL = 0;
	private final static int KB_SERIAL = 1;
	private final static int UNDEFINED = -1;
	private static int[] RCKeySerial = {KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_6};
	private static int[] KBKeySerial = {KeyEvent.KEYCODE_I, KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_I, KeyEvent.KEYCODE_S};
	private static int currentSerial = UNDEFINED;
	private static int pos = 0;
	private static long lastPressTime = 0;
	/**
	 * Call this method when press a key that is one of the useful key
	 * @param keyCode, the KeyCode of the key ,defined in KeyEvent class 
	 * @return true, if current key press serial has finish.otherwise return false.
	 */
	public static boolean pressOneKey(int keyCode){
		if(currentSerial==RC_SERIAL) {
			if(RCKeySerial[pos]==keyCode){
				long timeInterval = SystemClock.uptimeMillis() - lastPressTime;
				if(timeInterval<3000){
					++pos;
					if(pos==RCKeySerial.length){
						pos = 0;
						currentSerial = UNDEFINED;
						lastPressTime = 0;
						return true;
					}
				} else {
					if(keyCode == RCKeySerial[0]){
						pos = 1;
					} else {
						pos = 0;
					}
				}
			}
		} else if(currentSerial==KB_SERIAL) {
			if(KBKeySerial[pos]==keyCode){
				long timeIterval = SystemClock.uptimeMillis() - lastPressTime;
				if(timeIterval<3000){
					++pos;
					if(pos==KBKeySerial.length){
						pos = 0;
						currentSerial = UNDEFINED;
						lastPressTime = 0;
						return true;
					}
				} else {
					if(keyCode == KBKeySerial[0]){
						pos = 1;
					} else {
						pos = 0;
					}
				}
			}
		}
		lastPressTime = SystemClock.uptimeMillis();
		return false;
	}
	
	/**
	 * Check if the key is useful for current serial.
	 * @param keyCode
	 * @return true if the key is useful, otherwise return false and initialize all status.
	 */
	public static boolean checkUseful(int keyCode) {
		if(currentSerial==UNDEFINED){
			for(int rcKeyCode:RCKeySerial){
				if(keyCode==rcKeyCode){
					currentSerial=RC_SERIAL;
					lastPressTime = SystemClock.uptimeMillis();
					return true;
				}
			}
			for(int kbKeyCode:KBKeySerial){
				if(keyCode==kbKeyCode){
					currentSerial=KB_SERIAL;
					return true;
				}
			}
		} else if(currentSerial==RC_SERIAL) {
			for(int rcKeyCode:RCKeySerial){
				if(rcKeyCode==keyCode){
					return true;
				}
			}
			for(int kbKeyCode:KBKeySerial){
				if(kbKeyCode==keyCode){
					pos = 0;
					currentSerial = KB_SERIAL;
					return true;
				}
			}
			
		} else if(currentSerial==KB_SERIAL) {
			for(int kbKeyCode:KBKeySerial){
				if(kbKeyCode==keyCode){
					return true;
				}
			}
			for(int rcKeyCode:RCKeySerial){
				if(rcKeyCode==keyCode){
					pos = 0;
					currentSerial = RC_SERIAL;
					return true;
				}
			}
		}
		currentSerial = UNDEFINED;
		pos = 0;
		lastPressTime = 0;
		return false;
	}
}
