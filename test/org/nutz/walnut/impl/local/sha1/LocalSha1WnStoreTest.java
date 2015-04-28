package org.nutz.walnut.impl.local.sha1;

import static org.junit.Assert.*;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.WnTUs;
import org.nutz.walnut.api.io.AbstractWnStoreTest;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.WnBean;

public class LocalSha1WnStoreTest extends AbstractWnStoreTest {

    @Override
    protected WnNode _create_top_tree_node() {
        return WnTUs.create_tree_node(pp, "mnt-mongo-a");
    }

    @Test
    public void test_sha1_get_remove_history() {
        WnNode nd = tree.create(null, "xyz.txt", WnRace.FILE);
        WnObj o = new WnBean().setNode(nd);

        OutputStream ops;

        ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), "AAAAA");
        assertEquals(5, o.len());

        ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), "BBBB");
        assertEquals(4, o.len());

        ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), "CCC");
        assertEquals(3, o.len());

        ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), "DD");
        assertEquals(2, o.len());

        // 测试获取历史
        List<WnHistory> list = store.getHistoryList(o, -1);
        assertEquals(4, list.size());

        WnHistory his;

        his = list.get(0);
        assertEquals(Lang.sha1("DD"), his.sha1());
        assertEquals(2, his.len());

        his = list.get(1);
        assertEquals(Lang.sha1("CCC"), his.sha1());
        assertEquals(3, his.len());

        his = list.get(2);
        assertEquals(Lang.sha1("BBBB"), his.sha1());
        assertEquals(4, his.len());

        his = list.get(3);
        assertEquals(Lang.sha1("AAAAA"), his.sha1());
        assertEquals(5, his.len());

        // 测试删除最后一个历史记录
        list = store.cleanHistory(o, his.nanoStamp());
        assertEquals(1, list.size());
        his = list.get(0);
        assertEquals(Lang.sha1("AAAAA"), his.sha1());
        assertEquals(5, his.len());

        // 那么还应该剩下 3 条历史记录
        list = store.getHistoryList(o, -1);
        assertEquals(3, list.size());

        his = list.get(0);
        assertEquals(Lang.sha1("DD"), his.sha1());
        assertEquals(2, his.len());

        his = list.get(1);
        assertEquals(Lang.sha1("CCC"), his.sha1());
        assertEquals(3, his.len());

        his = list.get(2);
        assertEquals(Lang.sha1("BBBB"), his.sha1());
        assertEquals(4, his.len());

        // 全删除
        list = store.cleanHistory(o, -1);
        assertEquals(3, list.size());

        his = list.get(0);
        assertEquals(Lang.sha1("DD"), his.sha1());
        assertEquals(2, his.len());

        his = list.get(1);
        assertEquals(Lang.sha1("CCC"), his.sha1());
        assertEquals(3, his.len());

        his = list.get(2);
        assertEquals(Lang.sha1("BBBB"), his.sha1());
        assertEquals(4, his.len());

        // 那么还应该没有历史记录了
        list = store.getHistoryList(o, -1);
        assertEquals(0, list.size());

        // 对象的数据也被清空了
        assertNull(o.sha1());
        assertNull(o.data());
        assertEquals(0, o.len());

        // 数据库里也被清空了
        o = indexer.get(o.id());
        assertNull(o.sha1());
        assertNull(o.data());
        assertEquals(0, o.len());

    }

    @Test
    public void test_sha1_get_remove_history_by_remain() {
        WnNode nd = tree.create(null, "xyz.txt", WnRace.FILE);
        WnObj o = new WnBean().setNode(nd);

        OutputStream ops;

        ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), "AAAAA");
        assertEquals(5, o.len());

        ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), "BBBB");
        assertEquals(4, o.len());

        ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), "CCC");
        assertEquals(3, o.len());

        ops = store.getOutputStream(o, 0);
        Streams.writeAndClose(new OutputStreamWriter(ops), "DD");
        assertEquals(2, o.len());

        // 测试获取历史
        List<WnHistory> list = store.getHistoryList(o, -1);
        assertEquals(4, list.size());

        WnHistory his;

        his = list.get(0);
        assertEquals(Lang.sha1("DD"), his.sha1());
        assertEquals(2, his.len());

        his = list.get(1);
        assertEquals(Lang.sha1("CCC"), his.sha1());
        assertEquals(3, his.len());

        his = list.get(2);
        assertEquals(Lang.sha1("BBBB"), his.sha1());
        assertEquals(4, his.len());

        his = list.get(3);
        assertEquals(Lang.sha1("AAAAA"), his.sha1());
        assertEquals(5, his.len());

        // 测试删除最后一个历史记录
        list = store.cleanHistoryBy(o, 3);
        assertEquals(1, list.size());
        his = list.get(0);
        assertEquals(Lang.sha1("AAAAA"), his.sha1());
        assertEquals(5, his.len());

        // 那么还应该剩下 3 条历史记录
        list = store.getHistoryList(o, -1);
        assertEquals(3, list.size());

        his = list.get(0);
        assertEquals(Lang.sha1("DD"), his.sha1());
        assertEquals(2, his.len());

        his = list.get(1);
        assertEquals(Lang.sha1("CCC"), his.sha1());
        assertEquals(3, his.len());

        his = list.get(2);
        assertEquals(Lang.sha1("BBBB"), his.sha1());
        assertEquals(4, his.len());

        // 全删除
        list = store.cleanHistoryBy(o, 0);
        assertEquals(3, list.size());

        his = list.get(0);
        assertEquals(Lang.sha1("DD"), his.sha1());
        assertEquals(2, his.len());

        his = list.get(1);
        assertEquals(Lang.sha1("CCC"), his.sha1());
        assertEquals(3, his.len());

        his = list.get(2);
        assertEquals(Lang.sha1("BBBB"), his.sha1());
        assertEquals(4, his.len());

        // 那么还应该没有历史记录了
        list = store.getHistoryList(o, -1);
        assertEquals(0, list.size());

        // 对象的数据也被清空了
        assertNull(o.sha1());
        assertNull(o.data());
        assertEquals(0, o.len());

        // 数据库里也被清空了
        o = indexer.get(o.id());
        assertNull(o.sha1());
        assertNull(o.data());
        assertEquals(0, o.len());

    }

}
