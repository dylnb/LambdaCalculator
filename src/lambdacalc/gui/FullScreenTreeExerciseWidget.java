/*
 * Copyright (C) 2007-2014 Dylan Bumford, Lucas Champollion, Maribel Romero
 * and Joshua Tauberer
 * 
 * This file is part of The Lambda Calculator.
 * 
 * The Lambda Calculator is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Lambda Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with The Lambda Calculator.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
