package org.nutz.ioc.meta.issue399;

import org.nutz.ioc.loader.annotation.IocBean;
import com.site0.walnut.util.Wlang;

@IocBean(singleton=false, create="create", depose="depose")
public class Issue399Service {
	
	public static int CreateCount;
	public static int DeposeCount;
	
	public void create() {
		CreateCount ++; 
	}

	public String depose() {
		DeposeCount++;
		throw Wlang.impossible();
	}
}
