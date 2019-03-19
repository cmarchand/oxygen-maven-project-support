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

import java.net.URL;
import java.net.URLStreamHandler;
import ro.sync.exml.plugin.lock.LockHandler;
import ro.sync.exml.plugin.urlstreamhandler.URLHandlerReadOnlyCheckerExtension;
import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerPluginExtension;
import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension;
import top.marchand.oxygen.maven.project.support.impl.DependencyStreamHandler;

/**
 *
 * @author cmarchand
 */
public class DependencyProtocolHandlerExtension implements URLStreamHandlerWithLockPluginExtension, URLHandlerReadOnlyCheckerExtension {

    public DependencyProtocolHandlerExtension() {
        super();
    }

    
    @Override
    public URLStreamHandler getURLStreamHandler(String protocol) {
        if(DependencyStreamHandler.DEPENDENCY_PROTOCOL.startsWith(protocol)) {
            return new DependencyStreamHandler();
        } else return null;
    }

    @Override
    public LockHandler getLockHandler() {
        return null;
    }

    @Override
    public boolean isLockingSupported(String protocol) {
        return false;
    }

    @Override
    public boolean isReadOnly(URL url) {
        return url.toString().startsWith("dependency:/");
    }

    @Override
    public boolean canCheckReadOnly(String protocol) {
        return true;
    }
    
}
