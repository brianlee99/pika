package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class FunctionDefinitionNode extends ParseNode {

	public FunctionDefinitionNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.FUNC));
	}

	public FunctionDefinitionNode(ParseNode node) {
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
	// setting function signature
	public void setFunctionSignature() {
		LambdaNode lambdaChild = (LambdaNode) this.child(1);
		lambdaChild.setFunctionSignature();
	}
	
	////////////////////////////////////////////////////////////
	// convenience factory
	public static FunctionDefinitionNode withChildren(Token token, ParseNode... children) {
		FunctionDefinitionNode node = new FunctionDefinitionNode(token);
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
