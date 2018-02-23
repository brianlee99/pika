package semanticAnalyzer.signatures;

import java.util.ArrayList;
import java.util.List;

import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

public class PromotionChecker {
	
	public static FunctionSignature promote(List<Type> types, FunctionSignatures signatures) {
		int numChildren = types.size();
		
		FunctionSignature signature = tryLeftPromotion(types, signatures);
		if (numChildren == 2) {
			if (signature == null)
				signature = tryRightPromotion(types, signatures);
			if (signature == null)
				signature = tryBothPromotions(types, signatures);
		}
		return signature;
	}
	
	public static FunctionSignature tryLeftPromotion(List<Type> types, FunctionSignatures signatures) {
		Type leftType = types.get(0);
		FunctionSignature signature;
		if (leftType == PrimitiveType.CHARACTER) {
			types.set(0,  PrimitiveType.INTEGER);
			signature = signatures.acceptingSignature(types);
			if (signature != null)
				return signature;
		}

		if (leftType == PrimitiveType.INTEGER) {
			List<FunctionSignature> matchingSignatures = new ArrayList<>();
			
			// try rational
			types.set(0,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(types);
			if (signature != null) {
				matchingSignatures.add(signature);
			}
			
			// try floating
			types.set(0,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(types);
			if (signature != null) {
				matchingSignatures.add(signature);
			}
			
			int numMatches = matchingSignatures.size();
			if (numMatches == 0) {
				return null;
			}
			else if (numMatches == 1) {
				return matchingSignatures.get(0);
			}
			else if (numMatches > 1) {
				return null; // todo: fix this to throw an error
			}
		}
		// no matches
		return null;
	}
	
	public static FunctionSignature tryRightPromotion(List<Type> types, FunctionSignatures signatures) {
		assert (types.size() == 2);
		
		Type rightType = types.get(1);
		FunctionSignature signature;
		if (rightType == PrimitiveType.CHARACTER) {
			types.set(0,  PrimitiveType.INTEGER);
			signature = signatures.acceptingSignature(types);
			if (signature != null)
				return signature;
		}

		if (rightType == PrimitiveType.INTEGER) {
			List<FunctionSignature> matchingSignatures = new ArrayList<>();
			
			// try rational
			types.set(0,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(types);
			if (signature != null) {
				matchingSignatures.add(signature);
			}
			
			// try floating
			types.set(0,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(types);
			if (signature != null) {
				matchingSignatures.add(signature);
			}
			
			int numMatches = matchingSignatures.size();
			if (numMatches == 0) {
				return null;
			}
			else if (numMatches == 1) {
				return matchingSignatures.get(0);
			}
			else if (numMatches > 1) {
				return null; // todo: fix this to throw an error
			}
		}
		// no matches
		return null;
	}
	public static FunctionSignature tryBothPromotions(List<Type> types, FunctionSignatures signatures) {
		assert (types.size() == 2);
		
		Type leftType = types.get(0);
		Type rightType = types.get(1);
		
		FunctionSignature signature;
		
		// case 1: char / char
		
		// case 2: int / char
		
		// case 3 : char / int
		
		// case 4 : int / int
		
		if (rightType == PrimitiveType.CHARACTER) {
			types.set(0,  PrimitiveType.INTEGER);
			signature = signatures.acceptingSignature(types);
			if (signature != null)
				return signature;
		}

		if (rightType == PrimitiveType.INTEGER) {
			List<FunctionSignature> matchingSignatures = new ArrayList<>();
			
			// try rational
			types.set(0,  PrimitiveType.RATIONAL);
			signature = signatures.acceptingSignature(types);
			if (signature != null) {
				matchingSignatures.add(signature);
			}
			
			// try floating
			types.set(0,  PrimitiveType.FLOATING);
			signature = signatures.acceptingSignature(types);
			if (signature != null) {
				matchingSignatures.add(signature);
			}
			
			int numMatches = matchingSignatures.size();
			if (numMatches == 0) {
				return null;
			}
			else if (numMatches == 1) {
				return matchingSignatures.get(0);
			}
			else if (numMatches > 1) {
				return null; // todo: fix this to throw an error
			}
		}
		// no matches
		return null;
	}
}
