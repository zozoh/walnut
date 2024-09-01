package com.site0.walnut.ext.media.edi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.site0.walnut.ext.media.edi.bean.EdiSegment;

public class EdiSegmentFinder {

    private List<EdiSegment> list;

    private ListIterator<EdiSegment> it;

    public EdiSegmentFinder(List<EdiSegment> list) {
        this.list = list;
        reset();
    }

    public void reset() {
        it = this.list.listIterator();
    }

    public boolean isEnd() {
        return !it.hasNext();
    }

    public EdiSegment prev(String... tags) {
        // 防守
        if (!it.hasPrevious())
            return null;

        // 普通迭代
        if (tags.length == 0) {
            return it.previous();
        }

        // 循环查找
        while (it.hasPrevious()) {
            EdiSegment seg = it.previous();
            if (seg.is(tags)) {
                return seg;
            }
        }
        return null;
    }

    public EdiSegment next(String... tags) {
        // 防守
        if (!it.hasNext())
            return null;

        // 普通迭代
        if (tags.length == 0) {
            return it.next();
        }

        // 循环查找
        while (it.hasNext()) {
            EdiSegment seg = it.next();
            if (seg.is(tags)) {
                return seg;
            }
        }
        return null;
    }

    public List<EdiSegment> prevAll(String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasPrevious())
            return re;

        // 普通迭代
        boolean always = tags.length == 0;

        // 循环查找
        while (it.hasPrevious()) {
            EdiSegment seg = it.previous();
            if (always || seg.is(tags)) {
                re.add(seg);
            }
        }
        return re;
    }

    public List<EdiSegment> nextAll(String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasNext())
            return re;

        // 普通迭代
        boolean always = tags.length == 0;

        // 循环查找
        while (it.hasNext()) {
            EdiSegment seg = it.next();
            if (always || seg.is(tags)) {
                re.add(seg);
            }
        }
        return re;
    }

    public List<EdiSegment> nextAllUtilNoMatch(boolean noBreakUtilFound, String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasNext())
            return re;

        // 普通迭代
        boolean always = tags.length == 0;
        boolean foundOne = false;

        // 循环查找
        while (it.hasNext()) {
            EdiSegment seg = it.next();
            if (always || seg.is(tags)) {
                re.add(seg);
                foundOne = true;
            } else if (!noBreakUtilFound || foundOne) {
                // 不符合条件，回退一下指针，后续读取将从这个未匹配的开始
                it.previous();
                break;
            }
        }
        return re;
    }

    public List<EdiSegment> prevUntil(boolean inclusive, String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasPrevious())
            return re;

        // 循环查找
        while (it.hasPrevious()) {
            EdiSegment seg = it.previous();
            if (seg.is(tags)) {
                if (inclusive) {
                    re.add(seg);
                }
                break;
            } else {
                re.add(seg);
            }
        }
        return re;
    }

    public List<EdiSegment> nextUntil(boolean inclusive, String... tags) {
        List<EdiSegment> re = new ArrayList<>(list.size());
        // 防守
        if (!it.hasNext())
            return re;

        // 循环查找
        while (it.hasNext()) {
            EdiSegment seg = it.next();
            if (seg.is(tags)) {
                if (inclusive) {
                    re.add(seg);
                }
                break;
            } else {
                re.add(seg);
            }
        }
        return re;
    }

    public EdiSegment tryNext(String tag) {
        // 防守
        if (!it.hasNext())
            return null;

        // 找下一行, 判断
        EdiSegment seg = it.next();
        if (seg.isTag(tag)) {
            return seg;
        }

        //若找不到, 则回退一行
        it.previous();
        return null;
    }

//    public boolean moveBefore(String tag) {
//        // 防守
//        if (!it.hasNext())
//            return false;
//
//        // 找下一行, 判断
//        boolean findOne =  false;
//        EdiSegment seg = null;
//        while (it.hasNext()){
//            seg = it.next();
//            if(seg.isTag(tag)){
//                findOne = true;
//                it.previous();
//                break;
//            }
//        }
//
//        if(findOne) {
//            return true;
//        }
//
//        return false;
//    }

}
