package org.nutz.walnut.api.box;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.walnut.BaseBoxTest;

public abstract class AbstractWnBoxTest extends BaseBoxTest {

    @Test
    public void test_simple_pipe() {
        box.submit("echo 'hello' | md5sum");
        box.run();

        assertEquals(Lang.md5("hello\n") , touts());
    }

    @Test
    public void test_echo_hello2() {
        box.submit("echo 'hello' 'world'");
        box.run();

        assertEquals("hello world\n", outs());
    }

    @Test
    public void test_echo_hello() {
        box.submit("echo 'hello'");
        box.run();

        assertEquals("hello\n", outs());
    }

}
