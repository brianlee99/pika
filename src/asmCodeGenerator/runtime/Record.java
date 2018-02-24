package asmCodeGenerator.runtime;

import static asmCodeGenerator.Macros.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.RunTime.*;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.Macros;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType;
import semanticAnalyzer.types.Array;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;

public class Record {
	public static final int RECORD_TYPEID_OFFSET 		= 0;
	public static final int RECORD_STATUS_OFFSET 		= 4;
	
	// Length offsets
	public static final int ARRAY_LENGTH_OFFSET 		= 12;
	public static final int STRING_LENGTH_OFFSET		= 8;
	
	// Subtype size offset
	public static final int ARRAY_SUBTYPE_SIZE_OFFSET 	= 8;
	
	// Record Type IDs
	public static final int ARRAY_TYPE_ID        		= 7;
	public static final int STRING_TYPE_ID				= 6;
	
	// Record header sizes
	public static final int ARRAY_HEADER_SIZE    		= 16;
	public static final int STRING_HEADER_SIZE			= 12;
	
	// Status codes
	public static final int ARRAY_SUBTYPE_REF_STATUS		= 2;
	public static final int ARRAY_SUBTYPE_NOT_REF_STATUS 	= 0;
	public static final int STRING_STATUS					= 9;
	
	public static void createRecord(ASMCodeFragment code, int typecode, int statusFlags) {
		code.add(Call, MemoryManager.MEM_MANAGER_ALLOCATE);						// [ ... addr]
		storeITo(code, RECORD_CREATION_TEMP);
		
		writeIPBaseOffset(code, RECORD_CREATION_TEMP, Record.RECORD_TYPEID_OFFSET, typecode);
		writeIPBaseOffset(code, RECORD_CREATION_TEMP, Record.RECORD_STATUS_OFFSET, statusFlags);	
	}
	
	// Subroutine (NOT at the ASM level) that creates an empty array record.
	public static void createEmptyArrayRecord(ASMCodeFragment code, int statusFlags, int subtypeSize) {
		final int typecode = Record.ARRAY_TYPE_ID;
		
		code.add(Duplicate);									// [ ... nElems nElems]
		code.add(JumpNeg, NEGATIVE_LENGTH_ARRAY_RUNTIME_ERROR);
		
		code.add(Duplicate);									// [ ... nElems nElems]
		code.add(PushI, subtypeSize);							// [ ... nElems nElems subtypeSize]
		code.add(Multiply);										// [ ... nElems arraySize]
		code.add(Duplicate);									// [ ... nElems arraySize arraySize]
		storeITo(code, ARRAY_DATASIZE_TEMPORARY);				// [ ... nElems arraySize]
		code.add(PushI, Record.ARRAY_HEADER_SIZE);				// [ ... nElems arraySize 16]
		code.add(Add);											// [ ... nElems totalArraySize]
		
		createRecord(code, typecode, statusFlags);				// [ ... nElems]
		
		loadIFrom(code, RECORD_CREATION_TEMP);					// [ ... nElems ptr]
		code.add(PushI, Record.ARRAY_HEADER_SIZE);				// [ ... nElems ptr 16]
		code.add(Add);											// [ ... nElems elemsPtr]
		loadIFrom(code, ARRAY_DATASIZE_TEMPORARY);				// [ ... nElems elemsPtr arraySize]
		code.add(Call, CLEAR_N_BYTES);							// [ ... nElems]
		
		// Write subtype size + array length
		writeIPBaseOffset(code, RECORD_CREATION_TEMP, Record.ARRAY_SUBTYPE_SIZE_OFFSET, subtypeSize);
		writeIPtrOffset(code, RECORD_CREATION_TEMP, Record.ARRAY_LENGTH_OFFSET);
		
		// The array resides in record_creation_temp 
		loadIFrom(code, RECORD_CREATION_TEMP);
	}	
	
	// Subroutine that populates an array
	public static void populateArray(ASMCodeFragment code, int offset, Type type) {
		if (type == PrimitiveType.RATIONAL) {
																	// [ &arr num den ]
			storeITo(code, DENOMINATOR_1);							// [ &arr num ]
			storeITo(code, NUMERATOR_1);							// [ &arr ]
			storeITo(code, POPULATE_ARRAY_ADDRESS_TEMP);			// [ ]
			
			loadIFrom(code, NUMERATOR_1);							// [ num ]
			loadIFrom(code, DENOMINATOR_1);							// [ num den] 
			loadIFrom(code, POPULATE_ARRAY_ADDRESS_TEMP);			// [ num den &arr ]
			
			code.add(PushI, ARRAY_HEADER_SIZE); 					// [ num den &arr 16 ]
			code.add(Add);											// [ num den &firstElem ]
			code.add(PushI, offset); 								// [ num den &firstElem offset ]
			code.add(Add);											// [ num den &ithElem ]
			storeITo(code, POPULATE_ARRAY_ADDRESS_TEMP);
			loadIFrom(code, POPULATE_ARRAY_ADDRESS_TEMP);
			
			code.add(PushI, 4);				
			code.add(Add); 											// [ num den denomAddr ]
			code.add(Exchange);										// [ num denomAddr den ]
			code.add(StoreI);										// [ num ]
			loadIFrom(code, POPULATE_ARRAY_ADDRESS_TEMP);  			// [ num numAddr ]
			code.add(Exchange);										// [ numAddr num ]
			code.add(StoreI);										// [ ]
		}
		else {		
			code.add(Exchange); 									// [ ... item &arr ]
			code.add(PushI, ARRAY_HEADER_SIZE); 					// [ ... item &arr 16 ]
			code.add(Add);											// [ ... item &firstElem ]
			code.add(PushI, offset); 								// [ ... item &firstElem offset ]
			code.add(Add);											// [ ... item &ithElem ]
			storeITo(code, POPULATE_ARRAY_ADDRESS_TEMP);
			loadIFrom(code, POPULATE_ARRAY_ADDRESS_TEMP);
			
			if(type == PrimitiveType.INTEGER) {
				code.add(Exchange);									// [ ... addr item]
				code.add(StoreI);
			}
			if(type == PrimitiveType.FLOATING) {
				code.add(Exchange);									// [ ... addr item]
				code.add(StoreF);
			}
			if(type == PrimitiveType.BOOLEAN) {
				code.add(Exchange);									// [ ... addr item]
				code.add(StoreC);
			}
			if(type == PrimitiveType.CHARACTER) {
				code.add(Exchange);									// [ ... addr item]
				code.add(StoreC);
			}
			if (type == PrimitiveType.STRING) {
				code.add(Exchange);									// [ ... addr item]
				code.add(StoreI);
			}
			if (type instanceof Array) {
				code.add(Exchange);									// [ ... addr item]
				code.add(StoreI);
			}
			
		}

	}
	
	// Subroutine (NOT at the ASM level) that creates an empty array record.
	public static void cloneArrayRecord(ASMCodeFragment code, int statusFlags, int subtypeSize, Type subtype) {
		Labeller labeller = new Labeller("clone-arr");
		String loopBody = labeller.newLabel("loop-body");
		String loopEnd = labeller.newLabel("loop-end");
		String iLabel = labeller.newLabel("i");
		
		final int typecode = ARRAY_TYPE_ID;						// [ &arr ]
		code.add(Duplicate);									// [ &arr &arr ]
		code.add(PushI, ARRAY_LENGTH_OFFSET);  			
		code.add(Add); 											// [ &arr &length ]
		code.add(LoadI);  										// [ &arr nElems ]
		code.add(Duplicate);  									// [ &arr nElems nElems ]
		
		code.add(PushI, subtypeSize);							// [ &arr nElems nElems subtypeSize]
		code.add(Multiply);										// [ &arr nElems arraySize]
		code.add(Duplicate);									// [ &arr nElems arraySize arraySize]
		storeITo(code, ARRAY_DATASIZE_TEMPORARY);				// [ &arr nElems arraySize]
		code.add(PushI, ARRAY_HEADER_SIZE);						// [ &arr nElems arraySize 16]
		code.add(Add);											// [ &arr nElems totalArraySize]
		
		createRecord(code, typecode, statusFlags);				// [ &arr nElems]
		
		loadIFrom(code, RECORD_CREATION_TEMP);					// [ &arr nElems &newArr ]
		code.add(PushI, ARRAY_HEADER_SIZE);						// [ &arr nElems &newArr 16]
		code.add(Add);											// [ &arr nElems &newFirstElem ]
		loadIFrom(code, ARRAY_DATASIZE_TEMPORARY);				// [ &arr nElems &newFirstElem arraySize]
		code.add(Call, CLEAR_N_BYTES);							// [ &arr nElems]
		
		// Write subtype size + array length
		writeIPBaseOffset(code, RECORD_CREATION_TEMP, Record.ARRAY_SUBTYPE_SIZE_OFFSET, subtypeSize);
		writeIPtrOffset(code, RECORD_CREATION_TEMP, Record.ARRAY_LENGTH_OFFSET); 
																// [ &oldArr ]
		declareI(code, iLabel);
		code.add(PushI, 0);
		storeITo(code, iLabel);
		
		// loop body
		code.add(Label, loopBody);
		
		// bounds checking
		code.add(Duplicate);									// [ &oldArr &oldArr ]
		code.add(PushI, ARRAY_LENGTH_OFFSET);					// [ &oldArr &oldArr 12 ]
		code.add(Add); 											// [ &oldArr &length ]
		code.add(LoadI); 										// [ &oldArr length ]
		loadIFrom(code, iLabel);                       			// [ &oldArr length i ]
		code.add(Subtract);
		code.add(JumpFalse, loopEnd);           				// [ &oldArr ]
		
		// extract the ith element
		code.add(Duplicate);									// [ &oldArr &oldArr ]
		code.add(PushI, ARRAY_HEADER_SIZE);						// [ &oldArr &oldArr 16 ]
		code.add(Add);											// [ &oldArr &oldFirstElem ]
		loadIFrom(code, iLabel);								// [ &oldArr &oldFirstElem i ]
		code.add(PushI, subtypeSize);							// [ &oldArr &oldFirstElem i subtypesize ]
		code.add(Multiply);										// [ &oldArr &oldFirstElem offset ]
		code.add(Add);											// [ &oldArr &ithElem ]
		
		if (subtype == PrimitiveType.RATIONAL) {
			code.add(Duplicate); 								// [ &oldArr &ithElem &ithElem ]
			code.add(PushI, 4); 								// [ &oldArr &ithElem &ithElem 4 ]
			code.add(Add);										// [ &oldArr &num &den ]
			code.add(LoadI); 									// [ &oldArr &num den ]
			storeITo(code, DENOMINATOR_1);						// [ &oldArr &num ]
			code.add(LoadI); 									// [ &oldArr num ]
			storeITo(code, NUMERATOR_1); 						// [ &oldArr ]
		}
		if(subtype == PrimitiveType.INTEGER) {
			code.add(LoadI);
		}
		if(subtype == PrimitiveType.FLOATING) {
			code.add(LoadF);
		}
		if(subtype == PrimitiveType.BOOLEAN) {
			code.add(LoadC);
		}
		if(subtype == PrimitiveType.CHARACTER) {
			code.add(LoadC);
		}
		if (subtype == PrimitiveType.STRING) {
			code.add(LoadI);
		}
		if (subtype instanceof Array) {
			code.add(LoadI);
		}
																// [ &oldArr ithElem ]
		// copy into new array
		loadIFrom(code, RECORD_CREATION_TEMP);					// [ &oldArr ithElem &newArr ]
		code.add(PushI, ARRAY_HEADER_SIZE);						// [ &oldArr ithElem &newArr 16 ]
		code.add(Add);											// [ &oldArr ithElem &newFirstElem ]
		loadIFrom(code, iLabel);								// [ &oldArr ithElem &newFirstElem i ]
		code.add(PushI, subtypeSize);							// [ &oldArr ithElem &newFirstElem i subtypesize ]
		code.add(Multiply);										// [ &oldArr ithElem &newFirstElem offset ]
		code.add(Add);											// [ &oldArr ithElem &ithElem ]
		
		if (subtype == PrimitiveType.RATIONAL) {				// [ &oldArr &num ]
			code.add(Duplicate);								// [ &oldArr &num &num ]
			code.add(PushI, 4);									// [ &oldArr &num &num 4 ]
			code.add(Add);										// [ &oldArr &num &den ]
			loadIFrom(code, DENOMINATOR_1); 					// [ &oldArr &num &den den ] 
			code.add(StoreI);									// [ &oldArr &num ] 
			loadIFrom(code, NUMERATOR_1);						// [ &oldArr &num num ] 
			code.add(StoreI); 									// [ &oldArr ] 
		}
		if (subtype == PrimitiveType.INTEGER) {
			code.add(Exchange);
			code.add(StoreI);
		}
		if (subtype == PrimitiveType.FLOATING) {
			code.add(Exchange);
			code.add(StoreF);
		}
		if (subtype == PrimitiveType.BOOLEAN) {
			code.add(Exchange);
			code.add(StoreC);
		}
		if (subtype == PrimitiveType.CHARACTER) {
			code.add(Exchange);
			code.add(StoreC);
		}
		if (subtype == PrimitiveType.STRING) {
			code.add(Exchange);
			code.add(StoreI);
		}
		if (subtype instanceof Array) {
			code.add(Exchange);
			code.add(StoreI);
		}
																// [ &oldArr ]
		loadIFrom(code, iLabel);								// [ &oldArr i ]
		code.add(PushI, 1);										// [ &oldArr i 1 ]
		code.add(Add);											// [ &oldArr i+1 ]
		storeITo(code, iLabel);
		code.add(Jump, loopBody);
		
		code.add(Label, loopEnd);								// [ &oldArr ]
		code.add(Pop);											// [ ]
		loadIFrom(code, RECORD_CREATION_TEMP);					// [ &newArr ]
	}
	
	public static void createStringRecord(ASMCodeFragment code, int statusFlags, String string)  {
		final int typecode = Record.STRING_TYPE_ID;

		code.add(Duplicate);									// [ ... length length]
		storeITo(code, STRING_LENGTH_TEMPORARY);				// [ ... length]
		loadIFrom(code, STRING_LENGTH_TEMPORARY);				// [ ... length length]
		code.add(PushI, ARRAY_HEADER_SIZE + 1);					// [ ... length length 16+1]
		code.add(Add);											// [ ... length totalStringSize]
		
		createRecord(code, typecode, statusFlags);				// [ ... length]
		
		loadIFrom(code, RECORD_CREATION_TEMP);					// [ ... length ptr]
		code.add(PushI, STRING_HEADER_SIZE);					// [ ... length ptr 12]
		code.add(Add);											// [ ... length firstCharPtr]
		loadIFrom(code, STRING_LENGTH_TEMPORARY);				// [ ... length firstCharPtr length]
		code.add(PushI, 1);										
		code.add(Add);											// [ ... length firstCharPtr length+1]
		code.add(Call, CLEAR_N_BYTES);							// [ ... length ]
		
		// Write subtype size + array length
		writeIPtrOffset(code, RECORD_CREATION_TEMP, Record.STRING_LENGTH_OFFSET);
		
		// Actually write the contents of the string
		for (int i = 0; i < string.length(); i++) {
			code.add(PushI, string.charAt(i)); 					// [ ... ch ]
			loadIFrom(code, RECORD_CREATION_TEMP);				// [ ... ch ptr]
			code.add(PushI, STRING_HEADER_SIZE); 				// [ ... ch ptr 12]
			code.add(Add);										// [ ... ch firstCharPtr]
			writeCOffset(code, i);								// [ ]
		}
		
		// write the null terminator
		code.add(PushI, 0); 									// [ ... ch ]
		loadIFrom(code, RECORD_CREATION_TEMP);					// [ ... ch ptr]
		code.add(PushI, STRING_HEADER_SIZE); 					// [ ... ch ptr 12]
		code.add(Add);											// [ ... ch firstCharPtr]
		writeCOffset(code, string.length());					// [ ]
		
		// The array resides in record_creation_temp 
		loadIFrom(code, RECORD_CREATION_TEMP);					// [ ... ptr]
	}
	
	public static ASMCodeFragment releaseRecord(Type type) {
		ASMCodeFragment code = new ASMCodeFragment(CodeType.GENERATES_VOID);
		final int subtypeSize = 4;
		Labeller labeller = new Labeller("release");
		String endLabel = labeller.newLabel("end");
		String loop = labeller.newLabel("loop");
		String iLabel = labeller.newLabel("i");

		
		code.add(Duplicate); 									// [ ptr ptr ]
		code.add(PushI, RECORD_STATUS_OFFSET);					// [ ptr ptr 4 ]
		code.add(Add); 											// [ ptr &status ]
		code.add(Duplicate); 									// [ ptr &status &status ]
		code.add(LoadI); 										// [ ptr &status status ]
		
		// perform bit checks
		code.add(Duplicate); 									// [ ptr &status status status ]
		code.add(Duplicate); 									// [ ptr &status status status status ]
		code.add(PushI, 4);
		code.add(BTAnd);										// [ ptr &status status status isDeleted ]
		code.add(JumpTrue, endLabel); 							// [ ptr &status status status ]
		code.add(PushI, 8);
		code.add(BTAnd); 										// [ ptr &status status isPermanent ]
		code.add(JumpTrue, endLabel); 							// [ ptr &status status ]
		
		// set is-deleted to 1 (bit 2)
		code.add(PushI, 4); 									// [ ptr &status status 4 ]
		code.add(BTOr); 										// [ ptr &status newStatus ]
		code.add(StoreI); 										// [ ptr ]
		
		// check if reference type		
		code.add(Duplicate); 									// [ ptr ptr ]
		code.add(PushI, RECORD_STATUS_OFFSET);					// [ ptr ptr 4 ]
		code.add(Add); 											// [ ptr &status ]
		code.add(LoadI); 										// [ ptr status ]
		code.add(PushI, 2); 									// [ ptr status 2 ]
		code.add(BTAnd); 										// [ ptr subtypeIsReference ]
		code.add(JumpFalse, endLabel); 							// [ ptr ]
		
		// recurse if necessary
		declareI(code, iLabel);
		code.add(PushI, 0);
		storeITo(code, iLabel);
		
		// loop start
		code.add(Label, loop);
		
		// bounds checking
		code.add(Duplicate); 									// [ ptr ptr ]
		code.add(PushI, ARRAY_LENGTH_OFFSET);
		code.add(Add); 											// [ ptr &length ]
		code.add(LoadI); 										// [ ptr length ]
		loadIFrom(code, iLabel); 								// [ ptr length i ]
		code.add(Subtract); 									// [ ptr length-i ]
		code.add(JumpFalse, endLabel);							// [ ptr ]
		
		code.add(Duplicate);									// [ ptr ptr ]
		code.add(PushI, ARRAY_HEADER_SIZE); 					// [ ptr ptr 16 ]
		code.add(Add); 											// [ ptr &firstEl ]
		loadIFrom(code, iLabel);								// [ ptr &firstEl i ]
		
		code.add(PushI, subtypeSize);							// [ ptr &firstEl i 4 ]
		code.add(Multiply); 									// [ ptr &firstEl offset ]
		code.add(Add); 											// [ ptr &ithEl ]
		code.add(LoadI); 										// [ ptr ithEl ]
		
		if (type instanceof Array) {							// [ ptr ]
			code.append(releaseRecord(((Array) type).getSubtype()));
		} else {
			code.add(Pop);
		}
		

		loadIFrom(code, iLabel); 								// [ ptr i ]
		code.add(PushI, 1); 									// [ ptr i 1 ]
		code.add(Add); 											// [ ptr i+1 ]
		storeITo(code, iLabel);									// [ ptr ]
		code.add(Jump, loop);
		
		code.add(Label, endLabel);								// [ ptr ]
		code.add(Pop);											// [ ]
		
		return code;
	}
}
