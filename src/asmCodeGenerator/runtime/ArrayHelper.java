package asmCodeGenerator.runtime;

import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.RunTime.*;
import java.util.List;
import com.sun.org.apache.bcel.internal.classfile.Code;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import parseTree.ParseNode;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import static asmCodeGenerator.Macros.*;

public class ArrayHelper {
	
	public static void printfArray(ASMCodeFragment code, Type subtype) {
		
		Labeller labeller = new Labeller("print-array");
		
		//	[ ... base ]
		final String PRINTF_ARR_LOOP_BODY = labeller.newLabel("loop-body");
		final String PRINTF_ARR_LOOP_END  = labeller.newLabel("loop-end");
		
		// start array
		code.add(PushI, (int) '[' );
		code.add(PushD, CHARACTER_PRINT_FORMAT);
		code.add(Printf);
		
		// Retrieve the length of the array
		storeITo(code, PRINTF_ARR_BASE);
		
		loadIFrom(code, PRINTF_ARR_BASE);
		code.add(PushI, Record.ARRAY_LENGTH_OFFSET);
		code.add(Add);										// [ ... &length]
		code.add(LoadI);									// [ ... length]
		storeITo(code, PRINTF_ARR_LENGTH);
		
		// i <- 0
		code.add(PushI, 0);
		storeITo(code, PRINTF_ARR_I);
		
		// main loop
		code.add(Label, PRINTF_ARR_LOOP_BODY);
		loadIFrom(code, PRINTF_ARR_I);						// [ ... i]
		loadIFrom(code, PRINTF_ARR_LENGTH);					// [ ... i length]
		code.add(Subtract);
		code.add(JumpFalse, PRINTF_ARR_LOOP_END);			// [ ... ]
		
		loadIFrom(code, PRINTF_ARR_BASE);					// [ base ]
		code.add(PushI, Record.ARRAY_HEADER_SIZE);			
		code.add(Add);										// [ firstElPtr]
		loadIFrom(code, PRINTF_ARR_I);						// [ firstElementPtr i ]
		
		code.add(PushI, subtype.getSize());					// [ firstElPtr i subtypeSize ]
		code.add(Multiply);									// [ base offset ]
		code.add(Add); 										// [ ithElementPtr ]
		
		if (subtype == PrimitiveType.INTEGER) { 
			code.add(LoadI);								// [ value ]
			code.add(PushD, INTEGER_PRINT_FORMAT);
			code.add(Printf);
		}
		else if (subtype == PrimitiveType.FLOATING) {
			code.add(LoadF);								// [ value ]
			code.add(PushD, FLOATING_PRINT_FORMAT);
			code.add(Printf);
		}
		else if (subtype == PrimitiveType.CHARACTER) {
			code.add(LoadC);								// [ value ]
			code.add(PushD, CHARACTER_PRINT_FORMAT);
			code.add(Printf);
		}
		else if (subtype == PrimitiveType.BOOLEAN) {
			code.add(LoadC);								// [ value ]
			
			// convertToStringIfBoolean
			Labeller boolLabeller = new Labeller("print-boolean");
			String trueLabel = boolLabeller.newLabel("true");
			String endLabel = boolLabeller.newLabel("join");
			code.add(JumpTrue, trueLabel);
			code.add(PushD, RunTime.BOOLEAN_FALSE_STRING);
			code.add(Jump, endLabel);
			code.add(Label, trueLabel);
			code.add(PushD, RunTime.BOOLEAN_TRUE_STRING);
			code.add(Label, endLabel);

			code.add(PushD, BOOLEAN_PRINT_FORMAT);
			code.add(Printf);
		}
		else if (subtype == PrimitiveType.STRING) {
			code.add(LoadI);								// [ stringAddr ]
			code.add(PushI, Record.STRING_HEADER_SIZE);
			code.add(Add);
			code.add(PushD, STRING_PRINT_FORMAT);
			code.add(Printf);
		}
		else if (subtype == PrimitiveType.RATIONAL) {
			code.add(Duplicate);
			code.add(LoadI);
			code.add(Exchange);
			code.add(PushI, 4);
			code.add(Add);
			code.add(LoadI);
			code.add(Call, RunTime.PRINTF_RATIONAL);
		}
		else if (subtype instanceof Array) {
			code.add(LoadI);								// [ arrayAddr ]
			Type subSubtype = ((Array) subtype).getSubtype();
			printfArray(code, subSubtype);
		}
		
		// Increment i
		loadIFrom(code, PRINTF_ARR_I);
		code.add(PushI, 1);
		code.add(Add);
		storeITo(code, PRINTF_ARR_I);
		
		// print comma space, BUT ONLY IF i - length is not Zero
		loadIFrom(code, PRINTF_ARR_I);
		loadIFrom(code, PRINTF_ARR_LENGTH);
		code.add(Subtract);
		code.add(JumpFalse, PRINTF_ARR_LOOP_END);
		
		code.add(PushI, (int) ',');
		code.add(PushD, CHARACTER_PRINT_FORMAT);
		code.add(Printf);
		code.add(PushI, (int) ' ');
		code.add(PushD, CHARACTER_PRINT_FORMAT);
		code.add(Printf);
		code.add(Jump, PRINTF_ARR_LOOP_BODY);

		// end array
		code.add(Label, PRINTF_ARR_LOOP_END);
		code.add(PushI, (int) ']' );
		code.add(PushD, CHARACTER_PRINT_FORMAT);
		code.add(Printf);
	}
}
