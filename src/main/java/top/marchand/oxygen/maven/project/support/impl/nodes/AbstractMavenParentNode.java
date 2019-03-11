/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package top.marchand.oxygen.maven.project.support.impl.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;

/**
 *
 * @author cmarchand
 */
public abstract class AbstractMavenParentNode extends AbstractMavenNode {
    private List<AbstractMavenNode> children;
    private final Comparator<AbstractMavenNode> comp;

    public void appendChild(AbstractMavenNode child) {
        children.add(child);
        Collections.sort(children, comp);
        child.setParent(this);
    }

    public AbstractMavenParentNode() {
        super();
        children = new ArrayList<>();
        comp = (AbstractMavenNode o1, AbstractMavenNode o2) -> {
            if(o1 instanceof AbstractMavenParentNode && o2 instanceof AbstractMavenParentNode) return o1.getValue().compareTo(o2.getValue());
            else if(o1 instanceof MavenFileNode && o2 instanceof MavenFileNode) return o1.getValue().compareTo(o2.getValue());
            else if(o1 instanceof AbstractMavenParentNode) return -1;
            else return 1;
        };
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
