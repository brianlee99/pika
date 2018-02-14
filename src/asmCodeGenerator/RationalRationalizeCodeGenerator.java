package asmCodeGenerator;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.Macros.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.RunTime;
import parseTree.ParseNode;

public class RationalRationalizeCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		
		// Check that the denominator is non-zero
		fragment.add(ASMOpcode.Duplicate);
		fragment.add(ASMOpcode.JumpFalse, RunTime.RATIONAL_DIVIDE_BY_ZERO_RUNTIME_ERROR);
		
		storeITo(fragment, RunTime.EXPRESS_OVER_DENOMINATOR);
		storeITo(fragment, RunTime.DENOMINATOR_1);
		storeITo(fragment, RunTime.NUMERATOR_1);
		
		loadIFrom(fragment, RunTime.NUMERATOR_1);
		loadIFrom(fragment, RunTime.EXPRESS_OVER_DENOMINATOR);
		fragment.add(Multiply);
		
		loadIFrom(fragment, RunTime.DENOMINATOR_1);
		fragment.add(Divide);

		loadIFrom(fragment, RunTime.EXPRESS_OVER_DENOMINATOR);
		fragment.add(Call, RunTime.LOWEST_TERMS);
		
		return fragment;
	}

}
