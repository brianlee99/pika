package symbolTable;

import java.util.ArrayList;
import java.util.List;

public class ParameterMemoryAllocator implements MemoryAllocator {
	MemoryAccessMethod accessor;
	final int startingOffset;
	int currentOffset;
	int minOffset;
	String baseAddress;
	List<Integer> bookmarks;
	List<MemoryLocation> memoryLocations;
	
	public ParameterMemoryAllocator(MemoryAccessMethod accessor, String baseAddress, int startingOffset) {
		this.accessor = accessor;
		this.baseAddress = baseAddress;
		this.startingOffset = startingOffset;
		this.currentOffset = startingOffset;
		this.minOffset = startingOffset;
		this.bookmarks = new ArrayList<Integer>();
		this.memoryLocations = new ArrayList<>();
	}
	public ParameterMemoryAllocator(MemoryAccessMethod accessor,String baseAddress) {
		this(accessor, baseAddress, 0);
	}

	@Override
	public MemoryLocation allocate(int sizeInBytes) {
		currentOffset -= sizeInBytes;
		updateMin();
		MemoryLocation newLocation = new MemoryLocation(accessor, baseAddress, currentOffset);
		memoryLocations.add(newLocation);
		return newLocation;
	}
	private void updateMin() {
		if(minOffset > currentOffset) {
			minOffset = currentOffset;
		}
	}

	@Override
	public String getBaseAddress() {
		return baseAddress;
	}

	@Override
	public int getMaxAllocatedSize() {
		return startingOffset - minOffset;
	}
	
	@Override
	public void saveState() {
		bookmarks.add(currentOffset);
	}
	@Override
	public void restoreState() {
		assert bookmarks.size() > 0;
		int bookmarkIndex = bookmarks.size()-1;
		currentOffset = (int) bookmarks.remove(bookmarkIndex);
		// check if there are no bookmarks after the remove
		if (bookmarkIndex == 0) {
			for (MemoryLocation location : memoryLocations) {
				location.setOffset(0);
			}
		}
	}
}
