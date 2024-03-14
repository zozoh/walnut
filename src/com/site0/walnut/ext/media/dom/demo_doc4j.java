package com.site0.walnut.ext.media.dom;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class demo_doc4j extends DomFilter {

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        File f = Files.createFileIfNoExists2("D:/tmp/docx/test.docx");
        OutputStream out = Streams.fileOut(f);
        try {
            WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
            MainDocumentPart part = wordPackage.getMainDocumentPart();
            part.addStyledParagraphOfText("Title", "哈哈哈");
            part.addParagraphOfText("Welcome To Baeldung");

            InputStream ins = Streams.fileIn("D:/tmp/docx/abc.jpg");
            byte[] bs = Streams.readBytes(ins);
            BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordPackage,
                                                                                        bs);
            Inline inline = imagePart.createImageInline("Baeldung Image (filename hint)",
                                                        "Alt Text",
                                                        1,
                                                        2,
                                                        false);
            CTPositiveSize2D aExt = inline.getExtent();
            aExt.setCx(2121121);
            aExt.setCy(2031998);

            ObjectFactory factory = new ObjectFactory();
            P p = factory.createP();
            R r = factory.createR();

            Drawing drawing = factory.createDrawing();
            r.getContent().add(drawing);
            drawing.getAnchorOrInline().add(inline);

            R r2 = factory.createR();
            Drawing drawing2 = factory.createDrawing();
            r2.getContent().add(drawing2);
            drawing2.getAnchorOrInline().add(inline);

            p.getContent().add(r);
            p.getContent().add(r2);

            part.getContent().add(p);

            wordPackage.save(out);
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
        finally {
            Streams.safeClose(out);
        }
    }

}
