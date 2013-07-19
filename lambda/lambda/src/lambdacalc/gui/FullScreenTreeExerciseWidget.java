/*
 * FullScreenTreeExerciseWidget.java
 *
 * Created on June 19, 2007, 8:36 PM
 *
 */

package lambdacalc.gui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

/**
 *
 * @author champoll
 */
public class FullScreenTreeExerciseWidget extends TreeExerciseWidget {
 
    JFrame fullScreenFrame = new JFrame();
    
    GraphicsDevice theScreen =
                GraphicsEnvironment.
                getLocalGraphicsEnvironment().
                getDefaultScreenDevice();   
    
    TreeExerciseWidget parent;
    
    
    /** Creates a new instance of FullScreenTreeExerciseWidget */
    public FullScreenTreeExerciseWidget(TreeExerciseWidget parent) {
        
        this.parent = parent;
        
        fullScreenFrame.setUndecorated(true);
        
        setBackground(parent.getBackground());

        setFontSize(parent.curFontSize);
        
        this.initialize(parent.getExercise());
        this.setSelectedNode(parent.getSelectedNode());
        this.setErrorMessage(parent.getErrorMessage());
//        this.isFullScreenPanel = true;
       
        fullScreenFrame.getContentPane().add(this);
        
        this.btnFullScreen.setText("Exit full screen");
        this.btnFullScreen.removeActionListener(fullScreenActionListener);
        this.btnFullScreen.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
    }
    
    // override parent's method
    public void openFullScreenWindow() {
        throw new UnsupportedOperationException();
    }
    
    public void display() {
        if (!theScreen.isFullScreenSupported()) {
            System.err.println("Warning: Full screen mode not supported," +
                    "emulating by maximizing the window...");
        }
//        fullScreenFrame.addKeyListener(new KeyListener() {
//                public void keyPressed(KeyEvent event) {}
//                public void keyReleased(KeyEvent event) {
//                    if (event.getKeyChar() == KeyEvent.VK_ESCAPE) {
//                        if (isFullScreenPanel) {
//                            theScreen.setFullScreenWindow(null);
//                        }
//                    }
//                }
//                public void keyTyped(KeyEvent event) {}
//            }
//        );
        
        try {
            theScreen.setFullScreenWindow(fullScreenFrame);
        } catch (Exception e) {
            e.printStackTrace();
            theScreen.setFullScreenWindow(null);
        } 
    }
    
    public void exit() {
        parent.setSelectedNode(this.getSelectedNode());
        parent.setErrorMessage(this.getErrorMessage());
        theScreen.setFullScreenWindow(null);
        fullScreenFrame.dispose();
        TrainingWindow.getSingleton().requestFocus();
    }
        
    
    
}
