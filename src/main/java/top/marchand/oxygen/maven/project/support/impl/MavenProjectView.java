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
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import org.apache.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import top.marchand.oxygen.maven.project.support.impl.nodes.AbstractMavenNode;

/**
 * Maven view of Project
 * @author cmarchand
 */
public class MavenProjectView extends javax.swing.JPanel {
    private TreeModel model;
    private static final Logger LOGGER = Logger.getLogger(MavenProjectView.class);
    private final Processor proc = new Processor(Configuration.newConfiguration());

    /**
     * Creates new form MavenProjectView
     */
    public MavenProjectView() {
        super();
        model = new DefaultTreeModel(new EmptyTreeNode());
        initComponents();
        tree.setCellRenderer(new MavenTreeCellRenderer());
        loadModel(getProjectDir());
    }
    
    private void loadModel(File projectDir) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                lblStatus.setText("Project loading...");
                File pomFile = new File(projectDir, "pom.xml");
                if(!pomFile.exists()) {
                    model = new DefaultTreeModel(new EmptyTreeNode("Not a maven project"));
                } else {
                    MavenProjectExplorer explorer = new MavenProjectExplorer(projectDir.toPath(), proc);
                    AbstractMavenNode node = explorer.explore(chkShowTargetDirs.isSelected());
                    model = new DefaultTreeModel(node);
                }
                tree.setModel(model);
                lblStatus.setText("");
            }
        };
        SwingUtilities.invokeLater(r);
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
        tree = new javax.swing.JTree();
        chkShowTargetDirs = new javax.swing.JCheckBox();
        lblStatus = new javax.swing.JLabel();

        tree.setModel(model);
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

    private File getProjectDir() {
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
    
}