package com.site0.walnut.ooml;

import static org.junit.Assert.*;

import org.junit.Test;

public class OomlsTest {

    @Test
    public void test_measures_emus() {
        assertEquals(72, Oomls.convertMeasureI("emus_pt", 914400));
        assertEquals(1, Oomls.convertMeasureI("emus_in", 914400));
        assertEquals(254, Oomls.convertMeasureI("emus_cm", 914400 * 100));
        assertEquals(72 * 20, Oomls.convertMeasureI("emus_dxa", 914400));
    }

    @Test
    public void test_measures_dxa() {
        assertEquals(1, Oomls.convertMeasureI("dxa_pt", 20));
        assertEquals(1, Oomls.convertMeasureI("dxa_in", 20 * 72));
        assertEquals(254, Oomls.convertMeasureI("dxa_cm", 20 * 72 * 100));
        assertEquals(914400, Oomls.convertMeasureI("dxa_emus", 20 * 72));
    }

    @Test
    public void test_measures_cm() {
        assertEquals(72 * 100, Oomls.convertMeasureI("cm_pt", 254));
        assertEquals(100, Oomls.convertMeasureI("cm_in", 254));
        assertEquals(72 * 100 * 20, Oomls.convertMeasureI("cm_dxa", 254));
        assertEquals(360000, Oomls.convertMeasureI("cm_emus", 1));
    }

    @Test
    public void test_measures_in() {
        assertEquals(72, Oomls.convertMeasureI("in_pt", 1));
        assertEquals(254, Oomls.convertMeasureI("in_cm", 100));
        assertEquals(72 * 20, Oomls.convertMeasureI("in_dxa", 1));
        assertEquals(914400, Oomls.convertMeasureI("in_emus", 1));
    }

    @Test
    public void test_measures_pt() {
        assertEquals(1, Oomls.convertMeasureI("pt_in", 72));
        assertEquals(254, Oomls.convertMeasureI("pt_cm", 72 * 100));
        assertEquals(20, Oomls.convertMeasureI("pt_dxa", 1));
        assertEquals(914400, Oomls.convertMeasureI("pt_emus", 72));
    }

    @Test
    public void test_measures_combo() {
        // 像素-dxa
        assertEquals(15, Oomls.convertMeasureI("win_px_dxa", 1));
        assertEquals(1, Oomls.convertMeasureI("win_dxa_px", 15));
        assertEquals(9525, Oomls.convertMeasureI("win_px_emus", 1));
        assertEquals(1, Oomls.convertMeasureI("win_emus_px", 9525));
    }

    @Test
    public void test_measures_base() {
        // Windows 像素-磅
        assertEquals(3, Oomls.convertMeasureI("win_px_pt", 4));
        assertEquals(4, Oomls.convertMeasureI("win_pt_px", 3));

        // MacOS 像素-磅
        assertEquals(1, Oomls.convertMeasureI("mac_px_pt", 1));
        assertEquals(1, Oomls.convertMeasureI("mac_pt_px", 1));

        // 二十倍点-磅
        assertEquals(20, Oomls.convertMeasureI("pt_dxa", 1));
        assertEquals(1, Oomls.convertMeasureI("dxa_pt", 20));

        // 英寸-磅
        assertEquals(72, Oomls.convertMeasureI("in_pt", 1));
        assertEquals(1, Oomls.convertMeasureI("pt_in", 72));

        // 英寸-厘米
        assertEquals(254, Oomls.convertMeasureI("in_cm", 100));
        assertEquals(100, Oomls.convertMeasureI("cm_in", 254));

        // 英寸-英米单位
        assertEquals(914400, Oomls.convertMeasureI("in_emus", 1));
        assertEquals(1, Oomls.convertMeasureI("emus_in", 914400));

        // 厘米-英米单位
        assertEquals(360000, Oomls.convertMeasureI("cm_emus", 1));
        assertEquals(1, Oomls.convertMeasureI("emus_cm", 360000));
    }

}
