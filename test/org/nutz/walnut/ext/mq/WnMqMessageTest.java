package org.nutz.walnut.ext.mq;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Files;

public class WnMqMessageTest {

    @Test
    public void test_00() {
        WnMqMessage msg = new WnMqMessage("hello");
        assertEquals("sys", msg.getTopic());
        assertEquals(WnMqMsgType.CMD, msg.getType());
        assertEquals("hello", msg.getBody());
    }

    @Test
    public void test_01() {
        WnMqMessage msg = new WnMqMessage("@topic=abc\n\nhello;\n\nworld;");
        assertEquals("abc", msg.getTopic());
        assertEquals(WnMqMsgType.CMD, msg.getType());
        assertEquals("hello;\n\nworld;", msg.getBody());
    }

    @Test
    public void test_02() {
        String json = Files.read("org/nutz/walnut/ext/mq/msg02.json");
        WnMqMessage msg = Json.fromJson(WnMqMessage.class, json);
        assertEquals("xyz", msg.getTopic());
        assertEquals(WnMqMsgType.CMD, msg.getType());
        assertEquals("echo  'hello' > ~/abc.txt", msg.getBody());
    }

}
