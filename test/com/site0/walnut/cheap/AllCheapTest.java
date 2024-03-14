package com.site0.walnut.cheap;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.cheap.dom.AllDomTest;
import com.site0.walnut.cheap.markdown.CheapBlockParsingTest;
import com.site0.walnut.cheap.markdown.CheapMarkdownParsingTest;
import com.site0.walnut.cheap.xml.CheapXmlParsingTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllDomTest.class,
                     CheapBlockParsingTest.class,
                     CheapXmlParsingTest.class,
                     CheapMarkdownParsingTest.class})
public class AllCheapTest {}
