package org.nutz.walnut.ext.titanium.sidebar;

import java.util.List;

public class TiSidebarOutput {

    private String statusStoreKey;

    private List<TiSidebarOutputItem> sidebar;

    public String getStatusStoreKey() {
        return statusStoreKey;
    }

    public void setStatusStoreKey(String statusStoreKey) {
        this.statusStoreKey = statusStoreKey;
    }

    public List<TiSidebarOutputItem> getSidebar() {
        return sidebar;
    }

    public void setSidebar(List<TiSidebarOutputItem> sidebar) {
        this.sidebar = sidebar;
    }

}
