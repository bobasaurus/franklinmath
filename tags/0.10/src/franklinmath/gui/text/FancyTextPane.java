/*
Copyright 2009 Allen Franklin Jordan (allen.jordan@gmail.com).

This file is part of Franklin Math.

Franklin Math is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Franklin Math is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Franklin Math.  If not, see <http://www.gnu.org/licenses/>.
*/

package franklinmath.gui.text;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * A handy extension of JTextPane that adds functionality
 * @author Allen Jordan
 */
public class FancyTextPane extends JTextPane {

    protected StyledDocument document;

    public FancyTextPane(StyledDocument doc) {
        super(doc);
        document = doc;

        //try to avoid unwanted beeps (probably does nothing useful)
        document.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
    }

    public void Append(String value) {
        try {
            AttributeSet attribSet = document.getDefaultRootElement().getAttributes().copyAttributes();
            document.insertString(document.getLength(), value, attribSet);
        } catch (BadLocationException ex) {
        }
    }

    public void Prepend(String value) {
        try {
            AttributeSet attribSet = document.getDefaultRootElement().getAttributes().copyAttributes();
            document.insertString(0, value, attribSet);
        } catch (BadLocationException ex) {
        }
    }
    
    public void InsertAt(String value, int location) {
        try {
            AttributeSet attribSet = document.getDefaultRootElement().getAttributes().copyAttributes();
            document.insertString(location, value, attribSet);
        } catch (BadLocationException ex) {
        }
    }
    
    public void Append(Image image) {
        try {
            StyledDocument doc = (StyledDocument) this.getDocument();
            Style style = doc.addStyle("ImageStyle", null);
            StyleConstants.setIcon(style, new ImageIcon(image));
            doc.insertString(doc.getLength(), " ", style);
        } catch (BadLocationException ex) {
        }
    }
    
    public void Prepend(Image image, boolean addNewline) {
        try {
            StyledDocument doc = (StyledDocument) this.getDocument();
            Style style = doc.addStyle("ImageStyle", null);
            StyleConstants.setIcon(style, new ImageIcon(image));
            doc.insertString(0, " ", style);
            if (addNewline) doc.insertString(1, "\n", style);
        } catch (BadLocationException ex) {
        }
    }
    
    public void InsertAt(Image image, int location, boolean addNewline) {
        try {
            StyledDocument doc = (StyledDocument) this.getDocument();
            Style style = doc.addStyle("ImageStyle", null);
            StyleConstants.setIcon(style, new ImageIcon(image));
            doc.insertString(location, " ", style);
            if (addNewline) doc.insertString(location+1, "\n", style);
        } catch (BadLocationException ex) {
        }
    }
}
