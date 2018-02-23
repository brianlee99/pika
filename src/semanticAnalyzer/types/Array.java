package semanticAnalyzer.types;

public class Array implements Type {
	private Type subtype;
	private String infoString;
	
	public Array(Type subtype) {
		this.subtype = subtype;
		this.infoString = "arr[" + this.subtype.infoString() + "]";
	}
	
	@Override
	public int getSize() {
		return 4;
		//return subtype.getSize();
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
		if (otherType instanceof Array) {
			Array otherArray = (Array) otherType;
			return subtype.equivalent(otherArray.getSubtype());
		}
		return false;
	}
	

	@Override
	public Type getConcreteType() {
		Type concreteSubtype = subtype.getConcreteType();
		return new Array(concreteSubtype);
	}

}
