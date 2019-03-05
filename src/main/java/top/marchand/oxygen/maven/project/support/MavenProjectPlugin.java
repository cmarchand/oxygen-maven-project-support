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

import java.util.List;
import ro.sync.exml.plugin.PluginDescriptor;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;



/**
 *
 * @author cmarchand
 */
public class MavenProjectPlugin extends ro.sync.exml.plugin.Plugin {
    private static MavenProjectPlugin INSTANCE;
    private final PluginDescriptor pluginDescriptor;
    private List<Object> dependencies;
    
    public MavenProjectPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        if(INSTANCE!=null) throw new IllegalAccessError("MaveProjectPlugin has already been constructed");
        INSTANCE=this;
        this.pluginDescriptor = descriptor;
    }
    
    public static MavenProjectPlugin getInstance() {
        if(INSTANCE==null) throw new IllegalStateException("MavenProjectPlugin has not been constructed");
        return INSTANCE;
    }
    
    public PluginDescriptor getPluginDescriptor() {
        return pluginDescriptor;
    }
    
    public void updateDependencies() {
        String projectDirectory = PluginWorkspaceProvider.getPluginWorkspace().getUtilAccess().expandEditorVariables("${pd}", null);
        
    }
    
}
