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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import ro.sync.exml.plugin.option.OptionPagePluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import top.marchand.oxygen.maven.project.support.impl.MavenOptionView;

/**
 *
 * @author cmarchand
 */
public class MavenOptionsPage extends OptionPagePluginExtension {
    public static final String OPTION_PREFIX = "top.marchand.maven";
    public static final String OPTION_INSTALL_DIR = OPTION_PREFIX+".install.dir";
    public static final String DEFAULT_MAVEN_INSTALL_DIR = "";
    public static final String DEFAULT_REPO_LOCATION = "${home}/.m2/repository/";
    public static MavenOptionsPage INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(MavenOptionsPage.class);
    private MavenOptionView view;
    
    public MavenOptionsPage() {
        super();
        if(INSTANCE!=null) {
            LOGGER.warn("Multi instances of MavenOptionPage created");
        }
        INSTANCE = this;
    }
    
    @Override
    public void apply(PluginWorkspace pluginWorkspace) {
        pluginWorkspace.getOptionsStorage().setOption(OPTION_INSTALL_DIR, view.getMavenInstallDir());
    }

    @Override
    public void restoreDefaults() {
        view.setMavenInstallDir(DEFAULT_MAVEN_INSTALL_DIR);
    }

    @Override
    public String getTitle() {
        return "Maven";
    }

    @Override
    public JComponent init(PluginWorkspace pluginWorkspace) {
        if(view==null) {
            view = new MavenOptionView(pluginWorkspace);
        }
        view.setMavenInstallDir(pluginWorkspace.getOptionsStorage().getOption(OPTION_INSTALL_DIR, DEFAULT_MAVEN_INSTALL_DIR));
        return view;
    }

    /**
     * Returns the Maven installation directory, as configured in options
     * @return MAveninstallation directory
     */
    public String getMavenInstallDir() {
        return expand(PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage().getOption(OPTION_INSTALL_DIR, ""));
    }
    
    private String expand(String s) {
        return PluginWorkspaceProvider.getPluginWorkspace().getUtilAccess().expandEditorVariables(s, null);
    }
    
}
