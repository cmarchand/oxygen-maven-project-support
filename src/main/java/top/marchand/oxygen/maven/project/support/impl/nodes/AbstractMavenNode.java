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
package top.marchand.oxygen.maven.project.support.impl.nodes;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

/**
 *
 * @author cmarchand
 */
public abstract class AbstractMavenNode implements TreeNode, Comparable<AbstractMavenNode> {
    private AbstractMavenParentNode parent;
    
    /**
     * Returns the value of this node
     * @return 
     */
    public abstract String getValue();
    
    /**
     * Returns the icon to display on this node
     * @return 
     */
    public abstract Icon getIcon();

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public AbstractMavenParentNode getParent() {
        return parent;
    }

    public void setParent(AbstractMavenParentNode parent) {
        this.parent = parent;
    }
     @Override
    public int compareTo(AbstractMavenNode o) {
        return getValue().compareTo(o.getValue());
    }   
    
}
