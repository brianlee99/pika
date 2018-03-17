package parseTree.nodeTypes;

import java.util.ArrayList;
import java.util.List;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.LambdaType;
import semanticAnalyzer.types.Type;
import tokens.LextantToken;
import tokens.Token;

public class TypeListNode extends ParseNode {

	
	public TypeListNode(Token token) {
		super(token);
	}
	public TypeListNode(ParseNode node) {
		super(node);
	}

////////////////////////////////////////////////////////////
// attributes
	
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
////////////////////////////////////////////////////////////
// function signature
	public void setLambdaType() {
		List<Type> paramListTypes = new ArrayList<>();
		int nChildren = nChildren();
		for (int i = 0; i < nChildren; i++) {
			TypeNode typeChild = (TypeNode) child(i);
			typeChild.setTypeByToken();
			Type type = typeChild.getType();
			paramListTypes.add(type);
		}
		LambdaType lambdaType = new LambdaType(paramListTypes, null);
		setType(lambdaType);
	}
	
	
////////////////////////////////////////////////////////////
// factory method
	
	public static TypeListNode withChildren(Token token, List<ParseNode> children) {
		TypeListNode node = new TypeListNode(token);
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
