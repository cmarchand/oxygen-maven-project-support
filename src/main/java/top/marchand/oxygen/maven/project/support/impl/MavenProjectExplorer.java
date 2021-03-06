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
import top.marchand.oxygen.maven.project.support.impl.nodes.AbstractMavenParentNode;
import top.marchand.oxygen.maven.project.support.impl.nodes.MavenDirectoryNode;
import top.marchand.oxygen.maven.project.support.impl.nodes.MavenFileNode;
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
    
    // for updating
    private AbstractMavenNode nodeToUpdate;
    
    public MavenProjectExplorer(Path directory, Processor proc) {
        super();
        this.directory=directory;
        this.proc=proc;
    }
    
    public MavenProjectExplorer(Processor proc, AbstractMavenNode nodeToUpdate) {
        this(getDirectoryOfMavenNode(nodeToUpdate), proc);
        this.nodeToUpdate = nodeToUpdate;
//        LOGGER.debug("MavenProjectExplorer at "+directory.toString());
    }
    
    private static Path getDirectoryOfMavenNode(AbstractMavenNode node) {
        if(node instanceof MavenProjectNode) {
            MavenProjectNode mpn = (MavenProjectNode)node;
            return mpn.getProjectPath();
        } else {
            return getDirectoryOfMavenNode((AbstractMavenNode)node.getParent());
        }
    }
    
    /**
     * Constructs the project based on directory
     * @param includeTargetDirectory If <tt>target/</tt> directory must be included
     * @return A directory node, model of this maven directory
     */
    public AbstractMavenNode explore(boolean includeTargetDirectory) {
        try {
            MavenFileVisitor visitor = new MavenFileVisitor(directory, includeTargetDirectory);
            Files.walkFileTree(directory, visitor);
            return visitor.getRootNode();
        } catch(Throwable ex) {
            LOGGER.error("while exploring "+directory, ex);
        }
        return null;
    }
    
    public boolean update(boolean includeTargetDirectory) {
        try {
            Path startOfUpdatePath = ((MavenDirectoryNode)nodeToUpdate).getDirectory();
//            LOGGER.debug("update() startOfUpdatePath: "+startOfUpdatePath.toString());
            MavenFileVisitor visitor = new MavenFileVisitor(directory, includeTargetDirectory, nodeToUpdate);
            nodeToUpdate.removeAllChildren();
//            LOGGER.debug("updating from "+startOfUpdatePath.toString());
            Files.walkFileTree(startOfUpdatePath, visitor);
            return visitor.isPomUpdated();
        } catch(Throwable ex) {
            LOGGER.error("while exploring "+directory, ex);
            return false;
        }
    }
    
    private class MavenFileVisitor implements FileVisitor<Path> {
        private final Stack<MavenDirectoryNode> stack;
        private final boolean includeTargetDirectory;
        private final Path root;
        private static final String SRC = "src";
        private MavenProjectNode rootNode;
        private boolean pomUpdated;
        private boolean shouldIgnoreFirst = false;
        private String ignored = null;
        
        public MavenFileVisitor(Path root, boolean includeTargetDirectory) {
            super();
            this.includeTargetDirectory=includeTargetDirectory;
            this.root=root;
            stack = new Stack<>();
        }
        public MavenFileVisitor(Path root, boolean includeTargetDirectory, AbstractMavenNode nodeToUpdate) {
            this(root, includeTargetDirectory);
//            LOGGER.debug("MavenFileVisitor("+root.toString()+", "+nodeToUpdate.toString()+")");
            addToStack(nodeToUpdate);
//            LOGGER.debug("stack is "+stack.size()+" high");
            shouldIgnoreFirst = true;
        }
        private void addToStack(AbstractMavenNode node) {
//            LOGGER.debug("addToStack("+node.toString()+")");
            if(node==null) return;
            if(!(node instanceof MavenProjectNode)) {
                addToStack((AbstractMavenNode)node.getParent());
            }
            if(node instanceof MavenDirectoryNode ) { // || node instanceof MavenProjectNode
//                LOGGER.debug("stacking "+node.toString());
                stack.push((MavenDirectoryNode)node);
            } else {
//                LOGGER.warn("addToStack("+node.getClass().getName()+")");
            }
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path relative = root.relativize(dir);
//            LOGGER.debug("preVisitDirectory("+relative.toString()+")");
            if(root.equals(dir)) {
//                LOGGER.debug("starting maven project at "+dir.toString());
                Path pom = dir.resolve("pom.xml");
                MavenProjectNode node = new MavenProjectNode(getProjectName(pom.toFile()), dir);
                node.add(new MavenFileNode(pom));
                rootNode = node;
                pomUpdated=true;
                return FileVisitResult.CONTINUE;
            } else if(isTraversable(relative)) {
                Path pom = dir.resolve("pom.xml");
                if(Files.isRegularFile(pom)) {
//                    LOGGER.debug("found sub-maven project at "+dir.toString());
                    // This is a maven project, run a new explorer...
                    try {
                        MavenProjectExplorer explorer = new MavenProjectExplorer(dir, proc);
                        // TODO: refactor
                        if(stack.isEmpty()) {
                            rootNode.add(explorer.explore(includeTargetDirectory));
                        } else {
                            stack.peek().add(explorer.explore(includeTargetDirectory));
                        }
                        pomUpdated=true;
                        return FileVisitResult.SKIP_SUBTREE;
                    } catch(RuntimeException ex) {
                        // there are cases where it's not a valid maven project, fallback
                        if((relative.startsWith(SRC) && stack.size()<3) || !relative.startsWith(SRC)) {
                            MavenDirectoryNode node = new MavenDirectoryNode(dir);
                            if(stack.isEmpty()) {
                                rootNode.add(node);
                            } else {
                                stack.peek().add(node);
                            }
                            stack.push(node);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                } else {
//                    LOGGER.debug("found normal sub-dir at "+dir.toString());
                    if(shouldIgnoreFirst) {
//                        LOGGER.debug("\tignored.");
                        shouldIgnoreFirst=false;
                        ignored=dir.toString();
                        return FileVisitResult.CONTINUE;
                    }
                    // Normal directory
                    if((relative.startsWith(SRC) && stack.size()<3) || !relative.startsWith(SRC)) {
                        MavenDirectoryNode node = new MavenDirectoryNode(dir);
                        if(stack.isEmpty()) {
                            rootNode.add(node);
                        } else {
                            stack.peek().add(node);
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
//            LOGGER.debug("postVisitDirectory("+relative.toString()+")");
            if(root.equals(dir)) {
                // terminated
            } else if(isTraversable(relative)) {
                // it can not be a maven diretory, it has been skipped
                if((relative.startsWith(SRC) && relative.getNameCount()<=3) || !relative.startsWith(SRC)) {
                    if(!dir.toString().equals(ignored)) {
                        stack.pop();
                    }
                }
            }
            return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path relative = root.relativize(file);
//            LOGGER.debug("Visiting "+relative.toString());
            if(isFileAcceptable(file) && relative.getNameCount()>3 && relative.startsWith(SRC)) {
                String packageName=null;
                if(relative.getNameCount()>4) {
                    Path packagePath = relative.subpath(3, relative.getNameCount()-1);
//                    LOGGER.debug("Package Path: "+packagePath.toString());
                    packageName = packagePath.toString().replaceAll("[/\\\\]", ".");
//                    LOGGER.debug("Package name: "+packageName);
                } else {
                    packageName = "<default>";
                }
                MavenDirectoryNode node = (MavenDirectoryNode)stack.peek();
//                LOGGER.debug("Adding "+packageName+"."+file+" to "+node.toString());
                node.addPackageEntry(packageName, file);
            } else if(isFileAcceptable(file) && relative.startsWith(SRC)) {
                MavenDirectoryNode node = (MavenDirectoryNode)stack.peek();
                node.add(new MavenFileNode(file));
            } else if(isFileAcceptable(file) && !"pom.xml".equals(file.getFileName().toString())) {
                AbstractMavenParentNode parent = stack.isEmpty() ? rootNode : stack.peek();
                parent.add(new MavenFileNode(file));
            } else {
                LOGGER.debug("file skipped: "+relative.toString());
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
            // xspec compiled by oxygen. I do not want them
            if(dir.getFileName().toString().equals("xspec") && hasParent(dir, "xspec") && hasParent(dir, "src")) return false;
            if(includeTargetDirectory) return true;
            return !"target".equals(dir.getFileName().toString());
        }
        private boolean isFileAcceptable(Path file) {
            String filename = file.getFileName().toString();
            return !EXCLUDE_FILES.contains(filename) && !filename.endsWith(".xpr");
        }
        protected MavenProjectNode getRootNode() { return rootNode; }
        protected Path getRootPath() { return root; }
        protected boolean isPomUpdated() { return pomUpdated; }
    }
    
    protected String getProjectName(File pomFile) {
        LOGGER.debug("getting project name from "+pomFile.getAbsolutePath());
        try {
            return getProjectName(parsePomFile(pomFile));
        } catch(SaxonApiException ex) {
            LOGGER.error("while getting projectName from "+pomFile.getAbsolutePath(), ex);
            return "UNKNOWN";
        }
    }
    protected boolean hasParent(Path file, String parentName) {
        Path parent = file.getParent();
        while(parent!=null) {
            if(parentName.equals(parent.getFileName().toString())) return true;
            parent = parent.getParent();
        }
        return false;
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
