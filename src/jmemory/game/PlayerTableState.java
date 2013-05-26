/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.game;

import java.util.ArrayList;
import java.util.Iterator;
import jmemory.player.AIPlayer;
import jmemory.player.HumanPlayer;
import jmemory.player.Player;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class PlayerTableState extends BasicGameState {
	
	private static PlayerTableState instance = new PlayerTableState();
	public static PlayerTableState getInstance() {
		return instance;
	}
	private PlayerTableState() {}
	
	private static float DEFAULT_DIFFICULTY = 0.5f;
	private static Color PANEL_COLOR = new Color(0f, 0f, 0.5f, 1f);
	private static Color WINDOW_COLOR = new Color(0f, 0f, 0f, 0.6f);
	private static Color BORDER_COLOR = new Color(0f, 0.4f, 0.7f, 1f);
	private static Color TEXT_COLOR = new Color(0f, 0.9f, 0.5f, 1f);
	
	private boolean selectionMode = true, mousePressed = false;
	private int[] playerTypes = {1, 2, 0, 0, 0, 0};
	private String[] playerNames = {
		"Player 1", "Player 2", "Player 3", 
		"Player 4", "Player 5", "Player 6"};
	private int[] playerScores = new int[6];
	private float[] playerDifficulties = new float[6];
	private int nameEditFocus = -1, pictureCount, minPictureCount = 4, maxPictureCount, lastMouseX = 0, lastMouseY = 0;
	private GameContainer gc;
	private StateBasedGame sbg;
	
	public void setMode(boolean selectionMode){
		this.selectionMode = selectionMode;
	}
	
	public void setPlayers(ArrayList<Player> players){
		int i = 0;
		Iterator<Player> iter = players.iterator();
		while(i<6){
			if(iter.hasNext()){
				Player p = iter.next();
				if(p.isHuman()){
					playerTypes[i] = 1;
					playerDifficulties[i] = DEFAULT_DIFFICULTY;
				} else {
					playerTypes[i] = 2;
					playerDifficulties[i] = ((AIPlayer)p).getDifficulty();
				}
				playerNames[i] = p.getName();
				playerScores[i] = p.getScore();
			} else {
				playerTypes[i] = 0;
				playerNames[i] = "player " + (i+1);
				playerScores[i] = 0;
				playerDifficulties[i] = DEFAULT_DIFFICULTY;
			}
			i++;
		}
	}
	
	private int getTiledX(int mouseX){
		return (mouseX - (gc.getWidth() - 800) / 2) / 50;
	}
	
	private int getTiledY(int mouseY){
		return (mouseY - (gc.getHeight()- 600) / 2) / 50;
	}
	
	private float getDifficulty(int mouseX){
		return ((mouseX - (gc.getWidth() - 800f) / 2f) - 450f) / 299f;
	}
	
	@Override
	public int getID() {
		return 1;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		this.gc = gc;
		this.sbg = sbg;
		minPictureCount = 4;
		maxPictureCount = 8;
		pictureCount = (minPictureCount + maxPictureCount) / 2;
		for(int i=0; i<6; i++) playerDifficulties[i] = DEFAULT_DIFFICULTY;
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		if(button == Input.MOUSE_LEFT_BUTTON) mousePressed = true;
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		if(button == Input.MOUSE_LEFT_BUTTON) mousePressed = false;
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		nameEditFocus = -1;
		float d = getDifficulty(x);
		x = getTiledX(x);
		y = getTiledY(y);
		if(selectionMode){
			if(y >= 3 && y < 9){
				if(x >= 1 && x < 4){
					// type field clicked
					playerTypes[y-3] = (playerTypes[y-3] + 1) % 3;
				}
				if(x >= 4 && x < 9){
					// name field clicked
					nameEditFocus = y - 3;
				}
				if(x >= 9 && x < 15){
					// difficulty field clicked
					playerDifficulties[y-3] = d;
				}
			}
			if(x == 6 && y == 10) pictureCount = Math.max(minPictureCount, pictureCount - 1);
			if(x == 8 && y == 10) pictureCount = Math.min(maxPictureCount, pictureCount + 1);
			if(x >= 10 && x < 13 && y == 10){
				// start game button pressed. start game!
				ArrayList<Player> players = new ArrayList<>();
				GameplayState gs = GameplayState.getInstance();
				for(int i=0; i<6; i++){
					switch(playerTypes[i]){
						case 1:
							players.add(new HumanPlayer(playerNames[i], gs));
							break;
						case 2:
							players.add(new AIPlayer(playerNames[i], gs, playerDifficulties[i]));
							break;
					}
				}
				gs.newGame(players, pictureCount, 4f/3f);
				sbg.enterState(gs.getID());
				return;
			}
		} else {
			if(x >= 5 && x < 11 && y == 10) selectionMode = true;
		}
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		lastMouseX = getTiledX(newx);
		lastMouseY = getTiledY(newy);
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		nameEditFocus = -1;
		if(!selectionMode) return;
		float d = getDifficulty(newx);
		lastMouseX = getTiledX(newx);
		lastMouseY = getTiledY(newy);
		if(lastMouseY >= 3 && lastMouseY < 9 && lastMouseX >= 9 && lastMouseX < 15){
			// difficulty field dragged
			playerDifficulties[lastMouseY-3] = d;
		}
	}

	@Override
	public void mouseWheelMoved(int newValue) {
		if(mousePressed || !selectionMode) return;
		if(lastMouseY >= 3 && lastMouseY < 9){
			if(lastMouseX >= 1 && lastMouseX < 4){
				// mouse is on type field
				int t = playerTypes[lastMouseY-3];
				if(newValue > 0){
					t--;
					if(t<0) t = 2;
				} else {
					t = (t + 1) % 3;
				}
				playerTypes[lastMouseY-3] = t;
			}
			if(lastMouseX >= 9 && lastMouseX < 15){
				// mouse is on difficulty field
				float d = playerDifficulties[lastMouseY-3];
				if(newValue > 0){
					d = Math.min(1f, (float)Math.floor(d * 10f + 1f) / 10f);
				} else {
					d = Math.max(0f, (float)Math.ceil(d * 10f - 1f) / 10f);
				}
				playerDifficulties[lastMouseY-3] = d;
			}
		}
		if(lastMouseY == 10 && lastMouseX == 7){
			// mouse is on picture count field
			if(newValue > 0){
				pictureCount = Math.min(maxPictureCount, pictureCount + 1);
			} else {
				pictureCount = Math.max(minPictureCount, pictureCount - 1);
			}
		}
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics grphcs) throws SlickException {
		//selectionMode = false;
		grphcs.setColor(PANEL_COLOR);
		grphcs.fillRect(0, 0, 800, 600);
		
		grphcs.setColor(WINDOW_COLOR);
		grphcs.fillRect(250, 50, 300, 50);
		grphcs.fillRect(50, 150, 700, 300);
		if(selectionMode){
			grphcs.fillRect(150, 500, 300, 50);
			grphcs.fillRect(500, 500, 150, 50);
		} else {
			grphcs.fillRect(250, 500, 300, 50);
		}

		grphcs.setColor(PANEL_COLOR);
		if(selectionMode){
			for(int i=0; i<6; i++){
				int t = playerTypes[i];
				if(t == 2) grphcs.fillRect(450, 150 + i*50, playerDifficulties[i]*300, 50);
			}
		} else {
			int max = 1;
			for(int i=0; i<6; i++) if(playerScores[i] > max) max = playerScores[i];
			for(int i=0; i<6; i++){
				int t = playerTypes[i];
				if(t != 0) grphcs.fillRect(450, 150 + i*50, 300f * playerScores[i] / max, 50);
			}
		}
		
		grphcs.setColor(TEXT_COLOR);
		for(int i=0; i<6; i++){
			int t = playerTypes[i];
			switch(t){
				case 0: grphcs.drawString("No Player", 85, i*50 + 166); break;
				case 1: grphcs.drawString("Human", 105, i*50 + 166); break;
				case 2: grphcs.drawString("Computer", 90, i*50 + 166); break;
			}
			if(t == 0){
				grphcs.drawString("---", 305, i*50 + 166);
			} else {
				grphcs.drawString(playerNames[i], 220, i*50 + 166);
			}
			if(selectionMode){
				if(t == 2){
					grphcs.drawString(String.format("%.1f%%", playerDifficulties[i]*100f), 582, i*50 + 166);
				} else {
					grphcs.drawString("---", 590, i*50 + 166);
				}
			} else {
				if(t != 0){
					grphcs.drawString(String.format("%d", playerScores[i]), 590, i*50 + 166);
				} else {
					grphcs.drawString("---", 590, i*50 + 166);
				}
			}
		}
		if(selectionMode){
			grphcs.drawString("Select Players:", 330, 66);
			grphcs.drawString("Picture Count:", 162, 516);
			grphcs.drawLine(316, 516, 333, 516);
			grphcs.drawLine(316, 516, 325, 534);
			grphcs.drawLine(334, 516, 325, 534);
			
			grphcs.drawLine(416, 534, 433, 534);
			grphcs.drawLine(416, 534, 425, 516);
			grphcs.drawLine(434, 534, 425, 516);
			grphcs.drawString(String.format("%d", pictureCount), 370, 516);
			grphcs.drawString("Start Game", 530, 516);
		} else {
			grphcs.drawString("Scores:", 370, 66);
			grphcs.drawString("New Game", 360, 516);
		}
		
		grphcs.setColor(BORDER_COLOR);
		grphcs.drawRect(0, 0, 800, 600);
		grphcs.drawRect(250, 50, 300, 50);
		grphcs.drawRect(50, 150, 700, 300);
		if(selectionMode){
			grphcs.drawRect(150, 500, 300, 50);
			grphcs.drawLine(300, 500, 300, 550);
			grphcs.drawLine(350, 500, 350, 550);
			grphcs.drawLine(400, 500, 400, 550);
			grphcs.drawRect(500, 500, 150, 50);
		} else {
			grphcs.drawRect(250, 500, 300, 50);
		}
		for(int i=1; i<=5; i++) grphcs.drawLine(50, 150 + i*50, 750, 150 + i*50);
		grphcs.drawLine(200, 150, 200, 450);
		grphcs.drawLine(450, 150, 450, 450);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int i) throws SlickException {
		
	}
	
}
