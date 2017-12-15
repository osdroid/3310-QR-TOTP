package com.podervisual.trinkets;

import java.util.Hashtable;

public class OTP {
	
	public static Hashtable parseUri(String uri) throws Exception {
		Hashtable result = new Hashtable();
		String scheme = uri.substring(0, 10).toLowerCase();
		if (!scheme.equals("otpauth://"))
			throw new Exception("Unsupported scheme " + scheme);
		int idx1 = uri.indexOf('/', 10);
		result.put("type", uri.substring(10, idx1).toLowerCase());
		int idx2 = uri.indexOf('?', idx1);
		result.put("label", uri.substring(idx1 + 1, idx2));
		while (idx2 < uri.length()) {
			idx1 = idx2 + 1;
			idx2 = uri.indexOf('&', idx1);
			if (idx2 == -1)
				idx2 = uri.length();
			int idx3 = uri.indexOf('=', idx1);
			String parameter = uri.substring(idx1, idx3).toLowerCase();
			String value = uri.substring(idx3 + 1, idx2);
			result.put(parameter, value);
		}
		if (!result.containsKey("secret"))
			throw new Exception("Secret missing from URI");
		return result;
	}
	public static String getNDigits(long number, int digits) {
		String text = Long.toString(number);
		while (text.length() < digits)
			text = "0" + text;
		return text.substring(text.length() - digits);
	}
	public static int getIntValue(Hashtable parameters, String name, int defValue) {
		try {
			String storedValue = (String)parameters.get(name);
			if (storedValue != null)
				return Integer.parseInt(storedValue);
		} catch(Exception e) {}
		return defValue;
	}
	public static String calcTotp(String uri) throws Exception {
		Hashtable parameters = parseUri(uri);
		byte[] key = Base32.decode((String)parameters.get("secret"));
		long time = System.currentTimeMillis() / 
				(1000 * getIntValue(parameters, "period", 30));
		byte[] message = new byte[64 + 8];
		for (int i = 0; i < 64; i++)
			message[i] = (byte)((i < key.length ? key[i] : 0) ^ 0x36);
		for (int i = 7; i >= 0; i--) {
			message[64 + i] = (byte)(time & 0xFF);
			time >>= 8;
		}				
		byte[] sha = new SHA1().getDigestOfBytes(message);
		message = new byte[64 + 20];
		for (int i = 0; i < 64; i++)
			message[i] = (byte)((i < key.length ? key[i] : 0) ^ 0x5c);
		System.arraycopy(sha, 0, message, 64, 20);
		sha = new SHA1().getDigestOfBytes(message);
		long result = 0;
		int offset = sha[sha.length - 1] & 0xF;
		for (int i = 0; i < 4; i++) {
			result <<= 8;
			result |= (sha[offset + i] & 0xff);
		}
		result &= 0x7FFFFFFF;
		return getNDigits(result, getIntValue(parameters, "digits", 6));
	}
}
