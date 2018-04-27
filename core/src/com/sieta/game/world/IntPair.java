package com.sieta.game.world;

public class IntPair {
	public int x;
	public int y;
	
	public IntPair(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode () {
		return (x << 16) + y;
	}

	@Override
	public boolean equals (final Object O) {
		//if (getClass() != O.getClass()) return false;
		if (x != ((IntPair)(O)).x) return false;
		if (y != ((IntPair)(O)).y) return false;
		return true;
	}

	@Override
	public String toString() {
		return x + ";" + y;
	}
}
