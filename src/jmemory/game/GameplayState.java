/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.game;

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
	
	
	private enum State { waitForFistCard, waitForSecondCard, showCards }
	
	private boolean scrolling = false;
	private float scale = 100f, centerX = 1f, centerY = 1f;
	private GameContainer gc;
	private StateBasedGame sbg;
	private GameField field;
	private LinkedList<Card> allCards;
	private Image backImage;
	private ArrayList<Player> players;
	private Player currPlayer;
	private int currPlayerIndex, currPlayerTime, showCardsTimer;
	private State selectionState;
	private Coordinate cardLoc1, cardLoc2;

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
		allCards = new LinkedList<>();
		allCards.add(new Card(new Image("img/a.jpg")));
		allCards.add(new Card(new Image("img/b.jpg")));
		allCards.add(new Card(new Image("img/c.jpg")));
		allCards.add(new Card(new Image("img/d.jpg")));
		allCards.add(new Card(new Image("img/e.jpg")));
		allCards.add(new Card(new Image("img/f.jpg")));
		allCards.add(new Card(new Image("img/g.jpg")));
		allCards.add(new Card(new Image("img/h.jpg")));
		backImage = new Image("sys/back.png");
		players = new ArrayList<>(10);
		players.add(new HumanPlayer("Mensch 1", this));
		players.add(new AIPlayer("AI 1", this, 0.7f));
		newGame(players, 8, 4f/3f);
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
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		if(currPlayer.isHuman()){
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
