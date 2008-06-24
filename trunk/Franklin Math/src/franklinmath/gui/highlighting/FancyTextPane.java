package franklinmath.gui.highlighting;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import com.Ostermiller.Syntax.*;

/**
 * A handy extension of JTextPane that highlights Franklin Math syntax
 * @author Allen Jordan
 */
public class FancyTextPane extends JTextPane {

    protected HighlightedDocument document;

    public FancyTextPane(HighlightedDocument doc) {
        super(doc);
        document = doc;
        doc.setHighlightStyle(HighlightedDocument.JAVA_STYLE);

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
}
