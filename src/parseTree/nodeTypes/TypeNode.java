package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.PrimitiveType;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Punctuator;
import tokens.LextantToken;
import tokens.Token;

public class TypeNode extends ParseNode {

	
	public TypeNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.BOOL, Keyword.CHAR, Keyword.INT, Keyword.FLOAT, Keyword.STRING, Keyword.RAT, Punctuator.ARRAY_TYPE, Keyword.VOID, Punctuator.LAMBDA_TYPE));
	}
	public TypeNode(ParseNode node) {
		super(node);
	}

	////////////////////////////////////////////////////////////
	// attributes
	
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
	public void setTypeByToken() {
		Token token = getToken();
		if (token.isLextant(Keyword.BOOL)) {
			setType(PrimitiveType.BOOLEAN);
		}
		else if (token.isLextant(Keyword.CHAR)) {
			setType(PrimitiveType.CHARACTER);
		}
		else if (token.isLextant(Keyword.INT)) {
			setType(PrimitiveType.INTEGER);
		}
		else if (token.isLextant(Keyword.FLOAT)) {
			setType(PrimitiveType.FLOATING);
		}
		else if (token.isLextant(Keyword.STRING)) {
			setType(PrimitiveType.STRING);
		}
		else if (token.isLextant(Keyword.RAT)) {
			setType(PrimitiveType.RATIONAL);
		}
		else if (token.isLextant(Punctuator.ARRAY_TYPE)) {
			TypeNode subtypeNode = (TypeNode) child(0);
			setType(new ArrayType(subtypeNode.getType()));
		}
		else if (token.isLextant(Keyword.VOID)) {
			setType(PrimitiveType.VOID);
		}
	}
	
	////////////////////////////////////////////////////////////
	// factory method
	
	public static TypeNode withChildren(Token token, ParseNode... children) {
		TypeNode node = new TypeNode(token);
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
