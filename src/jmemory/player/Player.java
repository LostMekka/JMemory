/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.player;

import java.io.Serializable;
import jmemory.data.Coordinate;
import jmemory.game.GameplayState;
import org.newdawn.slick.Image;

/**
 *
 * @author LostMekka
 */
public abstract class Player implements Serializable {
	
	private String name;
	private int score = 0;

	public Player(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getScore() {
		return score;
	}

	public void incrementScore() {
		score += 2;
	}
	
	public void resetScore() {
		score = 0;
	}
	
	public abstract void notifyImage(Coordinate location, Image image);
	public abstract void notifyCardsTaken(Image image);
	public abstract boolean isHuman();
	
}
