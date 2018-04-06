package asmCodeGenerator;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;
import semanticAnalyzer.types.ArrayType;
import semanticAnalyzer.types.Type;

import static asmCodeGenerator.runtime.RunTime.*;
import static asmCodeGenerator.Macros.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.Record.*;

public class StringIndexingCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		Labeller labeller = new Labeller("str-index");
		String label = labeller.newLabel("in-bounds");
		
		storeITo(fragment, SUBSTRING_INDEX_1);
		storeITo(fragment, STRING_TEMP_1);
		
		// Check for a legal array
		loadIFrom(fragment, STRING_TEMP_1);
		fragment.add(JumpFalse, NULL_ARRAY_RUNTIME_ERROR);

		// check that i >= 0
		loadIFrom(fragment, SUBSTRING_INDEX_1);
		fragment.add(JumpNeg, INDEX_OUT_OF_BOUNDS_RUNTIME_ERROR);
		
		// check that i < length
		loadIFrom(fragment, SUBSTRING_INDEX_1);
		loadIFrom(fragment, STRING_TEMP_1);
		fragment.add(PushI, STRING_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);
		fragment.add(Subtract);
		fragment.add(JumpNeg, label);
		fragment.add(Jump, INDEX_OUT_OF_BOUNDS_RUNTIME_ERROR);
		
		fragment.add(Label, label);
		
		loadIFrom(fragment, STRING_TEMP_1);			// [ str1 ]
		fragment.add(PushI, STRING_HEADER_SIZE);
		fragment.add(Add); 							// [ strPtr ]
		loadIFrom(fragment, SUBSTRING_INDEX_1);			// [ strPtr i ]
		fragment.add(Add); 							// [ &ithLetter ]
		fragment.add(LoadC); 						// [ ithLetter ]
		
		return fragment;
	}

}
