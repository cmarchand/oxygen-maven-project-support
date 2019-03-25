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
package top.marchand.oxygen.maven.project.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import ro.sync.exml.plugin.PluginDescriptor;
import top.marchand.oxygen.maven.project.support.impl.DependencyStreamHandler;



/**
 * Main plugin class. Keep project dependencies.
 * @author cmarchand
 */
public class MavenProjectPlugin extends ro.sync.exml.plugin.Plugin {
    private static MavenProjectPlugin INSTANCE;
    private final PluginDescriptor pluginDescriptor;
    private final Map<String,String> mapping;
    
    public MavenProjectPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        if(INSTANCE!=null) throw new IllegalAccessError("MaveProjectPlugin has already been constructed");
        INSTANCE=this;
        this.pluginDescriptor = descriptor;
        mapping = new HashMap<>();
    }
    
    public static MavenProjectPlugin getInstance() {
        if(INSTANCE==null) throw new IllegalStateException("MavenProjectPlugin has not been constructed");
        return INSTANCE;
    }
    
    public PluginDescriptor getPluginDescriptor() {
        return pluginDescriptor;
    }
    
    public void setDependenciesMapping(Map<String,String> mapping) {
        this.mapping.clear();
        this.mapping.putAll(mapping);
    }
    
    /**
     * Returns a copy, to ensure nothing will be modified outside.
     * @return All project dependencies, and their URL in classpath
     */
    public Map<String,String> getDependenciesMapping() {
        HashMap<String,String> ret = new HashMap<>(mapping.size());
        ret.putAll(mapping);
        return ret;
    }
    
    public URL resolveDependencyUrl(URL u) {
        String sUrl = u.toString();
        String entry = getDependencyPrefix(sUrl);
        String then = sUrl.substring(entry.length()+1);
        String resolvedPrefix = mapping.get(entry);
        if(resolvedPrefix==null) return null;
        try {
            return new URL(resolvedPrefix+then);
        } catch(MalformedURLException ex) {
            return null;
        }
    }
    protected String getDependencyPrefix(String url) {
        int secondSlashPos = url.indexOf("/", DependencyStreamHandler.DEPENDENCY_PROTOCOL.length()+1);
        return url.substring(0, secondSlashPos);
    }
}
