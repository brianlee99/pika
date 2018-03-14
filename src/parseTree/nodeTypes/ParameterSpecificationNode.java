package parseTree.nodeTypes;

import java.util.ArrayList;
import java.util.List;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.LambdaType;
import semanticAnalyzer.types.Type;
import tokens.LextantToken;
import tokens.Token;

public class ParameterSpecificationNode extends ParseNode {

	
	public ParameterSpecificationNode(Token token) {
		super(token);
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
	// setting function signature
	public void setParameterType() {
		TypeNode child = (TypeNode) child(0);
		child.setTypeByToken();
		Type type = child.getType();
		setType(type);
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
