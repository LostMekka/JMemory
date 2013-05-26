/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.player;

import jmemory.data.GameField;
import jmemory.data.Coordinate;
import jmemory.game.GameplayState;
import java.util.LinkedList;
import java.util.Random;
import org.newdawn.slick.Image;

/**
 *
 * @author LostMekka
 */
public class AIPlayer extends Player {

	private enum State { awaitFirstPick, awaitSecondPick, done }
		
	private class DataItem {
		// DATA INVARIANT:
		// one of the following is true:
		//   * card1Loc == null && card2Loc == null
		//   * card1Loc != null && card2Loc == null
		//   * card1Loc != null && card2Loc != null && radius1 <= radius2
		private static final float MAX_RADIUS = 5f;
		
		private Image image;
		private Coordinate card1Loc = null, card2Loc = null;
		private float radius1 = 0f, radius2 = 0f;

		public DataItem(Image image) {
			this.image = image;
		}
		public Image getImage() {
			return image;
		}
		public Coordinate getCard1Coord(){
			return get(card1Loc, radius1);
		}
		public Coordinate getCard2Coord(){
			return get(card2Loc, radius2);
		}
		public Coordinate getOtherCardCoord(Coordinate location){
			if(location.equals(card1Loc)) return get(card2Loc, radius2);
			if(location.equals(card2Loc)) return get(card1Loc, radius1);
			System.err.println("WARNING: getOtherCardCoord got a wrong parameter location!");
			return null;
		}
		private Coordinate get(Coordinate c, float r){
			if(c == null){
				System.err.println("WARNING: DataItem.get() got a null coordinate!");
				return null;
			}
			double dir = RANDOM.nextDouble() * Math.PI * 2d;
			double dist = RANDOM.nextDouble() * r;
			int x = c.x + (int)Math.round(Math.cos(dir) * dist);
			int y = c.y + (int)Math.round(Math.sin(dir) * dist);
			return new Coordinate(x, y);
		}
		public byte getKnownCardCount(){
			byte ans = 0;
			if(card1Loc != null) ans++;
			if(card2Loc != null) ans++;
			return ans;
		}
		public float getCertainty(){
			// 2nd card is the one that is the least certain.
			if(card2Loc == null) return 0f;
			return 1f - radius2 / MAX_RADIUS;
		}
		public void forget(float difficulty){
			if(card1Loc != null){
				radius1 += MAX_RADIUS * (1f - difficulty) / 2f;
				if(radius1 >= MAX_RADIUS) card1Loc = null;
			}
			if(card2Loc != null){
				radius2 += MAX_RADIUS * (1f - difficulty) / 2f;
				if(radius2 >= MAX_RADIUS) card2Loc = null;
			}
		}
		public void learn(Coordinate l){
			if(card1Loc == null){
				// if 1st card is unknown, set it
				card1Loc = l;
				radius1 = 0f;
				return;
			}
			if(card2Loc == null && !l.equals(card1Loc)){
				// 1st card is known, but 2nd is not
				// set 2nd card but swap cards to keep invariant consistency
				card2Loc = card1Loc;
				radius2 = radius1;
				card1Loc = l;
				radius1 = 0f;
				return;
			}
			// if we land here, both card locations are known. just refresh (set the radius to zero again)
			if(l.equals(card1Loc)){
				// refresh 1st card
				radius1 = 0f;
				return;
			}
			if(l.equals(card2Loc)){
				// refresh 2nd card but swap cards to keep invariant consistency
				card2Loc = card1Loc;
				radius2 = radius1;
				card1Loc = l;
				radius1 = 0f;
			}
		}
	}
	
	private static final Random RANDOM = new Random();
	private static final int PICK_DELAY = 300;

	private State state = State.awaitFirstPick;
	private float difficulty;
	private DataItem selectedItem = null;
	private Image firstPickImage = null;
	private Coordinate firstPickLocation = null;
	private LinkedList<DataItem> knowledgeBase = new LinkedList<>();
	
	public AIPlayer(String name, GameplayState gameplayState, float difficulty) {
		super(name, gameplayState);
		this.difficulty = difficulty;
	}

	public float getDifficulty() {
		return difficulty;
	}
	
	public void pickFirst(GameField field){
		// pick a data item, or null if there is none present
		LinkedList<DataItem> bestItems = new LinkedList<>();
		int bestLevel = 0;
		float bestCertainty = 0f;
		for(DataItem i : knowledgeBase){
			int level = i.getKnownCardCount();
			float cert = i.getCertainty();
			if(level < bestLevel) continue;
			if(level > bestLevel){
				bestItems.clear();
				bestLevel = level;
			}
			if(level == bestLevel){
				if(cert < bestCertainty) continue;
				if(cert > bestCertainty){
					bestItems.clear();
					bestCertainty = cert;
				}
				if(cert == bestCertainty) bestItems.add(i);
			}
		}
		if(bestItems.isEmpty() || bestLevel < 2){
			// no images are known or:
			// only images with at least one missing location are known.
			// -> pick randomly
			selectedItem = null;
			firstPickLocation = field.getRandomValidCoord();
		} else {
			// some images with both locations are known.
			// pick one of the best and select it
			selectedItem = bestItems.get(RANDOM.nextInt(bestItems.size()));
			Coordinate guess;
			if(RANDOM.nextBoolean()){
				guess = selectedItem.getCard1Coord();
			} else {
				guess = selectedItem.getCard2Coord();
			}
			firstPickLocation = field.getNearestValidCoord(guess);
		}
		getGameplayState().selectCard(firstPickLocation);
		state = State.awaitSecondPick;
	}
	
	public void pickSecond(GameField field, Coordinate firstPickLocation, Image firstPickImage){
		// select another data item if we picked the wrong card "by accident"
		if(selectedItem == null || selectedItem.getImage() != firstPickImage){
			selectedItem = null;
			for(DataItem i : knowledgeBase){
				if(i.getImage() == firstPickImage){
					selectedItem = i;
					break;
				}
			}
		}
		if(selectedItem == null){
			// if there was no data item to the picked image, we are completely lost.
			// fortunately, if we update the knowledge base on every notifyImage,
			// this case does never happen.
			System.err.println("WARNING: Second pick algorithm detected a leak in knowlege base! Image picked first was not found.");
			getGameplayState().selectCard(field.getRandomValidCoord(firstPickLocation));
		} else {
			// normal case
			Coordinate guess = selectedItem.getOtherCardCoord(firstPickLocation);
			if(guess == null){
				guess = field.getRandomValidCoord(firstPickLocation);
			} else {
				guess = field.getNearestValidCoord(guess, firstPickLocation);
			}
			getGameplayState().selectCard(guess);
		}
		state = State.awaitFirstPick;
	}

	@Override
	public void notifyImage(Coordinate location, Image image) {
		// save image, if this happens to be the card we picked first
		if(state == State.awaitSecondPick && firstPickLocation.equals(location)){
			firstPickImage = image;
		}
		boolean dataItemFound = false;
		// update knowledge base
		for(DataItem i : knowledgeBase){
			if(image == i.getImage()){
				i.learn(location);
				dataItemFound = true;
				break;
			}
		}
		if(!dataItemFound){
			// there is no data item with that image.
			// -> create one
			DataItem i = new DataItem(image);
			i.learn(location);
			knowledgeBase.add(i);
		}
		// forget knowledge
		for(DataItem i : knowledgeBase) if(RANDOM.nextFloat() > difficulty) i.forget(difficulty);
	}

	@Override
	public void notifyCardsTaken(Image image) {
		DataItem item = null;
		for(DataItem i : knowledgeBase){
			if(i.getImage() == image){
				item = i;
				break;
			}
		}
		if(item != null)knowledgeBase.remove(item);
	}

	@Override
	public boolean isHuman() {
		return false;
	}
	
}
