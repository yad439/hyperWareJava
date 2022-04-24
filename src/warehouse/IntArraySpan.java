package warehouse;

import java.util.Objects;

final class IntArraySpan implements IntArrayView{
	private final int[] buffer;
	private final int from;
	private final int length;

	IntArraySpan(final int[] buffer, final int from, final int length) {
		Objects.checkFromIndexSize(from,length,buffer.length);
		this.buffer = buffer;
		this.from = from;
		this.length = length;
	}

	@Override
	public int size() {
		return length;
	}

	@Override
	public int get(final int index) {
		Objects.checkIndex(index,length);
		return buffer[from+index];
	}

	@Override
	public void set(final int index, final int value) {
		Objects.checkIndex(index,length);
		buffer[from+index]=value;
	}

	@Override
	public int getRealIndex(final int index) {
		Objects.checkIndex(index,length);
		return from+index;
	}

	@Override
	public int get(final int index, final int offset) {
		Objects.checkIndex(index,length);
		return buffer[from+index+offset];
	}

	@Override
	public void set(final int index, final int offset, final int value) {
		Objects.checkIndex(index,length);
		buffer[from+index+offset]=value;
	}

	@Override
	public int[] getBuffer() {
		return buffer;
	}
}
