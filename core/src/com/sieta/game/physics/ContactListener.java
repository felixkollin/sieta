package com.sieta.game.physics;


/**
 * This should be implemented by the collision handler.
 * @author felixkollin
 *
 */
public interface ContactListener {
	public void contact(Body a, Body b);
	
	/**
	 * Returns true if collision should be made.
	 * @param b
	 * @param t
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contact(Body b, short tile, float mass, int x, int y, float deltaTime);
	
	public void post(Body b, short tile, float mass, int x, int y, boolean slope);
}
