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

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.log4j.Logger;
import top.marchand.oxygen.maven.project.support.impl.nodes.AbstractMavenNode;

/**
 *
 * @author cmarchand
 */
public class MavenTreeCellRenderer extends DefaultTreeCellRenderer   {
    private static final Logger LOGGER = Logger.getLogger(MavenTreeCellRenderer.class);

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component ret = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if(value instanceof AbstractMavenNode) {
            Icon icon = ((AbstractMavenNode) value).getIcon();
            if(icon!=null) {
                setIcon(icon);
            }
        }
        return ret;
    }

    
}
