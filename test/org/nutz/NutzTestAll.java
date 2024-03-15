package org.nutz;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Prepare a database with URL: jdbc:mysql://localhost:3306/zzhtest support user
 * root@123456
 * 
 * @author zozoh
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({NutzTestAllWithDB.class, NutzTestAllWithoutDB.class})
public class NutzTestAll {

}
