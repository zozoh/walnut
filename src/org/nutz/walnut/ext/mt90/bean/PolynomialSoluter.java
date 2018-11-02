package org.nutz.walnut.ext.mt90.bean;

public class PolynomialSoluter {
    private double[][] matrix;
    private double[] result;
    private int order;
    
    public PolynomialSoluter(double[][] matrix) {
        this.matrix = matrix;
        this.order = matrix.length;
        this.result = new double[order];
    }

    public double[] getResult() {
        // 高斯消元-正向
        for (int i = 0; i < order; i++) {

            // 如果当前行对角线项为0则与后面的同列项非0的行交换
            if (!swithIfZero(i))
                return null;
            // 消元
            for (int j = i + 1; j < order; j++) {
                if (matrix[j][i] == 0)
                    continue;
                double factor = matrix[j][i] / matrix[i][i];
                for (int l = i; l < order + 1; l++)
                    matrix[j][l] = matrix[j][l] - matrix[i][l] * factor;
            }
        }

        // 高斯消元-反向-去掉了冗余计算
        for (int i = order - 1; i >= 0; i--) {
            result[i] = matrix[i][order] / matrix[i][i];
            for (int j = i - 1; j > -1; j--)
                matrix[j][order] = matrix[j][order] - result[i] * matrix[j][i];
        }
        return result;
    }

    private boolean swithIfZero(int i) {
        if (matrix[i][i] == 0) {
            int j = i + 1;

            // 找到对应位置非0的列
            while (j < order && matrix[j][i] == 0)
                j++;

            // 若对应位置全为0则无解
            if (j == order)
                return false;
            else
                switchRows(i, j);
        }
        return true;
    }

    private void switchRows(int i, int j) {
        double[] tmp = matrix[i];
        matrix[i] = matrix[j];
        matrix[j] = tmp;
    }
}
/*
 * --------------------- 作者：竖琴手 来源：CSDN 原文：https://blog.csdn.net/strangerzz/article/details/45244249 版权声明：本文为博主原创文章，转载请附上博文链接！
 */