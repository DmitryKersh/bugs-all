package com.github.dmitrykersh.bugs.api.util;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class wraps org.w3c.dom.NodeList returned by DOM parser methods.
 *
 * Features:
 * - filters all whitespace nodes
 * - iterable
 */

public class NdList implements NodeList, Iterable<Node> {

    private List<Node> nodes;

    public NdList(final @NotNull NodeList list) {
        nodes = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            if (!isWhitespaceNode(list.item(i))) {
                nodes.add(list.item(i));
            }
        }
    }

    @Override
    public Node item(int index) {
        return nodes.get(index);
    }

    @Override
    public int getLength() {
        return nodes.size();
    }

    private static boolean isWhitespaceNode(Node n) {
        if (n.getNodeType() == Node.TEXT_NODE) {
            String val = n.getNodeValue();
            return val.isBlank();
        } else {
            return false;
        }
    }

    @Override
    public Iterator<Node> iterator() {
        return nodes.iterator();
    }
}
