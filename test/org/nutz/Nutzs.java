package org.nutz;

import static java.lang.String.format;

import java.io.File;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.nutz.aop.ClassDefiner;
import org.nutz.aop.DefaultClassDefiner;
import org.nutz.dao.DatabaseMeta;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;

import com.site0.walnut.util.Wlang;

@SuppressWarnings("removal")
public class Nutzs {

    private static Properties pp;

    public static void checkProperties() {
        if (null == pp)
            loadProperties("nutz-test.properties");
    }

    public static void loadProperties(String fileName) {
        InputStream is = null;
        try {
            pp = new Properties();
            File f = Files.findFile(fileName);
            if(f == null)
                throw new RuntimeException("nutz-test.properties Not FOUND!!! tmpl.nutz-test.properties is a example.");
            is = Streams.fileIn(f);
            pp.load(is);
        }
        catch (Exception e) {
            throw Wlang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(is);
        }
    }

    public static String getDriver() {
        checkProperties();
        return Strings.trim(pp.getProperty("driver"));
    }

    public static String getUrl() {
        checkProperties();
        return Strings.trim(pp.getProperty("url"));
    }

    public static String getPassword() {
        checkProperties();
        return Strings.trim(pp.getProperty("password"));
    }

    public static String getUserName() {
        checkProperties();
        return Strings.trim(pp.getProperty("username"));
    }

    private static Map<String, Ioc> nuts = new HashMap<String, Ioc>();

    public static Ioc getIoc(String key) {
        Ioc nut = nuts.get(key);
        if (null == nut) {
            synchronized (Nutzs.class) {
                nut = nuts.get(key);
                try {
                    if (null == nut) {
                        nut = new NutIoc(new JsonLoader(key));
                        nuts.put(key, nut);
                    }
                }
                catch (Exception e) {
                    throw Wlang.wrapThrow(e);
                }
            }
        }
        return nut;
    }

    public static void depose() {
        for (Ioc ioc : nuts.values())
            ioc.depose();
        nuts.clear();
        nuts = null;
    }

    public static void notSupport(String message) {
        // junit.framework.Assert.fail(message);
    }

    public static void notSupport(DatabaseMeta meta) {
        notSupport(format("[%S] don't support this test", meta.getTypeName()));
    }

    /**
     * 调用此方法将改变AOP类名命名规则
     * @return
     */
    public static ClassDefiner cd() {
        return AccessController.doPrivileged(new PrivilegedAction<DefaultClassDefiner>() {
            public DefaultClassDefiner run() {
                return new DefaultClassDefiner();
            }
        });
    }
}
