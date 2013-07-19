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
public class TreeCanvas extends JComponent implements Scrollable {
    JTreeNode root;
    TreeLayoutMethod layout;

    int margin = 5; // space in pixels between nodes and component edge
    
    boolean animated = true;    
    javax.swing.Timer timer;
    boolean hadPositionChange = false;
    
    Dimension preferredSize = new Dimension(0,0);

    /**
     * Creates a new instance of TreeCanvas
     */
    public TreeCanvas() {
        timer = new javax.swing.Timer(100, new TimerHandler()); // not started yet
        
        layout = new MonospaceLayoutMethod();
        root = new JTreeNode(null, this);
        add(root);
        setLayout(new GridLayout()); // TODO: Size ourself to be the size of the root
        doLayout();
    }
    
    public JTreeNode getRoot() {
        return root;
    }
    
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    public Dimension getPreferredSize() {
        return preferredSize;
    }
    
    public void clear() {
        getRoot().clearChildren();
        
        // replace the root with a fresh instance to clear its state
        remove(root);
        root = new JTreeNode(null, this);
        add(root);
    }
    
    public void doLayout() {
        // Before adjusting the tree layout, we must
        // reposition all nodes according to the existing
        // layout in the event that a node's width changed
        // and it must be re-centered.
        recenterControls(getRoot());
        
        // Ask layout method to assign positionX/Y values
        // to each node.
        layout.layoutTree(getRoot());
        
        // Move nodes.
        if (!animated) {
            positionControls(getRoot(), false);
            preferredSize = getPreferredSize(getRoot());
            setSize(preferredSize);
        } else {
            timer.start();
            // size, preferredSize gets updated during animation
        }
    }
    
    public abstract class TreeLayoutMethod {
        public abstract void layoutTree(JTreeNode root);
    }
    
    public class MonospaceLayoutMethod extends TreeLayoutMethod {
        static final int NODE_VERTICAL_SPACING = 10;
        static final int NODE_HORIZONTAL_SPACING = 10;
        
        class NodeInfo {
            public Dimension subtreeSize;
        
            // relative to parent
            public int subtreeLeft, subtreeTop;
            
            // relative to subtreeLeft
            public int labelCenter;
        }

        public void layoutTree(JTreeNode root) {
            layoutSubtree(root);
            setNodePositions(root, 0, 0);
        }

        public void layoutSubtree(JTreeNode subtree) {
            if (subtree.layoutInfo == null || !(subtree.layoutInfo instanceof NodeInfo))
                subtree.layoutInfo = new NodeInfo();
                
            NodeInfo ni = (NodeInfo)subtree.layoutInfo;
            
            // Layout the label itself.
            if (subtree.getLabel() != null) {
                subtree.getLabel().validate();
                subtree.getLabel().setSize(subtree.getLabel().getPreferredSize());
                subtree.setSize(subtree.getLabel().getSize());
                subtree.getLabel().setLocation(0,0); // relative to the panel that contains just that node
            } else {
                subtree.setSize(new Dimension(0,0));
            }
            
            if (subtree.children.size() == 0) {
                if (subtree.getLabel() != null) {
                    ni.subtreeSize = subtree.getLabel().getSize();
                    ni.labelCenter = subtree.getWidth() / 2;
                } else {
                    ni.subtreeSize = new Dimension(0,0);
                    ni.labelCenter = 0;
                }
            } else {
                // Do layouts on the children so we get their sizes, and position
                // them one after the other.
                int tops = 0;
                if (subtree.getLabel() != null && subtree.getLabel().isVisible()) {
                    tops = subtree.getLabel().getHeight();
                }
                
                tops = tops + NODE_VERTICAL_SPACING;
                
                int left = 0;
                int maxHeight = 0;
                for (int i = 0; i < subtree.children.size(); i++) {
                    JTreeNode c = (JTreeNode)subtree.children.get(i);
                    layoutSubtree(c);
                    NodeInfo nic = (NodeInfo)c.layoutInfo;
                    
                    nic.subtreeLeft = left;
                    nic.subtreeTop = tops;
                    
                    left = left + nic.subtreeSize.width + NODE_HORIZONTAL_SPACING;
                    if (nic.subtreeSize.height > maxHeight) maxHeight = nic.subtreeSize.height;
                }
                
                int width = left;
                
                // The root position is where we put the center of our label, which
                // is centered between the first and last root positions of the children.
                NodeInfo nic1 = (NodeInfo)((JTreeNode)subtree.children.get(0)).layoutInfo;
                NodeInfo nic2 = (NodeInfo)((JTreeNode)subtree.children.get(subtree.children.size()-1)).layoutInfo;
                
                int rootPosition = ((nic1.subtreeLeft + nic1.labelCenter) + (nic2.subtreeLeft + nic2.labelCenter)) / 2;                
                
                // Position the label at the root position.
                if (subtree.getLabel() != null) {
                    // Center the label around the rootPosition
                    ni.labelCenter = rootPosition;
                    
                    // If that puts the left edge before the left edge of the subtree,
                    // push the label and all of its children over so that the label
                    // is flush on the left edge.
                    if (ni.labelCenter - subtree.getLabel().getWidth()/2 < 0) {
                        int push = subtree.getLabel().getWidth()/2 - ni.labelCenter;
                        ni.labelCenter += push;
                        width += push;
                        for (int i = 0; i < subtree.children.size(); i++) {
                            JTreeNode c = (JTreeNode)subtree.children.get(i);
                            NodeInfo nic = (NodeInfo)c.layoutInfo;
                            nic.subtreeLeft += push;
                        }
                    }
                        
                    // If the right edge of the label goes beyond the right edge of the last
                    // child, expand the width of this subtree.
                    if (ni.labelCenter + subtree.getLabel().getWidth()/2 > width)
                        width = ni.labelCenter + subtree.getLabel().getWidth()/2;
                }
                
                ni.subtreeSize = new Dimension(width, tops + maxHeight);
            }
            
        }
        
        public void setNodePositions(JTreeNode subtree, int parentLeft, int parentTop) {
            subtree.hasPosition = true;
            NodeInfo ni = (NodeInfo)subtree.layoutInfo;
            subtree.positionX = parentLeft + ni.subtreeLeft + ni.labelCenter;
            subtree.positionY = parentTop + ni.subtreeTop;
            for (int i = 0; i < subtree.children.size(); i++) {
                JTreeNode c = (JTreeNode)subtree.children.get(i);
                setNodePositions(c, parentLeft + ni.subtreeLeft, parentTop + ni.subtreeTop);
            }
        }
    }
    
    private void recenterControls(JTreeNode node) {
        // For all nodes that have been placed onto the screen,
        // redo their internal layouts and then make them centered
        // around the coordinate they should be centered on.
        // We use currentX/Y and not positionX/Y here because in animation,
        // positionX/Y may be changed to the desired location before
        // we get a chance to recenter according to where the node
        // actually is currently on screen.
        
        if (!node.hasPosition || !node.hasBeenPlaced) return; // a new node that has not been positioned yet
        
        if (node.getLabel() != null) {
            node.getLabel().validate();
            node.getLabel().setSize(node.getLabel().getPreferredSize());
            node.setSize(node.getLabel().getSize());
            node.getLabel().setLocation(0,0); // relative to the panel that contains just that node
        }
            
        node.setLocation(node.currentX - node.getWidth()/2, node.currentY);
            
        for (int i = 0; i < node.children.size(); i++) {
            JTreeNode c = (JTreeNode)node.children.get(i);
            recenterControls(c);
        }
    }
    
    private void positionControls(JTreeNode node, boolean incremental) {
        if (!incremental || !node.hasBeenPlaced) {
            if (node.getLabel() != null) {
                node.getLabel().validate();
                node.getLabel().setSize(node.getLabel().getPreferredSize());
                node.setSize(node.getLabel().getSize());
                node.getLabel().setLocation(0,0); // relative to the panel that contains just that node
            }
            
            node.setLocation(margin + node.positionX - node.getWidth()/2, margin + node.positionY);
        } else {
            hadPositionChange = (margin + node.positionX != node.getLocation().x) || (margin + node.positionY != node.getLocation().y);
            node.setLocation((margin + node.positionX + (node.getLocation().x+node.getWidth()/2))/2 - node.getWidth()/2, (margin + node.positionY + node.getLocation().y)/2);
        }
        
        node.currentX = node.getLocation().x + node.getWidth()/2;
        node.currentY = node.getLocation().y;
            
        for (int i = 0; i < node.children.size(); i++) {
            JTreeNode c = (JTreeNode)node.children.get(i);
            positionControls(c, incremental);
        }

        if (!node.hasBeenPlaced) {
            node.setVisible(true);        
            node.hasBeenPlaced = true;
        }
    }
    
    
     private Dimension getPreferredSize(JTreeNode node) {
        int width = 0, height = 0;
        int right = node.getLocation().x + node.getWidth() + margin;
        int bottom = node.getLocation().y + node.getHeight() + margin;
        if (right > width) width = right;
        if (bottom > height) height = bottom;
        for (int i = 0; i < node.arity(); i++) {
            Dimension d = getPreferredSize(node.getChild(i));
            if (d.width > width) width = d.width;
            if (d.height > height) height = d.height;
        }
        return new Dimension(width, height);
    }

   
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
            
        Graphics2D gg = (Graphics2D)g;
        gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
        // Draw lines from the root position to the roots of the children.
        g.setColor(Color.BLACK);
        
        paintLines(g, getRoot());
    }
   
    private void paintLines(Graphics g, JTreeNode node) {
        if (!node.hasBeenPlaced) return;   
        for (int i = 0; i < node.children.size(); i++) {
            JTreeNode c = (JTreeNode)node.children.get(i);
            if (!c.isVisible())
                continue;
            g.drawLine(node.getLocation().x + node.getWidth()/2, node.getLocation().y + node.getHeight(),
                c.getLocation().x + c.getWidth()/2, c.getLocation().y);
            paintLines(g, c);
        }
    }
    
    class TimerHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            hadPositionChange = false; // changed by positionControls
            positionControls(getRoot(), true);
            repaint(); // clear previous lines that were drawn
            if (!hadPositionChange) {
                timer.stop();
            }
            preferredSize = getPreferredSize(getRoot());
            setSize(preferredSize);
        }
    }
        
    public class JTreeNode extends JPanel implements ComponentListener {
        JTreeNode parent;
        TreeCanvas container;
        
        Component label = null;
        ArrayList children = new ArrayList();
        
        Object layoutInfo;
        int positionX, positionY; // desired layout position (x is the center of the label, y is top), not including margins
        boolean hasPosition = false, hasBeenPlaced = false;
        
        int currentX, currentY; // actual current on-screen position (x is at center of label, y is top), including margins
        
        /**
         * @param parent null if this is the root node
         */
        JTreeNode(JTreeNode parent, TreeCanvas container) {
            this.container = container;
            this.parent = parent;
            setLayout(null);
            setBackground(container.getBackground());
            setVisible(false); // not shown until first placed
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
                container.invalidate();
            }
        }
        
        public void setLabel(String label) {
            Label c = new Label(label);
            setLabel(c);
        }

        public JTreeNode addChild() {
            JTreeNode n = new JTreeNode(this, container);
            children.add(n);
            container.add(n); // to our own container layout
            container.invalidate();
            return n;
        }
        
        public void clearChildren() {
            // remove from layout
            for (int i = 0; i < children.size(); i++) {
                JTreeNode child = (JTreeNode)children.get(i);
                container.remove(child);
                child.clearChildren();
            }
            children.clear();
            container.invalidate();
        }
        
        public int arity() {
            return children.size();
        }
        
        public JTreeNode getChild(int index) {
            return (JTreeNode)children.get(index);
        }
        
        // When a change is made to the label, relayout everything.
        public void componentResized(ComponentEvent e) {
            container.invalidate();
        }
        public void componentMoved(ComponentEvent e) {
            // ignore this--we're responsible for moving controls
        }
        public void componentShown(ComponentEvent e) {
            container.invalidate();
        }
        public void componentHidden(ComponentEvent e) {
            container.invalidate();
        }
    }
    
    // Scrollable interface implementation
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                      int orientation,
                                      int direction) {
        return 20; // TODO
    }
    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                       int orientation,
                                       int direction) {
        return 80; // TODO
    }
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
