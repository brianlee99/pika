package asmCodeGenerator;

import static asmCodeGenerator.Macros.loadIFrom;
import static asmCodeGenerator.Macros.storeITo;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.RunTime;
import parseTree.ParseNode;

public class FloatingRationalizeCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		
		// Check that the denominator is non-zero
		fragment.add(ASMOpcode.Duplicate);
		fragment.add(ASMOpcode.JumpFalse, RunTime.RATIONAL_DIVIDE_BY_ZERO_RUNTIME_ERROR);
		
		storeITo(fragment, RunTime.EXPRESS_OVER_DENOMINATOR);
		loadIFrom(fragment, RunTime.EXPRESS_OVER_DENOMINATOR);
		fragment.add(ConvertF);
		fragment.add(FMultiply);
		fragment.add(ConvertI);
		
		loadIFrom(fragment, RunTime.EXPRESS_OVER_DENOMINATOR);
		fragment.add(Call, RunTime.LOWEST_TERMS);
		
		return fragment;
	}

}
