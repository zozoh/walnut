package org.nutz.walnut.tool;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import org.nutz.img.Images;
import org.nutz.lang.Files;

public class GenFileTypeCssSprite {

    private static int css_y_offset = 16;

    private static int css_size = 16;

    private static int base_size = 16;

    private static int[] row_x = new int[26];

    public static void main(String[] args) {

        // 首先找到
        BufferedImage im = new BufferedImage(base_size
                                             * 26,
                                             base_size * 26,
                                             BufferedImage.TYPE_INT_ARGB);
        Graphics2D gc = (Graphics2D) im.getGraphics();

        // 在图标目录遍历
        File home = Files.findFile("~/workspace/git/github/walnut/ROOT/etc/thumbnail");
        for (File d : home.listFiles()) {
            // 在图标目录 ...
            if (d.isDirectory()) {
                // 找文件
                File png = Files.getFile(d, base_size + "x" + base_size + ".png");
                // 如果存在 ..
                if (png.exists()) {
                    String nm = d.getName().toLowerCase();
                    char c = nm.charAt(0);
                    int row = ((int) c) - ((int) 'a');
                    int col = row_x[row]++;
                    // System.out.printf("%-12s:%c:%-8d: %dx%d\n", nm, c, (int)
                    // c, row, col);
                    System.out.printf("%-24s{background-position: %dpx %dpx;}\n",
                                      ".oicon[otp=\"" + nm + "\"]",
                                      col * -1 * css_size,
                                      (row * -1 * css_size) - css_y_offset);
                    overlay(gc, png, row, col);
                }
            }
        }

        // 释放
        gc.dispose();

        // 输出到文件
        Images.write(im, Files.createFileIfNoExists2("~/tmp/icons.png"));

    }

    static void overlay(Graphics2D gc, File pngF, int row, int col) {
        int x = col * base_size;
        int y = row * base_size;

        // 读取
        BufferedImage im = Images.read(pngF);
        gc.drawImage(im, x, y, base_size, base_size, null);
    }

}
