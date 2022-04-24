package warehouse;

final class FullIntArrayView implements IntArrayView {
	private final int[] buffer;

	FullIntArrayView(final int[] buffer) {
		this.buffer = buffer;
	}

	@Override
	public int size() {
		return buffer.length;
	}

	@Override
	public int get(final int index) {
		return buffer[index];
	}

	@Override
	public void set(final int index, final int value) {
		buffer[index]=value;
	}

	@Override
	public int getRealIndex(final int index) {
		return index;
	}

	@Override
	public int get(final int index, final int offset) {
		return index+offset;
	}

	@Override
	public void set(final int index, final int offset, final int value) {
		buffer[index+offset]=value;
	}

	@Override
	public int[] getBuffer() {
		return buffer;
	}
}
