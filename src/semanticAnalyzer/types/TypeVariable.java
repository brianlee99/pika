package semanticAnalyzer.types;

public class TypeVariable implements Type {
	
	private String name;
	private Type type;
	
	public TypeVariable(String name) {
		this.name = name;
		this.type = PrimitiveType.NO_TYPE;
	}
	
	public String getName() {
		return name;
	}
	
	public Type getType() {
		return type;
	}
	
	private void setType(Type type) {
		this.type = type;
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
		if (this.getType() == PrimitiveType.NO_TYPE) {
			setType(otherType);
			return true;
		}
		return getType().equivalent(otherType);
	}
	
	@Override
	public Type getConcreteType() {
		return getType().getConcreteType();
	}

	
	public void reset() {
		setType(PrimitiveType.NO_TYPE);
	}
}
