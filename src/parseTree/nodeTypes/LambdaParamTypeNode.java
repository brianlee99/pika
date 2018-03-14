package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Punctuator;
import tokens.LextantToken;
import tokens.Token;

public class LambdaParamTypeNode extends ParseNode {

	
	public LambdaParamTypeNode(Token token) {
		super(token);
		// assert(token.isLextant(Keyword.BOOL, Keyword.CHAR, Keyword.INT, Keyword.FLOAT, Keyword.STRING, Keyword.RAT, Punctuator.ARRAY_TYPE, Keyword.VOID));
	}
	public LambdaParamTypeNode(ParseNode node) {
		super(node);
	}

	////////////////////////////////////////////////////////////
	// attributes
	
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
	////////////////////////////////////////////////////////////
	// setting function signature
	public void setFunctionSignature() {
		ParameterListNode paramListChild = (ParameterListNode) this.child(0);
		paramListChild.setFunctionSignature();
	}
	
	////////////////////////////////////////////////////////////
	// factory method
	
	public static LambdaParamTypeNode withChildren(Token token, ParseNode... children) {
		LambdaParamTypeNode node = new LambdaParamTypeNode(token);
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
