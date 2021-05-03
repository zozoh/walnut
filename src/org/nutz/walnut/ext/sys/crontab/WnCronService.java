package org.nutz.walnut.ext.sys.crontab;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.runner.NutRunner;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.cron.CronEach;
import org.nutz.walnut.cron.WnCron;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.util.Wtime;

@IocBean(create = "init", depose = "depose")
public class WnCronService extends NutRunner {

	private static final Log log = Wlog.getEXT();

	public static WnCronService me;

	@Inject
	protected WnRun run;

	@Inject
	protected WnIo io;

	public List<CronJob> jobs;

	public WnCronService() {
		super("wn.crontab");
		me = this;
	}

	public long exec() throws InterruptedException {
		// boolean flag = true;
		List<CronJob> jobs = this.jobs;
		if (jobs == null || jobs.isEmpty()) {
			return 5000;
		}
		// while (flag) {
		int timer_int = 10;

		// 今天的零点是多少呢?
		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(Wn.now());
		Wtime.setDayStart(today);
		long dayOfBegin = today.getTimeInMillis();
		log.infof("初始化时间 %s", Times.sDT(new Date(dayOfBegin)));

		JobTime[] mats = new JobTime[86400 / timer_int];
		List<JobTime> jobTimes = new ArrayList<WnCronService.JobTime>();
		for (CronJob cronJob : jobs) {
			if (cronJob == null)
				continue;
			if (cronJob.quartz == null)
				continue;
			cronJob.quartz.each(mats, new CronEach<JobTime>() {
				public void invoke(JobTime[] array, int index) throws Exception {
					if (array[index] == null) {
						array[index] = new JobTime();
						array[index].timeAt = (index) * timer_int * 1000 + dayOfBegin + 99;
						// System.out.println(new Date(array[index].timeAt));
						jobTimes.add(array[index]);
					}
					array[index].jobs.add(cronJob);
				};
			}, today);
		}
		log.debugf("共%d个时间点有job需要运行", jobTimes.size());
		if (jobTimes.isEmpty()) {
			return 5000;
		}
		Collections.sort(jobTimes);
		for (JobTime jobTime : jobTimes) {
			long timenow = Wn.now();
			long diff = jobTime.timeAt - timenow;
			if (diff < 0) {
				log.debugf("已经过了任务执行时间 %s", Times.sDT(new Date(jobTime.timeAt)));
				continue;
			}
			log.debugf("当前时间 %s 下一个任务的执行时间 %s", Times.sDT(new Date(timenow)), Times.sDT(new Date(jobTime.timeAt)));
			if (diff > timer_int * 1000) {
				log.debugf("休眠%dms到下一个任务的开始时间", diff);
				getLock().wait(diff);
			}
			if (jobs != this.jobs) {
				return 100;
			}
			// 执行
			for (CronJob job : jobTime.jobs) {
				run.exec(job.name, job.user, job.cmd + " &");
			}
		}
		// }
		long time2nextday = 86400 * 1000L - (Wn.now() - dayOfBegin);
		log.infof("今天的所有任务已经执行完, 休眠到午夜12:00分, 时长%dms", time2nextday);
		return time2nextday;
	}

	public void startAtEs() {
		es.submit(this);
	}

	public boolean reload() {
		try {
			List<CronJob> jobs = _reload();
			if (jobs != null) {
				this.jobs = jobs;
				this.getLock().wakeup();
				return true;
			}
		} catch (Throwable e) {
			log.info("reload fail", e);
		}
		return false;
	}

	public List<CronJob> _reload() {
		WnObj wobj = io.fetch(null, "/etc/crontab");
		String cnt = "";
		if (wobj == null) {
			wobj = io.create(null, "/etc/crontab", WnRace.FILE);
			io.writeText(wobj, "#crontab\r\n");
		} else {
			cnt = io.readText(wobj);
		}
		String[] tmp = cnt.split("\n");
		List<CronJob> jobs = new ArrayList<CronJob>();
		CronJob job = new CronJob();
		for (int i = 0; i < tmp.length; i++) {
			String line = tmp[i];
			if (Strings.isBlank(line))
				continue;
			line = line.trim();
			if (line.startsWith("###")) {
				job = Lang.map2Object(Lang.map(line.substring(3)), CronJob.class);
			} else if (line.startsWith("#")) {
				continue;
			} else {
				job.cronline = line;
				int pos_begin = line.indexOf('<');
				if (pos_begin < 0) {
					log.warnf("bad cron line = %s", line);
					return null;
				}
				int pos_end = line.indexOf('>', pos_begin);
				if (pos_end < 0) {
					log.warnf("bad cron line = %s", line);
					return null;
				}
				job.cron = line.substring(0, pos_begin).trim();
				job.user = line.substring(pos_begin+1, pos_end).trim();
				job.cmd = line.substring(pos_end + 1);
				job.quartz = new WnCron(job.cron);
				if (Strings.isBlank(job.name)) {
					job.name = "cron-" + job.user + "-" + i;
				}
				jobs.add(job);
				log.infof("add cron : %s : %s", job.name, line);
				job = new CronJob();
			}
		}
		return jobs;
	}

	protected ExecutorService es;

	public void init() {
		es = Executors.newFixedThreadPool(32);
		reload();
		es.submit(this);
	}

	public void depose() {
		if (es != null) {
			es.shutdown();
			es = null;
		}
	}

	static class JobTime implements Comparable<JobTime> {
		public long timeAt;
		public List<CronJob> jobs = new ArrayList<CronJob>();

		@Override
		public int compareTo(JobTime o) {
			return Long.compare(this.timeAt, o.timeAt);
		}

	}
}