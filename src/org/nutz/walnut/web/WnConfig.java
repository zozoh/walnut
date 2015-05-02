package org.nutz.walnut.web;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.web.WebConfig;

public class WnConfig extends WebConfig {

    public WnConfig(String path) {
        super(path);
    }

    public File getSwapHome() {
        return Files.createDirIfNoExists(get("swap-home"));
    }

    public File getDataHome() {
        return Files.createDirIfNoExists(get("data-home"));
    }

}
