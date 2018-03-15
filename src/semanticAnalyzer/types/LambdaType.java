package semanticAnalyzer.types;

import java.util.ArrayList;
import java.util.List;

public class LambdaType implements Type {
	
	private String infoString;
	private List<Type> parameterTypes;
	private Type returnType;
	
	public LambdaType(List<Type> inputTypes, Type outputType) {
		this.parameterTypes = new ArrayList<>(inputTypes);
		this.returnType = outputType;
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
		int n = parameterTypes.size();
		for (int i = 0; i < n; i++) {
			Type inputType = parameterTypes.get(i);
			sb.append(inputType.toString());
			if (i < n-1) {
				sb.append(", ");
			}
		}
		
		sb.append(">");
		if (returnType != null) {
			sb.append(" -> ");
			sb.append(returnType.toString());
		}
		return sb.toString();
	}
	
	public List<Type> getParameterTypes() {
		return parameterTypes;
	}
	
	public Type getReturnType() {
		return returnType;
	}
	
	@Override
	public boolean equivalent(Type type) {
		if (!(type instanceof LambdaType)) return false;
		LambdaType otherType = (LambdaType) type;
		List<Type> otherInputTypes = otherType.getParameterTypes();
		
		if (parameterTypes.size() != otherInputTypes.size()) return false;
		
		for (int i = 0; i < parameterTypes.size(); i++) {
			if (!parameterTypes.get(i).equivalent(otherInputTypes.get(i))) return false;
		}
		
		if (!returnType.equivalent(otherType.getReturnType())) return false;
		return true;
	}
	
	@Override
	public Type getConcreteType() {
		//		Type concreteSubtype = subtype.getConcreteType();
		//		return new Array(concreteSubtype);
		return this;
	}
}
