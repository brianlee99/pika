package asmCodeGenerator;

import static asmCodeGenerator.codeStorage.ASMOpcode.Call;
import static asmCodeGenerator.runtime.RunTime.*;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;

public class RationalMultiplicationCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		fragment.add(Call, RATIONAL_MULTIPLY);
		fragment.add(Call, LOWEST_TERMS);
		
		return fragment;
		
	}

}
