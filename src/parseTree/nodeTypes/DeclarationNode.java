package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class DeclarationNode extends ParseNode {
	
	// private boolean mutable;
	
	public DeclarationNode(Token token) {
		super(token);
		// this.mutable = false; // by default
		assert(token.isLextant(Keyword.CONST, Keyword.VAR));
	}

	public DeclarationNode(ParseNode node) {
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
	//get/set mutability 
	
//	public boolean isMutable() {
//		return mutable;
//	}
//	public void setMutable(boolean mutable) {
//		this.mutable = mutable;
//	}
	
	////////////////////////////////////////////////////////////
	// convenience factory
	
	public static DeclarationNode withChildren(Token token, ParseNode declaredName, ParseNode initializer) {
		DeclarationNode node = new DeclarationNode(token);
		
		// set mutability
//		if (token.isLextant(Keyword.CONST))
//			node.mutable = false;
//		else if (token.isLextant(Keyword.VAR))
//			node.mutable = true;
		
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
