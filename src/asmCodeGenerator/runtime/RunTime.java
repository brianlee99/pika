package asmCodeGenerator.runtime;
import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import static asmCodeGenerator.Macros.*;
public class RunTime {
	public static final String EAT_LOCATION_ZERO      = "$eat-location-zero";		// helps us distinguish null pointers from real ones.
	public static final String INTEGER_PRINT_FORMAT   = "$print-format-integer";
	public static final String FLOATING_PRINT_FORMAT  = "$print-format-floating";
	public static final String BOOLEAN_PRINT_FORMAT   = "$print-format-boolean";
	public static final String CHARACTER_PRINT_FORMAT = "$print-format-character";
	public static final String STRING_PRINT_FORMAT    = "$print-format-string";
	public static final String RATIONAL_PRINT_FORMAT  = "$print-format-rational";
	public static final String NEWLINE_PRINT_FORMAT   = "$print-format-newline";
	public static final String SPACE_PRINT_FORMAT     = "$print-format-space";
	public static final String TAB_PRINT_FORMAT       = "$print-format-tab";
	public static final String BOOLEAN_TRUE_STRING    = "$boolean-true-string";
	public static final String BOOLEAN_FALSE_STRING   = "$boolean-false-string";
	public static final String GLOBAL_MEMORY_BLOCK    = "$global-memory-block";
	public static final String USABLE_MEMORY_START    = "$usable-memory-start";
	public static final String MAIN_PROGRAM_LABEL     = "$$main";
	
	public static final String GENERAL_RUNTIME_ERROR                 = "$$general-runtime-error";
	public static final String INTEGER_DIVIDE_BY_ZERO_RUNTIME_ERROR  = "$$i-divide-by-zero";
	public static final String FLOATING_DIVIDE_BY_ZERO_RUNTIME_ERROR = "$$f-divide-by-zero";
	public static final String RATIONAL_DIVIDE_BY_ZERO_RUNTIME_ERROR = "$$r-divide-by-zero";
	
	public static final String DENOMINATOR_ZERO_RUNTIME_ERROR = "$$denominator-zero";
	
	
	public static final String LOWEST_TERMS    = "$lowest-terms";
	public static final String PRINTF_RATIONAL = "$printf-rational";
	public static final String RETURN_ADDRESS  = "$return-address";
	
	public static final String RATIONAL_ADD      = "$rational-add";
//	public static final String RETURN_ADDRESS_ADD = "$return-address-add";
	public static final String RATIONAL_SUBTRACT = "$rational-subtract";
//	public static final String RETURN_ADDRESS_SUBTRACT = "$return-address-subtract";
	public static final String RATIONAL_MULTIPLY = "$rational-multiply";
//	public static final String RETURN_ADDRESS_MULTIPLY = "$return-address-multiply";
	public static final String RATIONAL_DIVIDE   = "$rational-divide";
//	public static final String RETURN_ADDRESS_DIVIDE = "$return-address-divide";
	
	public static final String NUMERATOR_1   = "$numerator-1";
	public static final String NUMERATOR_2   = "$numerator-2";
	public static final String DENOMINATOR_1 = "$denominator-1";
	public static final String DENOMINATOR_2 = "$denominator-2";
	public static final String QUOTIENT      = "$quotient";
	public static final String REMAINDER     = "$remainder";
	
	
	private ASMCodeFragment environmentASM() {
		ASMCodeFragment result = new ASMCodeFragment(GENERATES_VOID);
		result.append(jumpToMain());
		result.append(stringsForPrintf());
		result.append(variableStorage());
		result.append(lowestTerms());
		
		result.append(rationalAdd());
		result.append(rationalSubtract());
		result.append(rationalMultiply());
		result.append(rationalDivide());
		
		result.append(printfRational());
		
		result.append(runtimeErrors());
		result.add(DLabel, USABLE_MEMORY_START);
		return result;
	}
	
	private ASMCodeFragment jumpToMain() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Jump, MAIN_PROGRAM_LABEL);
		return frag;
	}
	
	private ASMCodeFragment variableStorage() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(DLabel, RETURN_ADDRESS);
		frag.add(DataZ, 4);  // maybe 8

		frag.add(DLabel, NUMERATOR_1);
		frag.add(DataZ, 4);

		frag.add(DLabel, NUMERATOR_2);
		frag.add(DataZ, 4);

		frag.add(DLabel, DENOMINATOR_1);
		frag.add(DataZ, 4);

		frag.add(DLabel, DENOMINATOR_2);
		frag.add(DataZ, 4);
		
		frag.add(DLabel, QUOTIENT);
		frag.add(DataZ, 4);

		frag.add(DLabel, REMAINDER);
		frag.add(DataZ, 4);	
		
		return frag;
		
	}

	private ASMCodeFragment stringsForPrintf() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(DLabel, EAT_LOCATION_ZERO);
		frag.add(DataZ, 8);
		frag.add(DLabel, INTEGER_PRINT_FORMAT);
		frag.add(DataS, "%d");
		frag.add(DLabel, FLOATING_PRINT_FORMAT);
		frag.add(DataS, "%g");
		frag.add(DLabel, BOOLEAN_PRINT_FORMAT);
		frag.add(DataS, "%s");
		frag.add(DLabel, CHARACTER_PRINT_FORMAT);
		frag.add(DataS, "%c");
		frag.add(DLabel, STRING_PRINT_FORMAT);
		frag.add(DataS, "%s");
		
		// TODO: fix this
		frag.add(DLabel, RATIONAL_PRINT_FORMAT);
		//frag.add(DataS, "%d_%d/%d");
		frag.add(DataS, "%d");
		
		frag.add(DLabel, NEWLINE_PRINT_FORMAT);
		frag.add(DataS, "\n");
		frag.add(DLabel, TAB_PRINT_FORMAT);
		frag.add(DataS, "\t");
		frag.add(DLabel, SPACE_PRINT_FORMAT);
		frag.add(DataS, " ");
		frag.add(DLabel, BOOLEAN_TRUE_STRING);
		frag.add(DataS, "true");
		frag.add(DLabel, BOOLEAN_FALSE_STRING);
		frag.add(DataS, "false");
		
		return frag;
	}

	// Lowest terms Euclidean GCD algorithm
	private ASMCodeFragment lowestTerms() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);  // [.. 6 27 43355(RA)]
		frag.add(Label, LOWEST_TERMS);
		frag.add(PushD, RETURN_ADDRESS);
		frag.add(Exchange);
		frag.add(StoreI);
		
		// check non-zero denominator
		frag.add(Duplicate);
		frag.add(JumpFalse, DENOMINATOR_ZERO_RUNTIME_ERROR);

		frag.add(DLabel, "$denominator");
		frag.add(DataZ, 4);
		frag.add(PushD, "$denominator");
		frag.add(Exchange);
		frag.add(StoreI);
		
		frag.add(DLabel, "$numerator");
		frag.add(DataZ, 4);
		frag.add(PushD, "$numerator");
		frag.add(Exchange);
		frag.add(StoreI);
		
		frag.add(DLabel, "$a");
		frag.add(DataZ, 4);
		frag.add(PushD, "$numerator");
		frag.add(LoadI);
		frag.add(PushD, "$a");
		frag.add(Exchange);
		// take the absolute value here and store into a
		frag.add(StoreI);
		
		frag.add(DLabel, "$b");
		frag.add(DataZ, 4);
		frag.add(PushD, "$denominator");
		frag.add(LoadI);
		frag.add(PushD, "$b");
		frag.add(Exchange);
		// take the absolute value here and store into b
		frag.add(StoreI);
		
		// main gcd algorithm
		frag.add(Label, "$gcd-loop");
		frag.add(PushD, "$b");
		frag.add(LoadI);
		frag.add(JumpFalse, "$gcd-end");
		

		frag.add(PushD, "$a");
		frag.add(LoadI);
		frag.add(PushD, "$b");
		frag.add(LoadI);
		frag.add(Remainder);
		frag.add(PushD, "$b");
		frag.add(LoadI);
		frag.add(PushD, "$a");
		frag.add(Exchange);
		frag.add(StoreI);
		frag.add(PushD, "$b");
		frag.add(Exchange);
		frag.add(StoreI);
		frag.add(Jump, "$gcd-loop");
		
		// once you've found the gcd, it is stored in 'b'
		
		frag.add(Label, "$gcd-end");
		// load up the lowest-terms rational

		frag.add(PushD, "$numerator");
		frag.add(LoadI);
		frag.add(PushD, "$a");
		frag.add(LoadI);
		frag.add(Divide);
		
		frag.add(PushD, "$denominator");
		frag.add(LoadI);
		frag.add(PushD, "$a");
		frag.add(LoadI);
		frag.add(Divide);
		
		// load
		frag.add(PushD, RETURN_ADDRESS);
		frag.add(LoadI);
		frag.add(Return);

		return frag;
	}
	
		private ASMCodeFragment rationalAdd() {
			ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID); 
			frag.add(Label, RATIONAL_ADD);
			frag.add(PushD, RETURN_ADDRESS);
			frag.add(Exchange);
			frag.add(StoreI);
			
			// [ ... num1 den1 num2 den2]
			
			frag.add(PushD, DENOMINATOR_2);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, NUMERATOR_2);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, DENOMINATOR_1);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, NUMERATOR_1);
			frag.add(Exchange);
			frag.add(StoreI);	
			
			// compute num1*denom2
			frag.add(PushD, NUMERATOR_1);
			frag.add(LoadI);
			frag.add(PushD, DENOMINATOR_2);
			frag.add(LoadI);
			frag.add(Multiply);
			
			// compute num2*denom1
			frag.add(PushD, NUMERATOR_2);
			frag.add(LoadI);
			frag.add(PushD, DENOMINATOR_1);
			frag.add(LoadI);
			frag.add(Multiply);
			
			// compute num1*denom2 + num2*denom1
			frag.add(Add);
			
			// computer denom1*denom2
			frag.add(PushD, DENOMINATOR_1);
			frag.add(LoadI);
			frag.add(PushD, DENOMINATOR_2);
			frag.add(LoadI);
			frag.add(Multiply);
			
			// load return address
			frag.add(PushD, RETURN_ADDRESS);
			frag.add(LoadI);
			frag.add(Return);

			return frag;
		}
	
		private ASMCodeFragment rationalSubtract() {
			ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID); 
			frag.add(Label, RATIONAL_SUBTRACT);
			frag.add(PushD, RETURN_ADDRESS);
			frag.add(Exchange);
			frag.add(StoreI);
			
			// [ ... num1 den1 num2 den2]
			
			frag.add(PushD, DENOMINATOR_2);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, NUMERATOR_2);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, DENOMINATOR_1);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, NUMERATOR_1);
			frag.add(Exchange);
			frag.add(StoreI);	
			
			// compute num1*denom2
			frag.add(PushD, NUMERATOR_1);
			frag.add(LoadI);
			frag.add(PushD, DENOMINATOR_2);
			frag.add(LoadI);
			frag.add(Multiply);
			
			// compute num2*denom1
			frag.add(PushD, NUMERATOR_2);
			frag.add(LoadI);
			frag.add(PushD, DENOMINATOR_1);
			frag.add(LoadI);
			frag.add(Multiply);
			
			// compute num1*denom2 - num2*denom1
			frag.add(Subtract);
			
			// computer denom1*denom2
			frag.add(PushD, DENOMINATOR_1);
			frag.add(LoadI);
			frag.add(PushD, DENOMINATOR_2);
			frag.add(LoadI);
			frag.add(Multiply);
			
			// load return address
			frag.add(PushD, RETURN_ADDRESS);
			frag.add(LoadI);
			frag.add(Return);

			return frag;
		}
		
		// new numerator: num1 * num2
		// new denominator: den1 * den2
		private ASMCodeFragment rationalMultiply() {
			ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID); 
			frag.add(Label, RATIONAL_MULTIPLY);
			frag.add(PushD, RETURN_ADDRESS);
			frag.add(Exchange);
			frag.add(StoreI);
			
			// [ ... num1 den1 num2 den2]
			
			frag.add(PushD, DENOMINATOR_2);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, NUMERATOR_2);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, DENOMINATOR_1);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, NUMERATOR_1);
			frag.add(Exchange);
			frag.add(StoreI);	
			
			// compute num1*num2
			frag.add(PushD, NUMERATOR_1);
			frag.add(LoadI);
			frag.add(PushD, NUMERATOR_2);
			frag.add(LoadI);
			frag.add(Multiply);
			
			// compute denom1*denom2
			frag.add(PushD, DENOMINATOR_1);
			frag.add(LoadI);
			frag.add(PushD, DENOMINATOR_2);
			frag.add(LoadI);
			frag.add(Multiply);
			
			// load return address
			frag.add(PushD, RETURN_ADDRESS);
			frag.add(LoadI);
			frag.add(Return);

			return frag;
		}
		
		// new numerator: num1 * den2
		// new denominator: den1 * num2
		// constraint : num2 cannot be 0.
		private ASMCodeFragment rationalDivide() {
			ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID); 
			frag.add(Label, RATIONAL_DIVIDE);
			frag.add(PushD, RETURN_ADDRESS);
			frag.add(Exchange);
			frag.add(StoreI);
			
			// [ ... num1 den1 num2 den2]
			
			frag.add(PushD, DENOMINATOR_2);
			frag.add(Exchange);
			frag.add(StoreI);
			
			// check that num2 is not zero
			frag.add(Duplicate);
			frag.add(JumpFalse, RATIONAL_DIVIDE_BY_ZERO_RUNTIME_ERROR);
			
			frag.add(PushD, NUMERATOR_2);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, DENOMINATOR_1);
			frag.add(Exchange);
			frag.add(StoreI);
			
			frag.add(PushD, NUMERATOR_1);
			frag.add(Exchange);
			frag.add(StoreI);	
			
			// compute num1*num2
			frag.add(PushD, NUMERATOR_1);
			frag.add(LoadI);
			frag.add(PushD, NUMERATOR_2);
			frag.add(LoadI);
			frag.add(Multiply);
			
			// compute denom1*denom2
			frag.add(PushD, DENOMINATOR_1);
			frag.add(LoadI);
			frag.add(PushD, DENOMINATOR_2);
			frag.add(LoadI);
			frag.add(Multiply);
			
			// load return address
			frag.add(PushD, RETURN_ADDRESS);
			frag.add(LoadI);
			frag.add(Return);

			return frag;
		}
		
		private ASMCodeFragment printfRational() {
			ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
			frag.add(Label, PRINTF_RATIONAL);
			storeITo(frag, RETURN_ADDRESS);  	// [.. 17 13]
			
			// -?\d*(_\d+/\d+);
			
			storeITo(frag, DENOMINATOR_1);
			storeITo(frag, NUMERATOR_1);
			
			loadIFrom(frag, NUMERATOR_1);
			loadIFrom(frag, DENOMINATOR_1);
			frag.add(Divide);					// [.. 1 ] 
			storeITo(frag, QUOTIENT);
			
			loadIFrom(frag, NUMERATOR_1);
			loadIFrom(frag, DENOMINATOR_1);
			frag.add(Remainder);
			storeITo(frag, REMAINDER);			// [.. 4 ]

			loadIFrom(frag, QUOTIENT);
			frag.add(JumpFalse, "$no-leading-number");
			
			loadIFrom(frag, QUOTIENT);			// [ .. 1]
			frag.add(PushD, INTEGER_PRINT_FORMAT); // [ .. 1 print_int ]
			frag.add(Printf);
			
			frag.add(Label, "$no-leading-number");
			loadIFrom(frag, REMAINDER);
			frag.add(JumpFalse, "$no-remainder");			
			
			frag.add(PushI, 95); // for the underline
			frag.add(PushD, CHARACTER_PRINT_FORMAT);
			frag.add(Printf);
			
			loadIFrom(frag, REMAINDER);
			frag.add(PushD, INTEGER_PRINT_FORMAT);
			frag.add(Printf);
			
			frag.add(PushI, 47); // for the slash
			frag.add(PushD, CHARACTER_PRINT_FORMAT);
			frag.add(Printf);
				
			loadIFrom(frag, DENOMINATOR_1);
			frag.add(PushD, INTEGER_PRINT_FORMAT);
			frag.add(Printf);			
			
			frag.add(Label, "$no-remainder");
			
			// load return address
			frag.add(PushD, RETURN_ADDRESS);
			frag.add(LoadI);
			frag.add(Return);
			
			return frag;
		}
		
	private ASMCodeFragment runtimeErrors() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		
		generalRuntimeError(frag);
		integerDivideByZeroError(frag);
		floatingDivideByZeroError(frag);
		rationalDivideByZeroError(frag);
		denominatorZeroError(frag);
		
		return frag;
	}
	private ASMCodeFragment generalRuntimeError(ASMCodeFragment frag) {
		String generalErrorMessage = "$errors-general-message";

		frag.add(DLabel, generalErrorMessage);
		frag.add(DataS, "Runtime error: %s\n");
		
		frag.add(Label, GENERAL_RUNTIME_ERROR);
		frag.add(PushD, generalErrorMessage);
		frag.add(Printf);
		frag.add(Halt);
		return frag;
	}
	private void integerDivideByZeroError(ASMCodeFragment frag) {
		String intDivideByZeroMessage = "$errors-int-divide-by-zero";
		
		frag.add(DLabel, intDivideByZeroMessage);
		frag.add(DataS, "integer divide by zero");
		
		frag.add(Label, INTEGER_DIVIDE_BY_ZERO_RUNTIME_ERROR);
		frag.add(PushD, intDivideByZeroMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}
	private void floatingDivideByZeroError(ASMCodeFragment frag) {
		String floatingDivideByZeroMessage = "$errors-float-divide-by-zero";
		
		frag.add(DLabel, floatingDivideByZeroMessage);
		frag.add(DataS, "float divide by zero");
		
		frag.add(Label, FLOATING_DIVIDE_BY_ZERO_RUNTIME_ERROR);
		frag.add(PushD, floatingDivideByZeroMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}
	private void rationalDivideByZeroError(ASMCodeFragment frag) {
		String rationalDivideByZeroMessage = "$errors-rat-divide-by-zero";
		
		frag.add(DLabel, rationalDivideByZeroMessage);
		frag.add(DataS, "rational divide by zero");
		
		frag.add(Label, RATIONAL_DIVIDE_BY_ZERO_RUNTIME_ERROR);
		frag.add(PushD, rationalDivideByZeroMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}
	private void denominatorZeroError(ASMCodeFragment frag) {
		String denominatorZeroMessage = "$errors-denominator-zero";
		
		frag.add(DLabel, denominatorZeroMessage);
		frag.add(DataS, "denominator zero");
		
		frag.add(Label, DENOMINATOR_ZERO_RUNTIME_ERROR);
		frag.add(PushD, denominatorZeroMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}
	public static ASMCodeFragment getEnvironment() {
		RunTime rt = new RunTime();
		return rt.environmentASM();
	}
}
