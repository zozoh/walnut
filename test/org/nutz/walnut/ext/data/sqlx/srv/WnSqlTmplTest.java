package org.nutz.walnut.ext.data.sqlx.srv;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;

public class WnSqlTmplTest {

    // @Test
    // public void test_var_as_where() {
    // String s = "SELECT * FROM t_pet WHERE ${@vars=where; pick=a,b}";
    // NutMap context = NutMap.WRAP("{a:'A',b:100}");
    // WnSqlTmpl sqlt = WnSqlTmpl.parse(s);
    // List<String> params = new ArrayList<>(2);
    // String sql = sqlt.render(context, params);
    // assertEquals("SELECT * FROM t_pet WHERE a=? AND b=?", sql);
    // assertEquals(2, params.size());
    // assertEquals("A", params.get(0));
    // assertEquals(100, params.get(1));
    // }

    @Test
    public void test_var_as_insert() {
        String s = "INSERT INTO t_pet(${@vars=insert.columns; pick=a,b})"
                   + " VALUES (${@vars=insert.values; pick=a,b})";
        NutMap context = NutMap.WRAP("{a:'A',b:100,c:false,d:'D'}");
        WnSqlTmpl sqlt = WnSqlTmpl.parse(s);
        List<String> params = new ArrayList<>(2);
        String sql = sqlt.render(context, params);
        assertEquals("INSERT INTO t_pet(a,b) VALUES (?,?)", sql);
        assertEquals(2, params.size());
        assertEquals("a", params.get(0));
        assertEquals("b", params.get(1));
        
        sql = sqlt.render(context, null);
        assertEquals("INSERT INTO t_pet(a,b) VALUES ('A',100)", sql);
    }

    @Test
    public void test_var_as_update() {
        String s = "UPDATE t_pet SET ${@vars=update; pick=a,b}";
        NutMap context = NutMap.WRAP("{a:'A',b:100,c:false,d:'D'}");
        WnSqlTmpl sqlt = WnSqlTmpl.parse(s);
        List<String> params = new ArrayList<>(2);
        String sql = sqlt.render(context, params);
        assertEquals("UPDATE t_pet SET a=?,b=?", sql);
        assertEquals(2, params.size());
        assertEquals("a", params.get(0));
        assertEquals("b", params.get(1));
        
        sql = sqlt.render(context, null);
        assertEquals("UPDATE t_pet SET a='A',b=100", sql);
    }
}
