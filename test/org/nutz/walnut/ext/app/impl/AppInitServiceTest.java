package org.nutz.walnut.ext.app.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.walnut.ext.app.bean.init.AppInitGroup;

public class AppInitServiceTest {

    private static final String CASE_HOME = "org/nutz/walnut/ext/app/impl/cases/";

    private static String _READ(String name) {
        return Files.read(CASE_HOME + name);
    }

    private static AppInitGroup _GROUP(String name) {
        String json = Files.read(CASE_HOME + name);
        return Json.fromJson(AppInitGroup.class, json);
    }

    @Test
    public void test_parse_0() {
        AppInitService init = new AppInitService();
        String input = _READ("init0_input.txt");
        AppInitGroup group = init.parse(input);
        AppInitGroup expect = _GROUP("init0_result.json");

        assertTrue(group.equals(expect));
    }

}
