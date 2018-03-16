package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.Type;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Punctuator;
import tokens.LextantToken;
import tokens.Token;

public class LambdaNode extends ParseNode {
	private String startLabel, endLabel, exitLabel;
	
	public LambdaNode(Token token) {
		super(token);
	}
	public LambdaNode(ParseNode node) {
		super(node);
	}	
	
	////////////////////////////////////////////////////////////
	// setting labels
	public void setStartLabel(String label) {
		this.startLabel = label;
	}
	public void setEndLabel(String label) {
		this.endLabel = label;
	}
	public void setExitLabel(String label) {
		this.exitLabel = label;
	}
	public String getStartLabel() {
		return startLabel;
	}
	public String getEndLabel() {
		return endLabel;
	}
	public String getExitLabel() {
		return exitLabel;
	}
	////////////////////////////////////////////////////////////
	// attributes
		
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	
		
	////////////////////////////////////////////////////////////
	// setting function signature
	public void setFunctionSignature() {
		LambdaParamTypeNode lambdaParamChild = (LambdaParamTypeNode) this.child(0);
		lambdaParamChild.setFunctionSignature();
		Type lambdaType = lambdaParamChild.getType();
		setType(lambdaType);
	}
	
	////////////////////////////////////////////////////////////
	// factory method
	
	public static LambdaNode withChildren(Token token, ParseNode... children) {
		LambdaNode node = new LambdaNode(token);
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
