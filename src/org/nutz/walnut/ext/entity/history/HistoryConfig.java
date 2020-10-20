package org.nutz.walnut.ext.entity.history;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.sql.WnDaoFieldsConfig;

public class HistoryConfig extends WnDaoFieldsConfig {

    private String[] requires;

    public HistoryConfig() {
        requires = Lang.array("uid", "tid", "opt");
    }

    public boolean isNotMatchRequires(HistoryRecord re) {
        if (null != requires && requires.length > 0) {
            for (String rq : requires) {
                // UID
                if ("uid".equals(rq)) {
                    if (Strings.isBlank(re.getUserId()))
                        return true;
                }
                // TID
                else if ("tid".equals(rq)) {
                    if (Strings.isBlank(re.getTargetId()))
                        return true;
                }
                // OPT
                else if ("opt".equals(rq)) {
                    if (Strings.isBlank(re.getOperation()))
                        return true;
                }
                // UNM
                else if ("unm".equals(rq)) {
                    if (Strings.isBlank(re.getUserName()))
                        return true;
                }
                // UTP
                else if ("utp".equals(rq)) {
                    if (Strings.isBlank(re.getUserType()))
                        return true;
                }
                // TNM
                else if ("tnm".equals(rq)) {
                    if (Strings.isBlank(re.getTargetName()))
                        return true;
                }
                // TTP
                else if ("ttp".equals(rq)) {
                    if (Strings.isBlank(re.getTargetType()))
                        return true;
                }
                // MOR
                else if ("mor".equals(rq)) {
                    if (Strings.isBlank(re.getMore()))
                        return true;
                }
            }
        }
        return false;
    }

    public void truncate(HistoryRecord re) {
        re.setId(this.truncate("id", re.getId()));
        re.setUserId(this.truncate("uid", re.getUserId()));
        re.setUserName(this.truncate("unm", re.getUserName()));
        re.setUserType(this.truncate("utp", re.getUserType()));
        re.setTargetId(this.truncate("tid", re.getTargetId()));
        re.setTargetName(this.truncate("tnm", re.getTargetName()));
        re.setTargetType(this.truncate("ttp", re.getTargetType()));
        re.setOperation(this.truncate("opt", re.getOperation()));
        re.setMore(this.truncate("mor", re.getMore()));
    }

    public String[] getRequires() {
        return requires;
    }

    public void setRequires(String[] requires) {
        this.requires = requires;
    }

}
