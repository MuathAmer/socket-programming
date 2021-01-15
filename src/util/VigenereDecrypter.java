package util;

public class VigenereDecrypter extends VigenereCipher {
	public VigenereDecrypter(String key) {
		super(key);
	}

	public void decryptData(byte[] buffer) {
		for (int i = 0; i < buffer.length; i++) {
			if (!Character.isAlphabetic(buffer[i]))
				continue;

			int modifiedVal = buffer[i] - (key.charAt(keyCurrIdx) - 'a');

//			If character was lowercase and became uppercase, or if it got out of range
			if ((Character.isLowerCase(buffer[i]) && Character.isUpperCase(modifiedVal))
					|| modifiedVal < 'A' || (modifiedVal < 'a' && modifiedVal > 'Z'))
				modifiedVal += 26;

			buffer[i] = (byte) modifiedVal;

			keyCurrIdx = (keyCurrIdx + 1) % key.length();
		}
	}
}
