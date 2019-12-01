package org.nutz.walnut.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.log4j.PropertyConfigurator;
import org.nutz.web.WebLauncher;

public class WnLauncher extends WebLauncher {

    public static void main(String[] args) {
        File log4j = new File("log4j.properties");
        if (log4j.exists() && log4j.canRead()) {
            try (InputStream ins = new FileInputStream(log4j)) {
                PropertyConfigurator.configure(ins);
            }
            catch (Throwable e) {
                System.out.println("读取日志配置文件出错了");
            }
        }
        WebLauncher.start(args);
    }
    
}