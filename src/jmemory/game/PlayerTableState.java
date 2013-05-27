/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.game;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
	
	public static final Color PANEL_COLOR = new Color(0f, 0f, 0.5f, 1f);
	public static final Color WINDOW_COLOR = new Color(0f, 0f, 0f, 0.6f);
	public static final Color BORDER_COLOR = new Color(0f, 0.4f, 0.7f, 1f);
	public static final Color TEXT_COLOR = new Color(0f, 0.9f, 0.5f, 1f);
	private static final String SETTINGS_FILE_NAME = "settings.jmem";
	private static final int MAX_PLAYER_NAME_LENGTH = 12;
	private static final float DEFAULT_DIFFICULTY = 0.5f;
	private static final char[] ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789".toCharArray();
	
	private boolean selectionMode = true, mousePressed = false;
	private int[] playerTypes = {1, 2, 0, 0, 0, 0};
	private String[] playerNames = {
		"Player 1", "Player 2", "Player 3", 
		"Player 4", "Player 5", "Player 6"};
	private int[] playerScores = new int[6];
	private float[] playerDifficulties = new float[6];
	private int nameEditFocus = -1, playerCount = 2,
			pictureCount, maxPictureCount, 
			lastMouseX = 0, lastMouseY = 0;
	private GameContainer gc;
	private StateBasedGame sbg;
	
	public void setMode(boolean selectionMode){
		this.selectionMode = selectionMode;
	}
	
	public void setPlayers(ArrayList<Player> players){
		playerCount = 0;
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
				playerCount++;
			} else {
				playerTypes[i] = 0;
				playerNames[i] = "player " + (i+1);
				playerScores[i] = 0;
				playerDifficulties[i] = DEFAULT_DIFFICULTY;
			}
			i++;
		}
	}
	
	private void changePlayerType(int i, boolean forward){
		if(forward){
			switch(playerTypes[i]){
				case 0:
					playerTypes[i] = 1;
					playerCount++;
					break;
				case 1:
					playerTypes[i] = 2;
					break;
				case 2:
					if(playerCount <= 1){
						playerTypes[i] = 1;
					} else {
						playerTypes[i] = 0;
						playerCount--;
					}
					break;
			}
		} else {
			switch(playerTypes[i]){
				case 0:
					playerTypes[i] = 2;
					playerCount++;
					break;
				case 1:
					if(playerCount <= 1){
						playerTypes[i] = 2;
					} else {
						playerTypes[i] = 0;
						playerCount--;
					}
					break;
				case 2:
					playerTypes[i] = 1;
					break;
			}
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
	
	private void startGame(){
		if(pictureCount <= 0) return;
		// construct player list
		ArrayList<Player> players = new ArrayList<>();
		GameplayState gs = GameplayState.getInstance();
		for(int i=0; i<6; i++){
			if(playerNames[i].isEmpty()) playerNames[i] = "Player " + i;
			switch(playerTypes[i]){
				case 1:
					players.add(new HumanPlayer(playerNames[i]));
					break;
				case 2:
					players.add(new AIPlayer(playerNames[i], playerDifficulties[i]));
					break;
			}
		}
		// save players to disk
		try{
			OutputStream os = new FileOutputStream(SETTINGS_FILE_NAME);
			OutputStream bos = new BufferedOutputStream(os);
			ObjectOutput out = new ObjectOutputStream(bos);
			try{
				out.writeObject(players);
				out.writeObject(pictureCount);
			} finally {
				out.close();
			}
		} catch(Exception e){
			System.err.println("ERROR: can't write players to file!");
			e.printStackTrace();
		}
		// start game
		gs.newGame(players, pictureCount, 4f/3f);
		sbg.enterState(gs.getID());
	}
	
	@Override
	public int getID() {
		return 1;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		this.gc = gc;
		this.sbg = sbg;
		maxPictureCount = 0;
		pictureCount = 0;
		for(int i=0; i<6; i++) playerDifficulties[i] = DEFAULT_DIFFICULTY;
		// read players from disk, if there is a save file
		ArrayList<Player> players = null;
		try{
			InputStream is = new FileInputStream(SETTINGS_FILE_NAME);
			InputStream bis = new BufferedInputStream(is);
			ObjectInput in = new ObjectInputStream(bis);
			try{
				players = (ArrayList<Player>)in.readObject();
				pictureCount = (Integer) in.readObject();
			} finally {
				in.close();
			}
		} catch(Exception e){
			System.err.println("ERROR: can't read players from file!");
			e.printStackTrace();
		}
		if(players != null) setPlayers(players);
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
					changePlayerType(y-3, true);
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
			if(x == 6 && y == 10) pictureCount = Math.max(2, pictureCount - 1);
			if(x == 8 && y == 10) pictureCount = Math.min(maxPictureCount, pictureCount + 1);
			if(x >= 10 && x < 13 && y == 10){
				// start game button pressed. start game!
				startGame();
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
				changePlayerType(lastMouseY-3, (newValue < 0));
			}
			if(lastMouseX >= 9 && lastMouseX < 15){
				// mouse is on difficulty field (change diff only if player is a bot)
				if(playerTypes[lastMouseY-3] == 2){
					float d = playerDifficulties[lastMouseY-3];
					if(newValue > 0){
						d = Math.min(1f, (float)Math.floor(d * 10f + 1f) / 10f);
					} else {
						d = Math.max(0f, (float)Math.ceil(d * 10f - 1f) / 10f);
					}
					playerDifficulties[lastMouseY-3] = d;
				}
			}
		}
		if(lastMouseY == 10 && lastMouseX == 7){
			// mouse is on picture count field
			if(newValue > 0){
				pictureCount = Math.min(maxPictureCount, pictureCount + 1);
			} else {
				pictureCount = Math.max(2, pictureCount - 1);
			}
		}
	}

	@Override
	public void keyPressed(int key, char c) {
		if(nameEditFocus >= 0){
			String s = playerNames[nameEditFocus];
			switch(key){
				case Input.KEY_ENTER:
						nameEditFocus = -1;
					break;
				case Input.KEY_TAB:
						while(playerTypes[nameEditFocus] != 0) nameEditFocus = (nameEditFocus + 1) % 6;
					break;
				case Input.KEY_BACK:
					if(!s.isEmpty()) playerNames[nameEditFocus] = s.substring(0, s.length()-1);
					break;
				default:
					if(playerNames[nameEditFocus].length() >= MAX_PLAYER_NAME_LENGTH) return;
					boolean hit = false;
					for(char ch : ALLOWED_CHARS) if(ch == c){
						hit = true;
						break;
					}
					if(!hit) return;
					playerNames[nameEditFocus] = s + c;
					break;
			}
		}
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics grphcs) throws SlickException {
		grphcs.translate((gc.getWidth() - 800) / 2, (gc.getHeight() - 600) / 2);
		
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
				if(i == nameEditFocus){
					grphcs.drawString(playerNames[i] + "_", 220, i*50 + 166);
				} else {
					grphcs.drawString(playerNames[i], 220, i*50 + 166);
				}
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
		if(pictureCount < 2 || pictureCount > maxPictureCount){
			maxPictureCount = GameplayState.getInstance().getMaxPictureCount();
			pictureCount = (maxPictureCount + 2) / 2;
		}
	}
	
}
