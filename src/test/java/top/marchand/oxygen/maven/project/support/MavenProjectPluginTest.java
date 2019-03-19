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

import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author cmarchand
 */
public class MavenProjectPluginTest {
    
    @Test
    public void getDependencyPrefixTest() {
        MavenProjectPlugin plugin = new MavenProjectPlugin(null);
        String url = "dependency:/eu.els.sie.mcqueen.publication.common.libraries+treatment/eu/els/sie/mcqueen/publication/common/treatment/xsl/info.xsl";
        assertEquals("dependency:/eu.els.sie.mcqueen.publication.common.libraries+treatment", MavenProjectPlugin.getInstance().getDependencyPrefix(url));
    }
    
}
