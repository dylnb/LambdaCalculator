    /*
 * TreeCanvas.java
 *
 * Created on May 22, 2007, 10:44 AM
 */

package lambdacalc.gui.tree;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * A widget that displays a tree.
 */
public class TreeCanvas extends Container {
    TreeNode root;
    
    javax.swing.Timer timer;
    
    /** Creates a new instance of TreeCanvas */
    public TreeCanvas() {
        root = new TreeNode(null, this);
        add(root);
        setLayout(new GridLayout()); // TODO: Size ourself to be the size of the root
        doLayout();
    }
    
    public TreeNode getRoot() {
        return root;
    }
    
    public Dimension getMaximumSize() {
        return root.getSize();
    }

    public Dimension getMinimumSize() {
        return root.getSize();
    }
    
    public void doLayout() {
        getRoot().layoutControls();
    }
    
    public class TreeNode extends Panel implements ComponentListener {
        static final int NODE_VERTICAL_SPACING = 10;
        static final int NODE_HORIZONTAL_SPACING = 10;
        
        TreeNode parent;
        TreeCanvas container;
        
        Component label = null;
        ArrayList children = new ArrayList();
        
        int rootPosition;
        
        boolean invalid_layout;
        
        TreeNode(TreeNode parent, TreeCanvas container) {
            this.container = container;
            this.parent = parent;
            setLayout(null);
            invalid_layout = true;
        }
        
        public Component getLabel() {
            return this.label;
        }

        public void setLabel(Component label) {
            if (this.label != null) {
                this.label.removeComponentListener(this);
                remove(this.label);
            }
            this.label = label;
            if (this.label != null) {
                add(label);
                label.addComponentListener(this);
                invalidate_layout();
                container.doLayout();
            }
        }
        
        public void setLabel(String label) {
            Label c = new Label(label);
            setLabel(c);
        }

        public TreeNode addChild() {
            TreeNode n = new TreeNode(this, container);
            children.add(n);
            add(n); // to our own container layout
            invalidate_layout();
            container.doLayout();
            return n;
        }
        
        public void clearChildren() {
            // remove from AWT layout
            for (int i = 0; i < children.size(); i++)
                remove((TreeNode)children.get(i));
            children.clear();
            invalidate_layout();
            container.doLayout();
        }
        
        public int arity() {
            return children.size();
        }
        
        public TreeNode getChild(int index) {
            return (TreeNode)children.get(index);
        }
        
        void invalidate_layout() {
            TreeNode n = this;
            while (n != null) {
                n.invalid_layout = true;
                n = n.parent;
            }
        }
        
        void layoutControls() {
            if (!invalid_layout)
                return;
            invalid_layout = false;
            
            if (getLabel() != null) {
                getLabel().doLayout();
                getLabel().setSize(getLabel().getPreferredSize());
            }
            
            if (children.size() == 0) {
                if (getLabel() != null) {
                    getLabel().setLocation(0, 0);
                    setSize(getLabel().getSize());
                    rootPosition = getWidth() / 2;
                } else {
                    setSize(new Dimension(0,0));
                    rootPosition = 0;
                }
            } else {
                // Do layouts on the children so we get their sizes, and position
                // them one after the other.
                int tops = 0;
                if (getLabel() != null && getLabel().isVisible()) {
                    tops = getLabel().getHeight();
                }
                
                tops = tops + NODE_VERTICAL_SPACING;
                
                int left = 0;
                int maxHeight = 0;
                for (int i = 0; i < children.size(); i++) {
                    TreeNode c = (TreeNode)children.get(i);
                    c.layoutControls();
                    c.setLocation(new Point(left, tops));
                    left = left + c.getWidth() + NODE_HORIZONTAL_SPACING;
                    if (c.getHeight() > maxHeight) maxHeight = c.getHeight();
                }
                
                int width = left;
                
                // The root position is where we put the center of our label, which
                // is centered between the first and last root positions of the children.
                rootPosition = (((TreeNode)children.get(0)).getLocation().x + ((TreeNode)children.get(0)).rootPosition
                        + ((TreeNode)children.get(children.size()-1)).getLocation().x + ((TreeNode)children.get(children.size()-1)).rootPosition) / 2;
                
                // Position the label at the root position.
                if (getLabel() != null) {
                    int labelleft = rootPosition - getLabel().getWidth()/2;
                    if (labelleft < 0)
                        labelleft = 0;
                    getLabel().setLocation(labelleft, 0);
                    if (labelleft + getLabel().getWidth() > width)
                        width = labelleft + getLabel().getWidth();
                }
                
                setSize(width, tops + maxHeight);
            }
        }
        
        public void paint(Graphics g) {
            super.paint(g);
            
            Graphics2D gg = (Graphics2D)g;
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw lines from the root position to the roots of the children.
            g.setColor(Color.BLACK);
            
            int rootY = getLabel() == null ? 0 : getLabel().getHeight() + 1;
            
            for (int i = 0; i < children.size(); i++) {
                TreeNode c = (TreeNode)children.get(i);
                g.drawLine(rootPosition, rootY, c.getLocation().x + c.rootPosition, c.getLocation().y);
            }
        }
        
        // When a change is made to the label, relayout everything.
        public void componentResized(ComponentEvent e) {
            invalidate_layout();
            container.doLayout();
        }
        public void componentMoved(ComponentEvent e) {
            // ignore this--we're responsible for moving controls
        }
        public void componentShown(ComponentEvent e) {
            invalidate_layout();
            container.doLayout();
        }
        public void componentHidden(ComponentEvent e) {
            invalidate_layout();
            container.doLayout();
        }
    }
    
}
