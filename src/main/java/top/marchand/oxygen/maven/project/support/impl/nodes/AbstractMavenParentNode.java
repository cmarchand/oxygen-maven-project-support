/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package top.marchand.oxygen.maven.project.support.impl.nodes;

import java.util.Collections;
import java.util.Comparator;
import javax.swing.tree.MutableTreeNode;

/**
 *
 * @author cmarchand
 */
public abstract class AbstractMavenParentNode extends AbstractMavenNode {
    private final Comparator<MutableTreeNode> comp;

    public AbstractMavenParentNode() {
        super();
        comp = (MutableTreeNode o1, MutableTreeNode o2) -> {
            if(o1 instanceof AbstractMavenParentNode && o2 instanceof AbstractMavenParentNode) return o1.toString().compareTo(o2.toString());
            else if(o1.getClass().equals(o2.getClass())) return o1.toString().compareTo(o2.toString());
            else if(o1 instanceof AbstractMavenParentNode) return -1;
            else return 1;
        };
    }

    @Override
    public void add(MutableTreeNode newChild) {
        addNoSort(newChild);
        sortNodes();
    }
    
    public void addNoSort(MutableTreeNode newChild) {
        super.add(newChild);
    }
    
    public void sortNodes() {
        Collections.sort(children, comp);
    }
    
    
}