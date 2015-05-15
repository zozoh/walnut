package org.nutz.walnut.impl.box;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;

public class TextTable {

    private int colnb;

    private boolean[] cell_align_left;

    private int[] cell_width;

    private List<String[]> rows;

    public TextTable(int colnb) {
        this.colnb = colnb;

        this.cell_width = new int[colnb];
        Arrays.fill(this.cell_width, 0);

        this.cell_align_left = new boolean[colnb];
        Arrays.fill(this.cell_align_left, true);

        this.rows = new LinkedList<String[]>();
    }

    public void alignRigth(int colIndex) {
        cell_align_left[colIndex] = false;
    }

    public void addRow(Collection<String> col) {
        String[] row = new String[colnb];
        int sz = col.size();
        int i = 0;
        // 从头填充
        for (String str : col) {
            if (i >= sz)
                break;
            row[i] = Strings.sNull(str, "");
            cell_width[i] = Math.max(cell_width[i], row[i].length());
            i++;
        }
        // 填充不足
        for (; i < colnb; i++) {
            row[i] = "";
        }
        // 记录行
        rows.add(row);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String[] row : rows) {
            int last = row.length - 1;
            for (int i = 0; i < last; i++) {
                String cell = row[i];
                if (cell_align_left[i]) {
                    sb.append(Strings.alignLeft(cell, cell_width[i], ' '));
                } else {
                    sb.append(Strings.alignRight(cell, cell_width[i], ' '));
                }
                sb.append(' ');
            }
            // 加上最后一列
            sb.append(row[last]).append('\n');
        }
        return sb.toString();
    }
}
