package asmCodeGenerator;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.RunTime.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;

public class RationalInitializerCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);

		// check non-zero denominator
		fragment.add(Duplicate);
		fragment.add(JumpFalse, RATIONAL_DIVIDE_BY_ZERO_RUNTIME_ERROR);
		
		fragment.add(Call, LOWEST_TERMS);
		return fragment;
	}

}
