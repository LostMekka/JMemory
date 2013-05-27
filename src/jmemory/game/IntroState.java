/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.game;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class IntroState extends BasicGameState {
	
	private static IntroState instance = new IntroState();
	public static IntroState getInstance() {
		return instance;
	}
	private IntroState() {}
	
	private static final int FADE_TIMEOUT = 800;
	private static final int STAY_TIMEOUT = 2500;
	private static final int FOLLOWUP_GAME_STATE = 1;
	
	private Image[] images;
	private int[] scales;
	private Sound[] sounds;
	private int timer = 0, state = -1;
	private float ix, iy, iw, ih;
	private float blendAlpha = 1f;
	
	private void updateImage(GameContainer gc){
		Image image = images[state/3];
		int scale = scales[state/3];
		iw = image.getWidth() * scale;
		ih = image.getHeight() * scale;
		ix = (gc.getWidth() - iw) / 2f;
		iy = (gc.getHeight() - ih) / 2f;
	}
	
	@Override
	public int getID() {
		return 0;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		images = new Image[]{
			new Image("sys/lmsLogo.png", false, Image.FILTER_NEAREST),
			new Image("sys/gameLogo.png", false, Image.FILTER_NEAREST),
		};
		scales = new int[]{ 4, 4 };
		sounds = new Sound[]{
			new Sound("sys/party.wav"),
			null,
		};
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics grphcs) throws SlickException {
		grphcs.setColor(Color.white);
		grphcs.fillRect(0, 0, gc.getWidth(), gc.getHeight());
		images[state/3].draw(ix, iy, iw, ih);
		grphcs.setColor(new Color(0f, 0f, 0f, blendAlpha));
		grphcs.fillRect(0, 0, gc.getWidth(), gc.getHeight());
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int i) throws SlickException {
		if(state == -1){
			state = 0;
			updateImage(gc);
		}
		timer += i;
		while((state % 3 != 1 && timer >= FADE_TIMEOUT) || 
				(state % 3 == 1 && timer >= STAY_TIMEOUT)){
			if(state % 3 == 1){
				timer -= STAY_TIMEOUT;
			} else {
				timer -= FADE_TIMEOUT;
			}
			state++;
			if(state >= images.length*3){
				sbg.enterState(FOLLOWUP_GAME_STATE);
				state = 0;
				return;
			}
			if(state % 3 == 0){
				updateImage(gc);
			}
			if(state % 3 == 1){
				Sound sound = sounds[state/3];
				if(sound != null) sound.play();
			}
		}
		if(state % 3 != 1){
			blendAlpha = (float)timer / (float)FADE_TIMEOUT;
			if(state % 3 == 0) blendAlpha = 1f - blendAlpha;
		}
	}
	
}
