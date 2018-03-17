package semanticAnalyzer.signatures;

import java.util.ArrayList;
import java.util.List;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Punctuator;
import parseTree.ParseNode;
import parseTree.nodeTypes.OperatorNode;
import parseTree.nodeTypes.TypeNode;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import tokens.LextantToken;
import tokens.Token;

public class PromotionHelper {
	public static void promote(Type targetType, ParseNode node, ParseNode child) {
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

	private static void promoteCharToInt(ParseNode node, ParseNode child) {
		
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
		TypeNode intNode = new TypeNode(intToken);

		OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, intNode);
		
		intNode.setType(PrimitiveType.INTEGER);
		castingNode.setType(PrimitiveType.INTEGER);
		
		node.replaceChild(child, castingNode);
	}
	
	private static void promoteCharToRat(ParseNode node, ParseNode child) {		
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
		TypeNode intNode = new TypeNode(intToken);
		
		Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
		TypeNode ratNode = new TypeNode(ratToken);
		
		OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, child, intNode);
		OperatorNode ratCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, ratNode);
		
		intNode.setType(PrimitiveType.INTEGER);
		ratNode.setType(PrimitiveType.RATIONAL);
		intCastingNode.setType(PrimitiveType.INTEGER);
		ratCastingNode.setType(PrimitiveType.RATIONAL);
		
		node.replaceChild(child, ratCastingNode);
	}
	
	private static void promoteIntToRat(ParseNode node, ParseNode child) {		
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		Token ratToken = LextantToken.artificial(node.getToken(), Keyword.RAT);
		TypeNode ratNode = new TypeNode(ratToken);

		OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, ratNode);
		
		ratNode.setType(PrimitiveType.RATIONAL);
		castingNode.setType(PrimitiveType.RATIONAL);
		
		node.replaceChild(child, castingNode);
	}
	
	private static void promoteCharToFloat(ParseNode node, ParseNode child) {		
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		Token intToken = LextantToken.artificial(node.getToken(), Keyword.INT);
		TypeNode intNode = new TypeNode(intToken);
		
		Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
		TypeNode floatNode = new TypeNode(floatToken);
		
		OperatorNode intCastingNode = OperatorNode.withChildren(castingToken, child, intNode);
		OperatorNode floatCastingNode = OperatorNode.withChildren(castingToken, intCastingNode, floatNode);
		
		intNode.setType(PrimitiveType.INTEGER);
		floatNode.setType(PrimitiveType.FLOATING);
		intCastingNode.setType(PrimitiveType.INTEGER);
		floatCastingNode.setType(PrimitiveType.FLOATING);
		
		node.replaceChild(child, floatCastingNode);
	}
	
	private static void promoteIntToFloat(ParseNode node, ParseNode child) {		
		Token castingToken = LextantToken.artificial(node.getToken(), Punctuator.CASTING);
		Token floatToken = LextantToken.artificial(node.getToken(), Keyword.FLOAT);
		TypeNode floatNode = new TypeNode(floatToken);

		OperatorNode castingNode = OperatorNode.withChildren(castingToken, child, floatNode);
		
		floatNode.setType(PrimitiveType.FLOATING);
		castingNode.setType(PrimitiveType.FLOATING);
		
		node.replaceChild(child, castingNode);
	}
	
	// Returns a list of all matching function signatures
	public static List<FunctionSignature> matchSignatureWithLeftPromotion(FunctionSignatures signatures, List<Type> childTypes) {
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
	
	public static  List<FunctionSignature> matchSignatureWithRightPromotion(FunctionSignatures signatures, List<Type> childTypes) {
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
	
	public static  List<FunctionSignature> matchSignatureWithBothPromotion(FunctionSignatures signatures, List<Type> childTypes) {
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
	
	public static void tryLeft(OperatorNode node, FunctionSignature signature, ParseNode child, Type targetType) {
		node.setType(signature.resultType());
		node.setSignature(signature);
		promote(targetType, node, child);
	}
	public static void tryLeft(OperatorNode node, FunctionSignature signature,  ParseNode leftChild,
			ParseNode rightChild, Type leftTarget, Type rightTarget) {
		node.setType(signature.resultType());
		node.setSignature(signature);
		promote(leftTarget, node, leftChild);
		promote(rightTarget, node, rightChild);
	}
}
