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

public class FoldCodeWithDefaultCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		
		Labeller labeller = new Labeller("fold-default");
		String endLabel = labeller.newLabel("end");
		String loopLabel = labeller.newLabel("loop");
		String label = labeller.newLabel("label");
		String temp1 = labeller.newLabel("temp");
		
		Type subtype = ((ArrayType) node.child(0).getType()).getSubtype();
		Type baseType = node.child(1).getType();
		int subtypeSize = subtype.getSize();
		int baseTypeSize = baseType.getSize();
		
		// declare temp variable types
		if (baseType == PrimitiveType.RATIONAL) {
			declareF(fragment, temp1);	
		}
		if (baseType == PrimitiveType.INTEGER) {
			declareI(fragment, temp1);
		}
		if (baseType == PrimitiveType.FLOATING) {	
			declareF(fragment, temp1);	
		}
		if (baseType == PrimitiveType.BOOLEAN) {
			declareC(fragment, temp1);	
		}
		if (baseType == PrimitiveType.CHARACTER) {
			declareC(fragment, temp1);	
		}
		if (baseType == PrimitiveType.STRING) {
			declareI(fragment, temp1);
		}
		if (baseType instanceof ArrayType) {
			declareI(fragment, temp1);
		}
		if (baseType instanceof LambdaType) {
			declareI(fragment, temp1);
		}
		
		storeITo(fragment, FOLD_LAMBDA);
		
		if (baseType == PrimitiveType.RATIONAL) {
			storeDenTo(fragment, temp1);	
			storeNumTo(fragment, temp1);	
		}
		if (baseType == PrimitiveType.INTEGER) {
			storeITo(fragment, temp1);
		}
		if (baseType == PrimitiveType.FLOATING) {	
			storeFTo(fragment, temp1);	
		}
		if (baseType == PrimitiveType.BOOLEAN) {
			storeCTo(fragment, temp1);
		}
		if (baseType == PrimitiveType.CHARACTER) {
			storeCTo(fragment, temp1);
		}
		if (baseType == PrimitiveType.STRING) {
			storeITo(fragment, temp1);
		}
		if (baseType instanceof ArrayType) {
			storeITo(fragment, temp1);
		}
		if (baseType instanceof LambdaType) {
			storeITo(fragment, temp1);
		}
		
		storeITo(fragment, FOLD_ARRAY);
		loadIFrom(fragment, FOLD_ARRAY);
		fragment.add(PushI, Record.ARRAY_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);
		storeITo(fragment, FOLD_ARRAY_LENGTH);
		
		// deal with corner cases
		// length = 0
		loadIFrom(fragment, FOLD_ARRAY_LENGTH);
		fragment.add(JumpFalse, endLabel);
		
		// length > 0
		// initialize i
		fragment.add(PushI, 0);
		storeITo(fragment, FOLD_I);
		
		// loop body
		fragment.add(Label, loopLabel);
		loadIFrom(fragment, FOLD_I);
		loadIFrom(fragment, FOLD_ARRAY_LENGTH);
		fragment.add(Subtract);
		fragment.add(JumpFalse, endLabel);
		
		///////////////////////////////////////////
		// push temp onto the stack
		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, baseTypeSize);
		fragment.add(Subtract);
		storeITo(fragment, STACK_POINTER);
		loadIFrom(fragment, STACK_POINTER);

		if (baseType == PrimitiveType.RATIONAL) {
			fragment.add(Duplicate);
			fragment.add(PushI, 4);
			fragment.add(Add);
			// store denominator first, then numerator
			loadDenFrom(fragment, temp1);
			fragment.add(StoreI);
			loadNumFrom(fragment, temp1);
			fragment.add(StoreI);
		}
		if (baseType == PrimitiveType.INTEGER) {
			loadIFrom(fragment, temp1);
			fragment.add(StoreI);
		}
		if (baseType == PrimitiveType.FLOATING) {
			loadFFrom(fragment, temp1);
			fragment.add(StoreF);
		}
		if (baseType == PrimitiveType.BOOLEAN) {
			loadCFrom(fragment, temp1);
			fragment.add(StoreC);
		}
		if (baseType == PrimitiveType.CHARACTER) {
			loadCFrom(fragment, temp1);
			fragment.add(StoreC);
		}
		if (baseType == PrimitiveType.STRING) {
			loadIFrom(fragment, temp1);
			fragment.add(StoreI);
		}
		if (baseType instanceof ArrayType) {
			loadIFrom(fragment, temp1);
			fragment.add(StoreI);
		}
		if (baseType instanceof LambdaType) {
			loadIFrom(fragment, temp1);
			fragment.add(StoreI);
		}
		
		///////////////////////////////////////////
		// push the ith element onto the stack
		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, subtypeSize);
		fragment.add(Subtract);
		storeITo(fragment, STACK_POINTER);
		loadIFrom(fragment, STACK_POINTER);
		
		loadIFrom(fragment, FOLD_ARRAY);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add); 
		loadIFrom(fragment, FOLD_I);
		fragment.add(PushI, subtypeSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
		if (subtype == PrimitiveType.RATIONAL) {
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
		if (subtype == PrimitiveType.INTEGER) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (subtype == PrimitiveType.FLOATING) {
			fragment.add(LoadF);
			fragment.add(StoreF);
		}
		if (subtype == PrimitiveType.BOOLEAN) {
			fragment.add(LoadC);
			fragment.add(StoreC);
		}
		if (subtype == PrimitiveType.CHARACTER) {
			fragment.add(LoadC);
			fragment.add(StoreC);
		}
		if (subtype == PrimitiveType.STRING) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (subtype instanceof ArrayType) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		if (subtype instanceof LambdaType) {
			fragment.add(LoadI);
			fragment.add(StoreI);
		}
		
		loadIFrom(fragment, FOLD_LAMBDA);
		fragment.add(CallV);
		
		// retrieve the result
		loadIFrom(fragment, STACK_POINTER);		
		if (baseType == PrimitiveType.RATIONAL) {
			fragment.add(LoadI);
			loadIFrom(fragment, STACK_POINTER);
			fragment.add(PushI, 4);
			fragment.add(Add);
			fragment.add(LoadI);
		} 
		if (baseType == PrimitiveType.INTEGER) {
			fragment.add(LoadI);
		}
		if (baseType == PrimitiveType.FLOATING) {
			fragment.add(LoadF);
		}
		if (baseType == PrimitiveType.BOOLEAN) {
			fragment.add(LoadC);
		}
		if (baseType == PrimitiveType.CHARACTER) {
			fragment.add(LoadC);
		}
		if (baseType == PrimitiveType.STRING) {
			fragment.add(LoadI);
		}
		if (baseType instanceof ArrayType) {
			fragment.add(LoadI);
		}
		if (baseType instanceof LambdaType) {
			fragment.add(LoadI);
		}

		// restore the stack pointer
		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, baseTypeSize);
		fragment.add(Add);
		storeITo(fragment, STACK_POINTER);
		
		// store the result into temp
		if (baseType == PrimitiveType.RATIONAL) {
			storeDenTo(fragment, temp1);
			storeNumTo(fragment, temp1);
		}
		if (baseType == PrimitiveType.INTEGER) {
			storeITo(fragment, temp1);
		}
		if (baseType == PrimitiveType.FLOATING) {
			storeFTo(fragment, temp1);
		}
		if (baseType == PrimitiveType.BOOLEAN) {
			storeCTo(fragment, temp1);
		}
		if (baseType == PrimitiveType.CHARACTER) {
			storeCTo(fragment, temp1);
		}
		if (baseType == PrimitiveType.STRING) {
			storeITo(fragment, temp1);
		}
		if (baseType instanceof ArrayType) {
			storeITo(fragment, temp1);
		}
		if (baseType instanceof LambdaType) {
			storeITo(fragment, temp1);
		}
		
		incrementInteger(fragment, FOLD_I);
		fragment.add(Jump, loopLabel);
		
		// end loop
		fragment.add(Label, endLabel);
		
		if (baseType == PrimitiveType.RATIONAL) {
			loadNumFrom(fragment, temp1);
			loadDenFrom(fragment, temp1);
		}
		if (baseType == PrimitiveType.INTEGER) {
			loadIFrom(fragment, temp1);
		}
		if (baseType == PrimitiveType.FLOATING) {
			loadFFrom(fragment, temp1);
		}
		if (baseType == PrimitiveType.BOOLEAN) {
			loadCFrom(fragment, temp1);
		}
		if (baseType == PrimitiveType.CHARACTER) {
			loadCFrom(fragment, temp1);
		}
		if (baseType == PrimitiveType.STRING) {
			loadIFrom(fragment, temp1);
		}
		if (baseType instanceof ArrayType) {
			loadIFrom(fragment, temp1);
		}
		if (baseType instanceof LambdaType) {
			loadIFrom(fragment, temp1);
		}
		
		
		return fragment;
	}

}
