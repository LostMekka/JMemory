/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.game;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class MemoryGame extends StateBasedGame {

	public MemoryGame() {
		super("JMemory by LostMekka for Juli");
	}

	@Override
	public void initStatesList(GameContainer gc) throws SlickException {
		addState(IntroState.getInstance());
		addState(PlayerTableState.getInstance());
		addState(GameplayState.getInstance());
	}
	
}
