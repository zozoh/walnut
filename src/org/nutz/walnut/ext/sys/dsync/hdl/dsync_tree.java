package org.nutz.walnut.ext.sys.dsync.hdl;

import org.nutz.walnut.ext.sys.dsync.DSyncContext;
import org.nutz.walnut.ext.sys.dsync.DSyncFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class dsync_tree extends DSyncFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args,"fIH", "^(force|items|head)$");
    }

    @Override
    protected void process(WnSystem sys, DSyncContext fc, ZParams params) {
        // 分析参数
        boolean force = params.is("force", "f");
        boolean items = params.is("items", "I");
        boolean head = params.is("head", "H");

        // 第一个参数为树的版本，如果没有声明，则试图加载 head 的版本
        // 加载 head 版本的时候，会检查文件夹是否同步，如果不同步会重新产生树
        // 当然，通过 -head 可以强制加载 head 的树，而不是自动检查同步
        String sha1 = params.val(0);

        // 获取树
        if (null != sha1 || head) {
            fc.trees = fc.api.loadTrees(fc.config, sha1);
        }
        // 加载树
        else {
            fc.trees = fc.api.genTrees(fc.config, force);
        }

        // 确保加载了对象
        if (items) {
            fc.api.loadTreesItems(fc.trees);
        }
    }

}
