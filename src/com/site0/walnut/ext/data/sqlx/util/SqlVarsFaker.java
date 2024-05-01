package com.site0.walnut.ext.data.sqlx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.fake.impl.WnBeanFaker;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class SqlVarsFaker {

    private final Pattern _P = Pattern.compile("^([0-9]+):(.+)$");

    private String path;

    private int count;

    private String lang;

    public SqlVarsFaker() {
        this.count = 1;
        this.lang = "zh_cn";
    }

    /**
     * 
     * @param str
     *            可以为下面的形式：
     *            <ul>
     *            <li>'10:/path/to/fake_bean.json' - 伪造10个对象
     *            <li>'/path/to/fake_bean.json' - 伪造1个对象
     *            </ul>
     */
    public SqlVarsFaker(String str) {
        this();
        Matcher m = _P.matcher(str);
        if (m.find()) {
            this.path = m.group(2);
            this.count = Integer.parseInt(m.group(1));
        }
        // 默认的
        else {
            this.path = str;
            this.count = 1;
        }
    }

    public List<NutBean> genList(WnSystem sys) {
        WnObj obj = Wn.checkObj(sys, this.path);
        NutMap map = sys.io.readJson(obj, NutMap.class);
        WnBeanFaker faker = new WnBeanFaker(this.lang, map);
        List<NutBean> list = new ArrayList<>(this.count);
        int i = 0;
        while (i < this.count) {
            NutBean bean = faker.next();
            i++;
            list.add(bean);
        }
        return list;
    }

    public NutMap genBean(WnSystem sys) {
        WnObj obj = Wn.checkObj(sys, this.path);
        NutMap map = sys.io.readJson(obj, NutMap.class);
        WnBeanFaker faker = new WnBeanFaker(this.lang, map);
        return (NutMap)faker.next();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

}
