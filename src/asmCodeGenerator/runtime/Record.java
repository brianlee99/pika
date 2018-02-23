package asmCodeGenerator.runtime;

import static asmCodeGenerator.Macros.loadIFrom;
import static asmCodeGenerator.Macros.storeITo;
import static asmCodeGenerator.Macros.writeCOffset;
import static asmCodeGenerator.Macros.writeIPBaseOffset;
import static asmCodeGenerator.Macros.writeIPtrOffset;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import static asmCodeGenerator.runtime.RunTime.*;

import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
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

		loadIFrom(code, RECORD_CREATION_TEMP);				// [ ... item arrPtr]
		code.add(PushI, Record.ARRAY_HEADER_SIZE);
		code.add(Add);										// [ ... item elemsPtr]
		code.add(PushI, offset);
		code.add(Add);										// [ ... item addr]
		storeITo(code, POPULATE_ARRAY_ADDRESS_TEMP);
		loadIFrom(code, POPULATE_ARRAY_ADDRESS_TEMP);
		
		if (type == PrimitiveType.RATIONAL) {
			code.add(PushI, 4);
			code.add(Add); 										// [ ... num denom denomAddr ]
			code.add(Exchange);									// [ ... num denomAddr denom ]
			code.add(StoreI);									// [ ... num  ]
			loadIFrom(code, POPULATE_ARRAY_ADDRESS_TEMP);  		// [ ... num numAddr ]
			code.add(Exchange);
			code.add(StoreI);
		}
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
	
	public static void createStringRecord(ASMCodeFragment code, int statusFlags, String string)  {
		final int typecode = Record.STRING_TYPE_ID;
		
		code.add(Duplicate);									// [ ... length length]
		storeITo(code, STRING_LENGTH_TEMPORARY);				// [ ... length]
		loadIFrom(code, STRING_LENGTH_TEMPORARY);				// [ ... length length]
		code.add(PushI, Record.ARRAY_HEADER_SIZE + 1);			// [ ... length length 16+1]
		code.add(Add);											// [ ... length totalStringSize]
		
		createRecord(code, typecode, statusFlags);				// [ ... length]
		
		loadIFrom(code, RECORD_CREATION_TEMP);					// [ ... length ptr]
		code.add(PushI, Record.STRING_HEADER_SIZE);				// [ ... length ptr 12]
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
			code.add(PushI, Record.STRING_HEADER_SIZE); 		// [ ... ch ptr 12]
			code.add(Add);										// [ ... ch firstCharPtr]
			writeCOffset(code, i);								// []
		}
		
		// write the null terminator
		code.add(PushI, 0); 								// [ ... ch ]
		loadIFrom(code, RECORD_CREATION_TEMP);				// [ ... ch ptr]
		code.add(PushI, Record.STRING_HEADER_SIZE); 		// [ ... ch ptr 12]
		code.add(Add);										// [ ... ch firstCharPtr]
		writeCOffset(code, string.length());				// []
		
		// The array resides in record_creation_temp 
		loadIFrom(code, RECORD_CREATION_TEMP);					// [ ... ptr]
	}
	

	
}
