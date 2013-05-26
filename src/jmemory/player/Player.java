/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.player;

import jmemory.data.Coordinate;
import jmemory.game.GameplayState;
import org.newdawn.slick.Image;

/**
 *
 * @author LostMekka
 */
public abstract class Player {
	
	private String name;
	private GameplayState gameplayState;
	private int score = 0;

	public Player(String name, GameplayState gameplayState) {
		this.name = name;
		this.gameplayState = gameplayState;
	}

	public GameplayState getGameplayState() {
		return gameplayState;
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
