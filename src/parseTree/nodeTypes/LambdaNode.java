package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Punctuator;
import tokens.LextantToken;
import tokens.Token;

public class LambdaNode extends ParseNode {

	
	public LambdaNode(Token token) {
		super(token);
		// assert(token.isLextant(Keyword.BOOL, Keyword.CHAR, Keyword.INT, Keyword.FLOAT, Keyword.STRING, Keyword.RAT, Punctuator.ARRAY_TYPE, Keyword.VOID));
	}
	public LambdaNode(ParseNode node) {
		super(node);
	}

////////////////////////////////////////////////////////////
// attributes
	
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
////////////////////////////////////////////////////////////
// factory method
	
	public static LambdaNode withChildren(Token token, ParseNode... children) {
		LambdaNode node = new LambdaNode(token);
		for (ParseNode child : children) {
			node.appendChild(child);
		}
		return node;
	}

///////////////////////////////////////////////////////////
// accept a visitor
	
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}

}
