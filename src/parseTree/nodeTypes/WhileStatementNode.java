package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import tokens.Token;

public class WhileStatementNode extends ParseNode {
	
	public WhileStatementNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.WHILE));
	}

	public WhileStatementNode(ParseNode node) {
		super(node);
	}

	
	////////////////////////////////////////////////////////////
	// attributes
	public static WhileStatementNode withChildren(Token token, ParseNode ... children) {
		WhileStatementNode node = new WhileStatementNode(token);
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
