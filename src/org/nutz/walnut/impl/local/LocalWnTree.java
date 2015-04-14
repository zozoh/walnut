package org.nutz.walnut.impl.local;

import java.io.File;
import java.util.regex.Pattern;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.AbstractWnTree;

public class LocalWnTree extends AbstractWnTree {

    private String rootPath;

    private File home;

    private MemNodeMap mnm;

    public LocalWnTree(WnTreeFactory factory, WnNode treeNode) {
        super(factory, treeNode);
        // 根据节点获取本地顶级目录
        String mnt = treeNode.mount();
        if (!mnt.startsWith("file://"))
            throw Er.create("e.io.tree.local.invalid.mnt", mnt);
        String ph = mnt.substring("file://".length());
        home = Files.createDirIfNoExists(ph);
        if (!home.isDirectory())
            throw Er.create("e.io.tree.local.invalid.home", home);
        rootPath = Strings.sBlank(treeNode.path(), "");
        // 分析自己的节点
        mnm = new MemNodeMap();
        File f = get_tree_cache_file();
        mnm.loadAndClose(Streams.fileInr(f));
    }

    private File get_tree_cache_file() {
        File f = Files.getFile(home, ".wn/tree");
        Files.createFileIfNoExists(f);
        return f;
    }

    private String check_rpath(File f) {
        String base = home.getAbsolutePath();
        String ph = f.getAbsolutePath();
        if (!ph.startsWith(base)) {
            throw Er.create("e.io.tree.local.OutOfPath", ph + " <!> " + base);
        }
        return ph.substring(base.length() + 1);
    }

    private String check_local_path(WnNode nd) {
        if (treeNode.isSameId(nd))
            return home.getAbsolutePath();

        if (!nd.tree().equals(this))
            throw Er.create("e.io.tree.local.OutOfMount", nd);

        String ph = nd.path();
        if (!ph.startsWith(rootPath))
            throw Er.create("e.io.tree.local.OutOfPath", ph + " <!> " + rootPath);

        String re = ph.substring(rootPath.length());
        if (!re.startsWith("/"))
            throw Er.create("e.io.tree.local.OutOfDir", ph + " <!> " + rootPath);

        return home.getAbsolutePath() + re;
    }

    private File check_local_file(String rpath) {
        File f = Files.getFile(home, rpath);
        if (!f.exists())
            throw Er.create("e.io.tree.nd.noexists", rpath);
        return f;
    }

    private File get_local_file(WnNode nd) {
        if (nd == treeNode)
            return home;
        if (nd instanceof LocalWnNode) {
            return ((LocalWnNode) nd).getFile();
        }
        return new File(check_local_path(nd));
    }

    private File check_local_file(WnNode nd) {
        File f = get_local_file(nd);
        if (null == f || !f.exists())
            throw Er.create("e.io.tree.nd.noexists", nd);
        return f;
    }

    private WnNode check_node(File f) {
        // 创建节点
        LocalWnNode nd = new LocalWnNode(this, f);

        // 得到相对路径
        String rpath = check_rpath(f);

        // TODO 从这里寻找对应的 ID，没有就生成一个
        String id = mnm.getId(rpath);
        if (null == id) {
            nd.genID();
            mnm.add(nd.id() + ":" + rpath);
        } else {
            nd.id(id);
        }
        nd.path(rootPath + "/" + rpath);
        return nd;
    }

    @Override
    public int eachChildren(WnNode p, String str, Each<WnNode> callback) {
        if (null == p)
            p = treeNode;

        // 得到相对路径
        File d = check_local_file(p);

        if (!d.isDirectory())
            throw Er.create("e.io.tree.local.shouldBeDir", str);

        // 设置名称过滤条件
        Pattern pat = null;
        if (!Strings.isBlank(str)) {
            if (str.startsWith("^")) {
                pat = Pattern.compile(str);
            }
            // 看看是通配符还是普通名字
            else {
                String s = str.replace("*", ".*");
                // 通配符的话，变正则表达式
                if (!s.equals(str)) {
                    pat = Pattern.compile("^" + s);
                }
            }
        }

        // 直接是名字
        if (null == pat) {
            File f = Files.getFile(d, str);
            if (!f.exists())
                return 0;
            WnNode nd = check_node(f);
            nd.setParent(p);

            try {
                callback.invoke(0, nd, 1);
            }
            catch (ExitLoop e) {}
            catch (ContinueLoop e) {}
            // 返回计数 1
            return 1;
        }

        // 列目录啊列目录
        int nb = 0;
        File[] fileList = d.listFiles();
        for (File f : fileList) {
            // 过滤
            if (!pat.matcher(f.getName()).find())
                continue;

            WnNode nd = check_node(f);
            nd.setParent(p);

            // 计数并调用回调
            try {
                callback.invoke(nb, nd, fileList.length);
            }
            catch (ExitLoop e) {
                break;
            }
            catch (ContinueLoop e) {}
            finally {
                nb++;
            }
        }

        return nb;
    }

    @Override
    public boolean hasChildren(WnNode nd) {
        File d = this.check_local_file(nd);
        return d.isDirectory() && d.list().length > 0;
    }

    @Override
    public WnNode create_node(WnNode p, String name, WnRace race) {
        // 首先，咱不支持 OBJ，因为本地文件木法表达
        if (race == WnRace.OBJ)
            throw Er.create("e.io.tree.local.OBJ");

        p = this.check_parent(p, race);

        File d;
        if (p == treeNode) {
            d = home;
        } else {
            d = ((LocalWnNode) p).getFile();
        }

        // 创建对象前先检查重名
        File f = Files.getFile(d, name);
        if (f.exists()) {
            throw Er.create("e.io.tree.local.nd.exists", f);
        }
        // 文件
        if (WnRace.FILE == race) {
            Files.createFileIfNoExists(f);
        }
        // 目录
        else if (WnRace.DIR == race) {
            Files.createDirIfNoExists(f);
        }
        // 不可能
        else {
            throw Lang.impossible();
        }

        return check_node(f);
    }

    @Override
    protected void delete_self(WnNode nd) {
        // 删除索引
        if (nd.hasID())
            mnm.removeById(nd.id());

        // 删除文件
        File f = get_local_file(nd);
        if (null != f && f.exists()) {
            if (f.isDirectory()) {
                Files.deleteDir(f);
            } else if (f.isFile()) {
                Files.deleteFile(f);
            } else {
                throw Lang.impossible();
            }
        }
    }

    @Override
    public void rename(WnNode nd, String newName) {
        LocalWnNode lnd = (LocalWnNode) nd;
        Files.rename(lnd.getFile(), newName);
    }

    @Override
    public void setMount(WnNode nd, String mnt) {
        LocalWnNode lnd = (LocalWnNode) nd;
        lnd.mount(mnt);
    }

    @Override
    public void _clean_for_unit_test() {
        Files.clearDir(home);
        mnm.clear();
        this._flush_buffer();
    }

    @Override
    public WnNode getNode(String id) {
        String rpath = mnm.getPath(id);
        if (null == rpath)
            throw Er.create("e.io.tree.nd.noexists", "id:" + id);
        File f = check_local_file(rpath);
        LocalWnNode nd = new LocalWnNode(this, f);
        nd.id(id);
        nd.path(rootPath + "/" + rpath);
        return nd;
    }

    @Override
    public WnNode loadParents(WnNode nd, boolean force) {
        if (treeNode.isSameId(nd)) {
            if (treeNode != nd) {
                nd.setParent(treeNode.parent());
            }
            return nd;
        }
        if (null == nd.parent() || force) {
            LocalWnNode lnd = (LocalWnNode) nd;
            WnNode p = this.check_node(lnd.getFile().getParentFile());
            loadParents(p, force);
            lnd.setParent(p);
        }
        return nd;
    }

    protected void _flush_buffer() {
        File f = get_tree_cache_file();
        mnm.writeAndClose(Streams.fileOutw(f));
    }

}
