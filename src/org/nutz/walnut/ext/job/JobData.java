package org.nutz.walnut.ext.job;

import org.nutz.lang.util.NutMap;

public class JobData {

	public String name;
	public String cmdText;
	public String cron;
	public String createByUser;
	public String runByUser;
	public NutMap env;
	public long runAt;
	public JobData() {
	}
	public JobData(String name, String cmdText, String cron, String createByUser, String runByUser, NutMap env,
			long runAt) {
		super();
		this.name = name;
		this.cmdText = cmdText;
		this.cron = cron;
		this.createByUser = createByUser;
		this.runByUser = runByUser;
		this.env = env;
		this.runAt = runAt;
	}
	public JobData(String name, String cmdText, String cron, String createByUser, String runByUser, NutMap env) {
		super();
		this.name = name;
		this.cmdText = cmdText;
		this.cron = cron;
		this.createByUser = createByUser;
		this.runByUser = runByUser;
		this.env = env;
	}
	
}
