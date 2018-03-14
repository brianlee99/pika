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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		for (Type inputType : inputTypes) {
			sb.append(inputType.toString());
			sb.append(", ");
		}
		sb.append(">");
		if (outputType != null) {
			sb.append("->");
			sb.append(outputType.toString());
		}
		return sb.toString();
	}
	
	public List<Type> getInputTypes() {
		return inputTypes;
	}
	public Type getOutputType() {
		return outputType;
	}
	
	@Override
	public boolean equivalent(Type type) {
		if (!(type instanceof LambdaType)) return false;
		LambdaType otherType = (LambdaType) type;
		List<Type> otherInputTypes = otherType.getInputTypes();
		
		if (inputTypes.size() != otherInputTypes.size()) return false;
		
		for (int i = 0; i < inputTypes.size(); i++) {
			if (!inputTypes.get(i).equivalent(otherInputTypes.get(i))) return false;
		}
		
		if (!outputType.equivalent(otherType.getOutputType())) return false;
		return true;
	}
	
	@Override
	public Type getConcreteType() {
		//		Type concreteSubtype = subtype.getConcreteType();
		//		return new Array(concreteSubtype);
		return this;
	}
}
