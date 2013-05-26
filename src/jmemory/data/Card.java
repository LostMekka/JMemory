/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.data;

import org.newdawn.slick.Image;

/**
 *
 * @author LostMekka
 */
public class Card {
	
	public enum State { hidden, revealing, revealed, hiding }
	
	private Image image;
	private State state = State.hidden;
	private float timer = 0f;

	public Card(Image image) {
		this.image = image;
		state = State.hidden;
		timer = 0f;
	}

	public Card(Card card) {
		image = card.image;
		state = State.hidden;
		timer = 0f;
	}

	public Image getImage() {
		return image;
	}

	public State getState() {
		return state;
	}

	public float getAnimationProgress() {
		return timer;
	}
	
	public boolean isTurning(){
		return (state == State.revealing) || (state == State.hiding);
	}
	
	public boolean isHidden(){
		return (state == State.hidden);
	}
	
	public boolean isRevealed(){
		return (state == State.revealed);
	}
	
	public boolean startTurning(){
		if(isTurning()) return false;
		if(isHidden()) state = State.revealing; else state = State.hiding;
		timer = 0f;
		return true;
	}
	
	public void tick(float time){
		if(!isTurning()) return;
		timer += time;
		if(timer >= 1f){
			if(state == State.revealing) state = State.revealed;
			if(state == State.hiding) state = State.hidden;
		}
	}
	
	public void hideImmediately(){
		state = State.hidden;
	}
	
	public void revealImmediately(){
		state = State.revealed;
	}
	
}
