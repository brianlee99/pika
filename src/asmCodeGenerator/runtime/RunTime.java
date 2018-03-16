package asmCodeGenerator.runtime;
import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
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
	public static final String FRAME_POINTER 		  = "$frame-ptr";
	public static final String STACK_POINTER		  = "$stack-ptr";
	
	// Runtime errors
	public static final String GENERAL_RUNTIME_ERROR                 = "$$general-runtime-error";
	public static final String INTEGER_DIVIDE_BY_ZERO_RUNTIME_ERROR  = "$$i-divide-by-zero";
	public static final String FLOATING_DIVIDE_BY_ZERO_RUNTIME_ERROR = "$$f-divide-by-zero";
	public static final String RATIONAL_DIVIDE_BY_ZERO_RUNTIME_ERROR = "$$r-divide-by-zero";
	public static final String NEGATIVE_LENGTH_ARRAY_RUNTIME_ERROR   = "$$negative-length-arr";
	public static final String INDEX_OUT_OF_BOUNDS_RUNTIME_ERROR     = "$$index-out-of-bounds";
	public static final String NULL_ARRAY_RUNTIME_ERROR				 = "$$null-array";
	
	public static final String LOWEST_TERMS    = "$lowest-terms";
	public static final String RETURN_ADDRESS  = "$return-address";
	
	public static final String PRINTF_RATIONAL = "$printf-rational";
	public static final String RATIONAL_PRINT_NEGATIVE_NUMERATOR = "$rat-print-negative-numerator";
	public static final String RATIONAL_PRINT_CHECK_DENOM_NEGATIVE = "$rat-print-check-denom-negative";
	public static final String RATIONAL_PRINT_CAlCULATE_QUOTIENT = "$rat-print-calculate-quotient";
	public static final String RATIONAL_PRINT_NEGATIVE_DENOMINATOR = "$rat-print-negative-denominator";
	public static final String RATIONAL_PRINT_END = "$rat-print-end";
	
	public static final String RATIONAL_ADD      = "$rational-add";
	public static final String RATIONAL_SUBTRACT = "$rational-subtract";
	public static final String RATIONAL_MULTIPLY = "$rational-multiply";
	public static final String RATIONAL_DIVIDE   = "$rational-divide";
	
	public static final String NUMERATOR_1   = "$numerator-1";
	public static final String NUMERATOR_2   = "$numerator-2";
	public static final String DENOMINATOR_1 = "$denominator-1";
	public static final String DENOMINATOR_2 = "$denominator-2";
	public static final String QUOTIENT      = "$quotient";
	public static final String REMAINDER     = "$remainder";
	
	public static final String EXPRESS_OVER_DENOMINATOR = "$express-over-denominator";
	
	// Printf array
	public static final String PRINTF_ARR_BASE		= "$printf-arr-base";
	public static final String PRINTF_ARR_LENGTH	= "$printf-arr-length";
	public static final String PRINTF_ARR_I			= "$printf-arr-i";
	
	// Array Subroutines
	public static final String CLEAR_N_BYTES	 			= "$clear-n-bytes";
	
	// Array subroutine variables
	public static final String RECORD_CREATION_TEMP     	= "$record-creation-temp";
	public static final String ARRAY_DATASIZE_TEMPORARY 	= "$array-datasize-temp";
	public static final String STRING_LENGTH_TEMPORARY  	= "$string-len-temp";
	public static final String ARRAY_INDEXING_ARRAY 		= "$a-indexing-array";
	public static final String ARRAY_INDEXING_INDEX 		= "$a-indexing-index";
	public static final String CLEAR_N_BYTES_OFFSET_TEMP 	= "$clear-n-bytes-offset-temp";
	public static final String POPULATE_ARRAY_ADDRESS_TEMP 	= "$pop-arr-addr-temp";
	
	// public static final String RELEASE_RECORD				= "$release-record";
	
	private ASMCodeFragment environmentASM() {
		ASMCodeFragment result = new ASMCodeFragment(GENERATES_VOID);
		result.append(jumpToMain());
		result.append(stringsForPrintf());
		result.append(runtimeErrors());
		result.append(variableStorage());
		result.append(initPointers());
		
		// Function calls
		result.append(lowestTerms());
		result.append(rationalAdd());
		result.append(rationalSubtract());
		result.append(rationalMultiply());
		result.append(rationalDivide());
		result.append(printfRational());
		result.append(clearNBytes());
		
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
		declareI(frag, RETURN_ADDRESS);

		declareI(frag, NUMERATOR_1);
		declareI(frag, NUMERATOR_2);
		declareI(frag, DENOMINATOR_1);
		declareI(frag, DENOMINATOR_2);
		declareI(frag, QUOTIENT);
		declareI(frag, REMAINDER);
		declareI(frag, EXPRESS_OVER_DENOMINATOR);

		declareI(frag, RECORD_CREATION_TEMP);
		declareI(frag, ARRAY_DATASIZE_TEMPORARY);
		declareI(frag, ARRAY_INDEXING_ARRAY);
		declareI(frag, ARRAY_INDEXING_INDEX);
		declareI(frag, STRING_LENGTH_TEMPORARY);
		declareI(frag, CLEAR_N_BYTES_OFFSET_TEMP);
		
		declareI(frag, PRINTF_ARR_BASE);
		declareI(frag, PRINTF_ARR_LENGTH);
		declareI(frag, PRINTF_ARR_I);
		declareI(frag, POPULATE_ARRAY_ADDRESS_TEMP);

		declareI(frag, FRAME_POINTER);
		declareI(frag, STACK_POINTER);
		
		return frag;
	}
	
	private ASMCodeFragment initPointers() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Memtop);
		storeITo(frag, FRAME_POINTER);
		frag.add(Memtop);
		storeITo(frag, STACK_POINTER);
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

		// Note: Rationals are dealt with separately.
		
		return frag;
	}

	// Lowest terms Euclidean GCD algorithm
	private ASMCodeFragment lowestTerms() {
		final String aLabel = "$a";
		final String bLabel = "$b";
		
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Label, LOWEST_TERMS);
		storeITo(frag, RETURN_ADDRESS);
		
		//frag.add(DLabel, DENOMINATOR_1);
		frag.add(DataZ, 4);
		frag.add(PushD, DENOMINATOR_1);
		frag.add(Exchange);
		frag.add(StoreI);
		
		//frag.add(DLabel, NUMERATOR_1);
		frag.add(DataZ, 4);
		frag.add(PushD, NUMERATOR_1);
		frag.add(Exchange);
		frag.add(StoreI);
		
		frag.add(DLabel, "$a");
		frag.add(DataZ, 4);
		frag.add(PushD, NUMERATOR_1);
		frag.add(LoadI);
		frag.add(PushD, "$a");
		frag.add(Exchange);
		// take the absolute value here and store into a
		frag.add(StoreI);
		
		frag.add(DLabel, "$b");
		frag.add(DataZ, 4);
		frag.add(PushD, DENOMINATOR_1);
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

		frag.add(PushD, NUMERATOR_1);
		frag.add(LoadI);
		frag.add(PushD, "$a");
		frag.add(LoadI);
		frag.add(Divide);
		
		frag.add(PushD, DENOMINATOR_1);
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
		storeITo(frag, RETURN_ADDRESS);		// [ num1 den1 num2 den2 ]
		
		// [ ... num1 den1 num2 den2]
		storeITo(frag, DENOMINATOR_2);		// [ num1 den1 num2 ]

		// check that num2 is not zero
		frag.add(Duplicate);
		frag.add(JumpFalse, RATIONAL_DIVIDE_BY_ZERO_RUNTIME_ERROR);
		
		storeITo(frag, NUMERATOR_2);		// [ num1 den1 ]
		storeITo(frag, DENOMINATOR_1);		// [ num1 ]
		storeITo(frag, NUMERATOR_1);		// [ ]
		
		// compute num1*den2
		loadIFrom(frag, NUMERATOR_1);		// [ num1 ]
		loadIFrom(frag, DENOMINATOR_2);		// [ den2 ]
		frag.add(Multiply);					// [ newNum ]
		
		// compute den1*num2
		loadIFrom(frag, DENOMINATOR_1);		// [ newNum den1 ]
		loadIFrom(frag, NUMERATOR_2);		// [ newNum num2 ]
		frag.add(Multiply);					// [ newNum newDen ]
		
		loadIFrom(frag, RETURN_ADDRESS);
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
		
		// check that exactly one of denominator or numerator is positive
		loadIFrom(frag, NUMERATOR_1);
		frag.add(JumpNeg, RATIONAL_PRINT_NEGATIVE_NUMERATOR);
		frag.add(Jump, RATIONAL_PRINT_CHECK_DENOM_NEGATIVE);
		frag.add(Label, RATIONAL_PRINT_NEGATIVE_NUMERATOR);
		frag.add(PushI, 45); 						// negative sign
		frag.add(PushD, CHARACTER_PRINT_FORMAT);
		frag.add(Printf);
		
		// turn numerator into a positive
		loadIFrom(frag, NUMERATOR_1);
		frag.add(Negate);
		storeITo(frag, NUMERATOR_1);
		frag.add(Jump, RATIONAL_PRINT_CAlCULATE_QUOTIENT);
		
		frag.add(Label, RATIONAL_PRINT_CHECK_DENOM_NEGATIVE);
		loadIFrom(frag, DENOMINATOR_1);
		frag.add(JumpNeg, RATIONAL_PRINT_NEGATIVE_DENOMINATOR);
		frag.add(Jump, RATIONAL_PRINT_CAlCULATE_QUOTIENT);
		frag.add(Label, RATIONAL_PRINT_NEGATIVE_DENOMINATOR);
		frag.add(PushI, 45); 						// negative sign
		frag.add(PushD, CHARACTER_PRINT_FORMAT);
		frag.add(Printf);
		
		// turn denominator into a positive
		loadIFrom(frag, DENOMINATOR_1);
		frag.add(Negate);
		storeITo(frag, DENOMINATOR_1);
		
		// calculate quotient and remainder
		frag.add(Label, RATIONAL_PRINT_CAlCULATE_QUOTIENT);
		loadIFrom(frag, NUMERATOR_1);
		loadIFrom(frag, DENOMINATOR_1);
		frag.add(Divide);					// [.. 1 ] 
		storeITo(frag, QUOTIENT);
		
		loadIFrom(frag, NUMERATOR_1);
		loadIFrom(frag, DENOMINATOR_1);
		frag.add(Remainder);
		storeITo(frag, REMAINDER);					// [.. 4 ]
		
		// print the leading number if it exists
		// if both quotient and remainder are 0, then just print 0.
		loadIFrom(frag, QUOTIENT);
		loadIFrom(frag, REMAINDER);
		frag.add(BTOr);
		frag.add(JumpTrue, "$rat-print-check-leading-number");
		frag.add(PushI, 0);
		frag.add(PushD, INTEGER_PRINT_FORMAT);
		frag.add(Printf);
		frag.add(Jump, RATIONAL_PRINT_END);
		
		// print the leading number if it exists
		frag.add(Label, "$rat-print-check-leading-number");
		loadIFrom(frag, QUOTIENT);
		frag.add(JumpFalse, "$rat-print-check-fraction");
		
		loadIFrom(frag, QUOTIENT);					// [ .. 1]
		frag.add(PushD, INTEGER_PRINT_FORMAT); 		// [ .. 1 print_int ]
		frag.add(Printf);
		
		// print out the fraction part, if it exists
		frag.add(Label, "$rat-print-check-fraction");
		loadIFrom(frag, REMAINDER);
		frag.add(JumpFalse, RATIONAL_PRINT_END);			
		
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
		
		frag.add(Label, RATIONAL_PRINT_END);
		
		// load return address
		loadIFrom(frag, RETURN_ADDRESS);
		frag.add(Return);
		
		return frag;
	}
	
	// Set N bytes to 0
	private ASMCodeFragment clearNBytes() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Label, CLEAR_N_BYTES);
		storeITo(frag, RETURN_ADDRESS);				// [ ... base numBytes]
		
		frag.add(Label, "$clear-n-bytes-loop");
		frag.add(Duplicate);
		frag.add(JumpNeg, "$clear-n-bytes-end");
		
		frag.add(PushI, 1);
		frag.add(Subtract);							// [ ... base offset]
		storeITo(frag, CLEAR_N_BYTES_OFFSET_TEMP);	// [ ... base]
		frag.add(Duplicate);						// [ ... base base]
		loadIFrom(frag, CLEAR_N_BYTES_OFFSET_TEMP);	// [ ... base base offset]
		frag.add(Add);								// [ ... base base+offset]
		frag.add(PushI, 0);							// [ ... base base+offset 0]
		frag.add(StoreC); 							// [ ... base]
		loadIFrom(frag, CLEAR_N_BYTES_OFFSET_TEMP);
		frag.add(Jump, "$clear-n-bytes-loop");
		
		frag.add(Label, "$clear-n-bytes-end");		// [ ... base -1 ]
		frag.add(Pop);
		frag.add(Pop);
		
		loadIFrom(frag, RETURN_ADDRESS);
		frag.add(Return);
		return frag;
	}

	private ASMCodeFragment runtimeErrors() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		
		generalRuntimeError(frag);
		integerDivideByZeroError(frag);
		floatingDivideByZeroError(frag);
		rationalDivideByZeroError(frag);
		negativeLengthArrayError(frag);
		indexOutOfBoundsError(frag);
		nullArrayError(frag);
		
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
	private void negativeLengthArrayError(ASMCodeFragment frag) {
		String negativeLengthArrayMessage = "$errors-negative-length-arr";
		
		frag.add(DLabel, negativeLengthArrayMessage);
		frag.add(DataS, "negative length array");
		
		frag.add(Label, NEGATIVE_LENGTH_ARRAY_RUNTIME_ERROR);
		frag.add(PushD, negativeLengthArrayMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}
	private void indexOutOfBoundsError(ASMCodeFragment frag) {
		String indexOutOfBoundsMessage = "$errors-index-out-of-bounds";
		frag.add(DLabel, indexOutOfBoundsMessage);
		frag.add(DataS, "index out of bounds");
		
		frag.add(Label, INDEX_OUT_OF_BOUNDS_RUNTIME_ERROR);
		frag.add(PushD, indexOutOfBoundsMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}
	private void nullArrayError(ASMCodeFragment frag) {
		String nullArrayMessage = "$errors-null-arr";
		frag.add(DLabel, nullArrayMessage);
		frag.add(DataS, "null array");
		
		frag.add(Label, NULL_ARRAY_RUNTIME_ERROR);
		frag.add(PushD, nullArrayMessage);
		frag.add(Jump, GENERAL_RUNTIME_ERROR);
	}
	public static ASMCodeFragment getEnvironment() {
		RunTime rt = new RunTime();
		return rt.environmentASM();
	}
}
