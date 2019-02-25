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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 *
 * @author cmarchand
 */
public class MavenDirectoryNode extends AbstractMavenParentNode {
    private final String value;
    private final Path directory;
    private final HashMap<String,MavenPackageNode> packages;
    private static final List<String> VALID_PARENT_DIRS = Arrays.asList("main","site","test");
    
    public MavenDirectoryNode(Path directory) {
        super();
        this.directory=directory;
        value=directory.getName(directory.getNameCount()-1).toString();
        packages = new HashMap<>();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Icon getIcon() {
        if(getParent()!=null && VALID_PARENT_DIRS.contains(getParent().getValue())) {
            switch(getValue().toLowerCase()) {
//                case "java": return ImageHandler.getInstance().get(ImageHandler.JAVA_ICON);
                case "ant": return ImageHandler.getInstance().get(ImageHandler.ANT_ICON);
                case "css": return ImageHandler.getInstance().get(ImageHandler.CSS_ICON);
                case "dtd": return ImageHandler.getInstance().get(ImageHandler.DTD_ICON);
                case "epub": return ImageHandler.getInstance().get(ImageHandler.EPUB_ICON);
                case "fo": return ImageHandler.getInstance().get(ImageHandler.FO_ICON);
                case "html": return ImageHandler.getInstance().get(ImageHandler.HTML_ICON);
                case "js":
                case "javascript": return ImageHandler.getInstance().get(ImageHandler.JS_ICON);
                case "json": return ImageHandler.getInstance().get(ImageHandler.JSON_ICON);
                case "md": return ImageHandler.getInstance().get(ImageHandler.MD_ICON);
                case "nvdl": return ImageHandler.getInstance().get(ImageHandler.NVDL_ICON);
                case "rnc": return ImageHandler.getInstance().get(ImageHandler.RNC_ICON);
                case "grammars":
                case "rng": return ImageHandler.getInstance().get(ImageHandler.RNG_ICON);
                case "sch": return ImageHandler.getInstance().get(ImageHandler.SCH_ICON);
                case "sql": return ImageHandler.getInstance().get(ImageHandler.SQL_ICON);
                case "wsdl": return ImageHandler.getInstance().get(ImageHandler.WSDL_ICON);
                case "xproc": return ImageHandler.getInstance().get(ImageHandler.XPROC_ICON);
                case "xspec": return ImageHandler.getInstance().get(ImageHandler.XSPEC_ICON);
                case "xsl": return ImageHandler.getInstance().get(ImageHandler.XSL_ICON);
            }
        }
        if(isLeaf()) {
            // change default icon to closed folder icon.
            return (Icon)(UIManager.get("Tree.closedIcon"));
        }
        return null;
    }

    public Path getDirectory() {
        return directory;
    }
    
    public void addPackageEntry(String packageName, Path file) {
        MavenPackageNode pack = packages.get(packageName);
        if(pack==null) {
            pack = new MavenPackageNode(packageName);
            packages.put(packageName, pack);
            appendChild(pack);
        }
        pack.appendChild(new MavenFileNode(file));
    }

}
