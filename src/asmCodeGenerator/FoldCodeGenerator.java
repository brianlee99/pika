package asmCodeGenerator;

import static asmCodeGenerator.Macros.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.Record.*;
import static asmCodeGenerator.runtime.RunTime.*;

import com.sun.org.apache.bcel.internal.classfile.Code;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.Record;
import parseTree.ParseNode;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.LambdaType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

public class FoldCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		Labeller labeller = new Labeller("map");
		String endLabel = labeller.newLabel("end");
		String loopLabel = labeller.newLabel("loop");
		
		Type returnType = ((LambdaType) node.child(1).getType()).getReturnType();
		int statusFlags = (returnType instanceof ArrayType || returnType == PrimitiveType.STRING) 
				? ARRAY_SUBTYPE_REF_STATUS 
				: ARRAY_SUBTYPE_NOT_REF_STATUS;
		int returnSize = returnType.getSize();
		
		Type parameterType = ((ArrayType) node.child(0).getType()).getSubtype();
		int parameterSize = parameterType.getSize();
		
		// [ array lambda ]

		storeITo(fragment, MAP_LAMBDA);
		storeITo(fragment, MAP_ARRAY);
		
		// create new array
		loadIFrom(fragment, MAP_ARRAY);
		fragment.add(PushI, Record.ARRAY_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);
		storeITo(fragment, MAP_ARRAY_LENGTH);
		
		loadIFrom(fragment, MAP_ARRAY_LENGTH);
		createEmptyArrayRecord(fragment, statusFlags, returnSize);
		storeITo(fragment, MAP_ARRAY_RESULT);
		
		// initialize i
		fragment.add(PushI, 0);
		storeITo(fragment, MAP_I);
		
		// loop body
		fragment.add(Label, loopLabel);
		loadIFrom(fragment, MAP_I);
		loadIFrom(fragment, MAP_ARRAY_LENGTH);
		fragment.add(Subtract);
		fragment.add(JumpFalse, endLabel);
		
		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, parameterSize);
		fragment.add(Subtract);
		storeITo(fragment, STACK_POINTER);
		loadIFrom(fragment, STACK_POINTER);				// [ SP ]
		
		// apply lambda on the ith element
		loadIFrom(fragment, MAP_ARRAY);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add); 
		loadIFrom(fragment, MAP_I);	
		fragment.add(PushI, parameterSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
		if (parameterType == PrimitiveType.RATIONAL) {
			fragment.add(Duplicate); 								// [ &oldArr &ithElem &ithElem ]
			fragment.add(PushI, 4); 								// [ &oldArr &ithElem &ithElem 4 ]
			fragment.add(Add);										// [ &oldArr &num &den ]
			fragment.add(LoadI); 									// [ &oldArr &num den ]
			storeITo(fragment, DENOMINATOR_1);						// [ &oldArr &num ]
			fragment.add(LoadI); 									// [ &oldArr num ]
			storeITo(fragment, NUMERATOR_1); 						// [ &oldArr ]
			
			fragment.add(Duplicate);
			fragment.add(PushI, 4);
			fragment.add(Add);
			loadIFrom(fragment, DENOMINATOR_1);
			fragment.add(StoreI);
			loadIFrom(fragment, NUMERATOR_1);
			fragment.add(StoreI);
		}
		if (parameterType == PrimitiveType.INTEGER) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (parameterType == PrimitiveType.FLOATING) {
			fragment.add(LoadF);
			fragment.add(StoreF);
		}
		if (parameterType == PrimitiveType.BOOLEAN) {
			fragment.add(LoadC);
			fragment.add(StoreC);
		}
		if (parameterType == PrimitiveType.CHARACTER) {
			fragment.add(LoadC);
			fragment.add(StoreC);
		}
		if (parameterType == PrimitiveType.STRING) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (parameterType instanceof ArrayType) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (parameterType instanceof LambdaType) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}

		loadIFrom(fragment, MAP_LAMBDA);
		fragment.add(CallV);
		loadIFrom(fragment, STACK_POINTER);
		
		if (returnType == PrimitiveType.RATIONAL) {
			fragment.add(LoadI);
			loadIFrom(fragment, STACK_POINTER);
			fragment.add(PushI, 4);
			fragment.add(Add);
			fragment.add(LoadI);
		} 
		if (returnType == PrimitiveType.INTEGER) {
			fragment.add(LoadI);
		}
		if (returnType == PrimitiveType.FLOATING) {
			fragment.add(LoadF);
		}
		if (returnType == PrimitiveType.BOOLEAN) {
			fragment.add(LoadC);
		}
		if (returnType == PrimitiveType.CHARACTER) {
			fragment.add(LoadC);
		}
		if (returnType == PrimitiveType.STRING) {
			fragment.add(LoadI);
		}
		if (returnType instanceof ArrayType) {
			fragment.add(LoadI);
		}
		if (returnType instanceof LambdaType) {
			fragment.add(LoadI);
		}
		
		// restore the stack pointer
		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, returnSize);
		fragment.add(Add);
		storeITo(fragment, STACK_POINTER);
		
		loadIFrom(fragment, MAP_ARRAY_RESULT);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add);
		loadIFrom(fragment, MAP_I);	
		fragment.add(PushI, returnSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
//		
//		if (returnType == PrimitiveType.RATIONAL) {					// [ &oldArr &num ]
//			fragment.add(Duplicate);								// [ &oldArr &num &num ]
//			fragment.add(PushI, 4);									// [ &oldArr &num &num 4 ]
//			fragment.add(Add);										// [ &oldArr &num &den ]
//			loadIFrom(fragment, DENOMINATOR_1); 					// [ &oldArr &num &den den ] 
//			fragment.add(StoreI);									// [ &oldArr &num ] 
//			loadIFrom(fragment, NUMERATOR_1);						// [ &oldArr &num num ] 
//			fragment.add(StoreI); 									// [ &oldArr ] 
//		}
		if (returnType == PrimitiveType.INTEGER) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		if (returnType == PrimitiveType.FLOATING) {
			fragment.add(Exchange);
			fragment.add(StoreF);
		}
		if (returnType == PrimitiveType.BOOLEAN) {
			fragment.add(Exchange);
			fragment.add(StoreC);
		}
		if (returnType == PrimitiveType.CHARACTER) {
			fragment.add(Exchange);
			fragment.add(StoreC);
		}
		if (returnType == PrimitiveType.STRING) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		if (returnType instanceof ArrayType) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		if (returnType instanceof LambdaType) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		
		incrementInteger(fragment, MAP_I);
		fragment.add(Jump, loopLabel);
		
		// end loop
		fragment.add(Label, endLabel);
		loadIFrom(fragment, MAP_ARRAY_RESULT);
		return fragment;
	}

}
