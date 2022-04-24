package util;

public interface Stateful<T> {
	T copySettings();
	T copyState();
}
