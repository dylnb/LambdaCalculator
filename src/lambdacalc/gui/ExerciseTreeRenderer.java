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
 * ExerciseTreeRenderer.java
 *
 * Created on June 2, 2006, 11:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import lambdacalc.exercises.*;

public class ExerciseTreeRenderer implements TreeCellRenderer {
    
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        return new MyComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
    
    class MyComponent extends JPanel {
        public MyComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            String text = null;
            if (value instanceof ExerciseFile) {
                text = ((ExerciseFile)value).getTitle();
            } else if (value instanceof ExerciseGroup) {
                text = (char)((int)'A' + ((ExerciseGroup)value).getIndex()) + ". " + ((ExerciseGroup)value).getTitle();
            } else if (value instanceof Exercise) {
                text = (((Exercise)value).getIndex()+1) + ". " + ((Exercise)value).getExerciseText();
            }            
            
            if (value instanceof Exercise && false) {
                JLabel status = new JLabel();
                status.setFont(Font.decode("Times New Roman"));
                add(status);
                if (((Exercise)value).isDone())
                    status.setText("\u2713"); // 2611 = box+check
                else
                    status.setText("\u2610");
                
                try {
                    status.setFont(Font.decode("Arial Unicode MS"));
                } catch (Exception e) {
                    status.setFont(tree.getFont());
                }
            }

            JLabel label = new JLabel();
            try {
                //label.setFont(Font.decode("Arial Unicode MS"));
                label.setFont(Font.decode("Serif 14 Plain"));
            } catch (Exception e) {
                Util.displayInformationMessage(this, e.getMessage(), "Error");
                //label.setFont(tree.getFont());
            }
            add(label);

            label.setText(text);
            
            setBackground(UIManager.getColor("Tree.background"));
            if (selected) {
                setBackground(UIManager.getColor("Tree.selectionBackground"));
                label.setForeground(UIManager.getColor("Tree.selectionForeground"));
            }

            int h = label.getFont().getBaselineFor('p');
            tree.setRowHeight(h);
        }
    }
}
