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

import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

/**
 *
 * @author cmarchand
 */
public class MavenFileNode extends AbstractMavenNode {
    private final Path file;
    private final String fileName;
    
    public MavenFileNode(Path file) {
        super();
        this.file=file;
        fileName = file.getFileName().toString();
    }

    @Override
    public String getValue() {
        return fileName;
    }

    @Override
    public ImageIcon getIcon() {
        // FIXME
        return null;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public int getIndex(TreeNode node) {
        return -1;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Enumeration children() {
        return Collections.emptyEnumeration();
    }
    
}
