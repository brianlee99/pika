package semanticAnalyzer.types;

import java.util.ArrayList;
import java.util.List;

public class LambdaType implements Type {
	
	private String infoString;
	private List<Type> inputTypes;
	private Type outputType;
	
	public LambdaType(List<Type> inputTypes, Type outputType) {
		this.inputTypes = new ArrayList<>(inputTypes);
		this.outputType = outputType;
		this.infoString = toString();
	}

	public int getSize() {
		return 4;
	}
	public String infoString() {
		return infoString;
	}
	
	@Override
	public boolean equivalent(Type type) {
//		if (otherType instanceof Array) {
//		Array otherArray = (Array) otherType;
//		return subtype.equivalent(otherArray.getSubtype());
//	}
//	return false;
		// really we need to check 
		return false;
	}
	
	@Override
	public Type getConcreteType() {
		// really we need to do something more sophisticated here
		//		Type concreteSubtype = subtype.getConcreteType();
//		return new Array(concreteSubtype);
		return this;
	}
}
