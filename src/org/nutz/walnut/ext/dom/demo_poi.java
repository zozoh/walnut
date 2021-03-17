package org.nutz.walnut.ext.dom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;

import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

public class demo_poi extends DomFilter {

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        FileOutputStream out = null;
        try {
            // 准备文档
            XWPFDocument document = new XWPFDocument();

            // Write the Document in file system
            out = new FileOutputStream(new File("D:\\tmp\\docx\\test.docx"));

            // 添加标题
            XWPFParagraph titleParagraph = document.createParagraph();

            // 设置段落居中
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun titleParagraphRun = titleParagraph.createRun();

            titleParagraphRun.setText("Java PoI");
            titleParagraphRun.setColor("000000");
            titleParagraphRun.setFontSize(20);

            // 段落
            XWPFParagraph firstParagraph = document.createParagraph();
            XWPFRun run = firstParagraph.createRun();
            run.setText("Java POI 生成word文件。");
            run.setColor("696969");
            run.setFontSize(16);

            XWPFParagraph p2 = document.createParagraph();
            XWPFRun run2 = p2.createRun();
            run2.setText("哈哈哈");
            InputStream ins = Streams.fileIn("D:/tmp/docx/abc.jpg");
            // run2.addPicture(ins, Document.PICTURE_TYPE_JPEG, "abc.jpg", 300,
            // 100);
            String picId = document.addPictureData(ins, Document.PICTURE_TYPE_JPEG);
            run2.addChart(picId);

            ins.close();
            run2.addTab();
            run2.setText("vvv");

            // 换行
            XWPFParagraph paragraph1 = document.createParagraph();
            XWPFRun paragraphRun1 = paragraph1.createRun();
            paragraphRun1.setText("\r");

            // 基本信息表格
            XWPFTable infoTable = document.createTable();
            // 去表格边框
            // infoTable.getCTTbl().getTblPr().unsetTblBorders();

            // 列宽自动分割
            CTTblWidth infoTableWidth = infoTable.getCTTbl().addNewTblPr().addNewTblW();
            infoTableWidth.setType(STTblWidth.DXA);
            infoTableWidth.setW(BigInteger.valueOf(9072));

            // 表格第一行
            XWPFTableRow infoTableRowOne = infoTable.getRow(0);
            infoTableRowOne.getCell(0).setText("职位");
            infoTableRowOne.addNewTableCell().setText(": Java 开发工程师");

            // 表格第二行
            XWPFTableRow infoTableRowTwo = infoTable.createRow();
            infoTableRowTwo.getCell(0).setText("姓名");
            infoTableRowTwo.getCell(1).setText(": seawater");

            // 表格第三行
            XWPFTableRow infoTableRowThree = infoTable.createRow();
            infoTableRowThree.getCell(0).setText("生日");
            infoTableRowThree.getCell(1).setText(": xxx-xx-xx");

            // 表格第四行
            XWPFTableRow infoTableRowFour = infoTable.createRow();
            infoTableRowFour.getCell(0).setText("性别");
            infoTableRowFour.getCell(1).setText(": 男");

            // 表格第五行
            XWPFTableRow infoTableRowFive = infoTable.createRow();
            infoTableRowFive.getCell(0).setText("现居地");
            infoTableRowFive.getCell(1).setText(": xx");
            CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
            XWPFHeaderFooterPolicy policy = new XWPFHeaderFooterPolicy(document, sectPr);

            // 添加页眉
            CTP ctpHeader = CTP.Factory.newInstance();
            CTR ctrHeader = ctpHeader.addNewR();
            CTText ctHeader = ctrHeader.addNewT();
            String headerText = "ctpHeader";
            ctHeader.setStringValue(headerText);
            XWPFParagraph headerParagraph = new XWPFParagraph(ctpHeader, document);
            // 设置为右对齐
            headerParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFParagraph[] parsHeader = new XWPFParagraph[1];
            parsHeader[0] = headerParagraph;
            policy.createHeader(XWPFHeaderFooterPolicy.DEFAULT, parsHeader);

            // 添加页脚
            CTP ctpFooter = CTP.Factory.newInstance();
            CTR ctrFooter = ctpFooter.addNewR();
            CTText ctFooter = ctrFooter.addNewT();
            String footerText = "ctpFooter";
            ctFooter.setStringValue(footerText);
            XWPFParagraph footerParagraph = new XWPFParagraph(ctpFooter, document);
            headerParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFParagraph[] parsFooter = new XWPFParagraph[1];
            parsFooter[0] = footerParagraph;
            policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT, parsFooter);

            document.write(out);
            out.close();

        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
        finally {
            Streams.safeClose(out);
        }
    }

}
