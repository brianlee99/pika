package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import semanticAnalyzer.types.PrimitiveType;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import tokens.LextantToken;
import tokens.Token;

public class TypeNode extends ParseNode {

	
	public TypeNode(Token token) {
		super(token);
		assert(token.isLextant(Keyword.BOOL, Keyword.CHAR, Keyword.INT, Keyword.FLOAT, Keyword.STRING));
		
		// Assigning a type to a TypeNode is convenient
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
		
	}
	public TypeNode(ParseNode node) {
		super(node);
	}

////////////////////////////////////////////////////////////
// attributes
	
	public LextantToken lextantToken() {
		return (LextantToken)token;
	}	

///////////////////////////////////////////////////////////
// accept a visitor
	
	public void accept(ParseNodeVisitor visitor) {
		visitor.visit(this);
	}

}
