package franklinmath.gui.highlighting;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * A handy extension of JTextPane that highlights Franklin Math syntax
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

    public void Append(Image image) {
        try {
            StyledDocument doc = (StyledDocument) this.getDocument();
            Style style = doc.addStyle("ImageStyle", null);
            StyleConstants.setIcon(style, new ImageIcon(image));
            doc.insertString(doc.getLength(), " ", style);
        } catch (BadLocationException ex) {
        }
    }
}
