package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.signatures.FunctionSignature;

import java.util.List;

import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class ArrayPopulationNode extends ParseNode {

	public ArrayPopulationNode(Token token) {
		super(token);
		assert(token instanceof LextantToken);
	}

	public ArrayPopulationNode(ParseNode node) {
		super(node);
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
	public static ArrayPopulationNode withChildren(Token token, List<ParseNode> childrenNodes) {
		ArrayPopulationNode node = new ArrayPopulationNode(token);
		for (ParseNode childNode : childrenNodes ) {
			node.appendChild(childNode);
		};
		
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
