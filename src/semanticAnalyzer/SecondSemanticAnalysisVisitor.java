package semanticAnalyzer;

import java.util.ArrayList;
import java.util.List;

import asmCodeGenerator.Labeller;
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
import parseTree.nodeTypes.CallNode;
import parseTree.nodeTypes.CharacterConstantNode;
import parseTree.nodeTypes.ContinueNode;
import parseTree.nodeTypes.IfStatementNode;
import parseTree.nodeTypes.BlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatingConstantNode;
import parseTree.nodeTypes.ForStatementNode;
import parseTree.nodeTypes.FunctionDefinitionNode;
import parseTree.nodeTypes.FunctionInvocationNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.LambdaNode;

import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReleaseStatementNode;
import parseTree.nodeTypes.ReturnNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TypeListNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.WhileStatementNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.signatures.FunctionSignatures;
import semanticAnalyzer.types.ArrayType;
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
		
		if (parent instanceof ForStatementNode) {		
			IdentifierNode identifier = (IdentifierNode) parent.child(0);
		
			//ParseNode initializer = node.child(1);
			//node.setType(declarationType);
			//identifier.setType(declarationType);
			
			if (parent.getToken().isLextant(Keyword.INDEX)) {
				addBinding(identifier, PrimitiveType.INTEGER, false);
			} else {			
				Type type = parent.child(1).getType();
				if (type == PrimitiveType.STRING) {
					addBinding(identifier, PrimitiveType.CHARACTER, false);
				}
				else if (type instanceof ArrayType) {
					addBinding(identifier, ((ArrayType) type).getSubtype(), false);
				}
			}
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
		if (!(node.getParent() instanceof FunctionDefinitionNode)) {
			node.setFunctionSignature();
		}
		
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
	// Call
	@Override
	public void visitLeave(CallNode node) {
		if (node.nChildren() == 0) {
			logError("Call requires an argument");
		}
		ParseNode child = node.child(0);
		if (!(child instanceof FunctionInvocationNode)) {
			logError("Can only call a Function Invocation");
		}
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
		List<Type> paramTypes = lambdaType.getParameterTypes();
		
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
	// TypeList and stuff.
	@Override
	public void visitLeave(TypeListNode node) {
		List<Type> paramListTypes = new ArrayList<>();
		int nChildren = node.nChildren();
		for (int i = 0; i < nChildren; i++) {
			TypeNode paramSpecChild = (TypeNode) node.child(i);
			Type type = paramSpecChild.getType();
			paramListTypes.add(type);
		}
		LambdaType lambdaType = new LambdaType(paramListTypes, null);
		node.setType(lambdaType);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// Break and continue
	@Override
	public void visit(BreakNode node) {
		boolean insideWhile = findWhileStatement(node);
		boolean insideFor = findForStatement(node);
		if (!insideWhile && !insideFor) {
			logError("Break statement is not inside a while/for statement");
		}
	}
	@Override
	public void visit(ContinueNode node) {
		boolean insideWhile = findWhileStatement(node);
		boolean insideFor = findForStatement(node);
		if (!insideWhile && !insideFor) {
			logError("Continue statement is not inside a while/for statement");
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
	private boolean findForStatement(ParseNode node) {
		if (node.getToken().isLextant(Keyword.FOR)) {
			return true;
		}
		else if (node instanceof ProgramNode) {
			return false;
		}
		return findForStatement(node.getParent());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// statements and declarations
	@Override
	public void visitLeave(PrintStatementNode node) {
		// just check that nothing is void
		for (ParseNode child : node.getChildren()) {
			Type childType = child.getType();
			if (childType == PrimitiveType.VOID) {
				logError("Expression contains a void type");
			}
		}
	}
	@Override
	public void visitLeave(DeclarationNode node) {
		IdentifierNode identifier = (IdentifierNode) node.child(0);
		ParseNode initializer = node.child(1);
		
		Type declarationType = initializer.getType();
		if (declarationType == PrimitiveType.VOID) {
			logError("Expression contains a void type");
		}
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
			IdentifierNode identifier = (IdentifierNode) target;
			
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
		else if (target instanceof OperatorNode && target.getToken().isLextant(Punctuator.ARRAY_INDEXING)) {

			promoteTargetType(expressionType, targetType, expression, node);
			expressionType = node.child(0).getType();
			targetType = node.child(1).getType();
			
			if (!expressionType.equivalent(targetType)) {
				logError("the identifier and expression types are not equal at " + node.getToken().getLocation());
			}
			
		}
		else if (target instanceof FunctionInvocationNode) {
			logError("Function invocation is not targetable");
		}
	}
	
	public void promoteTargetType(Type expressionType, Type targetType, ParseNode child, ParseNode node) {
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		if (targetType == PrimitiveType.INTEGER) {
			if (expressionType == PrimitiveType.CHARACTER) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);

				OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, intNode);
				
//				intNode.setType(PrimitiveType.INTEGER);
//				castingNode.setType(PrimitiveType.INTEGER);
				
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
				
//				intNode.setType(PrimitiveType.INTEGER);
//				floatNode.setType(PrimitiveType.FLOATING);
//				intCastingNode.setType(PrimitiveType.INTEGER);
//				floatCastingNode.setType(PrimitiveType.FLOATING);
				
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
				
//				floatNode.setType(PrimitiveType.FLOATING);
//				castingNode.setType(PrimitiveType.FLOATING);
				
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
				
//				intNode.setType(PrimitiveType.INTEGER);
//				ratNode.setType(PrimitiveType.RATIONAL);
//				intCastingNode.setType(PrimitiveType.INTEGER);
//				ratCastingNode.setType(PrimitiveType.RATIONAL);
				
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
				
//				ratNode.setType(PrimitiveType.RATIONAL);
//				castingNode.setType(PrimitiveType.RATIONAL);
				
				node.replaceChild(child, castingNode);
				visitLeave(ratNode);
				visitLeave(castingNode);
			}
		}
	}
	
	public void visitLeave(ReleaseStatementNode node) {
		ParseNode child = node.child(0);
		Type childType = child.getType();
		if (!(childType instanceof ArrayType) && childType != PrimitiveType.STRING) {
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
			ArrayType arrayType = new ArrayType(children.get(0).getType());
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
			List<FunctionSignature> matchingSignatures = matchSignatureWithLeftPromotion(signatures, childTypes);
			int length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
	        	ParseNode child = children.get(0);
				Type targetType = signature.getParamTypes()[0];
	        	tryLeft(node, signature, child, targetType);
	        }
	        else {
		        typeCheckError(node, childTypes);
		        node.setType(PrimitiveType.ERROR);
	        }
	        return;
		}

		// If the left side is a TypeNode, then try promoting just the right side.
		else if (children.get(0) instanceof TypeNode) {
			List<FunctionSignature> matchingSignatures = matchSignatureWithRightPromotion(signatures, childTypes);
			// just do the right side
	        int length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
	        	ParseNode child = children.get(1);
				Type targetType = signature.getParamTypes()[1];
	        	tryLeft(node, signature, child, targetType);
	        }
	        else {
		        typeCheckError(node, childTypes);
		        node.setType(PrimitiveType.ERROR);
	        }
			return;
		}
		
		
		// If the right side is a TypeNode, then try promoting just the left side.
		else if (children.get(1) instanceof TypeNode) {
			List<FunctionSignature> matchingSignatures = matchSignatureWithLeftPromotion(signatures, childTypes);
			// just do the right side
	        int length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
	        	ParseNode child = children.get(0);
				Type targetType = signature.getParamTypes()[0];
	        	tryLeft(node, signature, child, targetType);
	        }
	        else {
		        typeCheckError(node, childTypes);
		        node.setType(PrimitiveType.ERROR);
	        }
			return;
		}
		else {
	        // Try promoting just the left side.
	        List<FunctionSignature> matchingSignatures = matchSignatureWithLeftPromotion(signatures, childTypes);
	        
	        int length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
	        	ParseNode child = children.get(0);
				Type targetType = signature.getParamTypes()[0];
	        	tryLeft(node, signature, child, targetType);
	        	return;
	        }
	        else if (length > 1) {
		        typeCheckError(node, childTypes);
		        node.setType(PrimitiveType.ERROR);
	        	return;
	        }
	        
	        // Try promoting just the right side.
	        matchingSignatures = matchSignatureWithRightPromotion(signatures, childTypes);
	        length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
	        	ParseNode child = children.get(1);
				Type targetType = signature.getParamTypes()[1];
	        	tryLeft(node, signature, child, targetType);
	        	return;
	        }
	        else if (length > 1) {
		        typeCheckError(node, childTypes);
		        node.setType(PrimitiveType.ERROR);
	        	return;
	        }
	        
	        // Try promoting both sides.
	        matchingSignatures = matchSignatureWithBothPromotion(signatures, childTypes);
	        length = matchingSignatures.size();
	        if (length == 1) {
	        	signature = matchingSignatures.get(0);
				ParseNode leftChild = children.get(0);
				ParseNode rightChild = children.get(1);
				Type leftTarget = signature.getParamTypes()[0];
				Type rightTarget = signature.getParamTypes()[1];
				tryBoth(node, signature, leftChild, rightChild, leftTarget, rightTarget);
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
	
	// For loop
	@Override
	public void visitEnter(ForStatementNode node) {
		IdentifierNode identifier = (IdentifierNode) node.child(0);
		
		//ParseNode initializer = node.child(1);
		//node.setType(declarationType);
		//identifier.setType(declarationType);
		
		//addBinding(identifier, PrimitiveType.INTEGER, false);
		
		Type type = node.child(1).getType();
		if (type == PrimitiveType.STRING) {
			addBinding(identifier, PrimitiveType.CHARACTER, false);
		}
		else if (type instanceof ArrayType) {
			addBinding(identifier, ((ArrayType) type).getSubtype(), false);
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
	
	
	//////////////////////////////////////////////////////
	// Promotion Helper
	public void promote(Type targetType, ParseNode node, ParseNode child) {
		Type childType = child.getType();
		if (targetType == PrimitiveType.INTEGER) {
			promoteCharToInt(node, child);
		}
		else if (targetType == PrimitiveType.RATIONAL) {
			if (childType == PrimitiveType.INTEGER) {
				promoteIntToRat(node, child);
			}
			else if (childType == PrimitiveType.CHARACTER) {
				promoteCharToRat(node, child);
			}
		}
		else if (targetType == PrimitiveType.FLOATING) {
			if (childType == PrimitiveType.INTEGER) {
				promoteIntToFloat(node, child);
			}
			else if (childType == PrimitiveType.CHARACTER) {
				promoteCharToFloat(node, child);
			}
		}
	}

	private void promoteCharToInt(ParseNode node, ParseNode child) {
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
		TypeNode intNode = new TypeNode(intToken);

		OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, intNode);
		
//		intNode.setType(PrimitiveType.INTEGER);
//		castingNode.setType(PrimitiveType.INTEGER);
		visitLeave(intNode);
		visitLeave(castingNode);
		
		node.replaceChild(child, castingNode);
	}
	
	private void promoteCharToRat(ParseNode node, ParseNode child) {		
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
		TypeNode intNode = new TypeNode(intToken);
		
		Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
		TypeNode ratNode = new TypeNode(ratToken);
		
		OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, child, intNode);
		OperatorNode ratCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, ratNode);
		
//		intNode.setType(PrimitiveType.INTEGER);
//		ratNode.setType(PrimitiveType.RATIONAL);
//		intCastingNode.setType(PrimitiveType.INTEGER);
//		ratCastingNode.setType(PrimitiveType.RATIONAL);
		visitLeave(intNode);
		visitLeave(intCastingNode);
		visitLeave(ratNode);
		visitLeave(ratCastingNode);
		
		node.replaceChild(child, ratCastingNode);
	}
	
	private void promoteIntToRat(ParseNode node, ParseNode child) {		
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
		TypeNode ratNode = new TypeNode(ratToken);

		OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, ratNode);
		
//		ratNode.setType(PrimitiveType.RATIONAL);
//		castingNode.setType(PrimitiveType.RATIONAL);
		visitLeave(ratNode);
		visitLeave(castingNode);
		
		node.replaceChild(child, castingNode);
	}
	
	private void promoteCharToFloat(ParseNode node, ParseNode child) {		
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
		TypeNode intNode = new TypeNode(intToken);
		
		Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
		TypeNode floatNode = new TypeNode(floatToken);
		
		OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, child, intNode);
		OperatorNode floatCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, floatNode);
		
//		intNode.setType(PrimitiveType.INTEGER);
//		floatNode.setType(PrimitiveType.FLOATING);
//		intCastingNode.setType(PrimitiveType.INTEGER);
//		floatCastingNode.setType(PrimitiveType.FLOATING);
		visitLeave(intNode);
		visitLeave(intCastingNode);
		visitLeave(floatNode);
		visitLeave(floatCastingNode);
		
		node.replaceChild(child, floatCastingNode);
	}
	
	private void promoteIntToFloat(ParseNode node, ParseNode child) {		
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
		TypeNode floatNode = new TypeNode(floatToken);

		OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, floatNode);
		
//		floatNode.setType(PrimitiveType.FLOATING);
//		castingNode.setType(PrimitiveType.FLOATING);		
		visitLeave(floatNode);
		visitLeave(castingNode);
		
		
		node.replaceChild(child, castingNode);
	}
	
	// Returns a list of all matching function signatures
	public List<FunctionSignature> matchSignatureWithLeftPromotion(FunctionSignatures signatures, List<Type> childTypes) {
		List<FunctionSignature> matchingSignatures = new ArrayList<>();
		List<Type> newChildTypes = new ArrayList<>(childTypes);
		if (newChildTypes.get(0) == PrimitiveType.CHARACTER) {
			// Try promoting to an integer
			newChildTypes.set(0,  PrimitiveType.INTEGER);
			FunctionSignature signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
				return matchingSignatures;
			}
		}
		if (newChildTypes.get(0) == PrimitiveType.INTEGER) {
			// Try promoting to a rational
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			FunctionSignature signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			// Try promoting to a float
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
		}
		return matchingSignatures;
	}
	
	public List<FunctionSignature> matchSignatureWithRightPromotion(FunctionSignatures signatures, List<Type> childTypes) {
		List<FunctionSignature> matchingSignatures = new ArrayList<>();
		List<Type> newChildTypes = new ArrayList<>(childTypes);
		if (newChildTypes.get(1) == PrimitiveType.CHARACTER) {
			// Try promoting to an integer
			newChildTypes.set(1,  PrimitiveType.INTEGER);
			FunctionSignature signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
				return matchingSignatures;
			}
		}
		if (newChildTypes.get(1) == PrimitiveType.INTEGER) {
			// Try promoting to a rational
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			FunctionSignature signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			// Try promoting to a float
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
		}
		return matchingSignatures;
	}
	
	public List<FunctionSignature> matchSignatureWithBothPromotion(FunctionSignatures signatures, List<Type> childTypes) {
		List<FunctionSignature> matchingSignatures = new ArrayList<>();
		List<Type> newChildTypes = new ArrayList<>(childTypes);
		
		// INT, INT => 4 possibilities:
		// (rat, rat), (rat, float), (float, rat), (float, float)
		if (childTypes.get(0) == PrimitiveType.INTEGER && childTypes.get(1) == PrimitiveType.INTEGER) {
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			FunctionSignature signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			return matchingSignatures;
		}
		
		// CHAR, INT => 6 possibilities:
		// (int, float), (int, rat) 								RANK 1
		// (rat, rat), (rat, float), (float, rat), (float, float)	RANK 2
		if (childTypes.get(0) == PrimitiveType.CHARACTER && childTypes.get(1) == PrimitiveType.INTEGER) {
			newChildTypes.set(0,  PrimitiveType.INTEGER);
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			FunctionSignature signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.INTEGER);
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			if (matchingSignatures.size() > 0) {
				return matchingSignatures;
			} else {
				matchingSignatures.clear();
			}
			// ----------------------
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			return matchingSignatures;
		}
		
		// INT, CHAR => 6 possibilities:
		// (float, int), (rat, int) 								RANK 1
		// (rat, rat), (rat, float), (float, rat), (float, float)	RANK 2
		if (childTypes.get(0) == PrimitiveType.INTEGER && childTypes.get(1) == PrimitiveType.CHARACTER) {
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			newChildTypes.set(1,  PrimitiveType.INTEGER);
			FunctionSignature signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			newChildTypes.set(1,  PrimitiveType.INTEGER);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			if (matchingSignatures.size() > 0) {
				return matchingSignatures;
			} else {
				matchingSignatures.clear();
			}
			// ----------------------
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			return matchingSignatures;
		}
		
		// CHAR, CHAR => 9 possibilities:
		// (int, int)												RANK 1
		// (float, int), (rat, int), (int, float), (int, rat)		RANK 2
		// (rat, rat), (rat, float), (float, rat), (float, float)	RANK 3
		if (childTypes.get(0) == PrimitiveType.CHARACTER && childTypes.get(1) == PrimitiveType.CHARACTER) {
			// RANK 1
			newChildTypes.set(0,  PrimitiveType.INTEGER);
			newChildTypes.set(1,  PrimitiveType.INTEGER);
			FunctionSignature signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
				return matchingSignatures;
			}
			// ----------------------
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			newChildTypes.set(1,  PrimitiveType.INTEGER);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			newChildTypes.set(1,  PrimitiveType.INTEGER);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.INTEGER);
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.INTEGER);
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			if (matchingSignatures.size() > 0) {
				return matchingSignatures;
			} else {
				matchingSignatures.clear();
			}
			// ----------------------
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.RATIONAL);
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			newChildTypes.set(1,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			newChildTypes.set(0,  PrimitiveType.FLOATING);
			newChildTypes.set(1,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
			}
			
			return matchingSignatures;
		}
		return matchingSignatures;
	}
	
	public void tryLeft(OperatorNode node, FunctionSignature signature, ParseNode child, Type targetType) {
		node.setType(signature.resultType());
		node.setSignature(signature);
		promote(targetType, node, child);
	}
	public void tryBoth(OperatorNode node, FunctionSignature signature,  ParseNode leftChild,
			ParseNode rightChild, Type leftTarget, Type rightTarget) {
		node.setType(signature.resultType());
		node.setSignature(signature);
		promote(leftTarget, node, leftChild);
		promote(rightTarget, node, rightChild);
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
		return  ((parent instanceof DeclarationNode) && (node == parent.child(0))) 				||
				((parent instanceof ParameterSpecificationNode) && (node == parent.child(1)))	||
				((parent instanceof ForStatementNode) && (node == parent.child(0)));
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