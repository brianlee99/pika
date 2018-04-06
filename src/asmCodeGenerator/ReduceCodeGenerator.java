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

public class ReduceCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		Labeller labeller = new Labeller("reduce");
		String endLabel1 = labeller.newLabel("end1");
		String loopLabel1 = labeller.newLabel("loop1");
		String endLabel2 = labeller.newLabel("end2");
		String loopLabel2 = labeller.newLabel("loop2");
		String filterLabel1 = labeller.newLabel("filter1");
		String filterLabel2 = labeller.newLabel("filter2");
		
		
		Type returnType = ((LambdaType) node.child(1).getType()).getReturnType();
		int statusFlags = (returnType instanceof ArrayType || returnType == PrimitiveType.STRING) 
				? ARRAY_SUBTYPE_REF_STATUS 
				: ARRAY_SUBTYPE_NOT_REF_STATUS;
		int returnSize = returnType.getSize();
		
		Type parameterType = ((ArrayType) node.child(0).getType()).getSubtype();
		int parameterSize = parameterType.getSize();
		

		storeITo(fragment, REDUCE_LAMBDA);
		storeITo(fragment, REDUCE_ARRAY);
		
		// create new array
		loadIFrom(fragment, REDUCE_ARRAY);
		fragment.add(PushI, Record.ARRAY_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);
		storeITo(fragment, REDUCE_ARRAY_LENGTH);
		
		// initialize i
		fragment.add(PushI, 0);
		storeITo(fragment, REDUCE_I);
		fragment.add(PushI, 0);
		storeITo(fragment, REDUCE_ARRAY_RESULT_LENGTH);
		
		// loop body
		fragment.add(Label, loopLabel1);
		loadIFrom(fragment, REDUCE_I);
		loadIFrom(fragment, REDUCE_ARRAY_LENGTH);
		fragment.add(Subtract);
		fragment.add(JumpFalse, endLabel1);
		
		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, parameterSize);
		fragment.add(Subtract);
		storeITo(fragment, STACK_POINTER);
		loadIFrom(fragment, STACK_POINTER);
		
		// apply lambda on the ith element
		loadIFrom(fragment, REDUCE_ARRAY);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add); 
		loadIFrom(fragment, REDUCE_I);	
		fragment.add(PushI, parameterSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
		if (parameterType == PrimitiveType.RATIONAL) {
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

		loadIFrom(fragment, REDUCE_LAMBDA);
		fragment.add(CallV);
		loadIFrom(fragment, STACK_POINTER);
		
		fragment.add(LoadC);
		
		// restore the stack pointer
		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, returnSize);
		fragment.add(Add);
		storeITo(fragment, STACK_POINTER);
		
		// check if it's true
		fragment.add(JumpFalse, filterLabel1);
		incrementInteger(fragment, REDUCE_ARRAY_RESULT_LENGTH); 
		fragment.add(Label, filterLabel1);
		
		incrementInteger(fragment, REDUCE_I);
		fragment.add(Jump, loopLabel1);
		
		// end loop
		fragment.add(Label, endLabel1);
		
		// create new array, now that we have the length
		loadIFrom(fragment, REDUCE_ARRAY_RESULT_LENGTH);
		createEmptyArrayRecord(fragment, statusFlags, returnSize);
		storeITo(fragment, REDUCE_ARRAY_RESULT);
		
		//////////////////////////////////////////////////////
		// second pass, to populate
		
		// initialize i
		fragment.add(PushI, 0);
		storeITo(fragment, REDUCE_I);
		fragment.add(PushI, 0);
		storeITo(fragment, REDUCE_J);
		
		// loop body
		fragment.add(Label, loopLabel2);
		loadIFrom(fragment, REDUCE_I);
		loadIFrom(fragment, REDUCE_ARRAY_LENGTH);
		fragment.add(Subtract);
		fragment.add(JumpFalse, endLabel2);
		
		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, parameterSize);
		fragment.add(Subtract);
		storeITo(fragment, STACK_POINTER);
		loadIFrom(fragment, STACK_POINTER);
		
		// apply lambda on the ith element
		loadIFrom(fragment, REDUCE_ARRAY);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add); 
		loadIFrom(fragment, REDUCE_I);	
		fragment.add(PushI, parameterSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
		if (parameterType == PrimitiveType.RATIONAL) {
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

		loadIFrom(fragment, REDUCE_LAMBDA);
		fragment.add(CallV);
		loadIFrom(fragment, STACK_POINTER);
		
		fragment.add(LoadC);
		
		// restore the stack pointer
		loadIFrom(fragment, STACK_POINTER);
		fragment.add(PushI, returnSize);
		fragment.add(Add);
		storeITo(fragment, STACK_POINTER);
		
		// check if it's true
		fragment.add(JumpFalse, filterLabel2);
		
		// copy over the element
		loadIFrom(fragment, REDUCE_ARRAY);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add); 
		loadIFrom(fragment, REDUCE_I);	
		fragment.add(PushI, parameterSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
		if (parameterType == PrimitiveType.RATIONAL) {
			fragment.add(Duplicate); 								
			fragment.add(PushI, 4); 								
			fragment.add(Add);										
			fragment.add(LoadI); 									
			storeITo(fragment, DENOMINATOR_1);						
			fragment.add(LoadI); 									
			storeITo(fragment, NUMERATOR_1);
		}
		if (parameterType == PrimitiveType.INTEGER) {
			fragment.add(LoadI);
		}
		if (parameterType == PrimitiveType.FLOATING) {
			fragment.add(LoadF);
		}
		if (parameterType == PrimitiveType.BOOLEAN) {
			fragment.add(LoadC);
		}
		if (parameterType == PrimitiveType.CHARACTER) {
			fragment.add(LoadC);
		}
		if (parameterType == PrimitiveType.STRING) {
			fragment.add(LoadI);
		}
		if (parameterType instanceof ArrayType) {
			fragment.add(LoadI);
		}
		if (parameterType instanceof LambdaType) {
			fragment.add(LoadI);
		}
		
		// paste into the new array
		loadIFrom(fragment, REDUCE_ARRAY_RESULT);
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add); 
		loadIFrom(fragment, REDUCE_J);	
		fragment.add(PushI, parameterSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
		if (parameterType == PrimitiveType.RATIONAL) {
			fragment.add(Duplicate); 								
			fragment.add(PushI, 4); 								
			fragment.add(Add);										
			loadIFrom(fragment,DENOMINATOR_1);
			fragment.add(Exchange);
			fragment.add(StoreI);
			loadIFrom(fragment,NUMERATOR_1);
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		if (parameterType == PrimitiveType.INTEGER) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		if (parameterType == PrimitiveType.FLOATING) {
			fragment.add(Exchange);
			fragment.add(StoreF);
		}
		if (parameterType == PrimitiveType.BOOLEAN) {
			fragment.add(Exchange);
			fragment.add(StoreC);
		}
		if (parameterType == PrimitiveType.CHARACTER) {
			fragment.add(Exchange);
			fragment.add(StoreC);
		}
		if (parameterType == PrimitiveType.STRING) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		if (parameterType instanceof ArrayType) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		if (parameterType instanceof LambdaType) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		
		
		incrementInteger(fragment, REDUCE_J);
		
		fragment.add(Label, filterLabel2);
		
		incrementInteger(fragment, REDUCE_I);
		fragment.add(Jump, loopLabel2);
		
		// end loop2
		fragment.add(Label, endLabel2);
		loadIFrom(fragment, REDUCE_ARRAY_RESULT);
		return fragment;
	}

}
