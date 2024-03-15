package org.nutz.lang.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.site0.walnut.util.Wlang;

public class Encrypts {

	public static MessageDigest sha1() {
		try {
			return MessageDigest.getInstance("sha1");
		}
		catch (NoSuchAlgorithmException e) {
			throw Wlang.noImplement(); // 不可能
		}
	}
	
	public static MessageDigest md5() {
		try {
			return MessageDigest.getInstance("md5");
		}
		catch (NoSuchAlgorithmException e) {
			throw Wlang.noImplement(); // 不可能
		}
	}
}
