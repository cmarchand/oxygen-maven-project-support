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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 *
 * @author cmarchand
 */
public class ImageHandler {
    private static final String PACKAGE_BASE = "/top/marchand/oxygen/icons/";
    public static final String MAVEN_ICON = "Maven2Icon.gif";
    private static ImageHandler INSTANCE;
    
    private final Map<String,ImageIcon> cache;
    
    private ImageHandler() {
        super();
        cache = new HashMap<>();
    }
    
    public static ImageHandler getInstance() {
        if(INSTANCE==null) {
            INSTANCE = new ImageHandler();
        }
        return INSTANCE;
    }

    /**
     * Returns the image denoted by its id. <tt>imageId</tt> must be a constant
     * defined in this class as <tt>*_ICON</tt>.
     * @param imageId
     * @return the image found, or <tt>null</tt> if not found.
     */
    public ImageIcon get(String imageId) {
        ImageIcon ret = cache.get(imageId);
        if(ret==null) {
            URL url = this.getClass().getResource(PACKAGE_BASE+imageId);
            if(url!=null) {
                ret = new ImageIcon(url);
                cache.put(imageId, ret);
            }
        }
        return ret;
    }
    
}
