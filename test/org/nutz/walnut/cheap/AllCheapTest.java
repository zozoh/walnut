package org.nutz.walnut.cheap;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.cheap.dom.AllDomTest;
import org.nutz.walnut.cheap.markdown.CheapBlockParsingTest;
import org.nutz.walnut.cheap.markdown.CheapMarkdownParsingTest;
import org.nutz.walnut.cheap.xml.CheapXmlParsingTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllDomTest.class,
                     CheapBlockParsingTest.class,
                     CheapXmlParsingTest.class,
                     CheapMarkdownParsingTest.class})
public class AllCheapTest {}
