package org.nutz.walnut.ext.crontab;

import org.nutz.plugins.zcron.ZCron;

public class CronJob {

	public String name;
	public String user;
	public String cwd;
	public String cronline;
	public String cron;
	public String cmd;
	public ZCron quartz;
}