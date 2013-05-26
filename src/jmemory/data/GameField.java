/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory.data;

import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public class GameField {
	
	private static final Random RANDOM = new Random();
	
	private Card[][] field;
	private Coordinate size;
	private int cardsLeft;

	public GameField(LinkedList<Card> cardsToUse, float preferredAspectRatio) {
		constructNewField(cardsToUse, preferredAspectRatio);
	}
	
	public final void constructNewField(LinkedList<Card> cardsToUse, float preferredAspectRatio){
		int n = 2 * cardsToUse.size();
		
		// determine field dimension
		float leftoverPenaltyMultiplier = 4f;
		Coordinate bestSize = new Coordinate(1, n);
		float bestScore = Float.MAX_VALUE;
		float optimalW = (float)Math.sqrt(n * preferredAspectRatio);
		float optimalH = n / optimalW;
		for(int w = 1; w <= n; w++){
			int h = n / w;
			int leftover = n % w;
			leftover = Math.min(leftover, w - leftover);
			if(leftover > 0) h++;
			float score = 
					Math.abs(w - optimalW) + 
					Math.abs(h - optimalH) + 
					leftoverPenaltyMultiplier * (float)leftover / w;
			if(score < bestScore){
				bestScore = score;
				bestSize = new Coordinate(w, h);
			}
		}
		size = bestSize;

		// fill field with cards
		field = new Card[size.x][size.y];
		cardsLeft = cardsToUse.size();
		LinkedList<Card> availableCards = new LinkedList<>();
		for(Card c : cardsToUse){
			availableCards.add(c);
			availableCards.add(new Card(c));
		}
		for(int y=0; y<size.y; y++){
			for(int x=0; x<size.x; x++){
				if(availableCards.size() == 1){
					field[x][y] = availableCards.getFirst();
					return;
				}
				field[x][y] = availableCards.remove(RANDOM.nextInt(availableCards.size()));
			}
		}
		System.out.format("field constructed with %dx%d = %d cards (%d card types)%n", 
				size.x, size.y, size.x * size.y, cardsToUse.size());
	}
	
	public int getWidth(){
		return size.x;
	}
	
	public int getHeight(){
		return size.y;
	}
	
	public boolean isEmpty(){
		return (cardsLeft <= 0);
	}
	
	public int getDistinctCardCount(){
		return cardsLeft;
	}
	
	public int getTotalCardCount(){
		return cardsLeft * 2;
	}
	
	public Card getCard(Coordinate location){
		if(location.x<0 || location.x>=size.x || location.y<0 || location.y>=size.y) 
			return null;
		return field[location.x][location.y];
	}
	
	public Card removeCard(Card card){
		byte rem = 0;
		for(int yy=0; yy<size.x; yy++){
			for(int xx=0; xx<size.y; xx++){
				if(field[xx][yy] == card){
					field[xx][yy] = null;
					if(++rem >= 2){
						cardsLeft--;
						return card;
					}
				}
			}
		}
		throw new RuntimeException("no second card found to remove!!");
	}
	
	public Card removeCards(Coordinate location){
		if(location.x<0 || location.x>=size.x || location.y<0 || location.y>=size.y)
			return null;
		Card c = field[location.x][location.y];
		field[location.x][location.y] = null;
		for(int yy=0; yy<size.y; yy++){
			for(int xx=0; xx<size.x; xx++){
				if(field[xx][yy] != null && field[xx][yy].getImage() == c.getImage()){
					field[xx][yy] = null;
					cardsLeft--;
					return c;
				}
			}
		}
		throw new RuntimeException("no second card found to remove!!");
	}
	
	public LinkedList<Coordinate> getValidCoordinates(){
		return getValidCoordinates(null);
	}
	
	public LinkedList<Coordinate> getValidCoordinates(Coordinate ignore){
		LinkedList<Coordinate> ans = new LinkedList<>();
		for(int y=0; y<size.y; y++){
			for(int x=0; x<size.x; x++){
				if(field[x][y] != null){
					Coordinate c = new Coordinate(x, y);
					if(!c.equals(ignore)) ans.add(c);
				}
			}
		}
		return ans;
	}
	
	public Coordinate getRandomValidCoord(){
		return getRandomValidCoord(null);
	}
	
	public Coordinate getRandomValidCoord(Coordinate ignore){
		LinkedList<Coordinate> list = getValidCoordinates(ignore);
		return list.get(RANDOM.nextInt(list.size()));
	}
	
	public Coordinate getNearestValidCoord(Coordinate location){
		return getNearestValidCoord(location, null);
	}
	
	public Coordinate getNearestValidCoord(Coordinate location, Coordinate ignore){
		LinkedList<Coordinate> list = getValidCoordinates(ignore);
		LinkedList<Coordinate> bestCoords = new LinkedList<>();
		int bestDist = Integer.MAX_VALUE;
		for(Coordinate c : list){
			int d = c.getSquaredDistanceTo(location);
			if(d < bestDist){
				bestCoords.clear();
				bestDist = d;
			}
			if(d <= bestDist) bestCoords.add(c);
		}
		if(bestCoords.isEmpty()){
			System.err.println("WARNING: no nearest card found!");
			return null;
		}
		return bestCoords.get(RANDOM.nextInt(bestCoords.size()));
	}
	
}
