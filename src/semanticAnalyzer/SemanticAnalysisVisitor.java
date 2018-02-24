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
		
		promoteArray(childTypes, node.getChildren(), node);
		
		Array arrayType = new Array(childTypes.get(0));
		node.setType(arrayType);
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
					Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
					TypeNode intNode = new TypeNode(intToken);

					OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, intNode);
					node.replaceChild(child, castingNode);
					visitLeave(intNode);
					visitLeave(castingNode);
				}

			}
			else if (highestType == PrimitiveType.FLOATING) {
				if (childType == PrimitiveType.CHARACTER) {
				
				}
				else if (childType == PrimitiveType.INTEGER) {
					
				}
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
				TypeNode floatNode = new TypeNode(floatToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, child, intNode);
				OperatorNode ratCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, floatNode);
				
				node.replaceChild(child, ratCastingNode);
				visitLeave(intNode);
				visitLeave(floatNode);
				visitLeave(intCastingNode);
				visitLeave(ratCastingNode);
			}
		}
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
        
        // Try matching without promoting any of the operands.
        FunctionSignature signature = matchSignatureWithoutPromotion(signatures, childTypes);
		if (signature.accepts(childTypes)) {
			node.setType(signature.resultType());
			node.setSignature(signature);
			return;
		}
		
        // Try promoting just the left side.
        List<FunctionSignature> matchingSignatures = matchSignatureWithLeftPromotion(signatures, childTypes);
        int length = matchingSignatures.size();
        if (length == 1) {
        	signature = matchingSignatures.get(0);
			node.setType(signature.resultType());
			node.setSignature(signature);

			ParseNode leftNode = children.get(0);
			Type targetType = signature.getParamTypes()[0];
			Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
			
			if (targetType == PrimitiveType.INTEGER) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);

				OperatorNode castingNode = OperatorNode.withChildren(castingToken, leftNode, intNode);
				node.replaceChild(leftNode, castingNode);
				visitLeave(intNode);
				visitLeave(castingNode);
			}
			else if (targetType == PrimitiveType.RATIONAL) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
				TypeNode ratNode = new TypeNode(ratToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, leftNode, intNode);
				OperatorNode ratCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, ratNode);
				
				node.replaceChild(leftNode, ratCastingNode);
				visitLeave(intNode);
				visitLeave(ratNode);
				visitLeave(intCastingNode);
				visitLeave(ratCastingNode);
			}
			else if (targetType == PrimitiveType.FLOATING) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
				TypeNode floatNode = new TypeNode(floatToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, leftNode, intNode);
				OperatorNode floatCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, floatNode);
				
				node.replaceChild(leftNode, floatCastingNode);
				visitLeave(intNode);
				visitLeave(floatNode);
				visitLeave(intCastingNode);
				visitLeave(floatCastingNode);
			}
        	return;
        }
        else if (length > 1) {
	        typeCheckError(node, childTypes);
	        node.setType(PrimitiveType.ERROR);
        	return;
        }
        
        // Only proceed if there are 2 (or more) child types
        if (childTypes.size() == 1) {
	        typeCheckError(node, childTypes);
	        node.setType(PrimitiveType.ERROR);
        }
        
        // Try promoting just the right side.
        matchingSignatures = matchSignatureWithRightPromotion(signatures, childTypes);
        length = matchingSignatures.size();
        if (length == 1) {
        	signature = matchingSignatures.get(0);
			node.setType(signature.resultType());
			node.setSignature(signature);

			ParseNode rightNode = children.get(1);
			Type targetType = signature.getParamTypes()[1];
			Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
			
			if (targetType == PrimitiveType.INTEGER) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);

				OperatorNode castingNode = OperatorNode.withChildren(castingToken, rightNode, intNode);
				node.replaceChild(rightNode, castingNode);
				visitLeave(intNode);
				visitLeave(castingNode);
			}
			else if (targetType == PrimitiveType.RATIONAL) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
				TypeNode ratNode = new TypeNode(ratToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, rightNode, intNode);
				OperatorNode ratCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, ratNode);
				
				node.replaceChild(rightNode, ratCastingNode);
				visitLeave(intNode);
				visitLeave(ratNode);
				visitLeave(intCastingNode);
				visitLeave(ratCastingNode);
			}
			else if (targetType == PrimitiveType.FLOATING) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
				TypeNode floatNode = new TypeNode(floatToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, rightNode, intNode);
				OperatorNode floatCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, floatNode);
				
				node.replaceChild(rightNode, floatCastingNode);
				visitLeave(intNode);
				visitLeave(floatNode);
				visitLeave(intCastingNode);
				visitLeave(floatCastingNode);
			}
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
			node.setType(signature.resultType());
			node.setSignature(signature);

			ParseNode leftNode = children.get(0);
			ParseNode rightNode = children.get(1);
			Type leftTarget = signature.getParamTypes()[0];
			Type rightTarget = signature.getParamTypes()[1];
			Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
			
			// lets do the left side first
			if (leftTarget == PrimitiveType.INTEGER) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);

				OperatorNode castingNode = OperatorNode.withChildren(castingToken, leftNode, intNode);
				node.replaceChild(leftNode, castingNode);
				visitLeave(intNode);
				visitLeave(castingNode);
			}
			else if (leftTarget == PrimitiveType.RATIONAL) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
				TypeNode ratNode = new TypeNode(ratToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, leftNode, intNode);
				OperatorNode ratCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, ratNode);
				
				node.replaceChild(leftNode, ratCastingNode);
				visitLeave(intNode);
				visitLeave(ratNode);
				visitLeave(intCastingNode);
				visitLeave(ratCastingNode);
			}
			else if (leftTarget == PrimitiveType.FLOATING) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
				TypeNode floatNode = new TypeNode(floatToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, leftNode, intNode);
				OperatorNode floatCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, floatNode);
				
				node.replaceChild(leftNode, floatCastingNode);
				visitLeave(intNode);
				visitLeave(floatNode);
				visitLeave(intCastingNode);
				visitLeave(floatCastingNode);
			}
			
			// then the right side
			if (rightTarget == PrimitiveType.INTEGER) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);

				OperatorNode castingNode = OperatorNode.withChildren(castingToken, rightNode, intNode);
				node.replaceChild(rightNode, castingNode);
				visitLeave(intNode);
				visitLeave(castingNode);
			}
			else if (rightTarget == PrimitiveType.RATIONAL) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
				TypeNode ratNode = new TypeNode(ratToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, rightNode, intNode);
				OperatorNode ratCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, ratNode);
				
				node.replaceChild(rightNode, ratCastingNode);
				visitLeave(intNode);
				visitLeave(ratNode);
				visitLeave(intCastingNode);
				visitLeave(ratCastingNode);
			}
			else if (rightTarget == PrimitiveType.FLOATING) {
				Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
				TypeNode intNode = new TypeNode(intToken);
				
				Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
				TypeNode floatNode = new TypeNode(floatToken);
				
				OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, rightNode, intNode);
				OperatorNode floatCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, floatNode);
				
				node.replaceChild(rightNode, floatCastingNode);
				visitLeave(intNode);
				visitLeave(floatNode);
				visitLeave(intCastingNode);
				visitLeave(floatCastingNode);
			}
			return;
        }
        
        typeCheckError(node, childTypes);
        node.setType(PrimitiveType.ERROR);
    	return;

	}
	
	public FunctionSignature matchSignatureWithoutPromotion(FunctionSignatures signatures, List<Type> childTypes) {
		return signatures.acceptingSignature(childTypes);
	}
	
	public List<FunctionSignature> matchSignatureWithLeftPromotion(FunctionSignatures signatures, List<Type> childTypes) {
		List<FunctionSignature> matchingSignatures = new ArrayList<>();
		List<Type> newChildTypes = new ArrayList<>(childTypes);
		if (newChildTypes.get(0) == PrimitiveType.CHARACTER) {
			// Try promoting to an integer
			newChildTypes.set(0,  PrimitiveType.INTEGER);
			FunctionSignature signature = signatures.acceptingSignature(newChildTypes);
			if (signature.accepts(newChildTypes)) {
				matchingSignatures.add(signature);
				return matchingSignatures;			// done
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
				return matchingSignatures;			// done
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