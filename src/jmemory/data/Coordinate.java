/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.data;

/**
 *
 * @author LostMekka
 */
public class Coordinate {
	
	public int x, y;

	public Coordinate() {
		x = 0;
		y = 0;
	}

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Coordinate other = (Coordinate) obj;
		return this.x == other.x && this.y == other.y;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + this.x;
		hash = 79 * hash + this.y;
		return hash;
	}

	@Override
	public String toString() {
		return "{" + x + ", " + y + '}';
	}
	
	public int getSquaredDistanceTo(Coordinate c){
		return (x-c.x) * (x-c.x) + (y-c.y) * (y-c.y);
	}
	
}
