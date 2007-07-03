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
public class LambdaEnabledTextField extends JTextField {

     private boolean isTempText = false;
    
     public LambdaEnabledTextField() {
     	setFont(Util.getUnicodeFont(16));
     }
     
     public boolean isTempText() {
         return isTempText;
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
     
     /**
      * If this field is displaying a temporary text, delete it, otherwise do nothing.
      */
     public void deleteAnyTemporaryText() {
         if (isTempText == false) return;
         
         isTempText = false;
         setForeground(UIManager.getColor("TextField.foreground"));
         setText("");
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
         
         // Replace ALT plus various special keys with some special unicode
         // symbols. We handle both the letter-like replacements as well as
         // some symbol replacements, even though ALT isn't strictly necessary
         // in those cases.
         if (e.isAltDown()) {
             // Convert the key char to uppercase because we have the uppercase
             // characters stored in the constants below.
             char c = Character.toUpperCase( e.getKeyChar() );
             
             switch (c) {
                 case Lambda.INPUT_SYMBOL: c = Lambda.SYMBOL; break;
                 case ForAll.INPUT_SYMBOL: c = ForAll.SYMBOL; break;
                 case Exists.INPUT_SYMBOL: c = Exists.SYMBOL; break;
                 case Iota.INPUT_SYMBOL: c = Iota.SYMBOL; break;
                 case '6': c = And.SYMBOL; break; // i.e. sort of ALT+CARRET
                 case '7': c = And.SYMBOL; break; // i.e. sort of ALT+AMPERSAND
                 case Or.INPUT_SYMBOL: c = Or.SYMBOL; break;
                 case '`': c = Not.SYMBOL; break; // i.e. sort of ALT+tilde
                 default:
                     super.processKeyEvent(e);
                     return;
             }
            
             // If we got here, we decided that the user pressed a special key.
             e.setKeyChar(c);
             e.setModifiers(0); // this method is marked as deprecated, but hopefully we'll get away with it
             super.processKeyEvent(e);
         
         // And when ALT is not pressed, we have some special symbol replacements
         // as well. Note that SHIFT might be pressed in some of these cases. These
         // are the non-letter replacements.
         } else {
             char c = e.getKeyChar();
             
             switch (c) {
                 case And.INPUT_SYMBOL: c = And.SYMBOL; break;
                 case '^': c = And.SYMBOL; break; //alternative way of entering And
                 case Not.INPUT_SYMBOL: c = Not.SYMBOL; break;
                 case Identifier.PRIME_INPUT_SYMBOL: c = Identifier.PRIME; break;
                 default:
                     super.processKeyEvent(e);
                     return;
             }
            
             // If we got here, we decided that the user pressed a special key.
             e.setKeyChar(c);
             super.processKeyEvent(e);
             
         }
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
         // This is executed both when keys are pressed and when text is pasted
         // into the document. When things like [[IP]] are pasted in, we don't
         // want to replace the I with an iota. Thus, this method can't be
         // used for the letter-like replacements.
         /*
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
                      case Lambda.INPUT_SYMBOL: revised[i] = Lambda.SYMBOL; break;
                      case ForAll.INPUT_SYMBOL: revised[i] = ForAll.SYMBOL; break;
                      case Exists.INPUT_SYMBOL: revised[i] = Exists.SYMBOL; break;
                      case Iota.INPUT_SYMBOL: revised[i] = Iota.SYMBOL; break;
                      case And.INPUT_SYMBOL: revised[i] = And.SYMBOL; break;
                      //alternative way of entering And
                      case '^': revised[i] = And.SYMBOL; break;
                      case Or.INPUT_SYMBOL: revised[i] = Or.SYMBOL; break;
                      case Not.INPUT_SYMBOL: revised[i] = Not.SYMBOL; break;
                      case Identifier.PRIME_INPUT_SYMBOL: revised[i] = Identifier.PRIME; break;
                  }
              }
              
              super.insertString(offs, new String(revised), a);
         }
         */
         
         /*
          * Unlike the above note, we will retain this method because it seems
          * always OK to replace these multi-character special strings withour
          * unicode variants.
          */
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
                          
                          
                          // multi-character substitutions
                          
                          // Iff: <-> <=>
                          
                          if (c1 == '<' && (c2 == '-' || c2 == '=') && c3 == '>') {
                              replace(i-2, 3, String.valueOf(Iff.SYMBOL), null);
                              foundChange = true;
                              break;
                          
                          // If: -> =>
                              
                          } else if ((c2 == '-' || c2 == '=') && c3 == '>') {
                              replace(i-1, 2, String.valueOf(If.SYMBOL), null);
                              foundChange = true;
                              break;
                          
                          // Nonequal: !=
                          
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
