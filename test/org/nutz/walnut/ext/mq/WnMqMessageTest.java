package org.nutz.walnut.ext.mq;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Files;

public class WnMqMessageTest {

    @Test
    public void test_00() {
        WnMqMessage msg = new WnMqMessage("hello");
        assertEquals(WnMqMsgType.CMD, msg.getType());
        assertEquals("hello", msg.getBody());

        String str = msg.toString();
        msg = new WnMqMessage(str);

        assertEquals(WnMqMsgType.CMD, msg.getType());
        assertEquals("hello", msg.getBody());
    }

    @Test
    public void test_01() {
        WnMqMessage msg = new WnMqMessage("@user=abc\n@secret=MMM\n\nhello;\n\nworld;");
        assertEquals(WnMqMsgType.CMD, msg.getType());
        assertEquals("abc", msg.getUser());
        assertEquals("MMM", msg.getSecret());
        assertEquals("hello;\n\nworld;", msg.getBody());

        String str = msg.toString();
        msg = new WnMqMessage(str);

        assertEquals(WnMqMsgType.CMD, msg.getType());
        assertEquals("abc", msg.getUser());
        assertEquals("MMM", msg.getSecret());
        assertEquals("hello;\n\nworld;", msg.getBody());
    }

    @Test
    public void test_02() {
        String json = Files.read("org/nutz/walnut/ext/mq/msg02.json");
        WnMqMessage msg = Json.fromJson(WnMqMessage.class, json);
        assertEquals(WnMqMsgType.CMD, msg.getType());
        assertEquals("demo", msg.getUser());
        assertEquals("XXYYZZ", msg.getSecret());
        assertEquals("echo  'hello' > ~/abc.txt", msg.getBody());

        String str = msg.toString();
        msg = new WnMqMessage(str);

        assertEquals(WnMqMsgType.CMD, msg.getType());
        assertEquals("demo", msg.getUser());
        assertEquals("XXYYZZ", msg.getSecret());
        assertEquals("echo  'hello' > ~/abc.txt", msg.getBody());
    }

}
