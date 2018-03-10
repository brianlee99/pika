package parseTree.nodeTypes;

import java.util.List;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.LextantToken;
import tokens.Token;

public class ParameterListNode extends ParseNode {

	
	public ParameterListNode(Token token) {
		super(token);
		// assert(token.isLextant(Keyword.BOOL, Keyword.CHAR, Keyword.INT, Keyword.FLOAT, Keyword.STRING, Keyword.RAT, Punctuator.ARRAY_TYPE));
	}
	public ParameterListNode(ParseNode node) {
		super(node);
	}

////////////////////////////////////////////////////////////
// attributes
	
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
////////////////////////////////////////////////////////////
// factory method
	
	public static ParameterListNode withChildren(Token token, List<ParseNode> children) {
		ParameterListNode node = new ParameterListNode(token);
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
