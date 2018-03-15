package semanticAnalyzer.types;

public class ArrayType implements Type {
	private Type subtype;
	private String infoString;
	
	public ArrayType(Type subtype) {
		this.subtype = subtype;
		this.infoString = "arr[" + this.subtype.infoString() + "]";
	}
	
	@Override
	public int getSize() {
		return 4;
	}
	
	public Type getSubtype() {
		return subtype;
	}

	@Override
	public String infoString() {
		return infoString;
	}
	
	@Override
	public boolean equivalent(Type otherType) {
		if (otherType instanceof ArrayType) {
			ArrayType otherArray = (ArrayType) otherType;
			return subtype.equivalent(otherArray.getSubtype());
		}
		return false;
	}
	@Override
	public Type getConcreteType() {
		Type concreteSubtype = subtype.getConcreteType();
		return new ArrayType(concreteSubtype);
	}
}
