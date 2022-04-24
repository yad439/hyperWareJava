package warehouse;

final class IntRef {
	private int value=0;

	int getValue() {
		return value;
	}

	void increment(){value++;}

	void add(final int x){value+=x;}
}
