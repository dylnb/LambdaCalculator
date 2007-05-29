/*
 * LambdaEnabledTextField.java
 *
 * Created on June 1, 2006, 5:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

//TODO add acknowledgment to whoever was the original author

package lambdacalc.gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.text.*;

import lambdacalc.logic.*;

/**
 *
 * @author tauberer
 */
public class LambdaEnabledTextField extends JTextField implements Serializable {

     boolean isTempText = false;
    
     public LambdaEnabledTextField() {
     	setFont(Util.getUnicodeFont(16));
     }
     
     public void setText(String text) {
         isTempText = false;
         setForeground(UIManager.getColor("TextField.foreground"));
         super.setText(text);
     }
     
     public void setTemporaryText(String text) {
         isTempText = true;
         super.setText(text);
         setForeground(UIManager.getColor("TextField.inactiveForeground"));
         setCaretPosition(0);
     }
     
     private void onEvent() {
          if (isTempText) {
             isTempText = false;
             setForeground(UIManager.getColor("TextField.foreground"));
             setText("");
         }
     }
 
     protected void processKeyEvent(KeyEvent e) {
         onEvent();
         super.processKeyEvent(e);
     }
     
     protected void processMouseEvent(MouseEvent e) {
         if (e.getButton() != 0)
            onEvent();
         super.processMouseEvent(e);
     }
     
     protected Document createDefaultModel() {
          return new LambdaDocument();
     }
 
     class LambdaDocument extends PlainDocument {
         public void insertString(int offs, String str, AttributeSet a) 
              throws BadLocationException {

              if (isTempText) {
                  super.insertString(offs, str, a);
                  return;
              }

              if (str == null)
                  return;
              
              char[] revised = str.toCharArray();
              for (int i = 0; i < revised.length; i++) {
                  switch (revised[i]) {
                      case 'L': revised[i] = Lambda.SYMBOL; break;
                      case 'A': revised[i] = ForAll.SYMBOL; break;
                      case 'E': revised[i] = Exists.SYMBOL; break;
                      case 'I': revised[i] = Iota.SYMBOL; break;
                      case '&': revised[i] = And.SYMBOL; break;
                      case '^': revised[i] = And.SYMBOL; break;
                      case '|': revised[i] = Or.SYMBOL; break;
                      case '~': revised[i] = Not.SYMBOL; break;
                      case '\'': revised[i] = Identifier.PRIME; break;
                  }
              }
              
              super.insertString(offs, new String(revised), a);
         }
         
         protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng,
                            AttributeSet attr) {
              super.insertUpdate(chng, attr);
              
              if (isTempText) return;

            writeLock();
              try {
                  boolean foundChange = true;
                  while (foundChange) {
                      foundChange = false;
                      Content content = getContent();
                      char c1 = (char)0, c2 = (char)0;
                      for (int i = 0; i < content.length(); i++) {
                          char c3 = content.getString(i, 1).charAt(0);
                          if (c1 == '<' && (c2 == '-' || c2 == '=') && c3 == '>') {
                              replace(i-2, 3, String.valueOf(Iff.SYMBOL), null);
                              foundChange = true;
                              break;
                          } else if ((c2 == '-' || c2 == '=') && c3 == '>') {
                              replace(i-1, 2, String.valueOf(If.SYMBOL), null);
                              foundChange = true;
                              break;
                          } else if (c2 == '!' && c3 == '=') {
                              replace(i-1, 2, String.valueOf(Equality.NEQ_SYMBOL), null);
                              foundChange = true;
                              break;
                          }
                          c1 = c2;
                          c2 = c3;
                      }
                }
              } catch (BadLocationException e) {
              } finally {
                  writeUnlock();
              }
           }
     }
}
