/*
 * Copyright 2019 Christophe Marchand
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.marchand.oxygen.maven.project.support.impl;

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 * @author cmarchand
 */
public abstract class FilteredTreeModel extends DefaultTreeModel {
    private final TreeNode completeData;
    
    public FilteredTreeModel(TreeNode root) {
        super(root);
        completeData = root;
    }
    
    public void filter(String value) {
        if(value==null || value.isEmpty()) {
            setRoot(completeData);
        } else {
            DefaultMutableTreeNode root = createRootNode();
            for(Enumeration<TreeNode> enumer=completeData.children(); enumer.hasMoreElements();) {
                DefaultMutableTreeNode tn = (DefaultMutableTreeNode)enumer.nextElement();
                if(tn.toString().contains(value)) {
                    root.add((MutableTreeNode)(tn.clone()));
                }
            }
            setRoot(root);
        }
    }
    public abstract DefaultMutableTreeNode createRootNode();
}
