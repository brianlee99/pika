package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class ControlFlowStatementNode extends ParseNode {
	public ControlFlowStatementNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.IF, Keyword.WHILE));
	}

	public ControlFlowStatementNode(ParseNode node) {
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
	
	public static ControlFlowStatementNode withChildren(Token token, ParseNode ... children) {
		ControlFlowStatementNode node = new ControlFlowStatementNode(token);
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
