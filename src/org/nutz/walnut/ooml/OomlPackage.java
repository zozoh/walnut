package org.nutz.walnut.ooml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nutz.lang.Streams;

/**
 * 封装了一个 <code>OOML</code> 包的实体结构，以及读取方式
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class OomlPackage {

    private List<OomlEntry> list;

    private Map<String, OomlEntry> entries;

    /**
     * 从输入流读取全部实体，以及内容，然后关闭输入流
     * 
     * @param ins
     *            输入流（可以是普通输入流，或者Zip输入流）
     * @throws IOException
     */
    public void loadEntriesAndClose(InputStream ins) throws IOException {
        // 重置
        this.reset();

        // 准备压缩流
        ZipInputStream zip;
        if (ins instanceof ZipInputStream) {
            zip = (ZipInputStream) ins;
        } else {
            zip = new ZipInputStream(ins);
        }
        //
        // 读取
        ZipEntry en;
        byte[] bs = new byte[8192];
        try {
            while ((en = zip.getNextEntry()) != null) {
                OomlEntry oe = new OomlEntry(en, zip, bs);
                entries.put(oe.getPath(), oe);
                list.add(oe);
            }
        }
        // 安全关闭
        finally {
            Streams.safeClose(zip);
            if (zip != ins) {
                Streams.safeClose(ins);
            }
        }
    }

    /**
     * 根据路径获取任意实体。
     * <p>
     * <b>!本函数必须先执行过 </b><code>loadEntriesAndClose</code>
     * 
     * @param path
     *            实体路径
     * @return 实体对象
     */
    public OomlEntry getEntry(String path) {
        return entries.get(path);
    }

    /**
     * 保持顺序的获取本包全部项目
     * <p>
     * <b>!本函数必须先执行过 </b><code>loadEntriesAndClose</code>
     * 
     * @return 实体列表
     */
    public List<OomlEntry> getEntries() {
        return list;
    }

    /**
     * 重置包内容
     */
    public void reset() {
        this.list = new LinkedList<>();
        this.entries = new HashMap<>();
    }
}
