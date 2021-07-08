package org.nutz.walnut.util;

import java.io.File;
import java.io.InputStream;
import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;
import org.nutz.walnut.alg.sum.WnDigesting;

public abstract class Wsum {

    public static byte[] md5(byte[] buf) {
        WnDigesting wd = new WnDigesting("MD5");
        wd.addData(buf);
        return wd.getBytes();
    }

    public static byte[] md5(byte[] buf, int offset, int len) {
        byte[] bs = new byte[len];
        System.arraycopy(buf, offset, bs, 0, len);
        return md5(bs);
    }

    public static byte[] md5(InputStream ins) {
        WnDigesting wd = new WnDigesting("MD5");
        wd.addStream(ins);
        return wd.getBytes();
    }

    public static byte[] md5(File f) {
        InputStream ins = Streams.fileIn(f);
        return md5(ins);
    }

    public static byte[] md5(String str) {
        byte[] bs = str.getBytes(Encoding.CHARSET_UTF8);
        return md5(bs);
    }

    public static String md5AsString(byte[] buf) {
        WnDigesting wd = new WnDigesting("MD5");
        wd.addData(buf);
        return wd.getString();
    }

    public static String md5AsString(byte[] buf, int offset, int len) {
        byte[] bs = new byte[len];
        System.arraycopy(buf, offset, bs, 0, len);
        return md5AsString(bs);
    }

    public static String md5AsString(InputStream ins) {
        WnDigesting wd = new WnDigesting("MD5");
        wd.addStream(ins);
        return wd.getString();
    }

    public static String md5AsString(File f) {
        InputStream ins = Streams.fileIn(f);
        return md5AsString(ins);
    }

    public static String md5AsString(String str) {
        byte[] bs = str.getBytes(Encoding.CHARSET_UTF8);
        return md5AsString(bs);
    }

    public static byte[] sha1(byte[] buf) {
        WnDigesting wd = new WnDigesting("SHA1");
        wd.addData(buf);
        return wd.getBytes();
    }

    public static byte[] sha1(byte[] buf, int offset, int len) {
        byte[] bs = new byte[len];
        System.arraycopy(buf, offset, bs, 0, len);
        return sha1(bs);
    }

    public static byte[] sha1(InputStream ins) {
        WnDigesting wd = new WnDigesting("SHA1");
        wd.addStream(ins);
        return wd.getBytes();
    }

    public static byte[] sha1(File f) {
        InputStream ins = Streams.fileIn(f);
        return sha1(ins);
    }

    public static byte[] sha1(String str) {
        byte[] bs = str.getBytes(Encoding.CHARSET_UTF8);
        return sha1(bs);
    }

    public static String sha1AsString(byte[] buf) {
        WnDigesting wd = new WnDigesting("SHA1");
        wd.addData(buf);
        return wd.getString();
    }

    public static String sha1AsString(byte[] buf, int offset, int len) {
        byte[] bs = new byte[len];
        System.arraycopy(buf, offset, bs, 0, len);
        return sha1AsString(bs);
    }

    public static String sha1AsString(InputStream ins) {
        WnDigesting wd = new WnDigesting("SHA1");
        wd.addStream(ins);
        return wd.getString();
    }

    public static String sha1AsString(File f) {
        InputStream ins = Streams.fileIn(f);
        return sha1AsString(ins);
    }

    public static String sha1AsString(String str) {
        byte[] bs = str.getBytes(Encoding.CHARSET_UTF8);
        return sha1AsString(bs);
    }

    public static byte[] sha256(byte[] buf) {
        WnDigesting wd = new WnDigesting("SHA-256");
        wd.addData(buf);
        return wd.getBytes();
    }

    public static byte[] sha256(byte[] buf, int offset, int len) {
        byte[] bs = new byte[len];
        System.arraycopy(buf, offset, bs, 0, len);
        return sha256(bs);
    }

    public static byte[] sha256(InputStream ins) {
        WnDigesting wd = new WnDigesting("SHA-256");
        wd.addStream(ins);
        return wd.getBytes();
    }

    public static byte[] sha256(File f) {
        InputStream ins = Streams.fileIn(f);
        return sha256(ins);
    }

    public static byte[] sha256(String str) {
        byte[] bs = str.getBytes(Encoding.CHARSET_UTF8);
        return sha256(bs);
    }

    public static String sha256AsString(byte[] buf) {
        WnDigesting wd = new WnDigesting("SHA-256");
        wd.addData(buf);
        return wd.getString();
    }

    public static String sha256AsString(byte[] buf, int offset, int len) {
        byte[] bs = new byte[len];
        System.arraycopy(buf, offset, bs, 0, len);
        return sha256AsString(bs);
    }

    public static String sha256AsString(InputStream ins) {
        WnDigesting wd = new WnDigesting("SHA-256");
        wd.addStream(ins);
        return wd.getString();
    }

    public static String sha256AsString(File f) {
        InputStream ins = Streams.fileIn(f);
        return sha256AsString(ins);
    }

    public static String sha256AsString(String str) {
        byte[] bs = str.getBytes(Encoding.CHARSET_UTF8);
        return sha256AsString(bs);
    }

    public static String digestAsString(String algorithm, byte[] buf) {
        WnDigesting wd = new WnDigesting(algorithm);
        wd.addData(buf);
        return wd.getString();
    }

    public static String digestAsString(String algorithm, byte[] buf, int offset, int len) {
        byte[] bs = new byte[len];
        System.arraycopy(buf, offset, bs, 0, len);
        return digestAsString(algorithm, bs);
    }

    public static String digestAsString(String algorithm, InputStream ins) {
        WnDigesting wd = new WnDigesting(algorithm);
        wd.addStream(ins);
        return wd.getString();
    }

    public static String digestAsString(String algorithm, File f) {
        InputStream ins = Streams.fileIn(f);
        return digestAsString(algorithm, ins);
    }

    public static String digestAsString(String algorithm, String str) {
        byte[] bs = str.getBytes(Encoding.CHARSET_UTF8);
        return digestAsString(algorithm, bs);
    }

    public static byte[] digest(String algorithm, byte[] buf) {
        WnDigesting wd = new WnDigesting(algorithm);
        wd.addData(buf);
        return wd.getBytes();
    }

    public static byte[] digest(String algorithm, byte[] buf, int offset, int len) {
        byte[] bs = new byte[len];
        System.arraycopy(buf, offset, bs, 0, len);
        return digest(algorithm, bs);
    }

    public static byte[] digest(String algorithm, InputStream ins) {
        WnDigesting wd = new WnDigesting(algorithm);
        wd.addStream(ins);
        return wd.getBytes();
    }

    public static byte[] digest(String algorithm, File f) {
        InputStream ins = Streams.fileIn(f);
        return digest(algorithm, ins);
    }

    public static byte[] digest(String algorithm, String str) {
        byte[] bs = str.getBytes(Encoding.CHARSET_UTF8);
        return digest(algorithm, bs);
    }

}
