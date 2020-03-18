package org.nutz.walnut.impl.io.mnt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.Disks;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.Wn;

public class LocalFileMounter extends AbstractWnMounter {
	
	private static final Log log = Logs.get();

    private static final Pattern regex_id_mnt2 = Pattern.compile("^([\\d\\w]+)://(.+)$");

    @Override
    public WnObj get(MimeMap mimes, WnObj mo, String[] paths, int fromIndex, int toIndex) {
        // 获得挂载点对应的本地目录
        // data 的形式应该是 file://path/to/file
        String mnt = mo.mount();
        Matcher m = regex_id_mnt2.matcher(mnt);
        if (!m.find()) {
            throw Er.create("e.io.mnt.local.invalid", mnt);
        }
        String mntPath = m.group(2);
        File base = new File(Disks.normalize(mntPath));
        if (!base.exists()) {
        	log.errorf("e.io.mnt.local.noexists mnt=%s paths=%s", mnt, Json.toJson(paths));
            return null;
        }

        // 从父对象开始循环
        WnObj p = mo;
        WnObj o = p;
        File d = base;
        for (int i = fromIndex; i < toIndex; i++) {
            // 获得文件对象
            String nm = paths[i];
            File f = Files.getFile(d, nm);

            if (!f.exists())
                return null;

            o = __gen_obj(mimes, p, f);

            // 作为下一圈的父
            p = o;
            d = f;
        }
        // 返回
        return o;
    }

    private WnObj __gen_obj(MimeMap mimes, WnObj p, File f) {
        WnObj o;
        // 计算虚拟 ID
        String id = p.id();
        // 后面叠加自己的路径
        if (id.indexOf(":file:") > 0) {
            id = id + "%" + f.getName();
        }
        // 第一个虚 ID，添加前缀
        else {
            id = id + ":file:%%" + f.getName();
        }

        // 生成虚拟对象

        o = new WnBean();
        o.id(id);

        o.name(f.getName());
        if (f.isFile())
            o.race(WnRace.FILE);
        else
            o.race(WnRace.DIR);

        Wn.set_type(mimes, o, null);
        o.setParent(p);

        o.sha1("");
        o.data("file://" + f.getAbsolutePath());
        o.len(f.length());

        o.createTime(f.lastModified());
        o.lastModified(f.lastModified());

        o.mode(p.mode());
        o.creator(p.creator()).group(p.group()).mender(p.mender());

        o.mount(p.mount() + "/" + o.name());
        o.mountRootId(p.mountRootId());
        return o;
    }

    @Override
    public List<WnObj> getChildren(MimeMap mimes, WnObj mo, String name) {
        String mnt = mo.mount();
        Matcher m = regex_id_mnt2.matcher(mnt);
        if (!m.find()) {
            throw Er.create("e.io.mnt.local.invalid", mnt);
        }
        String mntPath = m.group(2);
        File d = new File(Disks.normalize(mntPath));
        if (!d.exists()) {
            throw Er.create("e.io.mnt.local.noexists", mnt);
        }

        if (!d.isDirectory()) {
            throw Er.create("e.io.mnt.local.onlyDirHasChildren", mnt);
        }

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

        // 读取
        File[] fs = d.listFiles();
        List<WnObj> reList = new ArrayList<WnObj>(fs.length);
        for (File f : fs) {
            String nm = f.getName();
            if (null == name || nm.equals(name) || (null != ptn && ptn.matcher(nm).find())) {
                reList.add(__gen_obj(mimes, mo, f));
            }
        }
        reList.sort((from, to)-> from.name().compareTo(to.name()));
        return reList;
    }
}
