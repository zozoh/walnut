package org.nutz.walnut.ext.bulk;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.old.bulk.api.BulkRestore;

public class TestBulkS0 extends BulkTest {

    @Test
    public void case_S0_a() {
        // 初始数据
        this._init_files_by("~/myfolder/", "~/myfolder/fileA", "~/myfolder/fileB");

        // 创建备份
        WnObj oDir = io.check(oHome, "myfolder");
        String hisId = bulks.backup(oDir, null, buIo);

        // 删除 fileA
        WnObj oFileA = io.check(oDir, "fileA");
        io.delete(oFileA);

        // 那么 fileA 应该不存在了
        assertFalse(io.exists(oDir, "fileA"));

        // 执行还原
        BulkRestore br = new BulkRestore();
        br.asTree1().asObj1();
        bulks.restore(oDir, hisId, null, buIo, br);

        // 那么 fileA 应该存在
        assertTrue(io.exists(oDir, "fileA"));
    }

    @Test
    public void case_S0_B() {
        // 初始数据
        this._init_files_by("~/myfolder/", "~/myfolder/fileA", "~/myfolder/fileB");

        // 写入一点内容
        WnObj oDir = io.check(oHome, "myfolder");
        WnObj oFileA = io.check(oDir, "fileA");
        WnObj oFileB = io.check(oDir, "fileB");
        io.writeText(oFileA, "I am A");
        io.writeText(oFileB, "I am B");

        // 创建备份
        String hisId = bulks.backup(oDir, null, buIo);

        // 确保两个文件内容符合预期
        assertEquals("I am A", io.readText(oFileA));
        assertEquals("I am B", io.readText(oFileB));

        // 更改文件内容
        io.writeText(oFileA, "A was changed!");
        io.writeText(oFileB, "B was changed!");

        // 确保两个文件内容符合预期
        assertEquals("A was changed!", io.readText(oFileA));
        assertEquals("B was changed!", io.readText(oFileB));

        // 执行还原
        BulkRestore br = new BulkRestore();
        br.asTree1().asObj1();
        bulks.restore(oDir, hisId, null, buIo, br);

        // 确保两个文件内容被还原回来了
        assertEquals("I am A", io.readText(oFileA));
        assertEquals("I am B", io.readText(oFileB));
    }

    @Test
    public void case_S0_c_tree1() {
        // 初始数据
        this._init_files_by("~/myfolder/", "~/myfolder/fileA", "~/myfolder/fileB");

        // 创建备份
        WnObj oDir = io.check(oHome, "myfolder");
        String hisId = bulks.backup(oDir, null, buIo);

        // 新增 C
        io.create(oDir, "fileC", WnRace.FILE);

        // 确保这个C是存在的
        assertTrue(io.exists(oDir, "fileC"));

        // 执行还原
        BulkRestore br = new BulkRestore();
        br.asTree1().asObj1();
        bulks.restore(oDir, hisId, null, buIo, br);

        // 那么 fileC 依然应该存在
        assertTrue(io.exists(oDir, "fileA"));
    }

    @Test
    public void case_S0_c_tree2() {
        // 初始数据
        this._init_files_by("~/myfolder/", "~/myfolder/fileA", "~/myfolder/fileB");

        // 创建备份
        WnObj oDir = io.check(oHome, "myfolder");
        String hisId = bulks.backup(oDir, null, buIo);

        // 新增 C
        io.create(oDir, "fileC", WnRace.FILE);

        // 确保这个C是存在的
        assertTrue(io.exists(oDir, "fileC"));

        // 执行还原
        BulkRestore br = new BulkRestore();
        br.asTree2().asObj1();
        bulks.restore(oDir, hisId, null, buIo, br);

        // 那么 fileC 依然应该存在
        assertTrue(io.exists(oDir, "fileA"));
    }

    @Test
    public void case_S0_c_tree3() {
        // 初始数据
        this._init_files_by("~/myfolder/", "~/myfolder/fileA", "~/myfolder/fileB");

        // 创建备份
        WnObj oDir = io.check(oHome, "myfolder");
        String hisId = bulks.backup(oDir, null, buIo);

        // 新增 C
        io.create(oDir, "fileC", WnRace.FILE);

        // 确保这个C是存在的
        assertTrue(io.exists(oDir, "fileC"));

        // 执行还原
        BulkRestore br = new BulkRestore();
        br.asTree3().asObj1();
        bulks.restore(oDir, hisId, null, buIo, br);

        // 那么 fileC 应该被删除掉了
        assertFalse(io.exists(oDir, "fileA"));
    }
}
