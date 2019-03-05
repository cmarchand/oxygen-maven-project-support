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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import org.apache.log4j.Logger;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import top.marchand.oxygen.maven.project.support.impl.nodes.AbstractMavenNode;
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
                    MavenProjectExplorer explorer = new MavenProjectExplorer(projectDir.toPath(), proc);
                    AbstractMavenNode node = explorer.explore(chkShowTargetDirs.isSelected());
                    localModel = new DefaultTreeModel(node);
                    LOGGER.debug("creating DependencyScanner");
                    return new DependencyScanner(pomFile.toPath());
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
        protected DependencyScanner(Path pomFile) {
            super();
            this.pomFile=pomFile;
        }

        @Override
        public String doInBackground() {
            LOGGER.info("Getting dependencies...");
            lblStatus.setText("Maven...");
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(pomFile.toFile());
            request.setOffline(true);
            request.setGoals(Collections.singletonList("dependency:tree"));
            request.setBatchMode(true);

            Invoker invoker = new DefaultInvoker();
            File mavenHome = new File(new File(System.getProperty("user.home")), "applications/apache-maven-3.6.0");
            invoker.setMavenHome(mavenHome);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStreamHandler psh = new PrintStreamHandler(ps, false);
            invoker.setOutputHandler(psh);
            invoker.setErrorHandler(psh);
            String output="";
            try {
                InvocationResult result = invoker.execute(request);
                ps.flush();
                output = baos.toString("UTF-8");
            } catch(MavenInvocationException | UnsupportedEncodingException ex) {
                LOGGER.error("while calling maven", ex);
            }
            for(DependencyEntry dep: untree(filter(output))) {
                LOGGER.debug(dep);
            }
            lblStatus.setText("");
            return "OK";
        }
        private List<DependencyEntry> untree(String input) {
            Pattern pattern = Pattern.compile("^[+-\\\\| ]*");
            List<DependencyEntry> ret = new ArrayList<>();
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

        @Override
        protected void done() {
            try {
                lblStatus.setText(get());
            } catch(Exception ex) {
                LOGGER.error(ex);
            }
        }
        
        private String filter(String output) {
            Pattern pattern = Pattern.compile("maven-dependency-plugin:[0-9]+\\.[0-9]+(\\.[0-9]+)?:tree");
            StringWriter sw = new StringWriter(output.length());
            PrintWriter writer = new PrintWriter(sw);
            BufferedReader reader = new BufferedReader(new StringReader(output));
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
                        writer.println(line);
                    }
                    line = reader.readLine();
                }
            } catch(IOException ex) {
                LOGGER.error("while filtering output", ex);
            }
            writer.flush();
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
    }
}
