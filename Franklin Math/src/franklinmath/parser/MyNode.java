/*
 * This class acts as a parent to the SimpleNode class in jjtree.  
 */

package franklinmath.parser;

/**
 *
 * @author Allen Jordan
 */
public class MyNode {
	protected java.util.Vector<Token> tokenList = new java.util.Vector<Token>();
	
	public void addToken(Token token) {
		if (token != null) tokenList.add(token);
	}

	public java.util.Vector<Token> getTokenList() {
		return tokenList;
	}
}
