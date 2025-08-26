package com.site0.walnut.ext.net.mailx.bean;

import com.site0.walnut.util.Ws;

public class MailFSConfig {

    /**
     * 指定映射 S3 存储桶的目录
     */
    private String home;

    /**
     * 指明收件箱的文件夹名称，默认为 `INBOX`
     */
    private String inbox;

    /**
     * 指明归档的路径，它会根据文件的 lastModified 进行日期时间格式化
     */
    private String archivePrefix;

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getInbox() {
        return Ws.sBlank(inbox, "INBOX");
    }

    public String getInboxpPrefix(String inboxName) {
        String re = Ws.sBlank(inboxName, this.getInbox());
        if (!re.endsWith("re")) {
            return re + "/";
        }
        return re;
    }

    public void setInbox(String inbox) {
        this.inbox = inbox;
    }

    public String getArchivePrefix() {
        return archivePrefix;
    }

    public void setArchivePrefix(String archive) {
        this.archivePrefix = archive;
    }

}
