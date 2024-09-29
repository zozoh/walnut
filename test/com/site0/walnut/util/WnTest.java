package com.site0.walnut.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.impl.box.WnSystem;

public class WnTest {

    @Test
    public void test_evalTimeAndOffset() {
        long ams;

        ams = Wtime.valueOf("(2021/11/9)+3d");
        assertEquals("2021-11-12", Wtime.formatDate(new Date(ams)));

        ams = Wtime.valueOf("(1636387320000)+3d");
        assertEquals("2021-11-12", Wtime.formatDate(new Date(ams)));

        ams = Wtime.valueOf("(2021/11/9)+3M");
        assertEquals("2022-02-09", Wtime.formatDate(new Date(ams)));

        ams = Wtime.valueOf("(2021/11/9)+2y");
        assertEquals("2023-11-09", Wtime.formatDate(new Date(ams)));
    }

    @Test
    public void test_evalDatetimeStrToAMS_2() {
        long ams, exp;
        // 开始测试
        ams = Wtime.valueOf("Sun");
        exp = Wtime.weekDayInMs(0);
        assertEquals(exp, ams);

        ams = Wtime.valueOf("Mon");
        exp = Wtime.weekDayInMs(1);
        assertEquals(exp, ams);

        ams = Wtime.valueOf("Tue");
        exp = Wtime.weekDayInMs(2);
        assertEquals(exp, ams);

        ams = Wtime.valueOf("Wed");
        exp = Wtime.weekDayInMs(3);
        assertEquals(exp, ams);

        ams = Wtime.valueOf("Thu");
        exp = Wtime.weekDayInMs(4);
        assertEquals(exp, ams);

        ams = Wtime.valueOf("Fri");
        exp = Wtime.weekDayInMs(5);
        assertEquals(exp, ams);

        ams = Wtime.valueOf("Sat");
        exp = Wtime.weekDayInMs(6);
        assertEquals(exp, ams);

        ams = Wtime.valueOf("monthBegin");
        exp = Wtime.monthDayInMs(0);
        assertEquals(exp, ams);

        ams = Wtime.valueOf("monthEnd");
        exp = Wtime.monthDayEndInMs(-1);
        assertEquals(exp, ams);
    }

    @Test
    public void test_evalDatetimeStrToAMS() {
        Calendar c = Calendar.getInstance();

        // 时间清零
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        // 准备今日开始结束时间
        long dayms = 86400000L;
        long now_ms0 = c.getTimeInMillis();
        long now_ms1 = now_ms0 + dayms;

        // 开始测试
        long ams = Wtime.valueOf("now");
        assertTrue(ams >= now_ms0);
        assertTrue(ams < now_ms1);

        ams = Wtime.valueOf("today");
        assertEquals(now_ms0, ams);

        ams = Wtime.valueOf("today+1d");
        assertEquals(now_ms0 + dayms, ams);

        ams = Wtime.valueOf("today-1d");
        assertEquals(now_ms0 - dayms, ams);

        ams = Wtime.valueOf("today+2d");
        assertEquals(now_ms0 + dayms * 2, ams);

        ams = Wtime.valueOf("today-2d");
        assertEquals(now_ms0 - dayms * 2, ams);
    }

    @Test
    public void test_explain_obj() {
        NutMap context = Wlang.map("x:100,name:'xiaobai',male:true");
        context.put("pet", Wlang.map("race:'dog',age:8"));

        NutMap map = Wlang.map("size:'=x',age:'=pet.age'");
        NutMap m2 = (NutMap) Wn.explainObj(context, map);
        assertEquals(100, m2.getInt("size"));
        assertEquals(8, m2.getInt("age"));

        map = Wlang.map("hasAge:'==pet.age'");
        m2 = (NutMap) Wn.explainObj(context, map);
        assertEquals(true, m2.getBoolean("hasAge"));

        map = Wlang.map("hasSex:'==pet.sex'");
        m2 = (NutMap) Wn.explainObj(context, map);
        assertEquals(false, m2.getBoolean("hasAge"));

        map = Wlang.map("brief:'->${pet.age}:${pet.race}@${name}'");
        m2 = (NutMap) Wn.explainObj(context, map);
        assertEquals("8:dog@xiaobai", m2.getString("brief"));

        map = Wlang.map("petName:'=page.name?AA'");
        m2 = (NutMap) Wn.explainObj(context, map);
        assertEquals("AA", m2.getString("petName"));
    }

    @Test
    public void test_explain_obj2() {
        NutMap context = Wlang.map("x:100,name:'xiaobai',male:true");
        context.put("pet", Wlang.map("race:'dog',age:8"));
        context.put("G", new WnElRuntime());

        NutMap map = Wlang.map("n:'=>name.length()',race:'=>pet.race.substring(0,1).toUpperCase()'");
        NutMap m2 = (NutMap) Wn.explainObj(context, map);
        assertEquals(7, m2.getInt("n"));
        assertEquals("D", m2.get("race"));

        map = Wlang.map("n1:'=>G.count(name)',n2:'=>G.count(abc)'");
        m2 = (NutMap) Wn.explainObj(context, map);
        assertEquals(1, m2.getInt("n1"));
        assertEquals(0, m2.getInt("n2"));
    }

    @Test
    public void test_explain_array() {
        NutMap context = Wlang.map("name:'abc',age:12,pet:{name:'xx'}");
        Object input = Json.fromJson("['=name',{b:'=pet.name'},'=age']");
        List<?> re = (List<?>) Wn.explainObj(context, input);
        JsonFormat jfmt = JsonFormat.compact().setQuoteName(false);

        assertEquals(3, re.size());
        assertEquals("abc", re.get(0));
        assertEquals("{b:\"xx\"}", Json.toJson(re.get(1), jfmt));
        assertEquals(12, re.get(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_explain_array_as_scope() {
        List<Object> list = new LinkedList<>();
        list.add(Wlang.map("name:'abc', age:12"));
        list.add(Wlang.map("name:'def', age:14"));
        list.add(Wlang.map("name:'ghi', age:30"));
        NutMap context = Wlang.map("list", list);
        Object input = Wlang.map("{list:[{a:'=name',b:'=age'}]}");
        JsonFormat jfmt = JsonFormat.compact().setQuoteName(false);

        NutMap re = (NutMap) Wn.explainObj(context, input);
        List<Object> reList = (List<Object>) re.get("list");
        assertEquals(3, reList.size());
        assertEquals("{a:\"abc\",b:12}", Json.toJson(reList.get(0), jfmt));
        assertEquals("{a:\"def\",b:14}", Json.toJson(reList.get(1), jfmt));
        assertEquals("{a:\"ghi\",b:30}", Json.toJson(reList.get(2), jfmt));

        input = Wlang.map("{list:[':scope=list',{a:'=name',b:'=age'}]}");
        reList = (List<Object>) re.get("list");
        assertEquals(3, reList.size());
        assertEquals("{a:\"abc\",b:12}", Json.toJson(reList.get(0), jfmt));
        assertEquals("{a:\"def\",b:14}", Json.toJson(reList.get(1), jfmt));
        assertEquals("{a:\"ghi\",b:30}", Json.toJson(reList.get(2), jfmt));
    }

    @Test
    public void test_s() {
        int mode = Wn.S.WM;
        assertEquals(false, Wn.S.canRead(mode));
        assertEquals(true, Wn.S.canWite(mode));
        assertEquals(false, Wn.S.canAppend(mode));
        assertEquals(true, Wn.S.canModify(mode));
        assertEquals(true, Wn.S.canWriteOrAppend(mode));
        assertEquals(false, Wn.S.isRead(mode));
        assertEquals(false, Wn.S.isWrite(mode));
        assertEquals(true, Wn.S.isWriteModify(mode));
        assertEquals(false, Wn.S.isWriteAppend(mode));
        assertEquals(false, Wn.S.isReadWrite(mode));

        mode = Wn.S.RW;
        assertEquals(true, Wn.S.canRead(mode));
        assertEquals(true, Wn.S.canWite(mode));
        assertEquals(false, Wn.S.canAppend(mode));
        assertEquals(false, Wn.S.canModify(mode));
        assertEquals(true, Wn.S.canWriteOrAppend(mode));
        assertEquals(false, Wn.S.isRead(mode));
        assertEquals(false, Wn.S.isWrite(mode));
        assertEquals(false, Wn.S.isWriteModify(mode));
        assertEquals(false, Wn.S.isWriteAppend(mode));
        assertEquals(true, Wn.S.isReadWrite(mode));
    }

    @Test
    public void test_appendPath() {
        assertEquals("/", Wn.appendPath("/", ""));
        assertEquals("", Wn.appendPath(null, ""));
        assertEquals("", Wn.appendPath("", ""));

        assertEquals("a/b/c", Wn.appendPath(null, "a", "b", "c"));
        assertEquals("/a/b/c", Wn.appendPath("/a/", "/b", "c"));
        assertEquals("/a/b/c", Wn.appendPath("/a//b", "c"));
    }

    @Test
    public void test_concatPath() {
        assertEquals("/", Wn.concatPath("/", ""));
        assertEquals("", Wn.concatPath(null, ""));
        assertEquals("", Wn.concatPath("", ""));

        assertEquals("a/b/c", Wn.concatPath(null, "a", "b", "c"));
        assertEquals("/b/c", Wn.concatPath("/a/", "/b", "c"));
        assertEquals("/a/b/c", Wn.concatPath("/a//b", "c"));

        assertEquals("~/x/y/z/", Wn.concatPath("/a/b", "~/x", "y/z/"));
        assertEquals("id:xxx/mo/qq", Wn.concatPath("/a/b", "id:xxx/mo", "qq"));

        assertEquals("~/xyz", Wn.concatPath("/a", "b", "~/xyz"));
        assertEquals("id:xxx", Wn.concatPath("~/a", "b", "id:xxx"));
        assertEquals("/x/y/z/", Wn.concatPath("~/a", "b", "/x/y/z/"));
    }

    @Test
    public void test_parse_mode() {
        assertEquals("rwxrwxrwx", Wn.Io.modeToStr(0777));
        assertEquals("rwxr-xr--", Wn.Io.modeToStr(0754));
        assertEquals("rwxr-xr-x", Wn.Io.modeToStr(0755));
        assertEquals("rwx------", Wn.Io.modeToStr(0700));
        assertEquals("r-x------", Wn.Io.modeToStr(0500));
        assertEquals("---------", Wn.Io.modeToStr(0000));

        assertEquals(0777, Wn.Io.modeFromStr("rwxrwxrwx"));
        assertEquals(0754, Wn.Io.modeFromStr("rwxr-xr--"));
        assertEquals(0755, Wn.Io.modeFromStr("rwxr-xr-x"));
        assertEquals(0700, Wn.Io.modeFromStr("rwx------"));
        assertEquals(0500, Wn.Io.modeFromStr("r-x------"));
        assertEquals(0000, Wn.Io.modeFromStr("---------"));

    }

    @Test
    public void test_wildcard() {
        assertTrue(Wn.matchWildcard("abc", "abc*"));
        assertTrue(Wn.matchWildcard("abcD", "abc*"));
        assertTrue(Wn.matchWildcard("abc", "*abc"));
        assertTrue(Wn.matchWildcard("Xabc", "*abc"));
        assertTrue(Wn.matchWildcard("abc", "*abc*"));
        assertTrue(Wn.matchWildcard("XabcY", "*abc*"));
        assertTrue(Wn.matchWildcard("Xabc", "*abc*"));
        assertTrue(Wn.matchWildcard("abcY", "*abc*"));
        assertTrue(Wn.matchWildcard("abcY", "**"));
        assertTrue(Wn.matchWildcard("abcY", "*"));

        assertFalse(Wn.matchWildcard("xyz", "xz*"));
        assertFalse(Wn.matchWildcard("xyz", "*t*"));
    }

    @Test
    public void test_normalize() {
        WnAccount me = new WnAccount();
        me.setMeta("home", "/home/zozoh");
        WnSystem sys = new WnSystem(null);
        sys.session = new WnAuthSession(R.UU32(), me);
        sys.session.getVars().put("HOME", "/home/zozoh");
        sys.session.getVars().put("PWD", "$HOME/workspace/test");
        sys.session.getVars().put("ABC", "haha");

        assertEquals("/home/zozoh/bin", Wn.normalizePath("~/bin", sys));
        assertEquals("/home/zozoh/workspace/test/bin", Wn.normalizePath("./bin", sys));
        assertEquals("cmd_echo 'haha'", Wn.normalizeStr("cmd_echo '$ABC'", sys));
        assertEquals("~/abc", Wn.normalizeStr("~/abc", sys));
        assertEquals("\\n", Wn.normalizeStr("\\n", sys));
    }

}
