package org.nutz.walnut.ext.ftp;

import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.AbstractUserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.walnut.util.WnRun;

@IocBean
public class WnFtpUserManager extends AbstractUserManager {
    
    @Inject
    protected WnRun wnRun;

    public User getUserByName(String userName) throws FtpException {
        if (!doesExist(userName)) {
            return null;
        }

        BaseUser user = new BaseUser();
        user.setName(userName);
        user.setEnabled(true);
        if ("root".equals(userName))
            user.setHomeDirectory("/root");
        else
            user.setHomeDirectory("/home/" + userName);

        List<Authority> authorities = new ArrayList<Authority>();

        authorities.add(new WritePermission());

        int maxLogin = 3;
        int maxLoginPerIP = 3;

        authorities.add(new ConcurrentLoginPermission(maxLogin, maxLoginPerIP));

        int uploadRate = 0;
        int downloadRate = 0;

        authorities.add(new TransferRatePermission(downloadRate, uploadRate));

        user.setAuthorities(authorities);

        user.setMaxIdleTime(600);

        return user;
    }

    public String[] getAllUserNames() throws FtpException {
        return new String[]{"root"};
    }

    public void delete(String username) throws FtpException {}

    public void save(User user) throws FtpException {}

    public boolean doesExist(String username) throws FtpException {
        try {
            return wnRun.usrs().fetch(username) != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    public User authenticate(Authentication authentication)
            throws AuthenticationFailedException {
        if (authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;

            String user = upauth.getUsername();
            String password = upauth.getPassword();

            if (user == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }

            if (password == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }
            if (!wnRun.usrs().checkPassword(user, password)) {
                throw new AuthenticationFailedException("Authentication failed");
            }

            try {
                return getUserByName(user);
            }
            catch (FtpException e) {
                e.printStackTrace();
                throw new AuthenticationFailedException("Authentication failed");
            }
        }
        throw new AuthenticationFailedException("Authentication failed");
    }

}
