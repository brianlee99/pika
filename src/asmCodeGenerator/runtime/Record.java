package asmCodeGenerator.runtime;

import static asmCodeGenerator.Macros.storeITo;
import static asmCodeGenerator.Macros.writeIPBaseOffset;
import static asmCodeGenerator.codeStorage.ASMOpcode.Call;
import static asmCodeGenerator.runtime.RunTime.*;

import asmCodeGenerator.codeStorage.ASMCodeFragment;

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
	
	
}
