package asmCodeGenerator;

import static asmCodeGenerator.Macros.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.Record.*;
import static asmCodeGenerator.runtime.RunTime.*;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.Record;
import parseTree.ParseNode;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.LambdaType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

public class ArrayReverseCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);			// [ arr ]
		
		int statusFlags = (node.child(0).getType() instanceof ArrayType || node.getType() == PrimitiveType.STRING) 
				? ARRAY_SUBTYPE_REF_STATUS 
				: ARRAY_SUBTYPE_NOT_REF_STATUS;
		Type subtype = ((ArrayType) node.child(0).getType()).getSubtype();
		int subtypeSize = subtype.getSize();
		
		Labeller labeller = new Labeller("array-reverse");
		String endLabel = labeller.newLabel("end");
		String loopLabel = labeller.newLabel("loop");
		
		storeITo(fragment, ARRAY_TEMP_1);
		loadIFrom(fragment, ARRAY_TEMP_1);
		fragment.add(PushI, Record.ARRAY_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);
		storeITo(fragment, ARRAY_TEMP_1_LENGTH);

		// create the resulting string itself
		loadIFrom(fragment, ARRAY_TEMP_1_LENGTH);
		createEmptyArrayRecord(fragment, statusFlags, subtypeSize);
		storeITo(fragment, ARRAY_RESULT);

		fragment.add(PushI, 0);
		storeITo(fragment, ARRAY_TEMP_I);
		
		// iterate over the array
		fragment.add(Label, loopLabel);
		loadIFrom(fragment, ARRAY_TEMP_I);
		loadIFrom(fragment, ARRAY_TEMP_1_LENGTH);
		fragment.add(Subtract);
		fragment.add(JumpFalse, endLabel);
		
		// access arr[i]
		loadIFrom(fragment, ARRAY_TEMP_1);			// [ str1 ]
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add); 							// [ strPtr ]
		loadIFrom(fragment, ARRAY_TEMP_I);			// [ strPtr i ]
		fragment.add(PushI, subtypeSize);
		fragment.add(Multiply);
		fragment.add(Add); 							// [ &ithElem ]
		
		if (subtype == PrimitiveType.RATIONAL) {
			fragment.add(Duplicate); 								// [ &oldArr &ithElem &ithElem ]
			fragment.add(PushI, 4); 								// [ &oldArr &ithElem &ithElem 4 ]
			fragment.add(Add);										// [ &oldArr &num &den ]
			fragment.add(LoadI); 									// [ &oldArr &num den ]
			storeITo(fragment, DENOMINATOR_1);						// [ &oldArr &num ]
			fragment.add(LoadI); 									// [ &oldArr num ]
			storeITo(fragment, NUMERATOR_1); 						// [ &oldArr ]
		}
		if (subtype == PrimitiveType.INTEGER) {
			fragment.add(LoadI);
		}
		if (subtype == PrimitiveType.FLOATING) {
			fragment.add(LoadF);
		}
		if (subtype == PrimitiveType.BOOLEAN) {
			fragment.add(LoadC);
		}
		if (subtype == PrimitiveType.CHARACTER) {
			fragment.add(LoadC);
		}
		if (subtype == PrimitiveType.STRING) {
			fragment.add(LoadI);
		}
		if (subtype instanceof ArrayType) {
			fragment.add(LoadI);
		}
		if (subtype instanceof LambdaType) {
			fragment.add(LoadI);
		}
		
		loadIFrom(fragment, ARRAY_RESULT);			// [ ithLetter result ]
		fragment.add(PushI, ARRAY_HEADER_SIZE);
		fragment.add(Add); 							// [ ithLetter &base ]	

		// compute n-1-i
		loadIFrom(fragment, ARRAY_TEMP_1_LENGTH);		
		fragment.add(PushI, 1);
		fragment.add(Subtract);
		loadIFrom(fragment, ARRAY_TEMP_I);
		fragment.add(Subtract);

		fragment.add(PushI, subtypeSize);
		fragment.add(Multiply);
		fragment.add(Add);
		
		if (subtype == PrimitiveType.RATIONAL) {				// [ &oldArr &num ]
			fragment.add(Duplicate);								// [ &oldArr &num &num ]
			fragment.add(PushI, 4);									// [ &oldArr &num &num 4 ]
			fragment.add(Add);										// [ &oldArr &num &den ]
			loadIFrom(fragment, DENOMINATOR_1); 					// [ &oldArr &num &den den ] 
			fragment.add(StoreI);									// [ &oldArr &num ] 
			loadIFrom(fragment, NUMERATOR_1);						// [ &oldArr &num num ] 
			fragment.add(StoreI); 									// [ &oldArr ] 
		}
		if (subtype == PrimitiveType.INTEGER) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		if (subtype == PrimitiveType.FLOATING) {
			fragment.add(Exchange);
			fragment.add(StoreF);
		}
		if (subtype == PrimitiveType.BOOLEAN) {
			fragment.add(Exchange);
			fragment.add(StoreC);
		}
		if (subtype == PrimitiveType.CHARACTER) {
			fragment.add(Exchange);
			fragment.add(StoreC);
		}
		if (subtype == PrimitiveType.STRING) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		if (subtype instanceof ArrayType) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}
		if (subtype instanceof LambdaType) {
			fragment.add(Exchange);
			fragment.add(StoreI);
		}

		incrementInteger(fragment, ARRAY_TEMP_I);
		fragment.add(Jump, loopLabel);
		
		fragment.add(Label, endLabel);
		loadIFrom(fragment, ARRAY_RESULT);
		
		return fragment;
	}

}
