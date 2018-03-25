package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class ForStatementNode extends ParseNode {
	private String loopLabel, endLabel;
	
	public ForStatementNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.INDEX, Keyword.ELEM));
	}

	public ForStatementNode(ParseNode node) {
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
	
	public static ForStatementNode withChildren(Token token, ParseNode ... children) {
		ForStatementNode node = new ForStatementNode(token);
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
