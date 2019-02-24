/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package top.marchand.oxygen.maven.project.support.impl.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.tree.TreeNode;

/**
 *
 * @author cmarchand
 */
public abstract class AbstractMavenParentNode extends AbstractMavenNode {
    private List<AbstractMavenNode> children;

    public void appendChild(AbstractMavenNode child) {
        children.add(child);
        Collections.sort(children);
        child.setParent(this);
    }

    public AbstractMavenParentNode() {
        super();
        children = new ArrayList<>();
    }
    @Override
    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return getChildCount()==0;
    }

    @Override
    public Enumeration children() {
        return Collections.enumeration(children);
    }

}
