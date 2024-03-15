package org.nutz.mvc.init.conf;

import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.init.module.AtMapModule;
import org.nutz.mvc.init.module.SimpleTestModule;

@Modules(value={AtMapModule.class, SimpleTestModule.class}, scanPackage=false)
public class MainModuleAtMap {}
