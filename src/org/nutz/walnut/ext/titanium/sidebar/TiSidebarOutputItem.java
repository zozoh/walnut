package org.nutz.walnut.ext.titanium.sidebar;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;

public class TiSidebarOutputItem {

    private List<TiSidebarOutputItem> items;

    private int depth;

    private String key;

    private String id;

    private String path;

    private Object icon;

    private String title;

    private String view;

    public TiSidebarOutputItem(TiSidebarOutputItem it) {
        this.depth = it.depth;
        this.key = it.key;
        this.id = it.key;
        this.path = it.path;
        this.icon = it.icon;
        this.title = it.title;
        this.view = it.view;

        if (null != it.items) {
            this.items = new LinkedList<>();
            for (TiSidebarOutputItem subIt : it.items) {
                TiSidebarOutputItem newSubIt = subIt.clone();
                this.items.add(newSubIt);
            }
        }
    }

    public TiSidebarOutputItem(int depth, TiSidebarInputItem it, NutBean o) {
        String dft_title = it.isGroup() ? "Group" : (null == o ? "NoTitle" : o.getString("nm"));
        Object dft_icon = null == o ? null : o.pick("tp", "mime", "race");

        this.depth = depth;
        this.key = _Vs(null, o, "id", it.getKey(), null);
        this.path = _Vs(null, o, "ph", it.getPath(), null);
        this.icon = _V(it.getIcon(), o, "icon", it.getDefaultIcon(), dft_icon);
        this.title = _Vs(it.getTitle(), o, "title", it.getDefaultTitle(), dft_title);
        this.view = _Vs(it.getView(), o, "view", it.getDefaultView(), null);

        if (null != o) {
            this.id = o.getString("id");
        }
    }

    private Object _V(String val, NutBean o, String okey, String dftval, Object dftobj) {
        if (!Strings.isBlank(val))
            return val;
        if (null != o && o.has(okey))
            return o.get(okey);
        if (!Strings.isBlank(dftval))
            return dftval;
        return dftobj;
    }

    private String _Vs(String val, NutBean o, String okey, String dftval, Object dftobj) {
        Object re = this._V(val, o, okey, dftval, dftobj);
        if (null != re)
            return re.toString();
        return null;
    }

    public TiSidebarOutputItem clone() {
        return new TiSidebarOutputItem(this);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TiSidebarOutputItem> getItems() {
        return items;
    }

    public boolean hasItems() {
        return null != this.items && items.size() > 0;
    }

    public void setItems(List<TiSidebarOutputItem> items) {
        this.items = items;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getIcon() {
        return icon;
    }

    public void setIcon(Object icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String text) {
        this.title = text;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

}
