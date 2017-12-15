package com.podervisual.trinkets;

 public class Base32 {
	 
	public static byte[] decode(String base32) {
		int length = base32.length() * 5 / 8;
		int[] checks = new int[] {1, 3, 4, 6};
		int currCheck = 0;
		int equalSigns = 0;
		for (int i = base32.length() - 1; base32.charAt(i) == '='; i--) {
			equalSigns++;
			if (checks[currCheck] == equalSigns) {
				currCheck++;
				length--;
			}
			if (currCheck >= checks.length)
				break;
		}		
		byte[] raw = new byte[length];
		int rawIndex = 0;
		
		int[] v = new int[8];
		for (int i = 0; i < base32.length(); i += 8) {
			for (int j = 0; j < 8; j++)
				v[j] = getValue(base32, i + j);
			setValue(raw, rawIndex++, (v[0] << 3) | (v[1] >> 2));
			setValue(raw, rawIndex++, (v[1] << 6) | (v[2] << 1) | (v[3] >> 4));
			setValue(raw, rawIndex++, (v[3] << 4) | (v[4] >> 1));
			setValue(raw, rawIndex++, (v[4] << 7) | (v[5] << 2) | (v[6] >> 3));
			setValue(raw, rawIndex++, (v[6] << 5) | v[7]);
		}
		return raw;
	}
 
	private static int getValue(char c) {
		if (c >= 'A' && c <= 'Z')
			return c - 'A';
		if (c >= '2' && c <= '7')
			return c - '2' + 26;
		return 0;
	}
	private static int getValue(String base32, int pos) {
		if (pos >= base32.length())
			return 0;
		return getValue(base32.charAt(pos));
	}
	private static void setValue(byte[] raw, int pos, int value) {
		if (pos >= raw.length)
			return;
		raw[pos] = (byte)(value & 0xFF);
	}
}