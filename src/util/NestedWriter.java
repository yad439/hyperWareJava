package util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import java.io.PrintWriter;
import java.io.Writer;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class NestedWriter extends Writer {
	@Delegate private final PrintWriter inner;
	private final String indent;

	public NestedWriter(final PrintWriter inner){
		this.inner=inner;
		indent="";
	}

	public void indent(){
		inner.print(indent);
	}

	public void printIndented(final String line){
		inner.print(indent);
		inner.print(line);
	}

	public void printIndented(final char line){
		inner.print(indent);
		inner.print(line);
	}

	public void printLine(final String line){
		inner.print(indent);
		inner.println(line);
	}

	public void printLine(final char line){
		inner.print(indent);
		inner.println(line);
	}

	public void formatLine(final String format, final Object... args){
		inner.print(indent);
		inner.printf(format,args);
		inner.println();
	}

	public NestedWriter getScoped(){
		return new NestedWriter(inner,indent+'\t');
	}
}
