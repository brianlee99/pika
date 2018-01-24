package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class CastingExpressionNode extends ParseNode {
	
	// private boolean mutable;
	
	public CastingExpressionNode(Token token) {
		super(token);
		// this.mutable = false; // by default
		// assert(token.isLextant(Keyword.CONST, Keyword.VAR));
	}

	public CastingExpressionNode(ParseNode node) {
		super(node);
	}
	
	
	////////////////////////////////////////////////////////////
	// attributes
	
	public Lextant getDeclarationType() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
	////////////////////////////////////////////////////////////
	// convenience factory
	
	public static CastingExpressionNode withChildren(Token token, ParseNode declaredName, ParseNode initializer) {
		CastingExpressionNode node = new CastingExpressionNode(token);
		
		node.appendChild(declaredName);
		node.appendChild(initializer);
		return node;
	}
	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}
