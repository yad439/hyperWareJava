package util;

import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

@RequiredArgsConstructor
public final class StreamIterable<T> implements Iterable<T> {
	private final Stream<T> stream;

	@Override
	public Iterator<T> iterator() {return stream.iterator();}

	@Override
	public void forEach(final Consumer<? super T> action) {stream.forEach(action);}

	@Override
	public Spliterator<T> spliterator() {return stream.spliterator();}
}
