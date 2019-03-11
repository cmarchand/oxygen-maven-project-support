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

import javax.swing.ImageIcon;

/**
 *
 * @author cmarchand
 */
public class MavenPackageNode extends AbstractMavenParentNode {
    private String packageName;
    
    public MavenPackageNode(String packageName) {
        super();
        this.packageName=packageName;
    }

    @Override
    public String getValue() {
        return packageName;
    }

    @Override
    public ImageIcon getIcon() {
        return ImageHandler.getInstance().get(ImageHandler.PACKAGE_ICON);
    }
    
}
