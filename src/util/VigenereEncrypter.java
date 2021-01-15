package util;

public class VigenereEncrypter extends VigenereCipher {
	public VigenereEncrypter(String key) {
		super(key);
	}

	public void encryptData(byte[] buffer) {
		for (int i = 0; i < buffer.length; i++) {
			if (!Character.isAlphabetic(buffer[i]))
				continue;

			int modifiedVal = buffer[i] + (key.charAt(keyCurrIdx) - 'a');

//			If character was uppercase and became lowercase, or if it got out of range
			if ((Character.isUpperCase(buffer[i]) && Character.isLowerCase(modifiedVal))
					|| modifiedVal > 'z' || (modifiedVal < 'a' && modifiedVal > 'Z'))
				modifiedVal -= 26;

			buffer[i] = (byte) modifiedVal;

			keyCurrIdx = (keyCurrIdx + 1) % key.length();
		}
	}
}
