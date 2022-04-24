package heuristic.util;

import java.util.Objects;

public final class DoubleRingBuffer {
	private final double[] buffer;
	private int startPos=0;
	private int endPos=0;
	private boolean full=false;

	public DoubleRingBuffer(final int size){
		buffer=new double[size];
	}

	public int size(){
		if(full)return buffer.length;
		if(endPos>=startPos)return endPos-startPos;
		return buffer.length-(startPos-endPos);
	}

	public int capacity(){return buffer.length;}

	public boolean isEmpty(){
		return startPos==endPos && !full;
	}

	public void addFirst(final double value){
		buffer[endPos]=value;
		assert !full || startPos==endPos;
		if (startPos == endPos && full) startPos = (startPos + 1) % buffer.length;
		endPos=(endPos+1)% buffer.length;
		if(startPos==endPos)full=true;
	}

	public double get(final int index){
		Objects.checkIndex(index,size());
		if(endPos-index>=0)return buffer[endPos-index];
		return buffer[endPos-index+ buffer.length];
	}

	public void clear(){
		startPos=0;
		endPos=0;
		full=false;
	}
}
