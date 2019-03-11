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

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.Platform;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ViewInfo;
import top.marchand.oxygen.maven.project.support.impl.MavenProjectView;

/**
 *
 * @author cmarchand
 */
public class MavenProjectViewExtension implements WorkspaceAccessPluginExtension {

    @Override
    public void applicationStarted(StandalonePluginWorkspace pluginWorkspaceAccess) {
        if(pluginWorkspaceAccess.getPlatform().equals(Platform.WEBAPP)) return;
        pluginWorkspaceAccess.addViewComponentCustomizer((ViewInfo viewInfo) -> {
            if("top.marchand.oxygen.maven.project.view".equals(viewInfo.getViewID())) {
                viewInfo.setComponent(new MavenProjectView(pluginWorkspaceAccess));
                viewInfo.setTitle("Maven Project");
            }
        });
    }

    @Override
    public boolean applicationClosing() {
        return true;
    }
    
}
