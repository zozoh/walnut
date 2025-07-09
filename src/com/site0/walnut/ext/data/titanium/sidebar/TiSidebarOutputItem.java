package com.site0.walnut.ext.data.titanium.sidebar;

import java.util.LinkedList;
import java.util.List;

import org.nutz.castor.Castors;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.tmpl.WnTmplX;

public class TiSidebarOutputItem {

    private List<TiSidebarOutputItem> items;

    private int depth;

    private String key;

    private String id;

    private String path;

    private Object icon;

    private String title;

    private String tip;

    private String view;

    public String toString() {
        String s = "";
        if (null != key) {
            s += ":" + key;
        }
        if (null != title) {
            s += ":" + title;
        }
        if (null != tip) {
            s += "(" + tip + ")";
        }
        if (null != path) {
            s += ":" + path;
        }
        if (null != view) {
            s += ":->" + view;
        }
        if (null != items && items.size() > 0) {
            for (TiSidebarOutputItem it : items) {
                s += "\n - " + it.toString();
            }
        }
        return s;
    }

    public TiSidebarOutputItem(TiSidebarOutputItem it) {
        this.depth = it.depth;
        this.key = it.key;
        this.id = it.key;
        this.path = it.path;
        this.icon = it.icon;
        this.title = it.title;
        this.tip = it.tip;
        this.view = it.view;

        if (null != it.items) {
            this.items = new LinkedList<>();
            for (TiSidebarOutputItem subIt : it.items) {
                TiSidebarOutputItem newSubIt = subIt.clone();
                this.items.add(newSubIt);
            }
        }
    }

    public TiSidebarOutputItem(int depth, TiSidebarInputItem it, NutBean o, NutBean vars) {
        String dft_title = it.isGroup() ? "Group" : (null == o ? "NoTitle" : o.getString("nm"));
        Object dft_icon = null == o ? null : o.pick("tp", "mime", "race");

        this.depth = depth;
        // ----------------------------------
        // ID
        if (null != o) {
            this.id = o.getString("id");
        }
        // ----------------------------------
        // Path
        this.path = _Vs(null, o, "ph", it.getPath(), null);
        // ----------------------------------
        // Key
        if (it.hasKey()) {
            this.key = it.getKey();
        }
        // Key by Path
        else if (this.hasPath()) {
            this.key = Files.getName(this.path);
        }
        // Key by Id
        else {
            this.key = this.id;
        }
        // ----------------------------------
        // 获取标题
        Castors cam = Castors.me();
        String title = it.getTitle();
        // 这里需要支持一下 Session 的变量，以便扩展多语言的支持
        if (null != title) {
            title = WnTmplX.exec(title, vars);
        }
        // 展开表达式
        if (!Strings.isBlank(title)) {
            title = cam.castToString(Wn.explainObj(o, title));
        }
        // 最后确保一下，一定有个标题，木有标题有默认标题来代替
        this.title = _Vs(title, o, "title", it.getDefaultTitle(), dft_title);

        // ----------------------------------
        // 获取提示
        String tip = it.getTip();
        // 这里需要支持一下 Session 的变量，以便扩展多语言的支持
        if (null != tip) {
            tip  = WnTmplX.exec(tip, vars);
        }
        // 展开表达式
        if (!Strings.isBlank(tip)) {
            tip = cam.castToString(Wn.explainObj(o, tip));
        }
        // 设置数据
        this.tip = tip;

        // ----------------------------------
        // Other fields
        this.icon = _V(it.getIcon(), o, "icon", it.getDefaultIcon(), dft_icon);
        this.view = _Vs(it.getView(), o, "view", it.getDefaultView(), null);
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

    public boolean hasKey() {
        return !Strings.isBlank(key);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean hasPath() {
        return !Strings.isBlank(path);
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

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

}
