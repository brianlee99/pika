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
import parseTree.nodeTypes.ParameterSpecificationNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.BreakNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.ContinueNode;
import parseTree.nodeTypes.IfStatementNode;
import parseTree.nodeTypes.BlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.FunctionInvocationNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.LambdaNode;
import parseTree.nodeTypes.LambdaParamTypeNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReleaseStatementNode;
import parseTree.nodeTypes.ReturnNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.WhileStatementNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.signatures.FunctionSignatures;
import semanticAnalyzer.signatures.PromotionHelper;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.LambdaType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;
import tokens.LextantToken;
import tokens.Token;

class SecondSemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitLeave(ParseNode node) {
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructs larger than statements
	@Override
	public void visitEnter(ProgramNode node) {
		enterScope(node);
	}
	public void visitLeave(ProgramNode node) {
		leaveScope(node);
	}
	public void visitEnter(BlockNode node) {
		ParseNode parent = node.getParent();
		if (parent instanceof LambdaNode) {
			enterScope(node);
		} else {
			enterSubscope(node);
		}
	}
	public void visitLeave(BlockNode node) {
		leaveScope(node);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// helper methods for scoping.
	private void enterScope(ParseNode node) {
		node.getScope().enter();
	}	
	private void enterSubscope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createSubscope();
		node.setScope(scope);
		scope.enter();
	}		
	private void leaveScope(ParseNode node) {
		node.getScope().leave();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// function definitions and such

	@Override
	public void visitEnter(LambdaNode node) {
		enterScope(node);
	}
	@Override
	public void visitLeave(LambdaNode node) {
		leaveScope(node);
	}

	@Override
	public void visitLeave(ParameterSpecificationNode node) {
		IdentifierNode identifier = (IdentifierNode) node.child(1);
		Type type = node.child(0).getType();
		
		if (type.equivalent(PrimitiveType.VOID)) {
			logError("The parameter may not be a void type");
			return;
		}
		
		identifier.setType(type);
		addBinding(identifier, type, false);
	}
	///////////////////////////////////////////////////////////////////////////
	// Return
	@Override
	public void visitLeave(ReturnNode node) {
		boolean insideLambda = findLambdaStatement(node);
		if (!insideLambda) {			
			logError("The return statement is not within a lambda");
			return;
		}
		LambdaNode lambda = findLambdaNode(node);
		Type lambdaReturnType = ((LambdaType) lambda.getType()).getReturnType();
		Type returnedType = (node.nChildren() == 1) ? node.child(0).getType() : PrimitiveType.VOID;
		node.setType(returnedType);
		
		if (!lambdaReturnType.equivalent(returnedType)) {
			logError("Returned type does not match the lambda return type");
			return;
		}
	}
	private boolean findLambdaStatement(ParseNode node) {
		if (node instanceof LambdaNode) {
			return true;
		}
		else if (node instanceof ProgramNode) {
			return false;
		}
		return findLambdaStatement(node.getParent());
	}
	private LambdaNode findLambdaNode(ParseNode node) {
		for (ParseNode parent : node.pathToRoot()) {
			if (parent instanceof LambdaNode) {
				return (LambdaNode) parent;
			}
		}
		return null; 
	}
	///////////////////////////////////////////////////////////////////////////
	// Function Invocation
	@Override
	public void visitLeave(FunctionInvocationNode node) {
		int nChildren = node.nChildren();
		List<Type> expressionTypes = new ArrayList<>();
		for (int i = 1; i < nChildren; i++) {
			Type type = node.child(i).getType();
			expressionTypes.add(type);
		}
		// check the function signature
		Type type = node.child(0).getType();
		
		if (!(type instanceof LambdaType)) {
			logError("The expression is not of a lambda type");
			return;
		}
		
		LambdaType lambdaType = (LambdaType) type;
		List<Type> paramTypes = lambdaType.getInputTypes();
		
		if (!parametersMatch(paramTypes, expressionTypes)) {
			logError("The parameter types do not match");
			return;
		}
		
		Type returnType = lambdaType.getReturnType();
		node.setType(returnType);
	}
	private boolean parametersMatch(List<Type> paramTypes, List<Type> expressionTypes) {
		if (paramTypes.size() != expressionTypes.size()) {
			return false;
		}
		int length = paramTypes.size();
		for (int i = 0; i < length; i++) {
			Type paramType = paramTypes.get(i);
			Type expressionType = expressionTypes.get(i);
			if (!paramType.equivalent(expressionType)) {
				return false;
			}
		}
		return true;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Break and continue
	@Override
	public void visit(BreakNode node) {
		boolean insideWhile = findWhileStatement(node);
		if (!insideWhile) {
			logError("Break statement is not inside a while statement");
		}
	}
	@Override
	public void visit(ContinueNode node) {
		boolean insideWhile = findWhileStatement(node);
		if (!insideWhile) {
			logError("Continue statement is not inside a while statement");
		}
	}
	private boolean findWhileStatement(ParseNode node) {
		if (node.getToken().isLextant(Keyword.WHILE)) {
			return true;
		}
		else if (node instanceof ProgramNode) {
			return false;
		}
		return findWhileStatement(node.getParent());
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
		
		boolean isMutable = node.getToken().isLextant(Keyword.VAR) ? true : false;
		addBinding(identifier, declarationType, isMutable);
	}
	
	@Override
	public void visitLeave(AssignmentNode node) {
		ParseNode target = node.child(0);
		ParseNode expression = node.child(1);
		
		Type expressionType = expression.getType();
		Type targetType = target.getType();
		
		if (target instanceof IdentifierNode) {
			IdentifierNode identifier = (IdentifierNode) node.child(0);
			
			if (!identifier.isMutable()) {
				logError("the identifier was declared as const at " + node.getToken().getLocation());
			}
			
			promoteTargetType(expressionType, targetType, expression, node);
			expressionType = node.child(0).getType();
			targetType = node.child(1).getType();
			
			if (!expressionType.equivalent(targetType)) {
				logError("the identifier and expression types are not equal at " + node.getToken().getLocation());
			}
		}
	}
	
	public void promoteTargetType(Type expressionType, Type targetType, ParseNode child, ParseNode node) {
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		if (targetType == PrimitiveType.INTEGER) {
			if (expressionType == PrimitiveType.CHARACTER) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);

				OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, intNode);
				node.replaceChild(child, castingNode);
				visitLeave(intNode);
				visitLeave(castingNode);
			}
		}
		else if (targetType == PrimitiveType.FLOATING) {
			if (expressionType == PrimitiveType.CHARACTER) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
				TypeNode floatNode = new TypeNode(floatToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, child, intNode);
				OperatorNode floatCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, floatNode);
				
				node.replaceChild(child, floatCastingNode);
				visitLeave(intNode);
				visitLeave(floatNode);
				visitLeave(intCastingNode);
				visitLeave(floatCastingNode);
			}
			else if (expressionType == PrimitiveType.INTEGER) {
				Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
				TypeNode floatNode = new TypeNode(floatToken);

				OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, floatNode);
				node.replaceChild(child, castingNode);
				visitLeave(floatNode);
				visitLeave(castingNode);
			}
		}
		else if (targetType == PrimitiveType.RATIONAL) {
			if (expressionType == PrimitiveType.CHARACTER) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
				TypeNode ratNode = new TypeNode(ratToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, child, intNode);
				OperatorNode ratCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, ratNode);
				
				node.replaceChild(child, ratCastingNode);
				visitLeave(intNode);
				visitLeave(ratNode);
				visitLeave(intCastingNode);
				visitLeave(ratCastingNode);
			}
			else if (expressionType == PrimitiveType.INTEGER) {
				Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
				TypeNode ratNode = new TypeNode(ratToken);

				OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, ratNode);
				node.replaceChild(child, castingNode);
				visitLeave(ratNode);
				visitLeave(castingNode);
			}
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
		List<Type> childTypes = new ArrayList<>();
		List<ParseNode> children = node.getChildren();
		for (ParseNode child : node.getChildren()) {
			childTypes.add(child.getType());
		}
		promoteArray(childTypes, children, node);
		if (typeCheckArray(children)) {
			Array arrayType = new Array(children.get(0).getType());
			node.setType(arrayType);
		}
		else {
	        typeCheckError(node, childTypes);
	        node.setType(PrimitiveType.ERROR);
		}
	}
	
	public void promoteArray(List<Type> childTypes, List<ParseNode> children, ParseNode node) {
		Type highestType = childTypes.get(0);
		for (Type type : childTypes) {
			if (	type != PrimitiveType.CHARACTER &&
					type != PrimitiveType.INTEGER 	&& 
					type != PrimitiveType.RATIONAL	&& 
					type != PrimitiveType.FLOATING	) {
				return;
			}
			if (highestType == PrimitiveType.CHARACTER) {
				if (type == PrimitiveType.RATIONAL || type == PrimitiveType.FLOATING || type == PrimitiveType.INTEGER)
					highestType = type;
			}
			else if (highestType == PrimitiveType.INTEGER) {
				if (type == PrimitiveType.RATIONAL || type == PrimitiveType.FLOATING)
					highestType = type;
			}
			else if (highestType == PrimitiveType.RATIONAL) {
				if (type == PrimitiveType.FLOATING)
					return;
			}
			else if (highestType == PrimitiveType.FLOATING) {
				if (type == PrimitiveType.RATIONAL)
					return;
			}
		}
		
		List<ParseNode> childrenCopy = new ArrayList<>(children);
		// once you determine the highest type, cast all nodes to that type
		for (ParseNode child : childrenCopy) {
			Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
			Type childType = child.getType();
			
			if (highestType == PrimitiveType.INTEGER) {
				if (childType == PrimitiveType.CHARACTER) {
					Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
					TypeNode intNode = new TypeNode(intToken);

					OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, intNode);
					node.replaceChild(child, castingNode);
					visitLeave(intNode);
					visitLeave(castingNode);
				}
			}
			else if (highestType == PrimitiveType.RATIONAL) {
				if (childType == PrimitiveType.CHARACTER) {
					Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
					TypeNode intNode = new TypeNode(intToken);
					
					Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
					TypeNode ratNode = new TypeNode(ratToken);
					
					OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, child, intNode);
					OperatorNode ratCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, ratNode);
					
					node.replaceChild(child, ratCastingNode);
					visitLeave(intNode);
					visitLeave(ratNode);
					visitLeave(intCastingNode);
					visitLeave(ratCastingNode);
				}
				else if (childType == PrimitiveType.INTEGER) {
					Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
					TypeNode ratNode = new TypeNode(ratToken);

					OperatorNode ratCastingNode = OperatorNode.withChildren(castingToken, child, ratNode);
					node.replaceChild(child, ratCastingNode);
					visitLeave(ratNode);
					visitLeave(ratCastingNode);
				}

			}
			else if (highestType == PrimitiveType.FLOATING) {
				if (childType == PrimitiveType.CHARACTER) {
					Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
					TypeNode intNode = new TypeNode(intToken);
					
					Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
					TypeNode floatNode = new TypeNode(floatToken);
					
					OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, child, intNode);
					OperatorNode floatCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, floatNode);
					
					node.replaceChild(child, floatCastingNode);
					visitLeave(intNode);
					visitLeave(floatNode);
					visitLeave(intCastingNode);
					visitLeave(floatCastingNode);
				}
				else if (childType == PrimitiveType.INTEGER) {
					Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
					TypeNode floatNode = new TypeNode(floatToken);

					OperatorNode floatCastingNode = OperatorNode.withChildren(castingToken, child, floatNode);
					node.replaceChild(child, floatCastingNode);
					visitLeave(floatNode);
					visitLeave(floatCastingNode);
				}
			}
		}
	}
	
	public boolean typeCheckArray(List<ParseNode> children) {
		Type type = children.get(0).getType();
		for (ParseNode child : children) {
			if (!child.getType().equivalent(type))
				return false;
		}
		return true;
	}
	
	@Override
	public void visitLeave(OperatorNode node) {
        List<ParseNode> children = node.getChildren();
        List<Type> childTypes = new ArrayList<>();
        for (ParseNode child : children) {
            childTypes.add(child.getType());
        }
        
        // Check that a new array operator is not set to a 'void' array
        if (node.getToken().isLextant(Keyword.NEW)) {
        	Type type = node.child(0).getType();
        	if (type.equivalent(PrimitiveType.VOID)) {
        		logError("Cannot initialize a void-array");
        		return;
        	}
        }

        Lextant operator = operatorFor(node);
        FunctionSignatures signatures = FunctionSignatures.signaturesOf(operator);
        
        // Try matching without promoting any of the operands.
        FunctionSignature signature = matchSignatureWithoutPromotion(signatures, childTypes);
		if (signature.accepts(childTypes)) {
			node.setType(signature.resultType());
			node.setSignature(signature);
			return;
		}
		
		// If there is only one child, then try promoting just the one.
		if (childTypes.size() == 1) {
			// just do the left side
			List<FunctionSignature> matchingSignatures = PromotionHelper.matchSignatureWithLeftPromotion(signatures, childTypes);
			int length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
	        	ParseNode child = children.get(0);
				Type targetType = signature.getParamTypes()[0];
	        	PromotionHelper.tryLeft(node, signature, child, targetType);
	        }
	        else {
		        typeCheckError(node, childTypes);
		        node.setType(PrimitiveType.ERROR);
	        }
	        return;
		}

		// If the left side is a TypeNode, then try promoting just the right side.
		else if (children.get(0) instanceof TypeNode) {
			List<FunctionSignature> matchingSignatures = PromotionHelper.matchSignatureWithRightPromotion(signatures, childTypes);
			// just do the right side
	        int length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
	        	ParseNode child = children.get(1);
				Type targetType = signature.getParamTypes()[1];
	        	PromotionHelper.tryLeft(node, signature, child, targetType);
	        }
	        else {
		        typeCheckError(node, childTypes);
		        node.setType(PrimitiveType.ERROR);
	        }
			return;
		}
		
		
		// If the right side is a TypeNode, then try promoting just the left side.
		else if (children.get(1) instanceof TypeNode) {
			List<FunctionSignature> matchingSignatures = PromotionHelper.matchSignatureWithLeftPromotion(signatures, childTypes);
			// just do the right side
	        int length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
	        	ParseNode child = children.get(0);
				Type targetType = signature.getParamTypes()[0];
	        	PromotionHelper.tryLeft(node, signature, child, targetType);
	        }
	        else {
		        typeCheckError(node, childTypes);
		        node.setType(PrimitiveType.ERROR);
	        }
			return;
		}
		else {
	        // Try promoting just the left side.
	        List<FunctionSignature> matchingSignatures = PromotionHelper.matchSignatureWithLeftPromotion(signatures, childTypes);
	        
	        int length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
	        	ParseNode child = children.get(0);
				Type targetType = signature.getParamTypes()[0];
	        	PromotionHelper.tryLeft(node, signature, child, targetType);
	        	return;
	        }
	        else if (length > 1) {
		        typeCheckError(node, childTypes);
		        node.setType(PrimitiveType.ERROR);
	        	return;
	        }
	        
	        // Try promoting just the right side.
	        matchingSignatures = PromotionHelper.matchSignatureWithRightPromotion(signatures, childTypes);
	        length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
	        	ParseNode child = children.get(1);
				Type targetType = signature.getParamTypes()[1];
	        	PromotionHelper.tryLeft(node, signature, child, targetType);
	        	return;
	        }
	        else if (length > 1) {
		        typeCheckError(node, childTypes);
		        node.setType(PrimitiveType.ERROR);
	        	return;
	        }
	        
	        // Try promoting both sides.
	        matchingSignatures = PromotionHelper.matchSignatureWithBothPromotion(signatures, childTypes);
	        length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
				ParseNode leftChild = children.get(0);
				ParseNode rightChild = children.get(1);
				Type leftTarget = signature.getParamTypes()[0];
				Type rightTarget = signature.getParamTypes()[1];
				PromotionHelper.tryLeft(node, signature, leftChild, rightChild, leftTarget, rightTarget);
				return;
	        }
	        
	        typeCheckError(node, childTypes);
	        node.setType(PrimitiveType.ERROR);
		}
		

	}
	
	public FunctionSignature matchSignatureWithoutPromotion(FunctionSignatures signatures, List<Type> childTypes) {
		return signatures.acceptingSignature(childTypes);
	}

	@Override
	public void visitLeave(IfStatementNode node) {
		Type conditionType = node.child(0).getType();
		if (conditionType != PrimitiveType.BOOLEAN) {
			logError("the condition must evaluate to a boolean at " + node.getToken().getLocation());
		}
	}
	
	@Override
	public void visitLeave(WhileStatementNode node) {
		Type conditionType = node.child(0).getType();
		if (conditionType != PrimitiveType.BOOLEAN) {
			logError("the condition must evaluate to a boolean at " + node.getToken().getLocation());
		}
	}
	
	@Override
	public void visitLeave(TypeNode node) {
		node.setTypeByToken();
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
		// else parent DeclarationNode (or ParameterSpecificationNode) does the processing.
	}
	private boolean isBeingDeclared(IdentifierNode node) {
		ParseNode parent = node.getParent();
		return  ((parent instanceof DeclarationNode) && (node == parent.child(0))) ||
				((parent instanceof ParameterSpecificationNode) && (node == parent.child(1)));
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