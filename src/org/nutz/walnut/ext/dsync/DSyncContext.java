package org.nutz.walnut.ext.dsync;

import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.dsync.bean.WnDataSyncConfig;
import org.nutz.walnut.ext.dsync.bean.WnDataSyncTree;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class DSyncContext extends JvmFilterContext {

    public WnDataSyncService api;

    public WnDataSyncConfig config;

    public List<WnDataSyncTree> trees;

    public WnObj oArchive;

    public boolean hasTrees() {
        return null != trees && !trees.isEmpty();
    }

    public boolean hasAchive() {
        return null != oArchive;
    }

}
