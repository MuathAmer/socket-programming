package util;

public abstract class VigenereCipher {

	public static final String DEFAULT_KEY = "muathtayma";

	protected final String key;
	protected int count;

	public VigenereCipher(String key) {
		this.key = key.toLowerCase();
		this.count = 0;
	}

}


