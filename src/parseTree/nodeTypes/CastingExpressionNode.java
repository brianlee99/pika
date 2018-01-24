package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

// For now, basically a clone of BinaryOperatorNode.
public class CastingExpressionNode extends ParseNode {
	
	
	public CastingExpressionNode(Token token) {
		super(token);
		assert(token instanceof LextantToken);
	}

	public CastingExpressionNode(ParseNode node) {
		super(node);
	}
	
	////////////////////////////////////////////////////////////
	// function signature
	private FunctionSignature signature = FunctionSignature.nullInstance();
	
	public final FunctionSignature getSignature() {
		return signature;
	}
	
	public final void setSignature(FunctionSignature signature) {
		this.signature = signature;
	}
	
	////////////////////////////////////////////////////////////
	// attributes
	
	public Lextant getOperator() {
		return lextantToken().getLextant();
	}
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
	
	
	////////////////////////////////////////////////////////////
	// convenience factory
	
	public static CastingExpressionNode withChildren(Token token, ParseNode left, ParseNode right) {
		CastingExpressionNode node = new CastingExpressionNode(token);
		node.appendChild(left);
		node.appendChild(right);
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
