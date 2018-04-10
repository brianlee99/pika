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

public class ZipCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		Labeller labeller = new Labeller("zip");
		String endLabel = labeller.newLabel("end");
		String loopLabel = labeller.newLabel("loop");
		
		Type firstType = ((ArrayType) node.child(0).getType()).getSubtype();
		Type secondType = ((ArrayType) node.child(1).getType()).getSubtype();
		Type returnType = ((LambdaType) node.child(2).getType()).getReturnType();
		
		int firstTypeSize = firstType.getSize();
		int secondTypeSize = secondType.getSize();
		int returnSize = returnType.getSize();
		
		int statusFlags = (returnType instanceof ArrayType || returnType == PrimitiveType.STRING) 
				? ARRAY_SUBTYPE_REF_STATUS 
				: ARRAY_SUBTYPE_NOT_REF_STATUS;
		
		storeITo(fragment, ZIP_LAMBDA);
		storeITo(fragment, ZIP_ARRAY_2);
		storeITo(fragment, ZIP_ARRAY_1);
		
		// check that arr1 and arr2 have the same length
		loadIFrom(fragment, ZIP_ARRAY_1);
		fragment.add(PushI, Record.ARRAY_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);
		storeITo(fragment, ZIP_ARRAY_LENGTH);

		loadIFrom(fragment, ZIP_ARRAY_LENGTH);
		loadIFrom(fragment, ZIP_ARRAY_2);
		fragment.add(PushI, Record.ARRAY_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);
		fragment.add(Subtract);
		fragment.add(JumpTrue, ZIP_UNEQUAL_SIZE_RUNTIME_ERROR);

		loadIFrom(fragment, ZIP_ARRAY_LENGTH);
		createEmptyArrayRecord(fragment, statusFlags, returnSize);
		storeITo(fragment, ZIP_RESULT);
		
		// initialize i
		fragment.add(PushI, 0);
		storeITo(fragment, ZIP_I);

		// loop body
		fragment.add(Label, loopLabel);
		loadIFrom(fragment, ZIP_I);
		loadIFrom(fragment, ZIP_ARRAY_LENGTH);
		fragment.add(Subtract);
		fragment.add(JumpFalse, endLabel);

		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, firstTypeSize);
		fragment.add(Subtract);
		storeITo(fragment, STACK_POINTER);
		loadIFrom(fragment, STACK_POINTER);				// [ SP ]

		loadIFrom(fragment, ZIP_ARRAY_1);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add); 
		loadIFrom(fragment, ZIP_I);	
		fragment.add(PushI, firstTypeSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
		if (firstType == PrimitiveType.RATIONAL) {
			fragment.add(Duplicate);
			fragment.add(PushI, 4);
			fragment.add(Add);
			fragment.add(LoadI);
			storeITo(fragment, DENOMINATOR_1);
			fragment.add(LoadI);
			storeITo(fragment, NUMERATOR_1);
			
			fragment.add(Duplicate);
			fragment.add(PushI, 4);
			fragment.add(Add);
			loadIFrom(fragment, DENOMINATOR_1);
			fragment.add(StoreI);
			loadIFrom(fragment, NUMERATOR_1);
			fragment.add(StoreI);
		}
		if (firstType == PrimitiveType.INTEGER) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (firstType == PrimitiveType.FLOATING) {
			fragment.add(LoadF);
			fragment.add(StoreF);
		}
		if (firstType == PrimitiveType.BOOLEAN) {
			fragment.add(LoadC);
			fragment.add(StoreC);
		}
		if (firstType == PrimitiveType.CHARACTER) {
			fragment.add(LoadC);
			fragment.add(StoreC);
		}
		if (firstType == PrimitiveType.STRING) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (firstType instanceof ArrayType) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (firstType instanceof LambdaType) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		
		/////////////////////////////////////////
		// second parameter
		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, secondTypeSize);
		fragment.add(Subtract);
		storeITo(fragment, STACK_POINTER);
		loadIFrom(fragment, STACK_POINTER);				// [ SP ]

		loadIFrom(fragment, ZIP_ARRAY_2);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add); 
		loadIFrom(fragment, ZIP_I);	
		fragment.add(PushI, secondTypeSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
		if (secondType == PrimitiveType.RATIONAL) {
			fragment.add(Duplicate);
			fragment.add(PushI, 4);
			fragment.add(Add);
			fragment.add(LoadI);
			storeITo(fragment, DENOMINATOR_1);
			fragment.add(LoadI);
			storeITo(fragment, NUMERATOR_1);
			
			fragment.add(Duplicate);
			fragment.add(PushI, 4);
			fragment.add(Add);
			loadIFrom(fragment, DENOMINATOR_1);
			fragment.add(StoreI);
			loadIFrom(fragment, NUMERATOR_1);
			fragment.add(StoreI);
		}
		if (secondType == PrimitiveType.INTEGER) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (secondType == PrimitiveType.FLOATING) {
			fragment.add(LoadF);
			fragment.add(StoreF);
		}
		if (secondType == PrimitiveType.BOOLEAN) {
			fragment.add(LoadC);
			fragment.add(StoreC);
		}
		if (secondType == PrimitiveType.CHARACTER) {
			fragment.add(LoadC);
			fragment.add(StoreC);
		}
		if (secondType == PrimitiveType.STRING) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (secondType instanceof ArrayType) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (secondType instanceof LambdaType) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		
		loadIFrom(fragment, ZIP_LAMBDA);
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
		
		loadIFrom(fragment, ZIP_RESULT);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add);
		loadIFrom(fragment, ZIP_I);	
		fragment.add(PushI, returnSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
		if (returnType == PrimitiveType.RATIONAL) {		
			// denominator
			fragment.add(PushI, 4);
			fragment.add(Add);
			fragment.add(Exchange);
			fragment.add(StoreI);
			// numerator
			loadIFrom(fragment, ZIP_RESULT);
			fragment.add(PushI, ARRAY_HEADER_SIZE);
			fragment.add(Add);
			loadIFrom(fragment, ZIP_I);	
			fragment.add(PushI, returnSize);
			fragment.add(Multiply);
			fragment.add(Add);
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
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
	
		incrementInteger(fragment, ZIP_I);
		fragment.add(Jump, loopLabel);

		// end loop
		fragment.add(Label, endLabel);
		loadIFrom(fragment, ZIP_RESULT);
		return fragment;
	}

}
