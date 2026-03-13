package com.site0.walnut.ext.data.sqlx.loader;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.nutz.json.Json;
import com.site0.walnut.util.Ws;

public class SqlEntryTest {

    @Test
    public void test_mixed() {
        String[] lines = {"-- AA",
                          "-- BB",
                          "SELECT XXX",
                          "-- @name=update",
                          "UPDATE XXX"};
        String input = Ws.join(lines, "\r\n");
        List<SqlEntry> list = SqlEntry.load(input);
        assertEquals(1, list.size());
        SqlEntry se = list.get(0);
        assertEquals("update", se.getName());
        assertNull(se.getType());
        assertNull(se.getDefaultPick());
        assertNull(se.getDefaultOmit());
        assertNull(se.getDefaultIgnoreNil());
        assertEquals("UPDATE XXX", se.getContent());
    }

    @Test
    public void test_simple() {
        String[] lines = {"-- @name = a11   ",
                          "-- @type = select",
                          "-- @pick = xyz  ",
                          "SELECT XXX",
                          "WHERE {}",
                          "-- @name = a22   ",
                          "-- @type = update",
                          "-- @omit = race,age  ",
                          "UPDATE XXX",
                          "SET {}",};
        String input = Ws.join(lines, "\r\n");
        List<SqlEntry> list = SqlEntry.load(input);
        assertEquals(2, list.size());
        assertEquals("a11", list.get(0).getName());
        assertEquals(SqlType.SELECT, list.get(0).getType());
        assertEquals("[\"xyz\"]", Json.toJson(list.get(0).getDefaultPick()));
        assertEquals("SELECT XXX WHERE {}", list.get(0).getContent());

        assertEquals("a22", list.get(1).getName());
        assertEquals(SqlType.UPDATE, list.get(1).getType());
        assertEquals("[\"race\", \"age\"]",
                     Json.toJson(list.get(1).getDefaultOmit()));
        assertEquals("UPDATE XXX SET {}", list.get(1).getContent());
    }

}
