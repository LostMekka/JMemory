/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.game;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import jmemory.player.AIPlayer;
import jmemory.data.Card;
import jmemory.data.Coordinate;
import jmemory.data.GameField;
import jmemory.player.HumanPlayer;
import jmemory.player.Player;
import static jmemory.data.Card.State.hiding;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class GameplayState extends BasicGameState {

	private static GameplayState instance = new GameplayState();
	public static GameplayState getInstance() {
		return instance;
	}
	private GameplayState() {}
	
	private static final float ANIMATION_SPEED = 3.5f;
	private static final float CARD_SIZE = 0.95f;
	private static final float CARD_BORDER = 0.05f;
	private static final int SHOW_CARDS_TIME = 1000;
	private static final int AI_PICK_DELAY = 300;
	private static float MAX_SCALE = 150f, MIN_SCALE = 20, SCALE_SPEED = 0.02f;

	private enum State { waitForFistCard, waitForSecondCard, showCards }
	
	private boolean scrolling = false;
	private float scale = 100f, centerX = 1f, centerY = 1f;
	private GameContainer gc;
	private StateBasedGame sbg;
	private GameField field;
	private LinkedList<Card> allCards = new LinkedList<>();
	private Image backImage;
	private ArrayList<Player> players;
	private Player currPlayer;
	private int currPlayerIndex, currPlayerTime, showCardsTimer;
	private State selectionState;
	private Coordinate cardLoc1, cardLoc2;
	private String infoText = "INFO TEXT WAS NOT SET PROPERLY!!!";
	private boolean leftMouseButtonDown = false, rightMouseButtonDown = false;

	private float getLeftX(){
		return centerX - gc.getWidth() / (2f * scale);
	}
	
	private float getRightX(){
		return centerX + gc.getWidth() / (2f * scale);
	}
	
	private float getTopY(){
		return centerY - gc.getHeight() / (2f * scale);
	}
	
	private float getBottomY(){
		return centerY + gc.getHeight() / (2f * scale);
	}
	
	private Coordinate getCardCoordinate(int x, int y){
		int xx = Math.round((x - gc.getWidth()/2f) / scale + centerX - 0.5f);
		int yy = Math.round((y - gc.getHeight()/2f) / scale + centerY - 0.5f);
		return new Coordinate(xx, yy);
	}
	
	private void changeToNextPlayer(){
		changeToPlayer(currPlayerIndex + 1);
	}
	
	private void changeToPlayer(int index){
		currPlayerTime = 0;
		currPlayerIndex = index % players.size();
		currPlayer = players.get(currPlayerIndex);
		selectionState = State.waitForFistCard;
		infoText = String.format("P%d: It is %s's turn. %s collected %d cards so far.", 
				currPlayerIndex, currPlayer.getName(), 
				currPlayer.getName(), currPlayer.getScore());
	}
	
	private void endTurn(){
		Card c1 = field.getCard(cardLoc1);
		Card c2 = field.getCard(cardLoc2);
		if(c1.getImage() == c2.getImage()){
			// hit! deal score, remove cards!
			currPlayer.incrementScore();
			// TODO: remove cards more gracefully
			field.removeCards(cardLoc1);
			c1.hideImmediately();
			c2.hideImmediately();
			// notify players of taken cards
			for(Player p : players) p.notifyCardsTaken(c1.getImage());
			if(field.isEmpty()){
				// game ended. show score screen
				PlayerTableState pts = PlayerTableState.getInstance();
				pts.setMode(false);
				pts.setPlayers(players);
				sbg.enterState(pts.getID());
				return;
			}
			changeToPlayer(currPlayerIndex);
		} else {
			// no hit. next player!
			c1.startTurning();
			c2.startTurning();
			changeToNextPlayer();
		}
	}
	
	private LinkedList<Card> getCardList(int count){
		if(count >= allCards.size()) return new LinkedList<>(allCards);
		if(count <= 2) count = 2;
		LinkedList<Card> tmp = new LinkedList<>(allCards);
		LinkedList<Card> ans = new LinkedList<>();
		Random ran = new Random();
		for(int i=0; i<count; i++) ans.add(tmp.remove(ran.nextInt(tmp.size())));
		return ans;
	}
	
	public int getMaxPictureCount(){
		return allCards.size();
	}
	
	public void selectCard(Coordinate location){
		Card c = field.getCard(location);
		if(c == null) return;
		switch(selectionState){
			case waitForFistCard:
				cardLoc1 = location;
				selectionState = State.waitForSecondCard;
				c.startTurning();
				// show the card to all players
				for(Player p: players) p.notifyImage(location, c.getImage());
				break;
			case waitForSecondCard:
				if(!cardLoc1.equals(location)){
					cardLoc2 = location;
					selectionState = State.showCards;
					showCardsTimer = SHOW_CARDS_TIME;
					c.startTurning();
					// show the card to all players
					for(Player p: players) p.notifyImage(location, c.getImage());
				}
				break;
			case showCards:
				// ignore request
				break;
		}
	}
	
	public void newGame(ArrayList<Player> players, int imageCount, float aspectRatio){
		this.players = players;
		for(Player p : players) p.resetScore();
		changeToPlayer(0);
		selectionState = State.waitForFistCard;
		currPlayerTime = 0;
		field = new GameField(getCardList(imageCount), aspectRatio);
		centerX = field.getWidth() / 2f;
		centerY = field.getHeight() / 2f;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}
	
	@Override
	public int getID() {
		return 2;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		this.gc = gc;
		this.sbg = sbg;
		// scan img folder for images and load ALL the images!!!
		File imgFolder = new File("img");
		String[] allFileNames = imgFolder.list();
		allCards.clear();
		for(String s : allFileNames){
			try{
				allCards.add(new Card(new Image("img/" + s)));
			} catch(Exception e){
				System.err.println("ERROR: Cannot read or process image file \"" + s + "\"!");
			}
		}
		backImage = new Image("sys/back.png");
	}

	@Override
	public void keyPressed(int key, char c) {
		if(c == ' ') scrolling = true;
	}

	@Override
	public void keyReleased(int key, char c) {
		if(c == ' ') scrolling = false;
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		if(button == Input.MOUSE_LEFT_BUTTON) leftMouseButtonDown = true;
		if(button == Input.MOUSE_RIGHT_BUTTON) rightMouseButtonDown = true;
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		if(button == Input.MOUSE_LEFT_BUTTON) leftMouseButtonDown = false;
		if(button == Input.MOUSE_RIGHT_BUTTON) rightMouseButtonDown = false;
	}
	
	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		if(scrolling){
			if(leftMouseButtonDown && !rightMouseButtonDown){
				// scroll
				centerX -= (newx - oldx) / scale;
				centerY -= (newy - oldy) / scale;
			}
			if(rightMouseButtonDown && !leftMouseButtonDown){
				// zoom
				float f = newy - oldy;
				scale *= Math.pow(2f, f * SCALE_SPEED);
				if(scale < MIN_SCALE) scale = MIN_SCALE;
				if(scale > MAX_SCALE) scale = MAX_SCALE;
			}
		}
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		if(currPlayer.isHuman() && !scrolling){
			selectCard(getCardCoordinate(x, y));
		}
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics grphcs) throws SlickException {
		grphcs.clear();
		grphcs.setColor(Color.white);
		int x1 = Math.max(0, Math.round(getLeftX() - 0.5f));
		int x2 = Math.min(field.getWidth()-1, Math.round(getRightX() + 0.5f));
		int y1 = Math.max(0, Math.round(getTopY() - 0.5f));
		int y2 = Math.min(field.getHeight()-1, Math.round(getBottomY() + 0.5f));
		for(int x = x1; x <= x2; x++){
			for(int y = y1; y <= y2; y++){
				Card c = field.getCard(new Coordinate(x, y));
				if(c == null) continue;
				float animationTime;
				Image image;
				switch(c.getState()){
					case hidden:
						image = backImage;
						animationTime = 0f;
						break;
					case revealed:
						image = c.getImage();
						animationTime = 0f;
						break;
					case hiding:
						animationTime = c.getAnimationProgress();
						if(animationTime < 0.5f){
							image = c.getImage();
						} else {
							animationTime = 1f - animationTime;
							image = backImage;
						}
						break;
					case revealing:
						animationTime = c.getAnimationProgress();
						if(animationTime < 0.5f){
							image = backImage;
						} else {
							animationTime = 1f - animationTime;
							image = c.getImage();
						}
						break;
					default: throw new RuntimeException("card state not recognized!");
				}
				grphcs.fillRect(
						gc.getWidth()/2f + scale * (x + animationTime - centerX), 
						gc.getHeight()/2f + scale * (y - centerY), 
						scale * CARD_SIZE * (1f - 2f * animationTime), 
						scale * CARD_SIZE);
				image.draw(
						gc.getWidth()/2f + scale * (x + animationTime + (1f - 2f * animationTime) * CARD_BORDER - centerX), 
						gc.getHeight()/2f + scale * (y + CARD_BORDER - centerY), 
						scale * (CARD_SIZE - 2f * CARD_BORDER) * (1f - 2f * animationTime),
						scale * (CARD_SIZE - 2f * CARD_BORDER));
			}
		}

		grphcs.setColor(PlayerTableState.PANEL_COLOR);
		grphcs.fillRect(0, gc.getHeight() - 40, gc.getWidth(), 40);
		grphcs.setColor(PlayerTableState.WINDOW_COLOR);
		grphcs.fillRect(10, gc.getHeight() - 30, gc.getWidth() - 20, 20);
		grphcs.setColor(PlayerTableState.BORDER_COLOR);
		grphcs.drawLine(0, gc.getHeight() - 40, gc.getWidth(), gc.getHeight() - 40);
		grphcs.drawRect(10, gc.getHeight() - 30, gc.getWidth() - 20, 20);
		grphcs.setColor(PlayerTableState.TEXT_COLOR);
		grphcs.drawString(infoText, 20, gc.getHeight() - 29);
	}

	Random r = new Random();
	int a = 0, m = 200;
	
	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int i) throws SlickException {
		float time = i / 1000f;
		for(int y = 0; y < field.getHeight(); y++){
			for(int x = 0; x < field.getWidth(); x++){
				Card c = field.getCard(new Coordinate(x, y));
				if(c != null) c.tick(time * ANIMATION_SPEED);
			}
		}
		
		if(selectionState == State.showCards){
			showCardsTimer -= i;
			if(showCardsTimer <= 0) endTurn();
		} else {
			if(!currPlayer.isHuman()){
				currPlayerTime += i;
				if(currPlayerTime >= AI_PICK_DELAY){
					currPlayerTime = 0;
					AIPlayer p = (AIPlayer)currPlayer;
					switch(selectionState){
						case waitForFistCard:
							p.pickFirst(field);
							break;
						case waitForSecondCard:
							p.pickSecond(field, cardLoc1, field.getCard(cardLoc1).getImage());
							break;
					}
				}
			}
		}
		
//		a += i;
//		if(a >= m){
//			a -= m;
//			int x = r.nextInt(field.getWidth());
//			int y = r.nextInt(field.getHeight());
//			field.getCard(new Coordinate(x, y)).startTurning();
//		}
	}
	
}
