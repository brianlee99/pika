package asmCodeGenerator;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.Record;
import parseTree.ParseNode;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;

public class StringLengthCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);		// [ ... base]
		
		fragment.add(PushI, Record.ARRAY_LENGTH_OFFSET);
		fragment.add(Add);
		fragment.add(LoadI);
		
		return fragment;
	}

}
