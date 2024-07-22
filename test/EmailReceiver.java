import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;

import java.util.HashMap;
import java.util.Properties;

import com.sun.mail.imap.IMAPStore;

public class EmailReceiver {

    public static void main(String[] args) {
        String host = "imap.163.com";
        String port = "993";
        String username = "zozohtnt@163.com";
        String password = "LVRNKITDYGTXGZDK";
        String inboxName = "INBOX";

        Properties props = new Properties();
        props.put("mail.imap.host", host);
        props.put("mail.imap.port", port);
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.auth", "true");
        props.put("mail.imap.starttls.enable", "true");
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.imap.socketFactory.fallback", "false");
        props.put("mail.imap.socketFactory.port", port);

        // 网易的 163 邮箱验证比较严格
        // @see
        // http://help.mail.163.com/faqDetail.do?code=d7a5dc8471cd0c0e8b4b8f4f8e49998b374173cfe9171305fa1ce630d7f67ac2eda07326646e6eb0
        //
        // 带上IMAP ID信息，由key和value组成，例如name，version，vendor，support-email等。
        HashMap<String, String> IAM = new HashMap<>();
        IAM.put("name", "myname");
        IAM.put("version", "1.0.0");
        IAM.put("vendor", "myclient");
        IAM.put("support-email", "testmail@test.com");

        Session session = Session.getInstance(props);
        // session.setDebug(true);
        try {
            IMAPStore store = (IMAPStore) session.getStore("imap");
            store.connect(host, username, password);

            // 这里 163 需要你表明身份
            store.id(IAM);

            // // 获取根文件夹并遍历所有子文件夹
            // System.out.println("================== 遍历所有子文件夹");
            // Folder rootFolder = store.getDefaultFolder();
            // printFolder(rootFolder, "");
            // System.out.println("================== 检查文件夹里面的未读邮件：");
            // checkUnreadMessages(rootFolder);

            System.out.println("================== 尝试读取收件箱：");

            Folder inbox = store.getFolder(inboxName);
            try {
                inbox.open(Folder.READ_WRITE); // 改为READ_WRITE模式，使用SELECT而不是EXAMINE

            }
            catch (MessagingException e) {
                e.printStackTrace();
            }

            // 搜索未读邮件
            FlagTerm flagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message[] messages = inbox.search(flagTerm);
            System.out.println("Number of messages in INBOX: " + messages.length);

            // 如果 INBOX 为空，尝试检查垃圾邮件文件夹
            if (messages.length == 0) {
                Folder spamFolder = store.getFolder("Junk");
                if (spamFolder.exists()) {
                    spamFolder.open(Folder.READ_WRITE);
                    messages = spamFolder.search(flagTerm);
                    System.out.println("Number of messages in Junk: " + messages.length);
                    spamFolder.close(false);
                } else {
                    System.out.println("No Junk folder found.");
                }
            }

            for (Message message : messages) {
                if (message instanceof MimeMessage) {
                    MimeMessage mimeMessage = (MimeMessage) message;
                    System.out.println("Subject: " + mimeMessage.getSubject());
                    System.out.println("From: " + mimeMessage.getFrom()[0].toString());
                    System.out.println("Content: " + mimeMessage.getContent().toString());
                    System.out.println("-----------------------------------------------------");
                }
            }

            inbox.close(false);
            store.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void printFolder(Folder folder, String indent) throws MessagingException {
        System.out.println(indent + folder.getFullName());
        if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {
            Folder[] subFolders = folder.list();
            for (Folder subFolder : subFolders) {
                printFolder(subFolder, indent + "  ");
            }
        }
    }

    void checkUnreadMessages(Folder folder) throws MessagingException {
        if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
            folder.open(Folder.READ_ONLY);
            Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            System.out.println("Number of unread messages in "
                               + folder.getFullName()
                               + ": "
                               + messages.length);
            for (Message message : messages) {
                if (message instanceof MimeMessage) {
                    MimeMessage mimeMessage = (MimeMessage) message;
                    try {
                        System.out.println("Subject: " + mimeMessage.getSubject());
                        System.out.println("From: " + mimeMessage.getFrom()[0].toString());
                        System.out.println("Content: " + mimeMessage.getContent().toString());
                        System.out.println("-----------------------------------------------------");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            folder.close(false);
        }
        if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {
            Folder[] subFolders = folder.list();
            for (Folder subFolder : subFolders) {
                checkUnreadMessages(subFolder);
            }
        }
    }
}