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
public class TreeCanvas extends JComponent {
    JTreeNode root;
    TreeLayoutMethod layout;

    boolean animated = true;    
    javax.swing.Timer timer;
    boolean hadPositionChange = false;

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
        return root.getSize();
    }

    public Dimension getMinimumSize() {
        return root.getSize();
    }
    
    public void doLayout() {
        layout.layoutTree(getRoot());
        if (!animated)
            positionControls(getRoot(), false);
        else
            timer.start();
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
                subtree.getLabel().doLayout();
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
                    // put the label flush on the left edge.
                    if (ni.labelCenter - subtree.getLabel().getWidth()/2 < 0)
                        ni.labelCenter = subtree.getLabel().getWidth()/2;
                        
                    // If that puts the right edge beyond the right edge of the last
                    // child, expand the width of this subtree and center the children.
                    if (ni.labelCenter + subtree.getLabel().getWidth()/2 > width) {
                        int oldwidth = width;
                        width = ni.labelCenter + subtree.getLabel().getWidth()/2;
                        
                        // Move all of the children over by half the expanded length
                        for (int i = 0; i < subtree.children.size(); i++) {
                            JTreeNode c = (JTreeNode)subtree.children.get(i);
                            NodeInfo nic = (NodeInfo)c.layoutInfo;
                            nic.subtreeLeft += (width-oldwidth)/2;
                        }
                    }
                }
                
                ni.subtreeSize = new Dimension(width, tops + maxHeight);
            }
            
        }
        
        public void setNodePositions(JTreeNode subtree, int parentLeft, int parentTop) {
            NodeInfo ni = (NodeInfo)subtree.layoutInfo;
            subtree.positionX = parentLeft + ni.subtreeLeft + ni.labelCenter - subtree.getWidth()/2;
            subtree.positionY = parentTop + ni.subtreeTop;
            for (int i = 0; i < subtree.children.size(); i++) {
                JTreeNode c = (JTreeNode)subtree.children.get(i);
                setNodePositions(c, parentLeft + ni.subtreeLeft, subtree.positionY + ni.subtreeTop);
            }
        }
    }
    
    private void positionControls(JTreeNode node, boolean incremental) {
        if (!incremental) {
            node.setLocation(node.positionX, node.positionY);
        } else {
            hadPositionChange = (node.positionX != node.getLocation().x) || (node.positionY != node.getLocation().y);
            node.setLocation((node.positionX + node.getLocation().x)/2, (node.positionY + node.getLocation().y)/2);
        }
            
        for (int i = 0; i < node.children.size(); i++) {
            JTreeNode c = (JTreeNode)node.children.get(i);
            positionControls(c, incremental);
        }
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
        for (int i = 0; i < node.children.size(); i++) {
            JTreeNode c = (JTreeNode)node.children.get(i);
            g.drawLine(node.getLocation().x + node.getWidth()/2, node.getLocation().y + node.getHeight(),
                c.getLocation().x + c.getWidth()/2, c.getLocation().y);
            paintLines(g, c);
        }
    }
    
    class TimerHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            hadPositionChange = false; // changed by positionControls
            positionControls(getRoot(), true);
            if (!hadPositionChange)
                timer.stop();
        }
    }
        
    public class JTreeNode extends JPanel implements ComponentListener {
        JTreeNode parent;
        TreeCanvas container;
        
        Component label = null;
        ArrayList children = new ArrayList();
        
        Object layoutInfo;
        int positionX, positionY;
        
        /**
         * @param parent null if this is the root node
         */
        JTreeNode(JTreeNode parent, TreeCanvas container) {
            this.container = container;
            this.parent = parent;
            setLayout(null);
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
                container.doLayout();
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
            container.doLayout();
            return n;
        }
        
        public void clearChildren() {
            // remove from layout
            for (int i = 0; i < children.size(); i++)
                container.remove((JTreeNode)children.get(i));
            children.clear();
            container.doLayout();
        }
        
        public int arity() {
            return children.size();
        }
        
        public JTreeNode getChild(int index) {
            return (JTreeNode)children.get(index);
        }
        
        // When a change is made to the label, relayout everything.
        public void componentResized(ComponentEvent e) {
            container.doLayout();
        }
        public void componentMoved(ComponentEvent e) {
            // ignore this--we're responsible for moving controls
        }
        public void componentShown(ComponentEvent e) {
            container.doLayout();
        }
        public void componentHidden(ComponentEvent e) {
            container.doLayout();
        }
    }
    
}
