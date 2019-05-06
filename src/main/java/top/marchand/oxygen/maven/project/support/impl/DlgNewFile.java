/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package top.marchand.oxygen.maven.project.support.impl;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import org.apache.log4j.Logger;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import top.marchand.oxygen.maven.project.support.MavenOptionsPage;
import top.marchand.oxygen.maven.project.support.impl.nodes.AbstractMavenParentNode;
import top.marchand.oxygen.maven.project.support.impl.nodes.HasIcon;
import top.marchand.oxygen.maven.project.support.impl.nodes.ImageHandler;
import top.marchand.oxygen.maven.project.support.impl.nodes.MavenFileNode;

/**
 *
 * @author cmarchand
 */
public class DlgNewFile extends javax.swing.JDialog {
//    private String result;
    private final FilteredTreeModel treeModel;
    private AbstractAction cancelAction = null, okAction = null;
    private static final Logger LOGGER = Logger.getLogger(DlgNewFile.class);
    
    private final DefaultTreeModel modelToUpdate;
    private final Path targetPath;
    private final AbstractMavenParentNode node;
    private final Processor proc;
    private final TreeSelectionModel selectionModel;
    
    private Template currentTemplate;
    /**
     * Creates new form DlgNewFile
     * @param owner Window
     * @param targetpath The targetd path
     * @param node The node to insert in
     * @param modelToUpdate The treeModel to update
     * @param proc Saxon proc to use
     */
    public DlgNewFile(Window owner, Path targetpath, AbstractMavenParentNode node, DefaultTreeModel modelToUpdate, Processor proc) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.targetPath=targetpath;
        this.node=node;
        this.modelToUpdate=modelToUpdate;
        this.proc = proc;
        this.selectionModel = new DefaultTreeSelectionModel();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        treeModel = new FilteredTreeModel(rootNode) {
            @Override
            public DefaultMutableTreeNode createRootNode() {
                return new DefaultMutableTreeNode();
            }
        };
        _initComponents();
    }

    private void _initComponents() {
        setTitle("New File...");
        initComponents();
        tree.setCellRenderer(new MavenTreeCellRenderer());
        setLocationRelativeTo(getParent());
        loadModel();
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setSelectionModel(selectionModel);
        tree.addTreeSelectionListener((TreeSelectionEvent e) -> {
            TreePath lead = e.getPath();
            if(lead!=null && tree.isPathSelected(lead)) {
                Object o = ((DefaultMutableTreeNode)(lead.getLastPathComponent())).getUserObject();
                if(o instanceof Template) {
                    currentTemplate = (Template)o;
                    String filename = currentTemplate.getFilename();
                    if(filename==null || filename.isEmpty()) {
                        filename="no-name."+currentTemplate.getExtension();
                    }
                    dfFilename.setText(filename);
                } else {
                    currentTemplate=null;
                }
            } else {
                dfFilename.setText("");
                currentTemplate=null;
            }
        });
        dfFilter.addCaretListener((CaretEvent e) -> {
            treeModel.filter(dfFilter.getText());
        });
    }
    
    protected void loadModel() {
        DefaultMutableTreeNode recentsNode = new TemplateContainerTreeNode("Recents", true);
        DefaultMutableTreeNode templatesNode = new TemplateContainerTreeNode("Templates", true);
        InputStream is = getClass().getResourceAsStream("/top/marchand/oxygen/templates/templates.xml");
        if(is==null) {
            LOGGER.error("templates.xml not found.");
            return;
        }
        try {
            XdmNode rootNode = proc.newDocumentBuilder().build(new StreamSource(is));
            XPathSelector xps = proc.newXPathCompiler().compile("/templates/template").load();
            HashMap<String,Template> templates = new HashMap<>();
            xps.setContextItem(rootNode);
            for(XdmSequenceIterator it = xps.evaluate().iterator(); it.hasNext(); ) {
                XdmNode tp = (XdmNode)it.next();
                Template template = new Template();
                XdmSequenceIterator childs = tp.axisIterator(Axis.CHILD);
                while( childs.hasNext() ) {
                    XdmNode child = (XdmNode)childs.next();
                    if(XdmNodeKind.ELEMENT.equals(child.getNodeKind())) {
                        switch(child.getNodeName().getLocalName()) {
                            case "name": { 
                                template.setName(child.getStringValue()); break; 
                            }
                            case "file": { 
                                template.setFile(child.getStringValue()); break; 
                            }
                            case "description": { 
                                template.setDescription(child.getStringValue()); break; 
                            }
                            case "extension": { 
                                template.setExtension(child.getStringValue()); break; 
                            }
                            case "filename": { 
                                template.setFilename(child.getStringValue()); break; 
                            }
                            case "icon": {
                                template.setIcon(child.getStringValue()); break;
                            }
                            default: { LOGGER.warn("Unexpected node in templates : "+child.getNodeName()); }
                        }
                    }
                }
                if(template.getName()!=null && template.getFile()!=null) {
                    templates.put(template.getName(), template);
                } else {
                    LOGGER.warn("template node doesn't have name");
                }
            }
            String[] recent = MavenOptionsPage.INSTANCE.getRecentTemplates().split(",");
            for(String s: recent) {
                Template tr = templates.get(s);
                if(tr!=null) {
                    recentsNode.add(new TemplateTreeNode(tr, false));
                }
            }
            templates.values().forEach((tp) -> {
                templatesNode.add(new TemplateTreeNode(tp, false));
            });
        } catch(SaxonApiException ex) {
            LOGGER.error("while loading templates model", ex);
        }
        ((DefaultMutableTreeNode)treeModel.getRoot()).add(recentsNode);
        ((DefaultMutableTreeNode)treeModel.getRoot()).add(templatesNode);
        tree.setModel(treeModel);
        tree.expandPath(new TreePath(new Object[]{treeModel.getRoot(), recentsNode}));
        tree.expandPath(new TreePath(new Object[]{treeModel.getRoot(), templatesNode}));
        LOGGER.info("template model loaded");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        dfFilter = new javax.swing.JTextField();
        pkOk = new javax.swing.JButton();
        pbCancel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        jLabel2 = new javax.swing.JLabel();
        dfFilename = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Filter");

        pkOk.setText("Ok");
        pkOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okPressed(evt);
            }
        });

        pbCancel.setText("Cancel");
        pbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelPressed(evt);
            }
        });

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        jScrollPane1.setViewportView(tree);

        jLabel2.setText("File name");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dfFilename, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pbCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pkOk, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dfFilter)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(dfFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pkOk)
                    .addComponent(pbCancel)
                    .addComponent(jLabel2)
                    .addComponent(dfFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okPressed
        Path fileToCreate = targetPath.resolve(dfFilename.getText());
        try (OutputStream wr = Files.newOutputStream(fileToCreate); InputStream is = getClass().getResourceAsStream(currentTemplate.getFile())) {
            byte[] buffer = new byte[2048];
            int read = is.read(buffer);
            while(read>0) {
                wr.write(buffer, 0, read);
                read = is.read(buffer);
            }
        } catch(IOException ex) {
            LOGGER.error("Creating "+fileToCreate.toString(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error creating "+fileToCreate.toString(), JOptionPane.ERROR_MESSAGE);
        }
        MavenOptionsPage.INSTANCE.setRecentTemplateName(currentTemplate.getName());
        MavenFileNode fileNode = new MavenFileNode(fileToCreate);
        node.add(fileNode);
        modelToUpdate.nodeStructureChanged(node);
        PluginWorkspaceProvider.getPluginWorkspace().open(fileNode.getFileUrl());
        setVisible(false);
    }//GEN-LAST:event_okPressed

    private void cancelPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelPressed
        setVisible(false);
    }//GEN-LAST:event_cancelPressed

    @Override
    protected JRootPane createRootPane() {
        cancelAction = new AbstractAction("Annuler") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
            }
        };
        okAction = new AbstractAction("Ok") {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed(e);
            }
        };
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
        JRootPane rp = new JRootPane();
        rp.registerKeyboardAction(cancelAction, "CANCEL", ks, JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(okAction, "OK", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rp;
    }
    
    private class Template implements HasIcon {
        private String name, file, description, extension, filename, icon;
        public Template() {
            super();
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getFile() { return file; }
        public void setFile(String file) { this.file = file; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getExtension() { return extension; }
        public void setExtension(String extension) { this.extension = extension; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        @Override
        public Icon getIcon() {
            if(icon!=null) {
                return ImageHandler.getInstance().get(icon);
            } else return null;
        }
        public void setIcon(String icon) { this.icon=icon; }
        @Override
        public String toString() {
            return name + (description!=null ? " ["+description+"]" : "");
        }
    }
    private class TemplateTreeNode extends DefaultMutableTreeNode implements HasIcon {

        public TemplateTreeNode(Object userObject) {
            super(userObject);
        }

        public TemplateTreeNode(Object userObject, boolean allowsChildren) {
            super(userObject, allowsChildren);
        }

        @Override
        public Icon getIcon() {
            if(getUserObject() instanceof HasIcon) {
                return ((HasIcon)getUserObject()).getIcon();
            } else return null;
        }
    }
    private class TemplateContainerTreeNode extends DefaultMutableTreeNode implements HasIcon {

        public TemplateContainerTreeNode(Object userObject) {
            super(userObject);
        }

        public TemplateContainerTreeNode(Object userObject, boolean allowsChildren) {
            super(userObject, allowsChildren);
        }

        @Override
        public Icon getIcon() {
            return ((DefaultTreeCellRenderer)tree.getCellRenderer()).getOpenIcon();
        }
        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField dfFilename;
    private javax.swing.JTextField dfFilter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton pbCancel;
    private javax.swing.JButton pkOk;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables
}
