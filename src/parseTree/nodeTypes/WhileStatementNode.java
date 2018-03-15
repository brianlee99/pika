package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class WhileStatementNode extends ParseNode {
	private String loopLabel, endLabel;
	
	public WhileStatementNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.WHILE));
	}

	public WhileStatementNode(ParseNode node) {
		super(node);
	}
	////////////////////////////////////////////////////////////
	// setting labels
	public void setLoopLabel(String label) {
		this.loopLabel = label;
	}
	public void setEndLabel(String label) {
		this.endLabel = label;
	}
	public String getLoopLabel() {
		return loopLabel;
	}
	public String getEndLabel() {
		return endLabel;
	}
	
	////////////////////////////////////////////////////////////
	// attributes
	public Lextant getControlFlowType() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken) token;
	}	
	
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
