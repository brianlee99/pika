package asmCodeGenerator;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.Record;
import parseTree.ParseNode;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.Type;

import static asmCodeGenerator.runtime.RunTime.*;
import static asmCodeGenerator.Macros.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.Record.*;

public class SubstringCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		
		Labeller labeller = new Labeller("substring");
		String label = labeller.newLabel("in-bounds");
		String endLabel = labeller.newLabel("end");
		String loopLabel = labeller.newLabel("loop");
		
		storeITo(fragment, SUBSTRING_INDEX_2);
		storeITo(fragment, SUBSTRING_INDEX_1);
		storeITo(fragment, STRING_TEMP_1);
		
		// Check for a legal array
		loadIFrom(fragment, STRING_TEMP_1);
		fragment.add(JumpFalse, NULL_ARRAY_RUNTIME_ERROR);
		
		// check that i >= 0
		loadIFrom(fragment, SUBSTRING_INDEX_1);
		fragment.add(JumpNeg, INDEX_OUT_OF_BOUNDS_RUNTIME_ERROR);
		
		// check that j <= length
		loadIFrom(fragment, SUBSTRING_INDEX_2);
		loadIFrom(fragment, STRING_TEMP_1);
		fragment.add(PushI, STRING_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);
		fragment.add(Subtract);
		fragment.add(JumpPos, INDEX_OUT_OF_BOUNDS_RUNTIME_ERROR);
		
		// check that i < j
		loadIFrom(fragment, SUBSTRING_INDEX_1);
		loadIFrom(fragment, SUBSTRING_INDEX_2);
		fragment.add(Subtract);
		fragment.add(JumpNeg, label);
		fragment.add(Jump, INDEX_OUT_OF_BOUNDS_RUNTIME_ERROR);
		
		fragment.add(Label, label);
		
		// compute the length of the new string
		loadIFrom(fragment, SUBSTRING_INDEX_2);			// [ j ]
		loadIFrom(fragment, SUBSTRING_INDEX_1);			// [ j i ]
		fragment.add(Subtract); 						// [ j-i ]
		
		createEmptyStringRecord(fragment, Record.STRING_STATUS);
		storeITo(fragment, STRING_RESULT);
		
		loadIFrom(fragment, SUBSTRING_INDEX_1);
		storeITo(fragment, STRING_TEMP_I);
		
		// general loop body
		fragment.add(Label, loopLabel);
		loadIFrom(fragment, STRING_TEMP_I);
		loadIFrom(fragment, SUBSTRING_INDEX_2);
		fragment.add(Subtract);
		fragment.add(JumpFalse, endLabel);
		
		loadIFrom(fragment, STRING_TEMP_1);			// [ str1 ]
		fragment.add(PushI, STRING_HEADER_SIZE);
		fragment.add(Add); 							// [ strPtr ]
		loadIFrom(fragment, STRING_TEMP_I);			// [ strPtr i ]
		fragment.add(Add); 							// [ &ithLetter ]
		fragment.add(LoadC); 						// [ ithLetter ]
		
		
		loadIFrom(fragment, STRING_RESULT);			// [ ithLetter result ]
		fragment.add(PushI, STRING_HEADER_SIZE);
		fragment.add(Add); 							// [ ithLetter &base ]
		loadIFrom(fragment, STRING_TEMP_I);			// [ ithLetter &base i ]
		fragment.add(Add); 							// [ ithLetter &base+offset ]
		loadIFrom(fragment, SUBSTRING_INDEX_1);
		fragment.add(Subtract);
		
		fragment.add(Exchange);						// [ &base+offset ithLetter ]
		fragment.add(StoreC);
		
		incrementInteger(fragment, STRING_TEMP_I);
		fragment.add(Jump, loopLabel);
		
		fragment.add(Label, endLabel);
		loadIFrom(fragment, STRING_RESULT);
		
		

		return fragment;
	}

}
