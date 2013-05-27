/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmemory;

import jmemory.game.MemoryGame;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.Input;

/**
 *
 * @author LostMekka
 */
public class JMemory {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			AppGameContainer c = new AppGameContainer(new MemoryGame(), 800, 600, false);
			c.setAlwaysRender(true);
			c.start();
		} catch (UnsupportedClassVersionError ex) {
			Logger.getLogger(JMemory.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(null, "Java version 7 is required. Please update Java.", "ERROR!", JOptionPane.ERROR_MESSAGE);
		} catch (Exception ex) {
			Logger.getLogger(JMemory.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(null, ex, "ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
}
