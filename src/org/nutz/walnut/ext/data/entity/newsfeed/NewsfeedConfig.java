package org.nutz.walnut.ext.data.entity.newsfeed;

import org.nutz.walnut.ext.sys.sql.WnDaoFieldsConfig;

public class NewsfeedConfig extends WnDaoFieldsConfig {

    public void truncate(Newsfeed nf) {
        nf.setId(this.truncate("id", nf.getId()));
        nf.setSourceId(this.truncate("src_id", nf.getSourceId()));
        nf.setSourceType(this.truncate("src_tp", nf.getSourceType()));
        nf.setTargetId(this.truncate("ta_id", nf.getTargetId()));
        nf.setTargetType(this.truncate("ta_tp", nf.getTargetType()));
        nf.setTitle(this.truncate("title", nf.getTitle()));
        nf.setContent(this.truncate("content", nf.getContent()));
        nf.setExt0(this.truncate("ext0", nf.getExt0()));
        nf.setExt1(this.truncate("ext1", nf.getExt1()));
        nf.setExt2(this.truncate("ext2", nf.getExt2()));
        nf.setExt3(this.truncate("ext3", nf.getExt3()));
        nf.setExt4(this.truncate("ext4", nf.getExt4()));
        nf.setExt5(this.truncate("ext5", nf.getExt5()));
        nf.setExt6(this.truncate("ext6", nf.getExt6()));
        nf.setExt7(this.truncate("ext7", nf.getExt7()));
        nf.setExt8(this.truncate("ext8", nf.getExt8()));
        nf.setExt9(this.truncate("ext9", nf.getExt9()));
    }

}
