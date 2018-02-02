package semanticAnalyzer.types;

public class TypeVariable implements Type {
	
	private String name;
	private Type typeConstraint;
	
	public TypeVariable(String name) {
		this.name = name;
		this.typeConstraint = PrimitiveType.NO_TYPE;
	}
	
	public String getName() {
		return name;
	}
	
	public Type getTypeConstraint() {
		return typeConstraint;
	}
	
	private void setType(Type typeConstraint) {
		this.typeConstraint = typeConstraint;
	}
	
	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public String infoString() {
		return toString();
	}
	
	public String toString() {
		return "<" + getName() + ">";
	}

}
