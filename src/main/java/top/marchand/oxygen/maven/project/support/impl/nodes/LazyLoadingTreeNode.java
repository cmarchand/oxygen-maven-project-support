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
package top.marchand.oxygen.maven.project.support.impl.nodes;

import java.awt.Cursor;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import org.apache.log4j.Logger;

/**
 * A class for a lazy loading content tree node.
 * 
 * @author cmarchand
 */
public abstract class LazyLoadingTreeNode extends DefaultMutableTreeNode implements HasValue {
    private boolean loaded = false;
    private static final Logger LOGGER = Logger.getLogger(LazyLoadingTreeNode.class);
    
    public LazyLoadingTreeNode(Object userObject) {
        super(userObject, true);
        add(new DefaultMutableTreeNode("Loading...", false));
    }
    
    /**
     * Call when children have to be load
     * @return All children of this node
     */
    public abstract List<MutableTreeNode> loadChildren();
    
    protected void setChildren(List<MutableTreeNode> childs) {
        removeAllChildren();
        setAllowsChildren(childs.size() > 0);
        childs.forEach((node) -> {
            add(node);
        });
        loaded = true;
    }
    
    /**
     * Call when loading starts and end. By default, change cursor
     * from DEFAULT to WAIT, and reverse.
     * @param view The view to update
     * @param running If the process is running ... or not
     */
    public void setRunning(JComponent view, boolean running) {
        if(running) {
            view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            view.setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Loads children nodes, in a swing worker
     * @param model The model to update
     * @param view The view to update
     */
    public void expandNode(final DefaultTreeModel model, final JComponent view) {
        if(loaded) return;
        SwingWorker<List<MutableTreeNode>, Void> worker = new SwingWorker<List<MutableTreeNode>, Void>() {
            @Override
            protected List<MutableTreeNode> doInBackground() throws Exception {
                LOGGER.debug("expanding "+getValue());
                setRunning(view, true);
                return loadChildren();
            }
            @Override
            protected void done() {
                try {
                    LOGGER.debug("\tdone");
                    setChildren(get());
                    model.nodeStructureChanged(LazyLoadingTreeNode.this);
                    setRunning(view, false);
                } catch(InterruptedException | ExecutionException ex) {
                    LOGGER.error("while setting children", ex);
                }
            }
        };
        worker.execute();
    }
    
}
