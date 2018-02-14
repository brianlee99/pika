package asmCodeGenerator;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import parseTree.ParseNode;

public class ShortCircuitOrCodeGenerator implements FullCodeGenerator {

	@Override
	public ASMCodeFragment generate(ParseNode node, ASMCodeFragment... args) {
		ASMCodeFragment fragment = new ASMCodeFragment(CodeType.GENERATES_VALUE);
		
		Labeller labeller = new Labeller("SC-Or");
		final String trueLabel = labeller.newLabel("short-circuit-true");
		final String endLabel = labeller.newLabel("end");
		
		// compute arg 1
		fragment.append(args[0]); 				// [... bool]
		
		// short circuiting test
		fragment.add(Duplicate); 				// [... bool bool]
		fragment.add(JumpTrue, trueLabel); 		// [... bool]
		fragment.add(Pop); 						// [... 0] -> [...]
		
		// compute arg 2
		fragment.append(args[1]); 				// [... bool]
		fragment.add(Jump, endLabel);
		
		// the end
		fragment.add(Label, trueLabel); // [... 1]
		fragment.add(Label, endLabel);
		
		return fragment;
	}

}
