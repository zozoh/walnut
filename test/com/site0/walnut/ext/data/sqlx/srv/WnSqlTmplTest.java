package com.site0.walnut.ext.data.sqlx.srv;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.util.Wlang;

public class WnSqlTmplTest {

    @Test
    public void test_var_in_scope() {
        String s = "SELECT * FROM t_pet WHERE ${@vars=where; scope=query; pick=a,b}";
        NutMap query = NutMap.WRAP("{a:'(100,500)',b:{$lt:6,$gte:9}}");
        NutMap context = Wlang.map("query", query);
        WnSqlTmpl sqlt = WnSqlTmpl.parse(s);
        List<SqlParam> params = new ArrayList<>(2);
        String sql = sqlt.render(context, params);
        assertEquals("SELECT * FROM t_pet WHERE (a>? AND a<?) AND (b<? AND b>=?)", sql);
        assertEquals(4, params.size());
        assertEquals("a=100", params.get(0).toString());
        assertEquals("a=500", params.get(1).toString());
        assertEquals("b=6", params.get(2).toString());
        assertEquals("b=9", params.get(3).toString());

        sql = sqlt.render(context, null);
        assertEquals("SELECT * FROM t_pet WHERE (a>100 AND a<500) AND (b<6 AND b>=9)", sql);
    }

    @Test
    public void test_var_as_where_in_range() {
        String s = "SELECT * FROM t_pet WHERE ${@vars=where; pick=a,b}";
        NutMap context = NutMap.WRAP("{a:'(100,500)',b:{$lt:6,$gte:9}}");
        WnSqlTmpl sqlt = WnSqlTmpl.parse(s);
        List<SqlParam> params = new ArrayList<>(2);
        String sql = sqlt.render(context, params);
        assertEquals("SELECT * FROM t_pet WHERE (a>? AND a<?) AND (b<? AND b>=?)", sql);
        assertEquals(4, params.size());
        assertEquals("a=100", params.get(0).toString());
        assertEquals("a=500", params.get(1).toString());
        assertEquals("b=6", params.get(2).toString());
        assertEquals("b=9", params.get(3).toString());

        sql = sqlt.render(context, null);
        assertEquals("SELECT * FROM t_pet WHERE (a>100 AND a<500) AND (b<6 AND b>=9)", sql);
    }

    @Test
    public void test_var_as_where() {
        String s = "SELECT * FROM t_pet WHERE ${@vars=where; pick=a,b}";
        NutMap context = NutMap.WRAP("{a:'A',b:100}");
        WnSqlTmpl sqlt = WnSqlTmpl.parse(s);
        List<SqlParam> params = new ArrayList<>(2);
        String sql = sqlt.render(context, params);
        assertEquals("SELECT * FROM t_pet WHERE a=? AND b=?", sql);
        assertEquals(2, params.size());
        assertEquals("a=\"A\"", params.get(0).toString());
        assertEquals("b=100", params.get(1).toString());

        sql = sqlt.render(context, null);
        assertEquals("SELECT * FROM t_pet WHERE a='A' AND b=100", sql);
    }

    @Test
    public void test_var_as_insert() {
        String s = "INSERT INTO t_pet(${@vars=insert.columns; pick=a,b})"
                   + " VALUES (${@vars=insert.values; pick=a,b})";
        NutMap context = NutMap.WRAP("{a:'A',b:100,c:false,d:'D'}");
        WnSqlTmpl sqlt = WnSqlTmpl.parse(s);
        List<SqlParam> params = new ArrayList<>(2);
        String sql = sqlt.render(context, params);
        assertEquals("INSERT INTO t_pet(a,b) VALUES (?,?)", sql);
        assertEquals(2, params.size());
        assertEquals("A", params.get(0).getValue());
        assertEquals(100, params.get(1).getValue());

        sql = sqlt.render(context, null);
        assertEquals("INSERT INTO t_pet(a,b) VALUES ('A',100)", sql);
    }

    @Test
    public void test_var_as_update() {
        String s = "UPDATE t_pet SET ${@vars=update; pick=a,b}";
        NutMap context = NutMap.WRAP("{a:'A',b:100,c:false,d:'D'}");
        WnSqlTmpl sqlt = WnSqlTmpl.parse(s);
        List<SqlParam> params = new ArrayList<>(2);
        String sql = sqlt.render(context, params);
        assertEquals("UPDATE t_pet SET a=?,b=?", sql);
        assertEquals(2, params.size());
        assertEquals("A", params.get(0).getValue());
        assertEquals(100, params.get(1).getValue());

        sql = sqlt.render(context, null);
        assertEquals("UPDATE t_pet SET a='A',b=100", sql);
    }
}
