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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import org.apache.log4j.Logger;
import top.marchand.oxygen.maven.project.support.impl.nodes.AbstractMavenNode;
import top.marchand.oxygen.maven.project.support.impl.nodes.MavenDirectoryNode;
import top.marchand.oxygen.maven.project.support.impl.nodes.MavenProjectNode;

/**
 *
 * @author cmarchand
 */
public class MavenProjectExplorer {
    private static final Logger LOGGER = Logger.getLogger(MavenProjectExplorer.class);
    private final Path directory;
    private final Processor proc;
    private static final List<String> EXCLUDE_DIRS = Arrays.asList(".git", ".project", ".svn");
    private static final List<String> EXCLUDE_FILES = Arrays.asList(".gitignore", ".DS_Store");
    
    public MavenProjectExplorer(Path directory, Processor proc) {
        super();
        this.directory=directory;
        this.proc=proc;
    }
    
    /**
     * Constructs the project based on directory
     * @param includeTargetDirectory
     * @return 
     */
    public AbstractMavenNode explore(boolean includeTargetDirectory) {
        try {
            MavenFileVisitor visitor = new MavenFileVisitor(directory, includeTargetDirectory);
            Files.walkFileTree(directory, visitor);
            return visitor.getRootNode();
        } catch(IOException ex) {
            // TODO
        }
        return null;
    }
    
    private class MavenFileVisitor implements FileVisitor<Path> {
        private final Stack<MavenDirectoryNode> stack;
        private final boolean includeTargetDirectory;
        private final Path root;
        private static final String SRC = "src";
        private MavenProjectNode rootNode;
        
        public MavenFileVisitor(Path root, boolean includeTargetDirectory) {
            super();
            this.includeTargetDirectory=includeTargetDirectory;
            this.root=root;
            stack = new Stack<>();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path relative = root.relativize(dir);
            LOGGER.debug("preVisitDirectory("+relative.toString()+")");
            if(root.equals(dir)) {
                MavenProjectNode node = new MavenProjectNode(getProjectName(dir.resolve("pom.xml").toFile()));
                rootNode = node;
                return FileVisitResult.CONTINUE;
            } else if(isTraversable(relative)) {
                Path pom = dir.resolve("pom.xml");
                if(Files.isRegularFile(pom)) {
                    // This is a maven project, run a new explorer...
                    MavenProjectExplorer explorer = new MavenProjectExplorer(dir, proc);
                    // TODO: refactor
                    if(stack.isEmpty()) {
                        rootNode.appendChild(explorer.explore(includeTargetDirectory));
                    } else {
                        stack.peek().appendChild(explorer.explore(includeTargetDirectory));
                    }
                    return FileVisitResult.SKIP_SUBTREE;
                } else {
                    // Normal directory
                    if((relative.startsWith(SRC) && stack.size()<3) || !relative.startsWith(SRC)) {
                        MavenDirectoryNode node = new MavenDirectoryNode(dir);
                        if(stack.isEmpty()) {
                            rootNode.appendChild(node);
                        } else {
                            stack.peek().appendChild(node);
                        }
                        stack.push(node);
                    }
                    return FileVisitResult.CONTINUE;
                }
            } else {
                return FileVisitResult.SKIP_SUBTREE;
            }
        }
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if(exc!=null) throw exc;
            Path relative = root.relativize(dir);
            LOGGER.debug("preVisitDirectory("+relative.toString()+")");
            if(root.equals(dir)) {
                // terminated
            } else if(isTraversable(relative)) {
                // it can not be a maven diretory, it has been skipped
                if((relative.startsWith(SRC) && relative.getNameCount()<=3) || !relative.startsWith(SRC)) {
                    stack.pop();
                }
            }
            return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path relative = root.relativize(file);
            LOGGER.debug("Visiting "+relative.toString());
            if(isFileAcceptable(file) && relative.getNameCount()>3 && relative.startsWith(SRC)) {
                String packageName=null;
                if(relative.getNameCount()>4) {
                    Path packagePath = relative.subpath(3, relative.getNameCount()-1);
                    LOGGER.debug("Package Path: "+packagePath.toString());
                    packageName = packagePath.toString().replaceAll("[/\\\\]", ".");
                    LOGGER.debug("Package name: "+packageName);
                } else {
                    packageName = "<default>";
                }
                MavenDirectoryNode node = (MavenDirectoryNode)stack.peek();
                node.addPackageEntry(packageName, file);
            }
            return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if(exc!=null) throw exc;
            return FileVisitResult.CONTINUE;
        }
        
        private boolean isTraversable(Path dir) {
            if(EXCLUDE_DIRS.contains(dir.getFileName().toString())) return false;
            if(includeTargetDirectory) return true;
            return !"target".equals(dir.getFileName().toString());
        }
        private boolean isFileAcceptable(Path file) {
            return !EXCLUDE_FILES.contains(file.getFileName().toString());
        }
        protected MavenProjectNode getRootNode() { return rootNode; }
    }
    
    protected String getProjectName(File pomFile) {
        try {
            return getProjectName(parsePomFile(pomFile));
        } catch(SaxonApiException ex) {
            LOGGER.error("while getting projectName from "+pomFile.getAbsolutePath(), ex);
            return "UNKNOWN";
        }
    }
    private XdmNode parsePomFile(File pomFile) throws SaxonApiException {
        DocumentBuilder builder = proc.newDocumentBuilder();
        return builder.build(pomFile);
    }
    
    private String getProjectName(XdmNode pom) throws SaxonApiException {
        XPathCompiler comp = proc.newXPathCompiler();
        comp.declareNamespace("pom", "http://maven.apache.org/POM/4.0.0");
        XPathSelector xs =comp.compile("/pom:project/(pom:name|pom:artifactId)[1]/text()").load();
        xs.setContextItem(pom);
        return xs.evaluateSingle().getStringValue();
    }
 }
