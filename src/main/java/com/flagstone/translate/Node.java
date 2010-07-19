/*
 * Node.java
 * Translate
 *
 * Copyright (c) 2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.flagstone.translate;

import java.util.ArrayList;
import java.util.List;


public class Node {

    private final NodeType type;
    private String value;
    private Node parent;
    private final List<Node> children;

    public Node(final NodeType nodeType) {
        type = nodeType;
        children = new ArrayList<Node>();
    }

    public Node(final NodeType nodeType, final String val) {
        type = nodeType;
        value = val;
        children = new ArrayList<Node>();
    }

    /**
     * Gets the type of the node.
     *
     * @return the type assigned to the node.
     */
    public NodeType getType() {
        return type;
    }

    /**
     * Get the string value assigned to a node.
     *
     * @return the string value assigned to a node.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the node at the specified index from the array of child nodes. If
     * the index is outside the range of the array then an ArrayIndexOutOfBounds
     * exception is thrown.
     *
     * @param index
     *            the index of the child node to return.
     * @return the ith node in the array of children.
     * @throws ArrayIndexOutOfBoundsException
     *             if (index < 0 || index >= length).
     */
    public Node get(int index) {
        return children.get(index);
    }

    /**
     * Replaces the node at position i in the array of children. If the position
     * is outside the range of the array (i< 0 || i >= length) then an
     * ArrayIndexOutOfBoundsException is thrown.
     *
     * @param i
     *            the index of the child node to replace.
     * @param aNode
     *            the node to replace the ith node.
     * @throws ArrayIndexOutOfBoundsException
     *             if (index < 0 || index >= length).
     */
    public void set(int i, Node node) {
        children.set(i, node);
    }

    /**
     * Adds a node to the array of children. If the node is null then it is
     * ignored.
     *
     * @param aNode
     *            the node to be added.
     */
    public Node add(Node node) {
        children.add(node);
        return this;
    }

    public Node addAll(List<Node> list) {
        children.addAll(list);
        return this;
    }

    /**
     * Inserts a node at position i in the array of children. The size of the
     * array is increased by one and the nodes from the insertion point onwards
     * are moved to the right.
     *
     * If the position is outside the range of the array (i< 0 || i >= length)
     * then an ArrayIndexOutOfBoundsException is thrown.
     *
     * @param index
     *            the index of the child node to replace.
     * @param aNode
     *            the node to replace the ith node.
     * @throws ArrayIndexOutOfBoundsException
     *             if (index < 0 || index >= length).
     */
    public void insert(int index, Node node) {
        children.add(index, node);
    }

    /**
     * Removes the node at position i in the array of children. The size of the
     * array is decreased by one and the nodes from the insertion point onwards
     * are moved to the left.
     *
     * If the position is outside the range of the array (i< 0 || i >= length)
     * then an ArrayIndexOutOfBoundsException is thrown.
     *
     * @param index
     *            the index of the child node to remove.
     * @throws ArrayIndexOutOfBoundsException
     *             if (index < 0 || index >= length).
     */
    public void remove(int index) {
        children.remove(index);
    }

    /**
     * Gets the parent node of this one. If no parent is define then null is
     * returned.
     *
     * @return the parent node of this one.
     */
    public Node getParent() {
        return parent;
    }
    
    public List<Node> getChildren() {
    	return children;
    }

    /**
     * Return the number of child nodes contained by this node.
     *
     * @return the number of child nodes.
     */
    public int count() {
        return children.size();
    }

    /**
     * Returns a string containing the type of node, any associated value and
     * the number of children.
     *
     * @return the string representation of the node.
     */
    @Override
    public String toString() {
        return type.toString() + " = " + value + "; ";
    }
}
