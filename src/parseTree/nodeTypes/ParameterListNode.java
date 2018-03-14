package parseTree.nodeTypes;

import java.util.ArrayList;
import java.util.List;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.LambdaType;
import semanticAnalyzer.types.Type;
import tokens.LextantToken;
import tokens.Token;

public class ParameterListNode extends ParseNode {

	
	public ParameterListNode(Token token) {
		super(token);
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
	// setting function signature
	public void setParameterListType() {
		List<Type> paramListTypes = new ArrayList<>();
		int nChildren = nChildren();
		for (int i = 0; i < nChildren; i++) {
			ParameterSpecificationNode paramSpecChild = (ParameterSpecificationNode) child(i);
			paramSpecChild.setParameterType();
			Type type = paramSpecChild.getType();
			paramListTypes.add(type);
		}
		LambdaType lambdaType = new LambdaType(paramListTypes, null);
		setType(lambdaType);
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
