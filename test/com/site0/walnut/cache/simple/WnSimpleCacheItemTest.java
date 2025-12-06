package com.site0.walnut.cache.simple;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnSimpleCacheItemTest {

    // 辅助方法：创建测试节点
    private WnSimpleCacheItem<String> createItem(String key) {
        return new WnSimpleCacheItem<>(key, key, 30000L); // data和key相同，过期时间无关紧要
    }

    // 辅助方法：创建测试节点
    private WnSimpleCacheItem<String> createItem(String key, long duration) {
        return new WnSimpleCacheItem<>(key, key + "_data", duration);
    }

    // -------------------------- 测试 insertAsMyPrev --------------------------

    /**
     * 场景1：当前节点是链表中唯一节点（无prev和next），插入prev节点后形成双节点链表
     */
    @Test
    public void testInsertAsMyPrev_IntoSingleNode() {
        // 初始化：A是唯一节点（prev=null, next=null）
        WnSimpleCacheItem<String> a = createItem("A");
        assertNull(a.prev());
        assertNull(a.next());

        // 操作：A插入B作为prev
        WnSimpleCacheItem<String> b = createItem("B");
        a.insertAsMyPrev(b);

        // 验证链表关系
        assertSame(b, a.prev()); // A的prev是B
        assertSame(a, b.next()); // B的next是A
        assertNull(b.prev()); // B的prev是null（新头节点）
        assertNull(a.next()); // A的next仍为null（保持尾节点）
    }

    /**
     * 场景2：当前节点有prev节点（非头节点），插入新节点到当前节点和原prev之间
     */
    @Test
    public void testInsertAsMyPrev_WithExistingPrev() {
        // 初始化：A <-> B（A是头，B是尾）
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        a.insertAsMyNext(b); // 构建A.next=B, B.prev=A
        assertEquals("A>B", a.toNextChainAsKeyStr());

        // 操作：B插入C作为prev（预期：A <-> C <-> B）
        WnSimpleCacheItem<String> c = createItem("C");
        b.insertAsMyPrev(c);

        // 验证链表关系
        // A的关系
        assertNull(a.prev());
        assertSame(c, a.next()); // A的next从B变为C
        // C的关系
        assertSame(a, c.prev()); // C的prev是A
        assertSame(b, c.next()); // C的next是B
        // B的关系
        assertSame(c, b.prev()); // B的prev从A变为C
        assertNull(b.next());
    }

    /**
     * 场景3：当前节点是头节点（prev=null），插入prev后新节点成为新头
     */
    @Test
    public void testInsertAsMyPrev_IntoHeadNode() {
        // 初始化：A是头节点（prev=null），A <-> B
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        a.insertAsMyNext(b);

        // 操作：A插入C作为prev（预期：C <-> A <-> B）
        WnSimpleCacheItem<String> c = createItem("C");
        a.insertAsMyPrev(c);

        // 验证链表关系
        // C的关系
        assertNull(c.prev());
        assertSame(a, c.next());
        // A的关系
        assertSame(c, a.prev());
        assertSame(b, a.next());
        // B的关系
        assertSame(a, b.prev());
        assertNull(b.next());
    }

    /**
     * 场景4：插入null节点，应无任何操作
     */
    @Test
    public void testInsertAsMyPrev_InsertNull() {
        // 初始化：A <-> B
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        a.insertAsMyNext(b);

        // 操作：插入null作为prev
        a.insertAsMyPrev(null);

        // 验证链表无变化
        assertNull(a.prev());
        assertSame(b, a.next());
        assertSame(a, b.prev());
        assertNull(b.next());
    }

    // -------------------------- 测试 insertAsMyNext --------------------------

    /**
     * 场景1：当前节点是链表中唯一节点（无prev和next），插入next节点后形成双节点链表
     */
    @Test
    public void testInsertAsMyNext_IntoSingleNode() {
        // 初始化：A是唯一节点（prev=null, next=null）
        WnSimpleCacheItem<String> a = createItem("A");
        assertNull(a.prev());
        assertNull(a.next());

        // 操作：A插入B作为next
        WnSimpleCacheItem<String> b = createItem("B");
        a.insertAsMyNext(b);

        // 验证链表关系
        assertSame(b, a.next()); // A的next是B
        assertSame(a, b.prev()); // B的prev是A
        assertNull(a.prev()); // A的prev仍为null（保持头节点）
        assertNull(b.next()); // B的next是null（新尾节点）
    }

    /**
     * 场景2：当前节点有next节点（非尾节点），插入新节点到当前节点和原next之间
     */
    @Test
    public void testInsertAsMyNext_WithExistingNext() {
        // 初始化：A <-> B（A是头，B是尾）
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        a.insertAsMyNext(b);

        // 操作：A插入C作为next（预期：A <-> C <-> B）
        WnSimpleCacheItem<String> c = createItem("C");
        a.insertAsMyNext(c);

        // 验证链表关系
        // A的关系
        assertNull(a.prev());
        assertSame(c, a.next()); // A的next从B变为C
        // C的关系
        assertSame(a, c.prev()); // C的prev是A
        assertSame(b, c.next()); // C的next是B
        // B的关系
        assertSame(c, b.prev()); // B的prev从A变为C
        assertNull(b.next());
    }

    /**
     * 场景3：当前节点是尾节点（next=null），插入next后新节点成为新尾
     */
    @Test
    public void testInsertAsMyNext_IntoTailNode() {
        // 初始化：A <-> B（B是尾节点，next=null）
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        a.insertAsMyNext(b);

        // 操作：B插入C作为next（预期：A <-> B <-> C）
        WnSimpleCacheItem<String> c = createItem("C");
        b.insertAsMyNext(c);

        // 验证链表关系
        // A的关系
        assertNull(a.prev());
        assertSame(b, a.next());
        // B的关系
        assertSame(a, b.prev());
        assertSame(c, b.next());
        // C的关系
        assertSame(b, c.prev());
        assertNull(c.next());
    }

    /**
     * 场景4：插入null节点，应无任何操作
     */
    @Test
    public void testInsertAsMyNext_InsertNull() {
        // 初始化：A <-> B
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        a.insertAsMyNext(b);

        // 操作：插入null作为next
        a.insertAsMyNext(null);

        // 验证链表无变化
        assertNull(a.prev());
        assertNull(a.next());
        assertSame(a, b.prev());
        assertNull(b.next());
    }

    /**
     * 场景5：复杂链表中间插入，验证多节点引用正确性
     */
    @Test
    public void testInsertAsMyNext_InComplexChain() {
        // 初始化：A <-> B <-> C <-> D（完整链表）
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        WnSimpleCacheItem<String> c = createItem("C");
        WnSimpleCacheItem<String> d = createItem("D");
        a.insertAsMyNext(b);
        b.insertAsMyNext(c);
        c.insertAsMyNext(d);

        // 操作：B插入X作为next（预期：A <-> B <-> X <-> C <-> D）
        WnSimpleCacheItem<String> x = createItem("X");
        b.insertAsMyNext(x);

        // 验证链表关系
        // A <-> B
        assertSame(b, a.next());
        assertSame(a, b.prev());
        // B <-> X
        assertSame(x, b.next());
        assertSame(b, x.prev());
        // X <-> C
        assertSame(c, x.next());
        assertSame(x, c.prev());
        // C <-> D
        assertSame(d, c.next());
        assertSame(c, d.prev());
        assertNull(d.next());
    }

    /**
     * 测试moveToPrev：将节点移动到目标节点的前面 场景：在A<->B<->C<->D中，将C移动到A前面
     */
    @Test
    public void testMoveToPrev() {
        // 初始化链表：A <-> B <-> C <-> D
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        WnSimpleCacheItem<String> c = createItem("C");
        WnSimpleCacheItem<String> d = createItem("D");
        a.insertAsMyNext(b);
        b.insertAsMyNext(c);
        c.insertAsMyNext(d);

        // 操作：将C移动到A的前面
        c.moveToPrev(a);

        // 预期结果：C <-> A <-> B <-> D
        // 验证C的关系
        assertNull(c.prev());
        assertSame(a, c.next());
        // 验证A的关系
        assertSame(c, a.prev());
        assertSame(b, a.next());
        // 验证B的关系
        assertSame(a, b.prev());
        assertSame(d, b.next());
        // 验证D的关系
        assertSame(b, d.prev());
        assertNull(d.next());
        // 确认C已从原位置移除（B和D直接相连）
        assertSame(d, b.next());
    }

    /**
     * 测试moveToNext：将节点移动到目标节点的后面 场景：在A<->B<->C<->D中，将A移动到C后面
     */
    @Test
    public void testMoveToNext() {
        // 初始化链表：A <-> B <-> C <-> D
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        WnSimpleCacheItem<String> c = createItem("C");
        WnSimpleCacheItem<String> d = createItem("D");
        a.insertAsMyNext(b);
        b.insertAsMyNext(c);
        c.insertAsMyNext(d);

        // 操作：将A移动到C的后面
        a.moveToNext(c);

        // 预期结果：B <-> C <-> A <-> D
        // 验证B的关系（新的头节点）
        assertNull(b.prev());
        assertSame(c, b.next());
        // 验证C的关系
        assertSame(b, c.prev());
        assertSame(a, c.next());
        // 验证A的关系
        assertSame(c, a.prev());
        assertSame(d, a.next());
        // 验证D的关系
        assertSame(a, d.prev());
        assertNull(d.next());
        // 确认A已从原位置移除（B直接连C）
        assertSame(c, b.next());
    }

    /**
     * 测试replaceBy：用新节点替换当前节点 场景：在A<->B<->C中，用X替换B
     */
    @Test
    public void testReplaceBy() {
        // 初始化链表：A <-> B <-> C
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        WnSimpleCacheItem<String> c = createItem("C");
        a.insertAsMyNext(b);
        b.insertAsMyNext(c);

        // 操作：用X替换B
        WnSimpleCacheItem<String> x = createItem("X");
        b.replaceBy(x);

        // 预期结果：A <-> X <-> C
        // 验证A的关系
        assertNull(a.prev());
        assertSame(x, a.next());
        // 验证X的关系
        assertSame(a, x.prev());
        assertSame(c, x.next());
        // 验证C的关系
        assertSame(x, c.prev());
        assertNull(c.next());
        // 确认B已从链表中移除（B的前后引用应不影响链表）
        assertNotSame(b, a.next());
        assertNotSame(b, c.prev());
    }

    /**
     * 测试remove：从链表中移除当前节点 场景：在A<->B<->C<->D中，移除B
     */
    @Test
    public void testRemove() {
        // 初始化链表：A <-> B <-> C <-> D
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        WnSimpleCacheItem<String> c = createItem("C");
        WnSimpleCacheItem<String> d = createItem("D");
        a.insertAsMyNext(b);
        b.insertAsMyNext(c);
        c.insertAsMyNext(d);

        // 操作：移除B节点
        b.remove();

        // 预期结果：A <-> C <-> D
        // 验证A的关系
        assertNull(a.prev());
        assertSame(c, a.next());
        // 验证C的关系
        assertSame(a, c.prev());
        assertSame(d, c.next());
        // 验证D的关系
        assertSame(c, d.prev());
        assertNull(d.next());
        // 确认B的前后引用已被清除（但B自身的prev/next不影响链表）
        assertNotSame(b, a.next());
        assertNotSame(b, c.prev());
    }

    // 13. moveToPrev测试（移动到头前/中间位置）
    @Test
    public void testMoveToPrev_EdgeCases() {
        // 场景1：移动到头节点前（成为新头）
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        a.insertAsMyNext(b);
        b.moveToPrev(a); // B <-> A
        assertNull(b.prev());
        assertSame(a, b.next());
        assertSame(b, a.prev());
        assertNull(a.next());

        // 场景2：移动到中间位置
        WnSimpleCacheItem<String> c = createItem("C");
        WnSimpleCacheItem<String> d = createItem("D");
        WnSimpleCacheItem<String> e = createItem("E");
        // C -> D -> E
        c.insertAsMyNext(d);
        d.insertAsMyNext(e);
        assertEquals("C>D>E", c.toNextChainAsKeyStr());
        // C -> D -> E => C -> E -> D
        e.moveToPrev(d);
        assertEquals("C>E>D", c.toNextChainAsKeyStr());

        // C -> E -> D => E -> C -> D
        e.moveToPrev(c);
        assertEquals("E>C>D", e.toNextChainAsKeyStr());
        assertEquals("C>D", c.toNextChainAsKeyStr());
        assertEquals("D", d.toNextChainAsKeyStr());

        assertEquals("D<C<E", d.toPrevChainAsKeyStr());
        assertEquals("C<E", c.toPrevChainAsKeyStr());
        assertEquals("E", e.toPrevChainAsKeyStr());
    }

    // 14. moveToNext测试（移动到尾后/中间位置）
    @Test
    public void testMoveToNext_EdgeCases() {
        // 场景1：移动到尾节点后（成为新尾）
        WnSimpleCacheItem<String> a = createItem("A");
        WnSimpleCacheItem<String> b = createItem("B");
        a.insertAsMyNext(b);
        a.moveToNext(b); // B <-> A
        assertNull(b.prev());
        assertSame(a, b.next());
        assertSame(b, a.prev());
        assertNull(a.next());

        // 场景2：移动到中间位置
        WnSimpleCacheItem<String> c = createItem("C");
        WnSimpleCacheItem<String> d = createItem("D");
        WnSimpleCacheItem<String> e = createItem("E");
        c.insertAsMyNext(d);
        d.insertAsMyNext(e);
        c.moveToNext(d); // d <-> c <-> e
        assertSame(c, d.next());
        assertSame(d, c.prev());
        assertSame(e, c.next());
        assertSame(c, e.prev());
    }

    // 1. 构造函数测试
    @Test
    public void testConstructor() {
        String key = "test";
        long duration = 5000L;
        WnSimpleCacheItem<String> item = new WnSimpleCacheItem<>(key, "data", duration);

        assertEquals(key, item.getKey());
        assertEquals("data", item.getData());
        assertEquals(0, item.getWeight());
        assertTrue(item.getExpireAt() > System.currentTimeMillis());
        assertEquals(duration, item.getDuration());
        assertNull(item.prev());
        assertNull(item.next());
    }

    // 2. isExpired测试（正常/过期/刚好过期）
    @Test
    public void testIsExpired() throws InterruptedException {
        // 场景1：未过期
        WnSimpleCacheItem<String> item = createItem("not_expired", 1000L);
        assertFalse(item.isExpired());

        // 场景2：已过期（使用0ms过期时间）
        WnSimpleCacheItem<String> expiredItem = createItem("expired", 0L);
        Thread.sleep(10); // 确保时间已过
        assertTrue(expiredItem.isExpired());
    }

    // 3. touch测试（更新过期时间）
    @Test
    public void testTouch() throws InterruptedException {
        WnSimpleCacheItem<String> item = createItem("touch", 1000L);
        long originalExpire = item.getExpireAt();
        Thread.sleep(100); // 等待100ms
        long newExpire = item.touch();

        assertTrue(newExpire > originalExpire);
        assertEquals(newExpire, item.getExpireAt());
    }

    // 4. isSame测试（key相同/不同/null比较）
    @Test
    public void testIsSame() {
        WnSimpleCacheItem<String> a = createItem("A", 1000L);
        WnSimpleCacheItem<String> a2 = createItem("A", 2000L); // 同key不同duration
        WnSimpleCacheItem<String> b = createItem("B", 1000L);

        assertTrue(a.isSame(a2));
        assertFalse(a.isSame(b));
        assertFalse(a.isSame(null));
    }

    // 5. getKey/getData/getWeight测试
    @Test
    public void testGetterMethods() {
        WnSimpleCacheItem<String> item = createItem("test", 1000L);
        item.increaseWeight(); // 权重+1

        assertEquals("test", item.getKey());
        assertEquals("test_data", item.getData());
        assertEquals(1, item.getWeight());
    }

    // 6. increaseWeight测试（正常增长/达到最大值）
    @Test
    public void testIncreaseWeight() {
        WnSimpleCacheItem<String> item = createItem("weight", 1000L);

        // 正常增长
        assertEquals(1, item.increaseWeight());
        assertEquals(2, item.increaseWeight());

        // 达到Integer.MAX_VALUE后不再增长
        try {
            java.lang.reflect.Field field = WnSimpleCacheItem.class.getDeclaredField("weight");
            field.setAccessible(true);
            field.set(item, Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, item.increaseWeight());
        }
        catch (Exception e) {
            fail("反射修改weight失败：" + e.getMessage());
        }
    }

    // 7. hasPrev/hasNext测试
    @Test
    public void testHasPrevAndHasNext() {
        WnSimpleCacheItem<String> a = createItem("A", 1000L);
        WnSimpleCacheItem<String> b = createItem("B", 1000L);
        a.insertAsMyNext(b);

        assertFalse(a.hasPrev());
        assertTrue(a.hasNext());
        assertTrue(b.hasPrev());
        assertFalse(b.hasNext());
    }

    // 8. prev/next/setPrev/setNext测试（直接修改指针）
    @Test
    public void testPrevNextSetters() {
        WnSimpleCacheItem<String> a = createItem("A", 1000L);
        WnSimpleCacheItem<String> b = createItem("B", 1000L);

        a.setNext(b);
        b.setPrev(a);

        assertSame(b, a.next());
        assertSame(a, b.prev());
    }

    // 9. insertAsMyPrev测试（覆盖边缘场景：插入到头/中间/尾）
    @Test
    public void testInsertAsMyPrev_EdgeCases() {
        // 场景1：插入到单节点前（成为新头）
        WnSimpleCacheItem<String> a = createItem("A", 1000L);
        WnSimpleCacheItem<String> b = createItem("B", 1000L);
        a.insertAsMyPrev(b);
        assertSame(b, a.prev());
        assertSame(a, b.next());
        assertNull(b.prev());

        // 场景2：插入到链表中间
        WnSimpleCacheItem<String> c = createItem("C", 1000L);
        a.insertAsMyPrev(c); // B <-> C <-> A
        assertSame(c, a.prev());
        assertSame(a, c.next());
        assertSame(b, c.prev());
        assertSame(c, b.next());

    }

    // 10. insertAsMyNext测试（覆盖边缘场景：插入到头/中间/尾）
    @Test
    public void testInsertAsMyNext_EdgeCases() {
        // 场景1：插入到单节点后（成为新尾）
        WnSimpleCacheItem<String> a = createItem("A", 1000L);
        WnSimpleCacheItem<String> b = createItem("B", 1000L);
        a.insertAsMyNext(b);
        assertSame(b, a.next());
        assertSame(a, b.prev());
        assertNull(b.next());

        // 场景2：插入到链表中间
        WnSimpleCacheItem<String> c = createItem("C", 1000L);
        a.insertAsMyNext(c); // A <-> C <-> B
        assertSame(c, a.next());
        assertSame(a, c.prev());
        assertSame(b, c.next());
        assertSame(c, b.prev());
    }

    // 11. replaceBy测试（替换为null/替换头/替换尾）
    @Test
    public void testReplaceBy_EdgeCases() {
        // 场景1：替换为null（移除当前节点）
        WnSimpleCacheItem<String> a = createItem("A", 1000L);
        WnSimpleCacheItem<String> b = createItem("B", 1000L);
        a.insertAsMyNext(b);
        b.replaceBy(null); // A -> null
        assertNull(a.next());

        // 场景2：替换头节点
        WnSimpleCacheItem<String> c = createItem("C", 1000L);
        WnSimpleCacheItem<String> d = createItem("D", 1000L);
        c.insertAsMyNext(d);
        WnSimpleCacheItem<String> newHead = createItem("newHead", 1000L);
        c.replaceBy(newHead); // newHead <-> D
        assertNull(newHead.prev());
        assertSame(d, newHead.next());
        assertSame(newHead, d.prev());

        // 场景3：替换尾节点
        WnSimpleCacheItem<String> e = createItem("E", 1000L);
        WnSimpleCacheItem<String> f = createItem("F", 1000L);
        e.insertAsMyNext(f);
        WnSimpleCacheItem<String> newTail = createItem("newTail", 1000L);
        f.replaceBy(newTail); // E <-> newTail
        assertSame(e, newTail.prev());
        assertNull(newTail.next());
        assertSame(newTail, e.next());
    }

    // 12. remove测试（移除头/尾/中间节点）
    @Test
    public void testRemove_EdgeCases() {
        // 场景1：移除头节点
        WnSimpleCacheItem<String> a = createItem("A", 1000L);
        WnSimpleCacheItem<String> b = createItem("B", 1000L);
        a.insertAsMyNext(b);
        a.remove(); // 只剩B
        assertNull(b.prev());

        // 场景2：移除尾节点
        WnSimpleCacheItem<String> c = createItem("C", 1000L);
        WnSimpleCacheItem<String> d = createItem("D", 1000L);
        c.insertAsMyNext(d);
        d.remove(); // 只剩C
        assertNull(c.next());

        // 场景3：移除中间节点
        WnSimpleCacheItem<String> e = createItem("E", 1000L);
        WnSimpleCacheItem<String> f = createItem("F", 1000L);
        WnSimpleCacheItem<String> g = createItem("G", 1000L);
        e.insertAsMyNext(f);
        f.insertAsMyNext(g);
        f.remove(); // E <-> G
        assertSame(g, e.next());
        assertSame(e, g.prev());
    }

}
