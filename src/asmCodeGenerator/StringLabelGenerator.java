package asmCodeGenerator;

import java.util.HashMap;

public class StringLabelGenerator {
	private HashMap<String, String> labelHashMap;
	
	public StringLabelGenerator() {
		labelHashMap = new HashMap<>();
	}
	
	public boolean hasString(String contents) {
		return labelHashMap.containsKey(contents);
	}
	
	public String findLabel(String contents) {
		return labelHashMap.get(contents);
	}
	
	public String generateLabel(String contents) {
		String newLabel = contents;
		labelHashMap.put(contents, newLabel);
		return newLabel;
	}
	
	
}
