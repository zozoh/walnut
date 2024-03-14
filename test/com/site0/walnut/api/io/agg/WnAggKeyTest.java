package com.site0.walnut.api.io.agg;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnAggKeyTest {

    @Test
    public void test_03() {
        WnAggKey ak = WnAggKey.parse("type=o_tp");

        assertTrue(ak instanceof WnAggGroupKey);
        assertEquals("o_tp", ak.getFromName());
        assertEquals("type", ak.getToName());
        assertFalse(((WnAggGroupKey) ak).hasFunc());
    }

    @Test
    public void test_02() {
        WnAggKey ak = WnAggKey.parse("abc");

        assertTrue(ak instanceof WnAggGroupKey);
        assertEquals("abc", ak.getFromName());
        assertEquals("abc", ak.getToName());
        assertFalse(((WnAggGroupKey) ak).hasFunc());
    }

    @Test
    public void test_01() {
        WnAggKey ak = WnAggKey.parse("name=TIMESTAMP_TO_DATE:ct");

        assertTrue(ak instanceof WnAggGroupKey);
        assertEquals("ct", ak.getFromName());
        assertEquals("name", ak.getToName());
        assertEquals("TIMESTAMP_TO_DATE", ak.getFuncName());
        assertEquals(WnAggTransMode.TIMESTAMP_TO_DATE, ((WnAggGroupKey) ak).getFunc());

        ak = WnAggKey.parse("name=timestamp_to_date:ct");

        assertTrue(ak instanceof WnAggGroupKey);
        assertEquals("ct", ak.getFromName());
        assertEquals("name", ak.getToName());
        assertEquals("TIMESTAMP_TO_DATE", ak.getFuncName());
        assertEquals(WnAggTransMode.TIMESTAMP_TO_DATE, ((WnAggGroupKey) ak).getFunc());
    }

    @Test
    public void test_00() {
        WnAggKey ak = WnAggKey.parse("value=COUNT:id");

        assertTrue(ak instanceof WnAggregateKey);
        assertEquals("id", ak.getFromName());
        assertEquals("value", ak.getToName());
        assertEquals("COUNT", ak.getFuncName());
        assertEquals(WnAggFunc.COUNT, ((WnAggregateKey) ak).getFunc());

        ak = WnAggKey.parse("value=count:id");

        assertTrue(ak instanceof WnAggregateKey);
        assertEquals("id", ak.getFromName());
        assertEquals("value", ak.getToName());
        assertEquals("COUNT", ak.getFuncName());
        assertEquals(WnAggFunc.COUNT, ((WnAggregateKey) ak).getFunc());
    }

}
