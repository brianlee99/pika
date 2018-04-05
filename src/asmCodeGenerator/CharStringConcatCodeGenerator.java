package asmCodeGenerator;

import static asmCodeGenerator.Macros.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.Record.*;
import static asmCodeGenerator.runtime.RunTime.*;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.Record;
import parseTree.ParseNode;

public class CharStringConcatCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);			// [ ch str ]

		Labeller labeller = new Labeller("string-char");
		String endLabel = labeller.newLabel("end");
		String loopLabel = labeller.newLabel("loop");
		
		storeITo(fragment, STRING_TEMP_1);			// [ ch ]
		loadIFrom(fragment, STRING_TEMP_1);			// [ ch str1 ]
		fragment.add(PushI, Record.STRING_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);						// [ ch str1_len ]
		storeITo(fragment, STRING_TEMP_1_LENGTH);	// [ ch ]
		
		// compute the length of the new string
		loadIFrom(fragment, STRING_TEMP_1_LENGTH);	// [ ch len ]
		fragment.add(PushI, 1);
		fragment.add(Add);							// [ ch newLen ]
		
		// create the resulting string itself
		createEmptyStringRecord(fragment, Record.STRING_STATUS);
		storeITo(fragment, STRING_RESULT);
		
		// append the character
		loadIFrom(fragment, STRING_RESULT);			// [ ch str ]
		fragment.add(PushI, STRING_HEADER_SIZE);
		fragment.add(Add); 							// [ ch &base ]
		fragment.add(Exchange);
		fragment.add(StoreC);
		
		fragment.add(PushI, 0);
		storeITo(fragment, STRING_TEMP_I);
		
		// iterate over the first string
		fragment.add(Label, loopLabel);
		loadIFrom(fragment, STRING_TEMP_I);
		loadIFrom(fragment, STRING_TEMP_1_LENGTH);
		fragment.add(Subtract);
		fragment.add(JumpFalse, endLabel);
		
		// access str1[i]
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
		fragment.add(PushI, 1);
		fragment.add(Add);
		
		fragment.add(Exchange);						// [ &base+offset ithLetter ]
		fragment.add(StoreC);
		
		incrementInteger(fragment, STRING_TEMP_I);
		fragment.add(Jump, loopLabel);
		
		fragment.add(Label, endLabel);				// [ ch ]
		

		
		loadIFrom(fragment, STRING_RESULT);
		
		return fragment;
	}

}
