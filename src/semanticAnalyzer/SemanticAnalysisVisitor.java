package semanticAnalyzer;

import java.util.ArrayList;
import java.util.List;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import logging.PikaLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.ArrayPopulationNode;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.ControlFlowStatementNode;
import parseTree.nodeTypes.BlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReleaseStatementNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TypeNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.signatures.FunctionSignatures;
import semanticAnalyzer.signatures.PromotionChecker;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;

class SemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitLeave(ParseNode node) {
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructs larger than statements
	@Override
	public void visitEnter(ProgramNode node) {
		enterProgramScope(node);
	}
	public void visitLeave(ProgramNode node) {
		leaveScope(node);
	}
	public void visitEnter(BlockNode node) {
	}
	public void visitLeave(BlockNode node) {
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// helper methods for scoping.
	private void enterProgramScope(ParseNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}	
	@SuppressWarnings("unused")
	private void enterSubscope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createSubscope();
		node.setScope(scope);
	}		
	private void leaveScope(ParseNode node) {
		node.getScope().leave();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// statements and declarations
	@Override
	public void visitLeave(PrintStatementNode node) {
	}
	@Override
	public void visitLeave(DeclarationNode node) {
		IdentifierNode identifier = (IdentifierNode) node.child(0);
		ParseNode initializer = node.child(1);
		
		Type declarationType = initializer.getType();
		node.setType(declarationType);
		identifier.setType(declarationType);
		
		// add 'mutable' boolean to binding
		boolean isMutable = node.getToken().isLextant(Keyword.VAR) ? true : false;
		addBinding(identifier, declarationType, isMutable);
	}
	
	@Override
	public void visitLeave(AssignmentNode node) {
		ParseNode target = node.child(0);
		ParseNode expression = node.child(1);
		
		Type expressionType = expression.getType();
		Type targetType = target.getType();
		
		// check that the target node is actually targetable
		
		// target could be an IdentifierNode, OperatorNode (array Indexing)
		if (target instanceof IdentifierNode) {
			IdentifierNode identifier = (IdentifierNode) node.child(0);
			
			if (!identifier.isMutable()) {
				logError("the identifier was declared as const at " + node.getToken().getLocation());
			}

			if (expressionType != targetType) {
				logError("the identifier and expression types are not equal at " + node.getToken().getLocation());
			}
		}
		else if (target instanceof OperatorNode) {
			// check that it's an array indexing node
		}
	}
	
	public void visitLeave(ReleaseStatementNode node) {
		ParseNode child = node.child(0);
		Type childType = child.getType();
		if (!(childType instanceof Array) && childType != PrimitiveType.STRING) {
			logError("the expression must be a reference type at " + node.getToken().getLocation());
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// expressions
	@Override
	public void visitLeave(ArrayPopulationNode node) {
		// int numChildren = node.nChildren();
		List<Type> childTypes = new ArrayList<>();
		for (ParseNode child : node.getChildren()) {
			childTypes.add(child.getType());
		}
		
		Array arrayType = new Array(childTypes.get(0));
		node.setType(arrayType);
	}
	
	@Override
	public void visitLeave(OperatorNode node) {
        List<ParseNode> children = node.getChildren();
        List<Type> childTypes = new ArrayList<>();
        for (ParseNode child : children) {
            childTypes.add(child.getType());
        }

        Lextant operator = operatorFor(node);
        FunctionSignatures signatures = FunctionSignatures.signaturesOf(operator);
        FunctionSignature signature = signatures.acceptingSignature(childTypes);
        
        // if signature 
        if (signature.accepts(childTypes)) {
            node.setType(signature.resultType());
            node.setSignature(signature);
        }
        else {
        	// signature = PromotionChecker.promote(childTypes, signatures);
        	
        	
            typeCheckError(node, childTypes);
            node.setType(PrimitiveType.ERROR);
        }

	}
	
	@Override
	public void visitLeave(ControlFlowStatementNode node) {
		Type conditionType = node.child(0).getType();
		if (conditionType != PrimitiveType.BOOLEAN) {
			logError("the condition must evaluate to a boolean at " + node.getToken().getLocation());
		}
	}
	
	@Override
	public void visitLeave(TypeNode node) {
		Token token = node.getToken();
		if (token.isLextant(Keyword.BOOL)) {
			node.setType(PrimitiveType.BOOLEAN);
		}
		else if (token.isLextant(Keyword.CHAR)) {
			node.setType(PrimitiveType.CHARACTER);
		}
		else if (token.isLextant(Keyword.INT)) {
			node.setType(PrimitiveType.INTEGER);
		}
		else if (token.isLextant(Keyword.FLOAT)) {
			node.setType(PrimitiveType.FLOATING);
		}
		else if (token.isLextant(Keyword.STRING)) {
			node.setType(PrimitiveType.STRING);
		}
		else if (token.isLextant(Keyword.RAT)) {
			node.setType(PrimitiveType.RATIONAL);
		}
		else if (token.isLextant(Punctuator.ARRAY_TYPE)) {
			TypeNode subtypeNode = (TypeNode) node.child(0);
			node.setType(new Array(subtypeNode.getType()));
		}
	}
	
	private Lextant operatorFor(OperatorNode node) {
		LextantToken token = (LextantToken) node.getToken();
		return token.getLextant();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// simple leaf nodes
	@Override
	public void visit(BooleanConstantNode node) {
		node.setType(PrimitiveType.BOOLEAN);
	}
	@Override
	public void visit(ErrorNode node) {
		node.setType(PrimitiveType.ERROR);
	}
	@Override
	public void visit(IntegerConstantNode node) {
		node.setType(PrimitiveType.INTEGER);
	}
	@Override
	public void visit(FloatingConstantNode node) {
		node.setType(PrimitiveType.FLOATING);
	}
	@Override
	public void visit(CharacterConstantNode node) {
		node.setType(PrimitiveType.CHARACTER);
	}
	@Override
	public void visit(StringConstantNode node) {
		node.setType(PrimitiveType.STRING);
	}
	@Override
	public void visit(NewlineNode node) {
	}
	@Override
	public void visit(SpaceNode node) {
	}
	///////////////////////////////////////////////////////////////////////////
	// IdentifierNodes, with helper methods
	@Override
	public void visit(IdentifierNode node) {
		if(!isBeingDeclared(node)) {		
			Binding binding = node.findVariableBinding();
			
			node.setType(binding.getType());
			node.setBinding(binding);
		}
		// else parent DeclarationNode does the processing.
	}
	private boolean isBeingDeclared(IdentifierNode node) {
		ParseNode parent = node.getParent();
		return (parent instanceof DeclarationNode) && (node == parent.child(0));
	}
	private void addBinding(IdentifierNode identifierNode, Type type, boolean isMutable) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type, isMutable);
		identifierNode.setBinding(binding);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// error logging/printing
//	private void castingTypeCheckError(ParseNode node, List<Type> operandTypes) {
//		Token token = node.getToken();
//		
//		logError("casting operator not defined for types " 
//				 + operandTypes  + " at " + token.getLocation());	
//	}
	private void typeCheckError(ParseNode node, List<Type> operandTypes) {
		Token token = node.getToken();
		
		logError("operator " + token.getLexeme() + " not defined for types " 
				 + operandTypes  + " at " + token.getLocation());	
	}
	private void logError(String message) {
		PikaLogger log = PikaLogger.getLogger("compiler.semanticAnalyzer");
		log.severe(message);
	}
}