/*
 * RuleSelectionDialog.java
 *
 * Created on June 18, 2007, 8:14 PM
 *
 */

package lambdacalc.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author champoll
 */
public class RuleSelectionDialog extends JDialog {
    
    private static RuleSelectionDialog dialog;
    
    private static int value = 0;

    private static RuleSelectionPanel panel;
    
    private RuleSelectionDialog(Frame frame,
                                Component locationComp,
                                String title) { 
        super(frame, title, true);
        
        //Create and initialize the buttons.
        JButton cancelButton = new JButton("Cancel");
        //cancelButton.addActionListener(this);
        //
        final JButton selectButton = new JButton("Select");
        selectButton.setActionCommand("Select");
        //
        
        panel = new RuleSelectionPanel();
        panel.setParentDialog(this);
        
        getContentPane().add(panel, BorderLayout.CENTER);
        getRootPane().setDefaultButton(panel.getFAButton());
        pack();
        setLocationRelativeTo(locationComp);        
        
    }
    /**
     * Set up and show the dialog.  The first Component argument
     * determines which frame the dialog depends on; it should be
     * a component in the dialog's controlling frame. The second
     * Component argument should be null if you want the dialog
     * to come up with its left corner in the center of the screen;
     * otherwise, it should be the component on top of which the
     * dialog should appear.
     */
    public static int showDialog(Component frameComp,
                                    Component locationComp) {
                                    
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        dialog = new RuleSelectionDialog(frame,
                                locationComp,
                                "Select a rule");
        
        dialog.setVisible(true);
        int result = dialog.panel.getValue();
        //dialog.setVisible(false);
        return result;
    }    
}
