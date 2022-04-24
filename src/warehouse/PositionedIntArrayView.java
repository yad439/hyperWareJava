package warehouse;

final class PositionedIntArrayView implements IntArrayView{
	private final int[] buffer;
	private final int[] positions;

	PositionedIntArrayView(final int[] buffer, final int[] positions) {
		this.buffer = buffer;
		this.positions = positions;
	}

	@Override
	public int size() {
		return positions.length;
	}

	@Override
	public int get(final int index) {
		return buffer[positions[index]];
	}

	@Override
	public void set(final int index, final int value) {
		buffer[positions[index]]=value;
	}

	@Override
	public int getRealIndex(final int index) {
		return positions[index];
	}

	@Override
	public int get(final int index, final int offset) {
		return buffer[positions[index]+offset];
	}

	@Override
	public void set(final int index, final int offset, final int value) {
		buffer[positions[index]+offset]=value;
	}

	@Override
	public int[] getBuffer() {
		return buffer;
	}
}
