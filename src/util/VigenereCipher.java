package util;

public abstract class VigenereCipher {

	public static final String DEFAULT_KEY = "muathamer";

	protected final String key;
	
//	Used to keep track of the index of current character at key to use for Vigenere
	protected int keyCurrIdx;

	public VigenereCipher(String key) {
		this.key = key.toLowerCase();
		this.keyCurrIdx = 0;
	}

}


