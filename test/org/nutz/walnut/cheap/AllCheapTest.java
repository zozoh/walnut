package org.nutz.walnut.cheap;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.cheap.dom.CheapNodeTest;
import org.nutz.walnut.cheap.html.CheapHtmlParsingTest;
import org.nutz.walnut.cheap.markdown.CheapBlockParsingTest;
import org.nutz.walnut.cheap.markdown.CheapMarkdownParsingTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({CheapNodeTest.class,
                     CheapBlockParsingTest.class,
                     CheapHtmlParsingTest.class,
                     CheapMarkdownParsingTest.class})
public class AllCheapTest {}
