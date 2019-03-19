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
package top.marchand.oxygen.maven.project.support.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.apache.log4j.Logger;
import top.marchand.oxygen.maven.project.support.MavenProjectPlugin;

/**
 *
 * @author cmarchand
 */
public class DependencyStreamHandler extends URLStreamHandler {
    private static final Logger LOGGER = Logger.getLogger(DependencyStreamHandler.class);
    public static final String DEPENDENCY_PROTOCOL = "dependency:/";
    
    public DependencyStreamHandler() {
        super();
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        URL resolvedUrl = MavenProjectPlugin.getInstance().resolveDependencyUrl(u);
        if(resolvedUrl==null) {
            LOGGER.debug("openConnection("+u+") -> null");
            return null;
        } else {
            LOGGER.debug("openConnection("+u+") -> "+resolvedUrl.toString());
            return new DependencyUrlConnection(u, resolvedUrl);
        }
    }
    
    private class DependencyUrlConnection extends URLConnection {

        private final URL resolvedUrl;

        public DependencyUrlConnection(URL url, URL resolvedUrl) {
            super(url);
            this.resolvedUrl = resolvedUrl;
        }

        @Override
        public void connect() throws IOException {
            // nothing to do
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return null;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            LOGGER.debug("getInputStream("+resolvedUrl.toExternalForm()+")");
            return resolvedUrl.openStream();
        }
    }
}
