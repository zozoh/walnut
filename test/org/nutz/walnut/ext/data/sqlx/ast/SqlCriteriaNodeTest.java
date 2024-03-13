package org.nutz.walnut.ext.data.sqlx.ast;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.sqlx.tmpl.SqlParam;
import org.nutz.walnut.util.Wlang;

public class SqlCriteriaNodeTest {

    @Test
    public void test_true() {
        List<SqlParam> params = new ArrayList<>(5);
        NutMap q = Wlang.map("a: true");
        SqlCriteriaNode cri = SqlCriteria.toCriNode(q);

        assertEquals("a=1", cri.toSql(false));
        assertEquals("a=?", cri.toSql(true));

        cri.setNot(true);
        assertEquals("NOT a=1", cri.toSql(false));
        assertEquals("NOT a=?", cri.toSql(true));

        params.clear();
        cri.joinParams(params);
        assertEquals(1, params.size());
        assertEquals("a=true", params.get(0).toString());
    }

    @Test
    public void test_or_list() {
        List<SqlParam> params = new ArrayList<>(5);
        NutMap q1 = Wlang.map("'!a':'[100,]'");
        NutMap q2 = Wlang.map("b:20,c:'[45,]'");
        List<Object> q = Wlang.list(q1, q2);
        SqlCriteriaNode cri = SqlCriteria.toCriNode(q);

        assertEquals("NOT a>=100 OR (b=20 AND c>=45)", cri.toSql(false));
        assertEquals("NOT a>=? OR (b=? AND c>=?)", cri.toSql(true));

        cri.setNot(true);
        assertEquals("NOT (NOT a>=100 OR (b=20 AND c>=45))", cri.toSql(false));
        assertEquals("NOT (NOT a>=? OR (b=? AND c>=?))", cri.toSql(true));

        params.clear();
        cri.joinParams(params);
        assertEquals(3, params.size());
        assertEquals("a=100", params.get(0).toString());
        assertEquals("b=20", params.get(1).toString());
        assertEquals("c=45", params.get(2).toString());
    }

    @Test
    public void test_not_not() {
        List<SqlParam> params = new ArrayList<>(5);
        NutMap q = Wlang.map("'!a': '[100,]'");
        SqlCriteriaNode cri = SqlCriteria.toCriNode(q);

        assertEquals("NOT a>=100", cri.toSql(false));
        assertEquals("NOT a>=?", cri.toSql(true));

        cri.setNot(true);
        assertEquals("a>=100", cri.toSql(false));
        assertEquals("a>=?", cri.toSql(true));

        params.clear();
        cri.joinParams(params);
        assertEquals(1, params.size());
        assertEquals("a=100", params.get(0).toString());
    }

    @Test
    public void test_name_not() {
        List<SqlParam> params = new ArrayList<>(5);
        NutMap q = Wlang.map("'!a':'AA',x:{$lt:8},'!y':'(,20]'");
        SqlCriteriaNode cri = SqlCriteria.toCriNode(q);

        assertEquals("NOT a='AA' AND x<8 AND NOT y<=20", cri.toSql(false));
        assertEquals("NOT a=? AND x<? AND NOT y<=?", cri.toSql(true));

        cri.setNot(true);
        assertEquals("NOT (NOT a='AA' AND x<8 AND NOT y<=20)", cri.toSql(false));
        assertEquals("NOT (NOT a=? AND x<? AND NOT y<=?)", cri.toSql(true));

        params.clear();
        cri.joinParams(params);
        assertEquals(3, params.size());
        assertEquals("a=\"AA\"", params.get(0).toString());
        assertEquals("x=8", params.get(1).toString());
        assertEquals("y=20", params.get(2).toString());
    }

    @Test
    public void test_like_and_enum() {
        List<SqlParam> params = new ArrayList<>(5);
        NutMap q = Wlang.map("a:'*A?B',b:[1,'X','Y']");
        SqlCriteriaNode cri = SqlCriteria.toCriNode(q);

        assertEquals("a LIKE '%A_B' AND b IN (1,'X','Y')", cri.toSql(false));
        assertEquals("a LIKE ? AND b IN (?,?,?)", cri.toSql(true));

        cri.setNot(true);
        assertEquals("NOT (a LIKE '%A_B' AND b IN (1,'X','Y'))", cri.toSql(false));
        assertEquals("NOT (a LIKE ? AND b IN (?,?,?))", cri.toSql(true));

        params.clear();
        cri.joinParams(params);
        assertEquals(4, params.size());
        assertEquals("a=\"%A_B\"", params.get(0).toString());
        assertEquals("b.0=1", params.get(1).toString());
        assertEquals("b.1=\"X\"", params.get(2).toString());
        assertEquals("b.2=\"Y\"", params.get(3).toString());
    }

    @Test
    public void test_range_and_regexp() {
        List<SqlParam> params = new ArrayList<>(5);
        NutMap q = Wlang.map("a:'[100,300)',b:'^xyz'");
        SqlCriteriaNode cri = SqlCriteria.toCriNode(q);

        assertEquals("(a>=100 AND a<300) AND b REGEXP '^xyz'", cri.toSql(false));
        assertEquals("(a>=? AND a<?) AND b REGEXP ?", cri.toSql(true));

        cri.setNot(true);
        assertEquals("NOT ((a>=100 AND a<300) AND b REGEXP '^xyz')", cri.toSql(false));
        assertEquals("NOT ((a>=? AND a<?) AND b REGEXP ?)", cri.toSql(true));

        params.clear();
        cri.joinParams(params);
        assertEquals(3, params.size());
        assertEquals("a=100", params.get(0).toString());
        assertEquals("a=300", params.get(1).toString());
        assertEquals("b=\"^xyz\"", params.get(2).toString());
    }

    @Test
    public void test_null_eq() {
        List<SqlParam> params = new ArrayList<>(5);
        NutMap q = Wlang.map("a:'AAA',b:null,c:''");
        SqlCriteriaNode cri = SqlCriteria.toCriNode(q);

        assertEquals("a='AAA' AND b IS NULL AND NOT c IS NULL", cri.toSql(false));
        assertEquals("a=? AND b IS NULL AND NOT c IS NULL", cri.toSql(true));

        cri.setNot(true);
        assertEquals("NOT (a='AAA' AND b IS NULL AND NOT c IS NULL)", cri.toSql(false));
        assertEquals("NOT (a=? AND b IS NULL AND NOT c IS NULL)", cri.toSql(true));

        params.clear();
        cri.joinParams(params);
        assertEquals(1, params.size());
        assertEquals("a=\"AAA\"", params.get(0).toString());
    }

}
