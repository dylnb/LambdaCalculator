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
     
     public static String getModifier() {
         if (Util.isMac()) return "Ctrl"; else return "Alt";
     }
 
     protected void processKeyEvent(KeyEvent e) {
         onEvent();
         
         //System.err.println(e);
         
         // Replace ALT plus various special keys with some special unicode
         // symbols. We handle both the letter-like replacements as well as
         // some symbol replacements, even though ALT isn't strictly necessary
         // in those cases.
         
         // On PCs, use the ALT key. On Macs, this almsot works,
         // but ALT+E and ALT+I add accent marks and this behavior
         // seems apparently out of our control.
         boolean mod = e.isAltDown();
         int key = KeyEvent.VK_ALT;
         
         // Detect whether we are on a Mac. If so, then we take
         // over the Control key. The Command key is used for copy/paste,
         // so we don't want to touch that (esp. since V (Cut) would
         // be mapped to OR.)
         if (Util.isMac()) {
//         if (KeyEvent.getKeyText(KeyEvent.VK_META).equals("Command")) {
            mod = e.isControlDown();
            key = KeyEvent.VK_CONTROL;
         }
         
         // On Windows, presses of ALT move the focus to the menu bar.
         // We need to consume presses and releases of the ALT key itself.
         if (!Util.isMac() && e.getKeyCode() == key) {
             e.consume();
             return;
         }
         
         if (mod) {
             // On OSX, ALT+key changes the key to something weird,
             // and we get KEY_TYPED events that have 0 as the keyCode
             // and a non-noormal unicode character as the keyChar.
             // At this point, we've lost the information about what
             // key was actually pressed. So, we simply ignore KEY_TYPED
             // event when ALT is downs (i.e. process them and have them
             // do nothing), and deal with our special keys on
             // KEY_PRESSED.
             
             if (e.getID() == KeyEvent.KEY_TYPED) {
                 e.consume();
                 return;
             }
         
             // On Linux, getKeyChar returns the letter that corresponds
             // with what would be typed had Alt not been pressed (i.e.
             // lowercase).
             // On Mac OSX, getKeyChar returns also the letter corresponding
             // to the key, but it maps ALT+key to some character
             // other than what's normal, and that useless character is
             // returned.
             // To get around the Mac problem, we ask the expressions for their
             // key events.
             
             //char c = new KeyEvent((Component)e.getSource(), e.getID(), e.getWhen(), 0, e.getKeyCode()).getKeyChar();
             //System.err.println(c);
             char c;
             int i = e.getKeyCode();
             switch (i) {
                 case Lambda.KEY_EVENT:
                     c = Lambda.SYMBOL;
                     break;
                 case ForAll.KEY_EVENT:
                     c = ForAll.SYMBOL;
                     break;
                 case Exists.KEY_EVENT:
                     c = Exists.SYMBOL;
                     break;
                 case Iota.KEY_EVENT:
                     c = Iota.SYMBOL;
                     break;
                 case And.KEY_EVENT:
                     //TODO localize the two following lines on intl keyboards!
                 case KeyEvent.VK_6: // modifier on the key that's the same as "&" on English keyboards
                 case KeyEvent.VK_7: // modifier on the key that's the same as "^" on English keyboards   
                 case And.ALTERNATE_INPUT_SYMBOL: // alternative for "and": caret (^)
                     if (!e.isShiftDown())
                        c = And.SYMBOL;
                     else
                         c = SetRelation.Intersect.SYMBOL;
                     break;
                 case Or.KEY_EVENT: 
                     if (!e.isShiftDown())
                        c = Or.SYMBOL;
                     else
                         c = SetRelation.Union.SYMBOL;
                     break;             
                 case SetRelation.Subset.KEY_EVENT:
                     c = SetRelation.Subset.SYMBOL;
                     break;
                 case SetRelation.Superset.KEY_EVENT:
                     c = SetRelation.Superset.SYMBOL;
                     break;
                 case SetWithElements.EMPTY_SET_KEY_EVENT:
                     c = SetWithElements.EMPTY_SET_SYMBOL;
                     break;
                 
                 default:
                     super.processKeyEvent(e);
                     return;
             }
            
             // If we got here, we decided that the user pressed a special key.

             // On Linux, we can simply update e with setKeyChar, setModifiers
             // and pass that up. This seems to not work on Mac OSX, since it
             // ignores the change to the modifier state and inserts some
             // other character.l
             //e.setKeyChar(c);
             //e.setModifiers(0); // this method is marked as deprecated, but hopefully we'll get away with it
             //super.processKeyEvent(e);
             
             // Instead of that, we insert the character that we want
             // directly into the document. We only want to insert
             // a character once per press/release, and KEY_TYPED is
             // the appropriate event to work with, but it seems (on
             // OSX?), KEY_TYPED don't get fired always...?
             e.consume();
             if (e.getID() == KeyEvent.KEY_PRESSED)
                 replaceSelection(c + "");
             
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
                 case Multiplication.INPUT_SYMBOL: c = Multiplication.SYMBOL; break;
                 default:
                     super.processKeyEvent(e);
                     return;
             }
            
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
          * always OK to replace these multi-character special strings with our
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
                          
                          } else if ((c2 == '!' || c2 == Not.SYMBOL) && c3 == '=') {
                              replace(i-1, 2, String.valueOf(Equality.NEQ_SYMBOL), null);
                              foundChange = true;
                              break;
                              
                          // Less than or equal, greater than or equal
                          } else if (c2 == '<' && c3 == '=') {
                              replace(i-1, 2, String.valueOf(NumericRelation.LessThanOrEqual.SYMBOL), null);
                              foundChange = true;
                              break;
                          } else if (c2 == '>' && c3 == '=') {
                              replace(i-1, 2, String.valueOf(NumericRelation.GreaterThanOrEqual.SYMBOL), null);
                              foundChange = true;
                              break;

                          // Not-subset, proper subset, not superset, proper superset
                          } else if ((c2 == '!' || c2 == Not.SYMBOL) && c3 == SetRelation.Subset.SYMBOL) {
                              replace(i-1, 2, String.valueOf(SetRelation.NotSubset.SYMBOL), null);
                              foundChange = true;
                              break;
                          } else if (c2 == SetRelation.Subset.SYMBOL && c3 == SetRelation.Subset.SYMBOL) {
                              replace(i-1, 2, String.valueOf(SetRelation.ProperSubset.SYMBOL), null);
                              foundChange = true;
                              break;
                          } else if ((c2 == '!' || c2 == Not.SYMBOL) && c3 == SetRelation.Superset.SYMBOL) {
                              replace(i-1, 2, String.valueOf(SetRelation.NotSuperset.SYMBOL), null);
                              foundChange = true;
                              break;
                          } else if (c2 == SetRelation.Superset.SYMBOL && c3 == SetRelation.Superset.SYMBOL) {
                              replace(i-1, 2, String.valueOf(SetRelation.ProperSuperset.SYMBOL), null);
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
