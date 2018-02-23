package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class ReleaseStatementNode extends ParseNode {
	public ReleaseStatementNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.RELEASE));
	}

	public ReleaseStatementNode(ParseNode node) {
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
	
	public static ReleaseStatementNode withChildren(Token token, ParseNode ... children) {
		ReleaseStatementNode node = new ReleaseStatementNode(token);
		for (ParseNode child : children) {
			node.appendChild(child);
		}
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
