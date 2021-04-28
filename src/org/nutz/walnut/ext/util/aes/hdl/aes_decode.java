package org.nutz.walnut.ext.util.aes.hdl;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.repo.Base64;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.weixin.repo.com.qq.weixin.mp.aes.AesException;

public class aes_decode implements JvmHdl {

    private static final Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String cipherStr = hc.params.get("cipher", "AES/CBC/PKCS5PADDING");
        String text = Cmds.checkParamOrPipe(sys, hc.params, 0);
        String aesKeyBase64 = hc.params.check("aeskey");
        String ivBase64 = hc.params.check("iv");
        byte[] result = decrypt(cipherStr, text, aesKeyBase64, ivBase64);
        if (result != null)
            sys.out.write(result);
    }

    /**
     * 对密文进行解密.
     * 
     * @param shartStrings
     *            需要解密的密文
     * @return 解密得到的明文
     * @throws AesException
     *             axes解密失败
     */
    static public byte[] decrypt(String cipherStr,
                                 String textBase64,
                                 String aesKeyBase64,
                                 String ivBase64)
            throws AesException {
        try {
            // 设置解密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance(cipherStr);
            SecretKeySpec key_spec = new SecretKeySpec(Base64.decode(aesKeyBase64), "AES");
            IvParameterSpec iv = new IvParameterSpec(Base64.decode(ivBase64));
            cipher.init(Cipher.DECRYPT_MODE, key_spec, iv);

            // 使用BASE64对密文进行解码
            byte[] encrypted = Base64.decode(textBase64);

            // 解密
            return cipher.doFinal(encrypted);
        }
        catch (Exception e) {
            log.warn("aes decode fail", e);
            return null;
        }
    }

    static public byte[] decrypt(String cipherStr, byte[] encrypted, byte[] aeskey, byte[] _iv)
            throws AesException {
        try {
            // 设置解密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance(cipherStr);
            SecretKeySpec key_spec = new SecretKeySpec(aeskey, "AES");
            IvParameterSpec iv = new IvParameterSpec(_iv);
            cipher.init(Cipher.DECRYPT_MODE, key_spec, iv);
            // 解密
            return cipher.doFinal(encrypted);
        }
        catch (Exception e) {
            log.warn("aes decode fail", e);
            return null;
        }
    }

}
