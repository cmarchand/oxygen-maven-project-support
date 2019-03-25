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
    public static final String ANT_ICON = "AntIcon16.png";
    public static final String CSS_ICON = "CssIcon16.png";
    public static final String DTD_ICON = "DtdIcon16.png";
    public static final String EPUB_ICON = "EpubIcon16.png";
    public static final String FO_ICON = "FoIcon16.png";
    public static final String GAULOIS_ICON = "gaulois2.png";
    public static final String HTML_ICON = "HtmlIcon16.png";
    public static final String JS_ICON = "JsIcon16.png";
    public static final String JSON_ICON = "JsonIcon16.png";
    public static final String MD_ICON = "MDIcon16.png";
    public static final String MAVEN_ICON = "Maven2Icon.gif";
    public static final String NVDL_ICON = "NVDLIcon16.png";
    public static final String PHP_ICON = "PhpIcon16.png";
    public static final String RNC_ICON = "RncIcon16.png";
    public static final String RNG_ICON = "RngIcon16.png";
    public static final String SCH_ICON = "SchIcon16.png";
    public static final String SQL_ICON = "SqlIcon16.png";
    public static final String WSDL_ICON = "WsdlIcon16.png";
    public static final String PACKAGE_ICON = "package.png";
    public static final String XPROC_ICON = "XProcIcon16.png";
    public static final String XSPEC_ICON = "XSpecIcon16.png";
    public static final String XSL_ICON = "XslIcon16.png";
    public static final String XQUERY_ICON = "";
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
     * @param imageId The image identifier, one of *_ICON constants.
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
