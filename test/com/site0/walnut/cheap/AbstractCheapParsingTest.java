package com.site0.walnut.cheap;

import java.io.File;

import com.site0.walnut.util.Wfiles;

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

    protected String _Fjson(String name) {
        return _F(name, "json");
    }

    protected String _F(String name, String type) {
        String pph = "com/site0/walnut/cheap/mock";
        String aph = String.format("%s/%s/%s.%2$s", pph, type, name);
        File f = Wfiles.findFile(aph);
        String str = Wfiles.read(f);
        return str.replace("\r\n", "\n");
    }

}