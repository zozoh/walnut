package org.nutz.walnut.ext.qiniu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.impl.io.WnMounter;
import org.nutz.walnut.util.Wn;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;

public class QiniuMouter implements WnMounter {

    public WnObj get(MimeMap mimes, WnObj mo, String[] paths, int fromIndex, int toIndex) {
        QiniuWnObj top = new QiniuWnObj(mo);
        BucketManager bm = top.bm();
        String path = null;
        try {
            // 从父对象开始循环
            WnObj p = mo;
            for (int i = fromIndex; i < toIndex; i++) {
                path = Strings.join("/", Arrays.copyOfRange(paths, fromIndex, i + 1));
                FileListing listing = bm.listFiles(top.bucket, path, null, 1, null);
                if (listing.items != null && listing.items.length > 0) {
                    FileInfo info = listing.items[0];
                    if (!path.equals(info.key)) {
                        info.key = info.key.substring(0, info.key.indexOf('/'));
                        p = gen(top, p, mimes, info, true);
                    } else {
                        return gen(top, p, mimes, info, false);
                    }
                } else {
                    return null;
                }
            }
            return p;
        }
        catch (QiniuException e) {
            throw Er.create("e.io.mnt.qiniu.error", top.bucket + "/" + path + " " + e.getMessage());
        }
    }

    public List<WnObj> getChildren(MimeMap mimes, WnObj mo, String name) {
        QiniuWnObj top = new QiniuWnObj(mo);
        BucketManager bm = top.bm();
        List<WnObj> list = new ArrayList<>();
        String marker = null;
        String prefix = Strings.isBlank(top.path) ? "" : top.path + "/";

        Pattern ptn = null;

        if (null != name) {
            // 正则
            if (name.startsWith("^")) {
                ptn = Pattern.compile(name);
            }
            // 通配符
            else if (name.contains("*")) {
                ptn = Pattern.compile("^" + name.replace("*", ".*"));
            }
        }
        Set<String> dirNames = new HashSet<>();
        while (true) {
            try {
                FileListing listing = bm.listFiles(top.bucket, prefix, marker, 1000, null);
                for (FileInfo info : listing.items) {
                    String _name = info.key.substring(prefix.length());
                    if (_name.contains("/")) {
                        String dirName = _name.split("/")[0];
                        if (dirNames.contains(dirName))
                            continue;
                        else {
                            if (null == name
                                || dirName.equals(name)
                                || (null != ptn && ptn.matcher(dirName).find())) {

                                info.key = prefix + dirName;
                                info.fsize = 4096;
                                info.hash = "";
                                list.add(gen(top, mo, mimes, info, true));
                                dirNames.add(dirName);
                            }
                            continue;
                        }
                    }
                    if (null == name
                        || _name.equals(name)
                        || (null != ptn && ptn.matcher(_name).find())) {
                        list.add(gen(top, mo, mimes, info, false));
                    }
                    continue;
                }
                if (listing.isEOF())
                    break;
                else
                    marker = listing.marker;
            }
            catch (QiniuException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    protected WnObj gen(QiniuWnObj top, WnObj p, MimeMap mimes, FileInfo info, boolean dir) {
        WnObj o;
        // 计算虚拟 ID
        String id = top.obj.id() + ":qiniu://" + top.bucket + "/" + info.key;

        // 生成虚拟对象

        o = new WnBean();
        top.copy2(o);
        o.id(id);

        o.name(info.key.substring(info.key.indexOf('/') + 1));

        if (dir)
            o.race(WnRace.DIR);
        else
            o.race(WnRace.FILE);

        if (mimes != null)
            Wn.set_type(mimes, o, null);
        o.setParent(p);

        o.sha1("");
        o.data("qiniu://" + top.bucket + "/" + info.key);
        o.len(info.fsize);

        o.createTime(info.putTime);
        o.lastModified(info.putTime);

        o.mode(p.mode());
        o.creator(p.creator()).group(p.creator()).mender(p.creator());

        o.mount(p.mount() + "/" + o.name());
        o.mountRootId(p.mountRootId());

        o.setv("qiniu_path", info.key);
        return o;
    }
}
