package org.nutz.walnut.impl.srv;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;

public class WnActiveCode {

    public String id;

    public int ac_day;

    public String ac_licence;

    public String ac_app;

    public String ac_tp;

    public String ow_dmn_id;

    public String ow_dmn_nm;

    public String buyer_id;

    public String buyer_nm;

    public long but_time;

    public long use_time;

    public long ac_expi;

    public WnActiveCode(WnObj oAcode) {
        this.id = oAcode.id();
        this.ac_day = oAcode.getInt("ac_day");
        this.ac_licence = oAcode.getString("ac_licence");
        this.ac_app = oAcode.getString("ac_app");
        this.ac_tp = oAcode.getString("ac_tp");
        this.ow_dmn_id = oAcode.getString("ow_dmn_id");
        this.ow_dmn_nm = oAcode.getString("ow_dmn_nm");
        this.buyer_id = oAcode.getString("buyer_id");
        this.buyer_nm = oAcode.getString("byer_nm");
        this.but_time = oAcode.getLong("but_time");
        this.use_time = oAcode.getLong("use_time");
        this.ac_expi = oAcode.getLong("ac_expi");
    }

    /**
     * @return 是否是自动生成的激活码
     */
    public boolean isAutoGen() {
        return false;
    }

    /**
     * @return 是否是过期的
     */
    public boolean isExpired() {
        return false;
    }

    public NutMap toMap() {
        NutMap map = new NutMap();
        map.put("id", this.id);
        map.put("ac_day", this.ac_day);
        map.put("ac_licence", this.ac_licence);
        map.put("ac_app", this.ac_app);
        map.put("ac_tp", this.ac_tp);
        map.put("ow_dmn_id", this.ow_dmn_id);
        map.put("ow_dmn_nm", this.ow_dmn_nm);
        map.put("buyer_id", this.buyer_id);
        map.put("buyer_nm", this.buyer_nm);
        map.put("but_time", this.but_time);
        map.put("use_time", this.use_time);
        map.put("ac_expi", this.ac_expi);
        return map;
    }

    public String toJson(JsonFormat jfmt) {
        return Json.toJson(this.toMap(), jfmt);
    }
}
