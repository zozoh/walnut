package org.nutz.walnut.ext.net.imap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.search.FlagTerm;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

//import com.sun.mail.iap.ProtocolException;
//import com.sun.mail.imap.IMAPFolder;
//import com.sun.mail.imap.IMAPStore;
//import com.sun.mail.imap.protocol.IMAPProtocol;

public class cmd_imap extends JvmFilterExecutor<ImapContext, ImapFilter> {

    public cmd_imap() {
        super(ImapContext.class, ImapFilter.class);
    }

    @Override
    protected ImapContext newContext() {
        return new ImapContext();
    }

    @Override
    protected void prepare(WnSystem sys, ImapContext fc) {
        //
        // 读取配置文件
        //
        String configName = fc.params.val_check(0);
        String ph = "~/.imap/" + configName + ".json";
        WnObj oConf = Wn.checkObj(sys, ph);
        WnImapConfig config = sys.io.readJson(oConf, WnImapConfig.class);

        // 建立连接
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.put("mail.imap.host", config.getHost());
        props.put("mail.imap.port", config.getPort() + "");

        // 创建Session实例对象
        try {
            Session session = Session.getInstance(props);
            fc.store = (IMAPStore) session.getStore("imap");
            String account = config.getAccount();
            String passwd = config.getPasswd();
            fc.store.connect(account, passwd);

            // 准备文件夹
            String folderName = fc.params.val(1, "INBOX");
            fc.folder = (IMAPFolder) fc.store.getFolder(folderName);

            /**
             * 打开文件夹非常蛋疼，因为需要设置 <code>clientParams</code>
             * <p>
             * 否则，163的服务器会返回 <code>A3 NO EXAMINE Unsafe Login</code> 的错误
             * <p>
             * 但是，设置 id 必须要在 folder打开的情况下才行。所以只能设置这样一个 Callback
             */
            Map<String, String> clientParams = config.getClientParams();
            fc.folder.doOptionalCommand("ID not supported", new IMAPFolder.ProtocolCommand() {
                @Override
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    return p.id(clientParams);
                }
            });
            fc.folder.open(Folder.READ_WRITE);
        }
        catch (Exception e) {
            throw Er.create("e.cmd.imap.FailToConnect", e);
        }

    }

    @Override
    protected void output(WnSystem sys, ImapContext fc) {
        try {
            fc.folder.close(false);
            fc.store.close();
        }
        catch (Exception e) {
            throw Er.create("e.cmd.imap.FailToClose", e);
        }
    }

    private static Map<String, Flags.Flag> flagMap = new HashMap<>();

    static {
        flagMap.put("ANSWERED", Flags.Flag.ANSWERED);
        flagMap.put("DELETED", Flags.Flag.DELETED);
        flagMap.put("DRAFT", Flags.Flag.DRAFT);
        flagMap.put("FLAGGED", Flags.Flag.FLAGGED);
        flagMap.put("RECENT", Flags.Flag.RECENT);
        flagMap.put("SEEN", Flags.Flag.SEEN);
        flagMap.put("USER", Flags.Flag.USER);
    }

    private static Pattern _P = Pattern.compile("^(([01]):)?(.+)$");

    public static List<FlagTerm> parseFlagTermList(String[] flagList) {
        List<FlagTerm> termList = new ArrayList<>(flagList.length);
        for (String flagItem : flagList) {
            Matcher m = _P.matcher(flagItem);
            if (m.find()) {
                // 标记，默认为 true
                boolean yes = !"0".equals(m.group(2));
                String[] ss = Ws.splitIgnoreBlank(m.group(3));
                if (ss.length > 0) {
                    List<Flags.Flag> list = new ArrayList<>(ss.length);
                    for (String s : ss) {
                        Flags.Flag f = flagMap.get(s.toUpperCase());
                        if (null != f) {
                            list.add(f);
                        }
                    }
                    if (list.size() > 0) {
                        Flags flags = new Flags();
                        for (Flags.Flag f : list) {
                            flags.add(f);
                        }
                        termList.add(new FlagTerm(flags, yes));
                    }
                }
            }
        }
        return termList;
    }

}
