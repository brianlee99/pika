package asmCodeGenerator;

import java.util.HashMap;

public class StringLabeller {

	private static int labelSequenceNumber = 0;
	
	private static HashMap<String, String> stringMap;
	
	public StringLabeller() {
		stringMap = new HashMap<>();
		// labelNumber = labelSequenceNumber;
	}
	
	private String makePrefix() {
		return "-string-constant-" + labelSequenceNumber;
	}

	public String getLabel(String preLabel) {
		return stringMap.get(preLabel);
	}
	
	public boolean containsLabel(String preLabel) {
		return stringMap.containsKey(preLabel);
	}
	
	public String createLabel(String preLabel) {
		labelSequenceNumber++;
		String label = makePrefix();
		stringMap.put(preLabel, label);
		return label;
	}

}
