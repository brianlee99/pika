package semanticAnalyzer.types;

public class Array implements Type {
	private Type subtype;
	private String infoString;
	
	public Array(Type subtype) {
		this.subtype = subtype;
		this.infoString = toString();
	}
	
	@Override
	public int getSize() {
		return subtype.getSize();
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

}
