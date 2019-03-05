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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;
import org.apache.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 *
 * @author cmarchand
 */
public class MavenFileNode extends AbstractMavenNode {
    private static final Logger LOGGER = Logger.getLogger(MavenFileNode.class);
    private final Path file;
    private final String fileName;
    private final URL fileUrl;
    
    public MavenFileNode(Path file) {
        super();
        this.file=file;
        fileName = file.getFileName().toString();
        URL url = null;
        try {
            url = file.toUri().toURL();
        } catch(MalformedURLException ex) { }
        fileUrl = url;
    }

    @Override
    public String getValue() {
        return fileName;
    }

    @Override
    public Icon getIcon() {
        Object ret = PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities().getIconDecoration(fileUrl);
        // ret is an object from an obfuscated class, which extends ImageIcon
        return (Icon)ret;
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

    public Path getFile() {
        return file;
    }

    public URL getFileUrl() {
        return fileUrl;
    }
    
}
