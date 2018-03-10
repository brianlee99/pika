package parseTree.nodeTypes;

import java.util.List;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.LextantToken;
import tokens.Token;

public class ParameterSpecificationNode extends ParseNode {

	
	public ParameterSpecificationNode(Token token) {
		super(token);
		// assert(token.isLextant(Keyword.BOOL, Keyword.CHAR, Keyword.INT, Keyword.FLOAT, Keyword.STRING, Keyword.RAT, Punctuator.ARRAY_TYPE));
	}
	public ParameterSpecificationNode(ParseNode node) {
		super(node);
	}

////////////////////////////////////////////////////////////
// attributes
	
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
////////////////////////////////////////////////////////////
// factory method
	
	public static ParameterSpecificationNode withChildren(Token token, ParseNode ... children) {
		ParameterSpecificationNode node = new ParameterSpecificationNode(token);
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
