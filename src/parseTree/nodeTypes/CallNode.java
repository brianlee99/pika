package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class CallNode extends ParseNode {

	public CallNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.CALL));
	}

	public CallNode(ParseNode node) {
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
	
	// Two children
	public static CallNode withChildren(Token token, ParseNode... children) {
		CallNode node = new CallNode(token);
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
