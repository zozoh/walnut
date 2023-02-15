package org.nutz.walnut.ext.net.mailx.hdl;

import java.io.InputStream;

import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.mailx.MailxContext;
import org.nutz.walnut.ext.net.mailx.MailxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.simplejavamail.api.mailer.config.Pkcs12Config;
import org.simplejavamail.api.mailer.config.Pkcs12Config.Pkcs12ConfigBuilder;

public class mailx_config extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        String json;
        // 从管道读取
        if (params.vals.length == 0) {
            json = sys.in.readAll();
        }
        // 从文件读取
        else {
            String ph = params.val_check(0);
            WnObj oConf = Wn.checkObj(sys, ph);
            json = sys.io.readText(oConf);
        }
        NutMap conf = Json.fromJson(NutMap.class, json);

        for (String key : conf.keySet()) {
            // to
            if ("to".equals(key)) {
                String to = conf.getString(key);
                if (!Ws.isBlank(to)) {
                    fc.builder.to(to.trim());
                }
            }
            // subject
            else if ("subject".equals(key)) {
                String subject = conf.getString(key);
                if (!Ws.isBlank(subject)) {
                    fc.builder.withSubject(subject);
                }
            }
            // content
            else if ("content".equals(key)) {
                setContent(fc, conf, conf.getString("content"));
            }
            // content path
            else if ("contentPath".equals(key)) {
                String cph = conf.getString("contentPath");
                WnObj co = Wn.checkObj(sys, cph);
                String content = sys.io.readText(co);
                setContent(fc, conf, content);
            }
            // attachment
            else if ("attachment".equals(key)) {
                String[] atPaths = conf.getAs("attachment", String[].class);
                for (String atPath : atPaths) {
                    WnObj ato = Wn.checkObj(sys, atPath);
                    String name = ato.name();
                    String mime = ato.mime();
                    byte[] bs = sys.io.readBytes(ato);
                    fc.builder.withAttachment(name, bs, mime);
                }
            }
            // security
            else if ("security".equals(key)) {
                NutMap secu = conf.getAs("security", NutMap.class);
                if (secu.is("type", "SMIME")) {
                    // 签名设置
                    NutMap sign = secu.getAs("sign", NutMap.class);
                    if (null != sign && !sign.isEmpty()) {
                        Pkcs12ConfigBuilder bu = Pkcs12Config.builder();
                        Pkcs12Config pkcs12;
                        String storePath = sign.getString("storePath");
                        WnObj oStore = Wn.checkObj(sys, storePath);
                        byte[] bs = sys.io.readBytes(oStore);
                        String storePasswd = sign.getString("storePassword");
                        String keyAlias = sign.getString("keyAlias");
                        String keyPassword = sign.getString("keyPassword");
                        pkcs12 = bu.pkcs12Store(bs)
                                   .storePassword(storePasswd)
                                   .keyAlias(keyAlias)
                                   .keyPassword(Ws.sBlank(keyPassword, ""))
                                   .build();
                        fc.builder.signWithSmime(pkcs12);
                    }

                    // 证书路径
                    String ecf = secu.getString("encryptCertFile");
                    if (!Ws.isBlank(ecf)) {
                        WnObj oCert = Wn.checkObj(sys, ecf);
                        InputStream ins = sys.io.getInputStream(oCert, 0);
                        try {
                            fc.builder.encryptWithSmime(ins);
                        }
                        finally {
                            Streams.safeClose(ins);
                        }
                    }
                }
                // 不支持
                else {
                    throw Er.create("e.cmd.mailx.security.NotSupportedType",
                                    secu.getString("type"));
                }
            }
        }
    }

    private void setContent(MailxContext fc, NutMap conf, String content) {
        if (!Ws.isBlank(content)) {
            String ct = conf.getString("contentType", "text");
            if ("html".equals(ct)) {
                fc.builder.withHTMLText(content);
            } else {
                fc.builder.withPlainText(content);
            }
        }
    }

}
