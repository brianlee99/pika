package semanticAnalyzer.signatures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;

import static semanticAnalyzer.types.PrimitiveType.*;

import asmCodeGenerator.ArrayCloningCodeGenerator;
import asmCodeGenerator.ArrayIndexingCodeGenerator;
import asmCodeGenerator.ArrayLengthCodeGenerator;
import asmCodeGenerator.FloatingDivideCodeGenerator;
import asmCodeGenerator.FloatingExpressOverCodeGenerator;
import asmCodeGenerator.FloatingRationalizeCodeGenerator;
import asmCodeGenerator.FloatingToRationalCodeGenerator;
import asmCodeGenerator.IntegerDivideCodeGenerator;
import asmCodeGenerator.IntegerToCharacterCodeGenerator;
import asmCodeGenerator.IntegerToRationalCodeGenerator;
import asmCodeGenerator.NewArrayCodeGenerator;
import asmCodeGenerator.RationalAdditionCodeGenerator;
import asmCodeGenerator.RationalDivisionCodeGenerator;
import asmCodeGenerator.RationalExpressOverCodeGenerator;
import asmCodeGenerator.RationalInitializerCodeGenerator;
import asmCodeGenerator.RationalMultiplicationCodeGenerator;
import asmCodeGenerator.RationalRationalizeCodeGenerator;
import asmCodeGenerator.RationalSubtractionCodeGenerator;
import asmCodeGenerator.RationalToFloatingCodeGenerator;
import asmCodeGenerator.ShortCircuitAndCodeGenerator;
import asmCodeGenerator.ShortCircuitOrCodeGenerator;
import asmCodeGenerator.codeStorage.ASMOpcode;
import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Punctuator;
import semanticAnalyzer.types.Type;
import semanticAnalyzer.types.TypeVariable;


public class FunctionSignatures extends ArrayList<FunctionSignature> {
	private static final long serialVersionUID = -4907792488209670697L;
	private static Map<Object, FunctionSignatures> signaturesForKey = new HashMap<Object, FunctionSignatures>();
	
	Object key;
	
	public FunctionSignatures(Object key, FunctionSignature ...functionSignatures) {
		this.key = key;
		for(FunctionSignature functionSignature: functionSignatures) {
			add(functionSignature);
		}
		signaturesForKey.put(key, this);
	}
	
	public Object getKey() {
		return key;
	}
	public boolean hasKey(Object key) {
		return this.key.equals(key);
	}
	
	public FunctionSignature acceptingSignature(List<Type> types) {
		for(FunctionSignature functionSignature: this) {
			if(functionSignature.accepts(types)) {
				return functionSignature;
			}
		}
		return FunctionSignature.nullInstance();
	}
	public boolean accepts(List<Type> types) {
		return !acceptingSignature(types).isNull();
	}


	
	/////////////////////////////////////////////////////////////////////////////////
	// access to FunctionSignatures by key object.
	
	public static FunctionSignatures nullSignatures = new FunctionSignatures(0, FunctionSignature.nullInstance());

	public static FunctionSignatures signaturesOf(Object key) {
		if(signaturesForKey.containsKey(key)) {
			return signaturesForKey.get(key);
		}
		return nullSignatures;
	}
	public static FunctionSignature signature(Object key, List<Type> types) {
		FunctionSignatures signatures = FunctionSignatures.signaturesOf(key);
		return signatures.acceptingSignature(types);
	}

	
	
	/////////////////////////////////////////////////////////////////////////////////
	// Put the signatures for operators in the following static block.
	
	static {
		TypeVariable S = new TypeVariable("S");
		
		// here's one example to get you started with FunctionSignatures: the signatures for addition.		
		// for this to work, you should statically import *

		new FunctionSignatures(Punctuator.ADD,
		    new FunctionSignature(ASMOpcode.Add, INTEGER, INTEGER, INTEGER),
		    new FunctionSignature(ASMOpcode.FAdd, FLOATING, FLOATING, FLOATING),
		    new FunctionSignature(new RationalAdditionCodeGenerator(), RATIONAL, RATIONAL, RATIONAL)
		);
		
		new FunctionSignatures(Punctuator.SUBTRACT,
		    new FunctionSignature(ASMOpcode.Subtract, INTEGER, INTEGER, INTEGER),
		    new FunctionSignature(ASMOpcode.FSubtract, FLOATING, FLOATING, FLOATING),
		    new FunctionSignature(new RationalSubtractionCodeGenerator(), RATIONAL, RATIONAL, RATIONAL)
		);
		
		new FunctionSignatures(Punctuator.MULTIPLY,
			new FunctionSignature(ASMOpcode.Multiply, INTEGER, INTEGER, INTEGER),
			new FunctionSignature(ASMOpcode.FMultiply, FLOATING, FLOATING, FLOATING),
		    new FunctionSignature(new RationalMultiplicationCodeGenerator(), RATIONAL, RATIONAL, RATIONAL)
		);
		
		new FunctionSignatures(Punctuator.DIVIDE,
			new FunctionSignature(new IntegerDivideCodeGenerator(), INTEGER, INTEGER, INTEGER),
			new FunctionSignature(new FloatingDivideCodeGenerator(), FLOATING, FLOATING, FLOATING),
		    new FunctionSignature(new RationalDivisionCodeGenerator(), RATIONAL, RATIONAL, RATIONAL)
		);
		
		// Over
		new FunctionSignatures(Punctuator.OVER,
			new FunctionSignature(new RationalInitializerCodeGenerator(), INTEGER, INTEGER, RATIONAL)
		);
		
		// Express Over
		new FunctionSignatures(Punctuator.EXPRESS_OVER,
			new FunctionSignature(new RationalExpressOverCodeGenerator(), RATIONAL, INTEGER, INTEGER),
			new FunctionSignature(new FloatingExpressOverCodeGenerator(), FLOATING, INTEGER, INTEGER)
		);
		
		// Rationalize
		new FunctionSignatures(Punctuator.RATIONALIZE,
			new FunctionSignature(new RationalRationalizeCodeGenerator(), RATIONAL, INTEGER, RATIONAL),
			new FunctionSignature(new FloatingRationalizeCodeGenerator(), FLOATING, INTEGER, RATIONAL)
		);	
		
		Punctuator[] comparisons = {
			Punctuator.GREATER,
			Punctuator.LESS,
			Punctuator.EQUALS,
			Punctuator.NOT_EQUALS,
			Punctuator.GREATER_EQUALS,
			Punctuator.LESS_EQUALS
		};
		
		for (Punctuator comparison : comparisons) {
			FunctionSignature iSignature = new FunctionSignature(1, INTEGER, INTEGER, BOOLEAN);
			FunctionSignature cSignature = new FunctionSignature(1, CHARACTER, CHARACTER, BOOLEAN);
			FunctionSignature fSignature = new FunctionSignature(1, FLOATING, FLOATING, BOOLEAN);
			FunctionSignature bSignature = new FunctionSignature(1, BOOLEAN, BOOLEAN, BOOLEAN);
			FunctionSignature sSignature = new FunctionSignature(1, STRING, STRING, BOOLEAN);
			FunctionSignature rSignature = new FunctionSignature(1, RATIONAL, RATIONAL, BOOLEAN);
			FunctionSignature aSignature = new FunctionSignature(1, new Array(S), new Array(S), BOOLEAN);
			
			if (comparison == Punctuator.EQUALS || comparison == Punctuator.NOT_EQUALS) {
				new FunctionSignatures(comparison, iSignature, cSignature, fSignature, bSignature, sSignature, rSignature, aSignature);
			}
			else {
				new FunctionSignatures(comparison, iSignature, cSignature, fSignature, rSignature);
			}
		}
		
		// For Type Casting
		new FunctionSignatures(Punctuator.CASTING,
			// Integer -> target type
			new FunctionSignature(ASMOpcode.ConvertF, INTEGER, FLOATING, FLOATING),
			new FunctionSignature(new IntegerToCharacterCodeGenerator(), INTEGER, CHARACTER, CHARACTER),
			new FunctionSignature(ASMOpcode.Nop, INTEGER, BOOLEAN, BOOLEAN),
			new FunctionSignature(ASMOpcode.Nop, INTEGER, INTEGER, INTEGER),
			new FunctionSignature(new IntegerToRationalCodeGenerator(), INTEGER, RATIONAL, RATIONAL),
			
			// Character -> target type
			new FunctionSignature(ASMOpcode.Nop, CHARACTER, INTEGER, INTEGER),
			new FunctionSignature(ASMOpcode.Nop, CHARACTER, BOOLEAN, BOOLEAN),
			new FunctionSignature(ASMOpcode.Nop, CHARACTER, CHARACTER, CHARACTER),
			new FunctionSignature(new IntegerToRationalCodeGenerator(), CHARACTER, RATIONAL, RATIONAL),
			
			// Floating -> target type
			new FunctionSignature(ASMOpcode.ConvertI, FLOATING, INTEGER, INTEGER),
			new FunctionSignature(ASMOpcode.Nop, FLOATING, FLOATING, FLOATING),
			new FunctionSignature(new FloatingToRationalCodeGenerator(), FLOATING, RATIONAL, RATIONAL),
			new FunctionSignature(1, FLOATING, RATIONAL, RATIONAL),
			
			// Boolean -> target type
			new FunctionSignature(ASMOpcode.Nop, BOOLEAN, BOOLEAN, BOOLEAN),
			
			// String -> target type
			new FunctionSignature(ASMOpcode.Nop, STRING, STRING, STRING),
			
			// Rational -> target type
			new FunctionSignature(ASMOpcode.Nop, RATIONAL, RATIONAL, RATIONAL),
			new FunctionSignature(new RationalToFloatingCodeGenerator(), RATIONAL, FLOATING, FLOATING),
			new FunctionSignature(ASMOpcode.Divide, RATIONAL, INTEGER, INTEGER)
		);
		
		// OR and AND
		new FunctionSignatures(Punctuator.OR,
			new FunctionSignature(new ShortCircuitOrCodeGenerator(), BOOLEAN, BOOLEAN, BOOLEAN)
		);
		new FunctionSignatures(Punctuator.AND,
			new FunctionSignature(new ShortCircuitAndCodeGenerator(), BOOLEAN, BOOLEAN, BOOLEAN)
		);
		
		// Array Indexing
		new FunctionSignatures(Punctuator.ARRAY_INDEXING,
			new FunctionSignature(
				new ArrayIndexingCodeGenerator(),
				new Array(S), INTEGER, S
			)
		);
		
		////////////////////////////////////////////////////
		// Unary Operators
		
		// NOT
		new FunctionSignatures(Punctuator.NOT,
			new FunctionSignature(1, BOOLEAN, BOOLEAN)
		);
		
		// Clone operation
		// Array(S) -> Array(S)
		new FunctionSignatures(Keyword.CLONE,
			new FunctionSignature(
				new ArrayCloningCodeGenerator(),
				new Array(S), new Array(S)
			)
		);
						
		// Empty array generation
		new FunctionSignatures(Keyword.NEW,
			new FunctionSignature(
				new NewArrayCodeGenerator(),
				S, INTEGER, new Array(S)
			)
		);
		
		// length
		new FunctionSignatures(Keyword.LENGTH,
			new FunctionSignature(
				new ArrayLengthCodeGenerator(),
				new Array(S), INTEGER
			)
		);

		
		// First, we use the operator itself (in this case the Punctuator ADD) as the key.
		// Then, we give that key two signatures: one an (INT x INT -> INT) and the other
		// a (FLOAT x FLOAT -> FLOAT).  Each signature has a "whichVariant" parameter where
		// I'm placing the instruction (ASMOpcode) that needs to be executed.
		//
		// I'll follow the convention that if a signature has an ASMOpcode for its whichVariant,
		// then to generate code for the operation, one only needs to generate the code for
		// the operands (in order) and then add to that the Opcode.  For instance, the code for
		// floating addition should look like:
		//
		//		(generate argument 1)	: may be many instructions
		//		(generate argument 2)   : ditto
		//		FAdd					: just one instruction
		//
		// If the code that an operator should generate is more complicated than this, then
		// I will not use an ASMOpcode for the whichVariant.  In these cases I typically use
		// a small object with one method (the "Command" design pattern) that generates the
		// required code.

	}

}
