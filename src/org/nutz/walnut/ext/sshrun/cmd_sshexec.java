package org.nutz.walnut.ext.sshrun;

import java.util.Arrays;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.NoCloseInputStream;
import org.apache.sshd.common.util.io.NoCloseOutputStream;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_sshexec extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "v|fuse");
        String host = params.get("host", "127.0.0.1");
        int port = params.getInt("port", 22);
        //boolean fuse = params.is("fuse");
        String login = params.get("user", sys.me.name());
        String password = params.get("password", "SEID:"+sys.se.id());
        String command = params.check("command");
        int connect_timeout = params.getInt("connect_timeout", 15)*1000;
        int verify_timeout = params.getInt("verify_timeout", 15)*1000;
        int exec_timeout = params.getInt("exec_timeout", 15*60)*1000;

        try(SshClient client = SshClient.setUpDefaultClient()) {
            client.start();
            client.setServerKeyVerifier((session, address, abc) -> {
                    return true;
            });
            ConnectFuture future = client.connect(login, host, port);
            future.await(connect_timeout);
            try(ClientSession session = future.getSession()) {
                session.addPasswordIdentity(password);
                session.auth().verify(verify_timeout);

                try(ClientChannel channel = session.createExecChannel(command)) {
                    if (sys.pipeId > 0)
                        channel.setIn(new NoCloseInputStream(sys.in.getInputStream()));
                    channel.setOut(new NoCloseOutputStream(sys.out.getOutputStream()));
                    channel.setErr(new NoCloseOutputStream(sys.err.getOutputStream()));
                    channel.open();
                    channel.waitFor(Arrays.asList(ClientChannelEvent.CLOSED), exec_timeout);
                } finally {
                    session.close(false);
                }
          } finally {
              client.stop();
          }
       }
    }

}
