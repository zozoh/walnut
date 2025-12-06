package com.site0.walnut.cache.simple;

import static org.junit.Assert.*;

import org.junit.Test;

public class WnSimpleCacheTestByGrok3 {

    private static class TestCache extends WnSimpleCache<String> {
        public TestCache(int minItemCount, int maxItemCount, int du) {
            super(minItemCount, maxItemCount, du);
        }

        @Override
        public String loadItemData(String key) {
            return "Data for " + key;
        }

        public String getHeadKey() {
            return head != null ? head.getKey() : null;
        }

        public String getTailKey() {
            return tail != null ? tail.getKey() : null;
        }

        public String getListOrder() {
            if (head == null)
                return "";
            return head.toNextChainAsKeyStr();
        }
    }

    @Test
    public void testAddItem() {
        TestCache cache = new TestCache(2, 3, 100); // min=2, max=3, du=100s

        // Add "A"
        cache.put("A", "Data A");
        assertEquals(1, cache.size());
        assertEquals("A", cache.getHeadKey());
        assertEquals("A", cache.getTailKey());
        assertEquals("A", cache.getListOrder());

        // Add "B"
        cache.put("B", "Data B");
        assertEquals(2, cache.size());
        assertEquals("B", cache.getHeadKey()); // Newer item with weight=0
                                               // becomes head
        assertEquals("A", cache.getTailKey());
        assertEquals("B>A", cache.getListOrder());

        // Add "C"
        cache.put("C", "Data C");
        assertEquals(3, cache.size());
        assertEquals("C", cache.getHeadKey());
        assertEquals("A", cache.getTailKey());
        assertEquals("C>B>A", cache.getListOrder());

        // Add "D", exceeds maxItemCount, but no eviction until clearItems
        cache.put("D", "Data D");
        assertEquals(3, cache.size());
        assertEquals("D", cache.getHeadKey());
        assertEquals("B", cache.getTailKey());
        assertEquals("D>C>B", cache.getListOrder());

        // Clear items to enforce limits
        cache.cleanUp();
        assertEquals(2, cache.size()); // Evicts until size=minItemCount
        assertEquals("D", cache.getHeadKey());
        assertEquals("C", cache.getTailKey());
        assertEquals("D>C", cache.getListOrder());
    }

    @Test
    public void testGetItem() {
        TestCache cache = new TestCache(2, 3, 100000);

        // Get "A" (miss)
        assertEquals("Data for A", cache.get("A"));
        assertEquals(1, cache.size());
        assertEquals("A", cache.getHeadKey());
        assertEquals("A", cache.getTailKey());

        // Get "B" (miss)
        assertEquals("Data for B", cache.get("B"));
        assertEquals(2, cache.size());
        assertEquals("B", cache.getHeadKey()); // Weight=1, newer
        assertEquals("A", cache.getTailKey());
        assertEquals("B>A", cache.getListOrder());

        // Get "A" (hit)
        assertEquals("Data for A", cache.get("A"));
        assertEquals(2, cache.size());
        assertEquals("A", cache.getHeadKey()); // Weight=2, moves to head
        assertEquals("B", cache.getTailKey());
        assertEquals("A>B", cache.getListOrder());

        // Get "C" (miss)
        assertEquals("Data for C", cache.get("C"));
        assertEquals(3, cache.size());
        assertEquals("A", cache.getHeadKey()); // A(w=2) > C(w=1)
        assertEquals("B", cache.getTailKey());
        assertEquals("A>C>B", cache.getListOrder());

        // Get "B" (hit)
        assertEquals("Data for B", cache.get("B"));
        assertEquals(3, cache.size());
        assertEquals("B", cache.getHeadKey()); // B(w=2) moves to head
        assertEquals("C", cache.getTailKey());
        assertEquals("B>A>C", cache.getListOrder());
    }

    @Test
    public void testRemoveItem() {
        TestCache cache = new TestCache(2, 3, 100000);
        cache.put("A", "Data A");
        cache.put("B", "Data B");
        cache.put("C", "Data C");
        assertEquals(3, cache.size());
        assertEquals("C>B>A", cache.getListOrder());

        // Remove existing "B"
        assertEquals("Data B", cache.remove("B"));
        assertEquals(2, cache.size());
        assertEquals("C>A", cache.getListOrder());

        // Remove non-existing "D"
        assertNull(cache.remove("D"));
        assertEquals(2, cache.size());
        assertEquals("C>A", cache.getListOrder());

        // Remove all items one by one
        cache.remove("C");
        assertEquals(1, cache.size());
        assertEquals("A", cache.getListOrder());
        cache.remove("A");
        assertEquals(0, cache.size());
        assertEquals("", cache.getListOrder());
    }

    @Test
    public void testClearItemsWithExpiration() throws InterruptedException {
        TestCache cache = new TestCache(2, 3, 1); // du=1s
        cache.put("A", "Data A");
        cache.put("B", "Data B");
        Thread.sleep(1100); // All items expire
        cache.cleanUp();
        assertEquals(0, cache.size());
        assertNull(cache.getHeadKey());
        assertNull(cache.getTailKey());
    }

    @Test
    public void testClearItemsPartialExpiration() throws InterruptedException {
        TestCache cache = new TestCache(2, 3, 1); // du=1s
        cache.put("A", "Data A");
        Thread.sleep(500);
        cache.put("B", "Data B");
        Thread.sleep(600); // "A" expires, "B" does not
        cache.cleanUp();
        assertEquals(1, cache.size());
        assertEquals("B", cache.getHeadKey());
        assertEquals("B", cache.getTailKey());
    }

    @Test
    public void testClearItemsExceedMax() {
        TestCache cache = new TestCache(2, 3, 100000);
        cache.put("A", "Data A");
        cache.put("B", "Data B");
        cache.put("C", "Data C");
        cache.put("D", "Data D");
        assertEquals(3, cache.size());
        cache.cleanUp();
        assertEquals(2, cache.size()); // Evicts to minItemCount
        assertEquals("D>C", cache.getListOrder());
    }

    @Test
    public void testClearAll() {
        TestCache cache = new TestCache(2, 3, 100000);
        cache.put("A", "Data A");
        cache.put("B", "Data B");
        assertEquals(2, cache.size());
        cache.clearAll();
        assertEquals(0, cache.size());
        assertNull(cache.getHeadKey());
        assertNull(cache.getTailKey());
    }

    @Test
    public void testSizeAndLimits() {
        TestCache cache = new TestCache(5, 10, 60000);
        assertEquals(5, cache.getMinItemCount());
        assertEquals(10, cache.getMaxItemCount());
        assertEquals(0, cache.size());

        cache.put("A", "Data A");
        assertEquals(1, cache.size());
        assertEquals(5, cache.getMinItemCount());
        assertEquals(10, cache.getMaxItemCount());
    }

    @Test
    public void testEdgeCases() {
        TestCache cache = new TestCache(1, 1, 100000);

        // Add more than maxItemCount
        cache.put("A", "Data A");
        cache.put("B", "Data B");
        assertEquals(2, cache.size());
        cache.cleanUp();
        assertEquals(1, cache.size());
        assertEquals("B", cache.getHeadKey()); // "B" is newer

        // Add duplicate key
        cache.clearAll();
        cache.put("A", "Data A");
        cache.put("A", "New Data A"); // Should replace
        assertEquals(1, cache.size());
        assertEquals("New Data A", cache.get("A"));

        // Get with max weight
        TestCache cache2 = new TestCache(1, 1, 100000);
        cache2.put("X", "Data X");
        for (int i = 0; i < 1000; i++) {
            cache2.get("X"); // Increase weight, should not overflow
        }
        assertEquals("Data X", cache2.get("X"));
        assertEquals(1, cache2.size());
    }
}