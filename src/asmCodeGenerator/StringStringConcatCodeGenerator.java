package asmCodeGenerator;

import static asmCodeGenerator.Macros.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.Record.*;
import static asmCodeGenerator.runtime.RunTime.*;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.Record;
import parseTree.ParseNode;

public class StringStringConcatCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);			// [ str1 str2 ]

		Labeller labeller = new Labeller("string-concat");
		String endLabel1 = labeller.newLabel("end1");
		String loopLabel1 = labeller.newLabel("loop1");
		String endLabel2 = labeller.newLabel("end2");
		String loopLabel2 = labeller.newLabel("loop2");
		
		storeITo(fragment, STRING_TEMP_2);			// [ str1 ]
		storeITo(fragment, STRING_TEMP_1);			// [ ]
		
		loadIFrom(fragment, STRING_TEMP_1);			// [ str1 ]
		fragment.add(PushI, Record.STRING_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);						// [ str1_len ]
		storeITo(fragment, STRING_TEMP_1_LENGTH);	// [ ]
		
		loadIFrom(fragment, STRING_TEMP_2);			// [ str2 ]
		fragment.add(PushI, Record.STRING_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);						// [ str2_len ]
		storeITo(fragment, STRING_TEMP_2_LENGTH);	// [ ]
		
		// compute the length of the new string
		loadIFrom(fragment, STRING_TEMP_1_LENGTH);	// [ str1_len ]
		loadIFrom(fragment, STRING_TEMP_2_LENGTH);	// [ str1_len str2_len ]
		fragment.add(Add); 							// [ str3_len ]
		
		// create the resulting string itself
		createEmptyStringRecord(fragment, Record.STRING_STATUS);
		storeITo(fragment, STRING_RESULT);
		
		fragment.add(PushI, 0);
		storeITo(fragment, STRING_TEMP_I);
		
		// iterate over the first string
		fragment.add(Label, loopLabel1);
		loadIFrom(fragment, STRING_TEMP_I);
		loadIFrom(fragment, STRING_TEMP_1_LENGTH);
		fragment.add(Subtract);
		fragment.add(JumpFalse, endLabel1);
		
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
		
		fragment.add(Exchange);						// [ &base+offset ithLetter ]
		fragment.add(StoreC);
		
		incrementInteger(fragment, STRING_TEMP_I);
		fragment.add(Jump, loopLabel1);
		
		fragment.add(Label, endLabel1);
		fragment.add(PushI, 0);
		storeITo(fragment, STRING_TEMP_I);
		
		// iterate over the second string
		fragment.add(Label, loopLabel2);
		loadIFrom(fragment, STRING_TEMP_I);
		loadIFrom(fragment, STRING_TEMP_2_LENGTH);		
		fragment.add(Subtract);
		fragment.add(JumpFalse, endLabel2);
		
		// access str2[i]
		loadIFrom(fragment, STRING_TEMP_2);			// [ str2 ]
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
		// also add the length of string1
		loadIFrom(fragment, STRING_TEMP_1_LENGTH);
		fragment.add(Add);
		
		fragment.add(Exchange);						// [ &base+offset ithLetter ]
		fragment.add(StoreC);
		
		incrementInteger(fragment, STRING_TEMP_I);
		fragment.add(Jump, loopLabel2);
		
		fragment.add(Label, endLabel2);
		
		loadIFrom(fragment, STRING_RESULT);
		return fragment;
	}

}
