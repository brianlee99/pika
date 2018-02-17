package asmCodeGenerator;

import static asmCodeGenerator.Macros.loadIFrom;
import static asmCodeGenerator.Macros.storeITo;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import asmCodeGenerator.runtime.RunTime;
import parseTree.ParseNode;

public class FloatingToRationalCodeGenerator implements SimpleCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		
		fragment.add(PushI, 223092870);
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
