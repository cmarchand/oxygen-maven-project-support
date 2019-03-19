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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import org.apache.log4j.Logger;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.actions.MenusAndToolbarsContributorCustomizer;
import top.marchand.oxygen.maven.project.support.MavenOptionsPage;
import top.marchand.oxygen.maven.project.support.MavenProjectPlugin;
import top.marchand.oxygen.maven.project.support.impl.nodes.AbstractMavenNode;
import top.marchand.oxygen.maven.project.support.impl.nodes.ImageHandler;
import top.marchand.oxygen.maven.project.support.impl.nodes.MavenFileNode;

/**
 * Maven view of Project
 * @author cmarchand
 */
public class MavenProjectView extends javax.swing.JPanel {
    private TreeModel model;
    private static final Logger LOGGER = Logger.getLogger(MavenProjectView.class);
    private final Processor proc = new Processor(Configuration.newConfiguration());
    private final StandalonePluginWorkspace pluginWorkspaceAccess;
    private File currentProjectDir;

    /**
     * Creates new form MavenProjectView
     * @param pluginWorkspaceAccess The plugin workspace access, required to open
     * files in editor.
     */
    public MavenProjectView(StandalonePluginWorkspace pluginWorkspaceAccess) {
        super();
        this.pluginWorkspaceAccess=pluginWorkspaceAccess;
        model = new DefaultTreeModel(new EmptyTreeNode());
        initComponents();
        tree.setCellRenderer(new MavenTreeCellRenderer());
        loadModel(getProjectDir());
        addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if("ancestor".equals(evt.getPropertyName())) {
                if(
                        (evt.getOldValue()!=null && !evt.getOldValue().equals(evt.getNewValue())) ||
                        (evt.getNewValue()!=null && !evt.getNewValue().equals(evt.getOldValue()))) {
                    File projectDir = getProjectDir();
                    if(currentProjectDir==null || !currentProjectDir.equals(projectDir)) {
                        loadModel(projectDir);
                    }
                }
            }
        });
        pluginWorkspaceAccess.addMenusAndToolbarsContributorCustomizer(new MenusAndToolbarsContributorCustomizer() {
            @Override
            public void customizeTextPopUpMenu(JPopupMenu popUp, WSTextEditorPage textPage) {
                LOGGER.debug("customizeTextPopupMenu");
//                super.customizeTextPopUpMenu(popUp, textPage);
//                LOGGER.debug("\tcarret position: "+textPage.getCaretOffset());
                Class clazz = textPage.getClass();
                if(textPage.getCaretOffset()<6) return;
                try {
                    Method mGetDocument = clazz.getMethod("getDocument", new Class[0]);
                    Object ret = mGetDocument.invoke(textPage,new Object[0]);
                    javax.swing.text.Document doc = (javax.swing.text.Document)ret;
                    String before = doc.getText(textPage.getCaretOffset()-6, 6);
                    String after = doc.getText(textPage.getCaretOffset(), 1);
                    if("\"".equals(after) || "'".equals(after)) {
                        if("href=\"".equals(before) || "href='".equals(before)) {
                            popUp.insert(new AbstractAction("Dependency resource...", ImageHandler.getInstance().get(ImageHandler.MAVEN_ICON)) {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    DlgChooseDependencyResource dlg = new DlgChooseDependencyResource(SwingUtilities.getWindowAncestor(MavenProjectView.this));
                                    String url = dlg.getDependencyUrl();
                                    if(url!=null) {
                                        try {
                                            doc.insertString(textPage.getCaretOffset(), url, null);
                                        } catch(BadLocationException ex) {
                                            LOGGER.error("Inserting at wrong position:", ex);
                                        }
                                    }
                                }
                            }, 0);
                            popUp.insert(new JPopupMenu.Separator(), 1);
                        }
                    }
                } catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | BadLocationException ex) {
                    LOGGER.error("while invoking getDocument()", ex);
                }
            }
            
        });
    }
    
    private void loadModel(File projectDir) {
        SwingWorker<SwingWorker<String,Integer>, Integer>  worker = new SwingWorker<SwingWorker<String,Integer>, Integer>() {
            private TreeModel localModel;
            @Override
            public SwingWorker<String,Integer> doInBackground() {
                lblStatus.setText("Project loading...");
                File pomFile = new File(projectDir, "pom.xml");
                if(!pomFile.exists()) {
                    localModel = new DefaultTreeModel(new EmptyTreeNode("Not a maven project"));
                    return null;
                } else {
                    MavenOptionsPage optionPage = MavenOptionsPage.INSTANCE;
                    MavenProjectExplorer explorer = new MavenProjectExplorer(projectDir.toPath(), proc);
                    AbstractMavenNode node = explorer.explore(chkShowTargetDirs.isSelected());
                    localModel = new DefaultTreeModel(node);
                    LOGGER.debug("creating DependencyScanner");
                    if("".equals(optionPage.getMavenInstallDir())) {
                        JOptionPane.showMessageDialog(MavenProjectView.this, "Maven installation directory is not defined\nYou have to define it in preferences page.", "Maven not configured", JOptionPane.WARNING_MESSAGE);
                        return null;
                    }
                    LOGGER.debug("Maven install dir: "+optionPage.getMavenInstallDir());
                    LOGGER.debug("Local repository: "+optionPage.getMavenLocalRepositoryLocation());
                    try {
                        return new DependencyScanner(pomFile.toPath(), new File(new URI(optionPage.getMavenInstallDir())), optionPage.getMavenLocalRepositoryLocation());
                    } catch(URISyntaxException ex) {
                        return null;
                    }
                }
            }

            @Override
            protected void done() {
                LOGGER.debug("installing model");
                tree.setModel(localModel);
                lblStatus.setText("");
                currentProjectDir = projectDir;
                try {
                    SwingWorker<String,Integer> worker = get();
                    if(worker!=null) {
                        LOGGER.debug("submitting scanner");
                        worker.execute();
                    } else {
                        LOGGER.debug("no post-task to execute");
                    }
                } catch(InterruptedException | ExecutionException ex) {
                    LOGGER.error("Interrupted...", ex);
                }
            }
            
        };
        worker.execute();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tree = new MavenJTree();
        chkShowTargetDirs = new javax.swing.JCheckBox();
        lblStatus = new javax.swing.JLabel();

        tree.setModel(model);
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMouseClicked(evt);
            }
        });
        tree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(tree);

        chkShowTargetDirs.setText("Show target/ dirs");
        chkShowTargetDirs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowTargetDirsActionPerformed(evt);
            }
        });

        lblStatus.setFont(new java.awt.Font("Lucida Grande", 2, 13)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(chkShowTargetDirs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkShowTargetDirs)
                    .addComponent(lblStatus)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chkShowTargetDirsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowTargetDirsActionPerformed
        loadModel(getProjectDir());
    }//GEN-LAST:event_chkShowTargetDirsActionPerformed

    private void treeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseClicked
        // trying to open file, if it's a file
        if(evt.getClickCount()==2) {
            TreePath tp = tree.getClosestPathForLocation(evt.getX(), evt.getY());
            Object o = tp.getLastPathComponent();
            if(o instanceof MavenFileNode) {
                MavenFileNode node = (MavenFileNode)o;
                pluginWorkspaceAccess.open(node.getFileUrl());
            }
        }
    }//GEN-LAST:event_treeMouseClicked

    private void treeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER) {
            for(TreePath tp: tree.getSelectionPaths()) {
                Object o = tp.getLastPathComponent();
                if(o instanceof MavenFileNode) {
                    MavenFileNode node = (MavenFileNode)o;
                    pluginWorkspaceAccess.open(node.getFileUrl());
                }
            }
        }
    }//GEN-LAST:event_treeKeyPressed

    protected final File getProjectDir() {
        String projectDirectory = PluginWorkspaceProvider.getPluginWorkspace().getUtilAccess().expandEditorVariables("${pd}", null);
        return new File(projectDirectory);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkShowTargetDirs;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables

    private class EmptyTreeNode implements TreeNode {
        private final String value;
        public EmptyTreeNode() {
            this("EMPTY");
        }
        public EmptyTreeNode(String value) {
            super();
            this.value=value;
        }
        @Override
        public TreeNode getChildAt(int childIndex) {
            return null;
        }
        @Override
        public int getChildCount() {
            return 0;
        }
        @Override
        public TreeNode getParent() {
            return null;
        }
        @Override
        public int getIndex(TreeNode node) {
            return -1;
        }
        @Override
        public boolean getAllowsChildren() {
            return false;
        }
        @Override
        public boolean isLeaf() {
            return true;
        }
        @Override
        public Enumeration children() {
            return Collections.emptyEnumeration();
        }
        public String getValue() { return value; }

        @Override
        public String toString() {
            return getValue();
        }
    }
    
    private class MavenJTree extends JTree {
        public MavenJTree() {
            super();
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        @Override
        public String getToolTipText(MouseEvent event) {
            TreePath tp = getClosestPathForLocation(event.getX(), event.getY());
            Object o = tp.getLastPathComponent();
            if(o instanceof AbstractMavenNode) {
                AbstractMavenNode node = (AbstractMavenNode)o;
                // there is an important chance that renderer is outside of viewport
                return node.getValue();
            }
            return super.getToolTipText(event);
        }
    }
    
    protected class DependencyScanner extends SwingWorker<String, Integer> {

        private final Path pomFile;
        private String repositoryUrl=null;
        private final File mavenInstallDir;
        private final File localRepository;
        protected DependencyScanner(Path pomFile, File mavenInstallDir, File localRepository) {
            super();
            this.pomFile=pomFile;
            this.mavenInstallDir=mavenInstallDir;
            this.localRepository=localRepository;
        }

        @Override
        public String doInBackground() {
            LOGGER.info("Getting dependencies...");
            lblStatus.setText("Deps...");
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(pomFile.toFile());
            request.setOffline(true);
            request.setGoals(Arrays.asList("clean compile"));   // dependency:tree
            // -B
            request.setBatchMode(true);
            // -e
            request.setShowErrors(true);
            // -X
            request.setDebug(true);
            request.setMavenOpts("-Dmaven.test.skip=true");

            Invoker invoker = new DefaultInvoker();
//            File mavenHome = new File(new File(System.getProperty("user.home")), "applications/apache-maven-3.6.0");
            invoker.setMavenHome(mavenInstallDir);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStreamHandler psh = new PrintStreamHandler(ps, false);
            invoker.setOutputHandler(psh);
            invoker.setErrorHandler(psh);
            String output="";
            try {
                InvocationResult result = invoker.execute(request);
                ps.flush();
                if(result.getExitCode()==0) {
                    output = baos.toString("UTF-8");
                    LOGGER.info("Maven terminated");
                } else {
                    Exception ex = result.getExecutionException();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    JOptionPane.showMessageDialog(MavenProjectView.this, sw.toString(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                }
            } catch(MavenInvocationException | UnsupportedEncodingException ex) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    JOptionPane.showMessageDialog(MavenProjectView.this, sw.toString(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
            if(output.length()>0) {
                List<String> classpath = filterClasspath(output);
                Map<String,String> artifactsMapping = new HashMap<>();
                classpath.forEach((s) -> {
                    Map.Entry<String,String> entry = getArtifactMapping(s);
                    if(entry!=null) {
                        artifactsMapping.put(entry.getKey(), entry.getValue());
                    }
                });
//                for(Map.Entry entry: artifactsMapping.entrySet()) {
//                    LOGGER.debug(entry.getKey()+"="+entry.getValue());
//                }
                MavenProjectPlugin.getInstance().setDependenciesMapping(artifactsMapping);
            }
//            String filtered = filter(output);
//            untree(filtered).forEach((dep) -> {
//                LOGGER.debug(dep);
//            });
            lblStatus.setText("");
            return "OK";
        }
        private Collection<DependencyEntry> untree(final String input) {
            Pattern pattern = Pattern.compile("^[+-\\\\| ]*");
            Collection<DependencyEntry> ret = new HashSet<>();
            BufferedReader reader = new BufferedReader(new StringReader(input));
            try {
                String line=reader.readLine();
                while(line!=null) {
                    if(line.startsWith("[INFO] ")) {
                        line = line.substring(7);
                    }
                    if(line.trim().length()==0) continue;
                    Matcher m = pattern.matcher(line);
                    if(m.find()) {
                        String prefix = m.group();
                        DependencyEntry dep = new DependencyEntry((prefix.length() / 3),line.substring(prefix.length()));
                        ret.add(dep);
                    }
                    line = reader.readLine();
                }
            } catch(IOException ex) {
                LOGGER.error("while processing tree", ex);
            }
            return ret;
        }
        private List<String> filterClasspath(final String input) {
            TreeSet<String> set = new TreeSet<>();
            BufferedReader reader = new BufferedReader(new StringReader(input));
            try {
                String line=reader.readLine();
                while(line!=null) {
                    if(line.contains("classpathElements")) {
                        //LOGGER.debug("classpathElements found");
                        if(!line.contains("project.compileClasspathElements")) {
                            //LOGGER.debug("no project in");
                            if(line.startsWith("[DEBUG]")) {
                                line = line.substring("DEBUG]".length()+1).trim();
                            }
                            //LOGGER.debug("Line is ##"+line.substring(0, 50));
                            line = line.substring(line.indexOf("[")+1);
                            //LOGGER.debug("removes  before [: "+line.substring(0, 30)+"...");
                            line = line.substring(0, line.length()-1);
                            String[] entries = line.split(", ");
                            LOGGER.debug("found "+entries.length+" entries");
                            set.addAll(Arrays.asList(entries));
                        }
                    }
                    line = reader.readLine();
                }
            } catch(IOException ex) {
                LOGGER.error("while filtering classpath", ex);
            }
            List<String> ret = new ArrayList<>(set.size());
            ret.addAll(set);
            return ret;
        }
        @Override
        protected void done() {
            try {
                lblStatus.setText(get());
            } catch(InterruptedException | ExecutionException ex) {
                LOGGER.error(ex);
            }
            LOGGER.info("repository Url: "+repositoryUrl);
        }
        private Map.Entry<String,String> getArtifactMapping(String classpathEntry) {
            File f = new File(classpathEntry);
            if(f.isDirectory()) {
                // is it a /target/classes entry ?
                if(classpathEntry.endsWith("/target/classes")) {
                    File pomFile = new File(f.getParentFile().getParentFile(),"pom.xml");
                    try {
                        XdmNode node = proc.newDocumentBuilder().build(pomFile);
                        XPathCompiler comp = proc.newXPathCompiler();
                        comp.declareNamespace("pom", "http://maven.apache.org/POM/4.0.0");
                        XPathSelector sel = comp.compile("/pom:project/pom:artifactId/text()").load();
                        sel.setContextItem(node);
                        String artifactId = sel.evaluateSingle().getStringValue();
                        sel = comp.compile("(/pom:project/pom:groupId/text(), /pom:project/pom:parent/pom:groupId/text())[1]").load();
                        sel.setContextItem(node);
                        String groupId = sel.evaluateSingle().getStringValue();
                        String entry = "dependency:/"+groupId+"+"+artifactId;
                        String path = classpathEntry.replaceAll("\\\\", "/");
                        path = "file:"+(path.startsWith("/")?"":"/")+path+"/";
                        return new AbstractMap.SimpleEntry<>(entry, path);
                    } catch(SaxonApiException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                        return null;
                    }
                } else {
                    return null;
                }
            } else if(classpathEntry.endsWith(".jar")) {
                try {
                    JarFile jar = new JarFile(f);
                    for(Enumeration<JarEntry> enumer = jar.entries(); enumer.hasMoreElements();) {
                        JarEntry entry = enumer.nextElement();
                        if(entry.getName().endsWith("/pom.xml")) {
                            String[] els = entry.getName().split("/");
                            String entryName = "dependency:/"+els[2]+"+"+els[3];
                            URL url = pluginWorkspaceAccess.getUtilAccess().convertFileToURL(f);
                            return new AbstractMap.SimpleEntry<>(entryName, "zip:"+url.toString()+"!/");
                        }
                    }
                    return null;
                } catch(IOException ex) {
                    LOGGER.error("while extracting pom from "+f.getAbsolutePath(), ex);
                    return null;
                }
            } else {
                return null;
            }
        }
        private String filter(final String input) {
            Pattern pattern = Pattern.compile("maven-dependency-plugin:[0-9]+\\.[0-9]+(\\.[0-9]+)?:tree");
            StringWriter sw = new StringWriter();
            try (PrintWriter writer = new PrintWriter(sw)) {
                BufferedReader reader = new BufferedReader(new StringReader(input));
                try {
                    String line = reader.readLine();
                    boolean inTree = false;
                    while(line!=null) {
                        if(!inTree) {
                            Matcher m = pattern.matcher(line);
                            if(m.find()) {
                                inTree = true;
                            }
                        } else if(inTree && ("[INFO]".equals(line.trim()) || line.startsWith("[INFO] ---------------------"))) {
                            inTree = false;
                        } else if(inTree) {
                            if(line.startsWith("[INFO]")) {
                                writer.println(line);
                            } else if(line.startsWith("      url:") && repositoryUrl==null) {
                                repositoryUrl = line.substring("      url:".length());
                            }
                        }
                        line = reader.readLine();
                    }
                } catch(IOException ex) {
                    LOGGER.error("while filtering output", ex);
                }
                writer.flush();
            }
            return sw.toString();
        }
    }

    private class DependencyEntry {
        private final int level;
        private final String artifactCoordinate;
        public DependencyEntry(int level, String artifact) {
            super();
            this.level=level;
            this.artifactCoordinate=artifact;
        }
        public int getLevel() { return level; }
        public String getArtifactCoordinate() { return artifactCoordinate; }
        @Override
        public String toString() {
            return artifactCoordinate+" ("+level+")";
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof DependencyEntry) {
                DependencyEntry other = (DependencyEntry)obj;
                return getArtifactCoordinate().equals(other.getArtifactCoordinate());
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + Objects.hashCode(this.artifactCoordinate);
            return hash;
        }
        
    }
}
