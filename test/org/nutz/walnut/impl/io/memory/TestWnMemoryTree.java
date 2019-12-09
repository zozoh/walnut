package org.nutz.walnut.impl.io.memory;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.random.R;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.BaseIoTest;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

public class TestWnMemoryTree extends BaseIoTest {
    
    @Override
    protected void on_before(PropertiesProxy pp) {
        super.on_before(pp);
        Wn.WC().remove("_memory_tree");
    }
    
    @Override
    protected void on_after(PropertiesProxy pp) {
        super.on_after(pp);
        Wn.WC().remove("_memory_tree");
    }

    @Test
    public void test_create_delete() {
        WnObj tmpDir = io.createIfNoExists(null, "/tmp_" + R.UU32(), WnRace.DIR);
        io.setMount(tmpDir, "memory://_");

        // 新增100个文件夹和文件
        for (int i = 0; i < 100; i++) {
            WnObj tmp2 = io.createIfNoExists(tmpDir, "abc_dir_" + i, WnRace.DIR);
            WnObj tmp3 = io.createIfNoExists(tmpDir, "abc_dir_" + i + "/data", WnRace.FILE);
            assertNotNull(tmp2);
            assertNotNull(tmp3);
        }
        assertEquals(100, io.getChildren(tmpDir, null).size());
        io.walk(tmpDir, new Callback<WnObj>() {
            public void invoke(WnObj obj) {
                assertTrue(obj.isMount());
                if (obj.isFILE())
                    assertTrue(obj.data() != null);
            }
        }, WalkMode.DEPTH_LEAF_FIRST);
        
        // 逐一删除
        for (int i = 0; i < 100; i++) {
            io.delete(io.check(tmpDir, "abc_dir_"+i), true);
        }
        assertEquals(0, io.getChildren(tmpDir, null).size());
    }
    
    @Test
    public void test_write_read() {
        WnObj tmpDir = io.createIfNoExists(null, "/tmp_" + R.UU32(), WnRace.DIR);
        io.setMount(tmpDir, "memory://_");

        // 新增100个文件夹和文件
        for (int i = 0; i < 100; i++) {
            WnObj tmp3 = io.createIfNoExists(tmpDir, "abc_dir_" + i + "/data", WnRace.FILE);
            io.writeText(tmp3, "ABC" + i);
        }
        
        // 检查每个文件的内容
        for (int i = 0; i < 100; i++) {
            assertEquals("ABC"+i, io.readText(io.check(tmpDir, "abc_dir_" + i + "/data")));
        }
    }
    

    @Test
    public void test_rename() {
        WnObj tmpDir = io.createIfNoExists(null, "/tmp_" + R.UU32(), WnRace.DIR);
        io.setMount(tmpDir, "memory://_");

        // 新增100个文件夹和文件
        for (int i = 0; i < 100; i++) {
            //System.out.println(">> i=" + i);
            WnObj tmp3 = io.createIfNoExists(tmpDir, "abc_dir_" + i + "/data", WnRace.FILE);
            io.writeText(tmp3, "ABC" + i);
        }
        
        // 全部改名
        for (int i = 0; i < 100; i++) {
            io.rename(io.check(tmpDir, "abc_dir_" + i + "/data"), "data2");
        }
        
        // 全部check一下
        for (int i = 0; i < 100; i++) {
            io.check(tmpDir, "abc_dir_" + i + "/data2");
        }
    }
}
