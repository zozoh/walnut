package org.nutz.walnut.cheap;

import java.io.File;

import org.nutz.lang.Files;

public class AbstractCheapParsingTest {

    protected String _Fxml(String name) {
        return _F(name, "xml");
    }

    protected String _Fhtml(String name) {
        return _F(name, "html");
    }

    protected String _Fmd(String name) {
        return _F(name, "md");
    }

    protected String _F(String name, String type) {
        String pph = "org/nutz/walnut/cheap/mock";
        String aph = String.format("%s/%s/%s.%2$s", pph, type, name);
        File f = Files.findFile(aph);
        String str = Files.read(f);
        return str.replace("\r\n", "\n");
    }

}