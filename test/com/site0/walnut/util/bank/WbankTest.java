package com.site0.walnut.util.bank;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WbankTest {

    @Test
    public void test_01() {
        PartitionOptions opt = new PartitionOptions();
        opt.to = HDirecton.left;
        opt.width = 3;
        opt.sep = ",";
        opt.decimalPlaces = 0;

        assertEquals("10", Wbank.toBankText("10", opt));
        assertEquals("1,001", Wbank.toBankText("1001", opt));
        assertEquals("1,001.12345", Wbank.toBankText("1001.12345", opt));
        assertEquals("821,034,121,001.5",
                     Wbank.toBankText("821034121001.5", new PartitionOptions() {
                         {
                             to = HDirecton.left;
                             width = 3;
                             sep = ",";
                             decimalPlaces = 0;
                         }
                     }));

        PartitionOptions rightOpt = new PartitionOptions();
        rightOpt.to = HDirecton.right;
        rightOpt.width = 3;
        rightOpt.sep = ",";
        rightOpt.decimalPlaces = 0;

        assertEquals("100,1", Wbank.toBankText("1001", rightOpt));
        assertEquals("100,1.1315", Wbank.toBankText("1001.1315", rightOpt));
    }

    @Test
    public void test_00() {
        assertEquals("10.00", Wbank.toBankText("10", null));
        assertEquals("1,001.00", Wbank.toBankText("1001", null));
        assertEquals("1,001.12345", Wbank.toBankText("1001.12345", null));
        assertEquals("821,034,121,001.50",
                     Wbank.toBankText("821034121001.5", null));

        PartitionOptions opt = new PartitionOptions();
        opt.to = HDirecton.right;
        assertEquals("100,1.15", Wbank.toBankText("1001.15", opt));
    }
}
