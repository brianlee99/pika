package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class BreakNode extends ParseNode {
	public BreakNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.BREAK));
	}

	public BreakNode(ParseNode node) {
		super(node);
	}

	
	////////////////////////////////////////////////////////////
	// attributes
	public Lextant getControlFlowType() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken) token;
	}	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
		
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}

}
