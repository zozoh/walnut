package org.nutz.walnut.ooml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.util.archive.WnArchiveWriting;
import org.nutz.walnut.util.archive.impl.WnZipArchiveWriting;

/**
 * 封装了一个 <code>OOML</code> 包的实体结构，以及读取方式
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class OomlPackage {

    private List<OomlEntry> list;

    private Map<String, OomlEntry> entries;

    private Map<String, OomlRels> relsCache;

    private OomlContentTypes contentTypes;

    public OomlPackage() {
        this.list = new LinkedList<>();
        this.entries = new HashMap<>();
        this.relsCache = new HashMap<>();
    }

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

    public void writeAndClose(OutputStream ops) {
        WnArchiveWriting ag = null;
        try {
            // 准备输出流
            ag = new WnZipArchiveWriting(ops);
            // 逐个写入条目
            List<OomlEntry> list = this.getEntries();
            for (OomlEntry en : list) {
                String rph = en.getPath();
                byte[] bs = en.getContent();
                ag.addFileEntry(rph, bs);
            }
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        // 确保写入
        finally {
            Streams.safeFlush(ag);
            Streams.safeClose(ag);
            Streams.safeClose(ops);
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

    public CheapDocument loadEntryAsXml(String path) {
        OomlEntry en = this.getEntry(path);
        if (null == en) {
            return null;
        }
        return Oomls.parseEntryAsXml(en);
    }

    /**
     * 根据指定的正则表达式规定的路径，获取指定的文档条目。
     * 
     * @param regex
     *            表示条目路径。被这个正则表达式匹配的条目，才会被返回。<br>
     *            如果为 null 则表示返回全部条目
     * @return 符合正则表达式的全部条目
     */
    public List<OomlEntry> findEntriesByPath(String regex) {
        List<OomlEntry> re = new ArrayList<>(list.size());
        for (OomlEntry en : list) {
            String rph = en.getPath();
            if (null == regex || rph.matches(regex)) {
                re.add(en);
            }
        }
        return re;
    }

    public OomlRels loadRelationships(OomlEntry en) {
        String rph = en.getRelsPath();
        OomlRels rels = relsCache.get(rph);
        if (null == rels) {
            OomlEntry ree = this.getEntry(rph);
            rels = new OomlRels(ree);
            relsCache.put(rph, rels);
        }
        return rels;
    }

    public void saveAllRelationshipsFromCache() {
        if (!relsCache.isEmpty()) {
            for (Map.Entry<String, OomlRels> it : relsCache.entrySet()) {
                String rph = it.getKey();
                OomlRels rel = it.getValue();
                OomlEntry en = this.getEntry(rph);
                byte[] content = rel.toByte();
                en.setContent(content);
            }
        }
    }

    public OomlContentTypes loadContentTypes() {
        if (null == this.contentTypes) {
            String rph = "[Content_Types].xml";
            OomlEntry en = this.getEntry(rph);
            String xml = en.getContentStr();
            this.contentTypes = new OomlContentTypes(xml);
        }
        return this.contentTypes;
    }

    public void saveContentTypes() {
        if (null != this.contentTypes) {
            String rph = "[Content_Types].xml";
            OomlEntry en = this.getEntry(rph);
            byte[] content = this.contentTypes.toByte();
            en.setContent(content);
        }
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
        this.list.clear();
        this.entries.clear();
        this.contentTypes = null;
        this.clearCache();
    }

    public void clearCache() {
        this.relsCache.clear();
    }
}
