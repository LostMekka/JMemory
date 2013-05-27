/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.player;

import jmemory.data.Coordinate;
import org.newdawn.slick.Image;

/**
 *
 * @author LostMekka
 */
public class HumanPlayer extends Player {

	public HumanPlayer(String name) {
		super(name);
	}

	@Override
	public void notifyImage(Coordinate location, Image image) {}

	@Override
	public void notifyCardsTaken(Image image) {}
	
	@Override
	public boolean isHuman() {
		return true;
	}

}
