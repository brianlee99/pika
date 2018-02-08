package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import tokens.Token;

public class IfStatementNode extends ParseNode {
	
	public IfStatementNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.PRINT));
	}

	public IfStatementNode(ParseNode node) {
		super(node);
	}

	
	////////////////////////////////////////////////////////////
	// attributes
	public static IfStatementNode withChildren(Token token, ParseNode declaredName, ParseNode assignedValue) {
		IfStatementNode node = new IfStatementNode(token);
		node.appendChild(declaredName);
		node.appendChild(assignedValue);
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
