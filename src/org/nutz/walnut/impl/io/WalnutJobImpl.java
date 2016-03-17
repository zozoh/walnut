package org.nutz.walnut.impl.io;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;

public class WalnutJobImpl implements WalnutJob, Callable<Object> {

	protected WnIo io;
	
	protected ExecutorService es;
	
	protected String root = "/sys/job";
	
	public void init() {
		if (es == null)
			es = Executors.newFixedThreadPool(64);
		io.createIfNoExists(null, root, WnRace.DIR);
	}

	public boolean hasNext() {
		return true;
	}

	public synchronized WnObj next() {
		long now = System.currentTimeMillis();
		String pid = io.check(null, root).id();
		WnQuery query = new WnQuery();
		query.setv("pid", pid);
		query.limit(1);
		query.sortBy("job_ava", 1);
		query.setv("job_ava", new NutMap().setv("gt", now));
		List<WnObj> list = io.query(query);
		if (list == null || list.isEmpty())
			return null;
		WnObj jobDir = list.get(0);
		io.appendMeta(jobDir, "job_ava:"+(now+24*60*60*1000L));
		return jobDir;
	}
	
	public Object call() throws Exception {
		while (true) {
			WnObj next = this.next();
			if (next == null) {
				Lang.quiteSleep(1000);
				continue;
			}
			es.submit(new WalnutJobExecutor(next));
		}
	}
	
	public class WalnutJobExecutor implements Callable<Object> {
		
		protected WnObj jobId;
		
		public WalnutJobExecutor(WnObj jobId) {
			this.jobId = jobId;
		}

		public Object call() throws Exception {
			return null;
		}
		
	}
}
