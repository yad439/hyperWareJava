package warehouse;

interface IntArrayView {
	int size();
	int get(int index);
	void set(int index, int value);
	int getRealIndex(int index);
	int get(int index,int offset);
	void set(int index, int offset, int value);
	default void swap(final int index1,final int index2){
		final var tmp = get(index1);
		set(index1, get(index2));
		set(index2, tmp);
	}
	default void move(final int from,final int to){
		final var tmp = get(from);
		if (to < from) for(var i=from-1;i>=to;i--) set(i+1,get(i));
		else for(var i=from+1;i<=to;i++) set(i-1,get(i));
		set(to, tmp);
	}
	int[] getBuffer();
}
