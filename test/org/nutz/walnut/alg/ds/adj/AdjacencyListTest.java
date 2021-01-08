package org.nutz.walnut.alg.ds.adj;

import static org.junit.Assert.*;

import org.junit.Test;

public class AdjacencyListTest {

    @Test
    public void test_simple_add() {
        AdjacencyList al = new AdjacencyList();
        AdjacencyNode nA = al.addNode("A");
        AdjacencyNode nB = al.addNode(nA, "B");
        AdjacencyNode nC = al.addNode(nA, "C");
        AdjacencyNode nD = al.addNode(nB, "D");
        AdjacencyNode nE = al.addNode(nC, "E");

        AdjacencyNode d = al.findNode("D");
        assertEquals(d.getIndex(), nD.getIndex());

        AdjacencyNode e = al.findNode("E");
        assertEquals(e.getIndex(), nE.getIndex());

        String[] ss = nA.getEdgeNodeDatas(String.class);
        assertEquals(2, ss.length);
        assertEquals("B", ss[0]);
        assertEquals("C", ss[1]);

        ss = nB.getEdgeNodeDatas(String.class);
        assertEquals(1, ss.length);
        assertEquals("D", ss[0]);

        ss = nC.getEdgeNodeDatas(String.class);
        assertEquals(1, ss.length);
        assertEquals("E", ss[0]);

        // System.out.println(al.toString());
    }

}
