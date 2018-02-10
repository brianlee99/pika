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

	@Override
	public boolean equivalent(Type otherType) {
		if (otherType instanceof TypeVariable) {
			throw new RuntimeException(
					"equals attempted on two types containing type variables.");
		}
		if (this.getTypeConstraint() == PrimitiveType.NO_TYPE) {
			setType(otherType);
			return true;
		}
		return this.getTypeConstraint().equivalent(otherType);
	}

}
