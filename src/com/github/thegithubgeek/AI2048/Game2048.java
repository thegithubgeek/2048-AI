package com.github.thegithubgeek.AI2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 * @author Franklin Wang, Riley Kong
 */
@SuppressWarnings("serial")
public class Game2048 extends JPanel {
	private static final Color BG_COLOR = new Color(170, 170, 170);
	private static final String FONT_NAME = "Arial";
	private static final int TILE_SIZE = 64;
	private static final int TILES_MARGIN = 16;

	public static Tile[] myTiles;
	static boolean myWin = false;
	static boolean myLose = false;
	static int myScore = 0;
	public static double meanScore = 0;
	static double prevScore = 0;
	public static double diff = 0;
	public static double gen = 1;
	DecimalFormat reg = new DecimalFormat("######");
	DecimalFormat fmt = new DecimalFormat("+###;-#");
	
	public Game2048() {
		setPreferredSize(new Dimension(500, 340));
		setFocusable(true);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					resetGame();
				}
				if (!canMove()) {
					myLose = true;
				}

				if (!myWin && !myLose) {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_LEFT:
						left();
						repaint();
						break;
					case KeyEvent.VK_RIGHT:
						right();
						repaint();
						break;
					case KeyEvent.VK_DOWN:
						down();
						repaint();
						break;
					case KeyEvent.VK_UP:
						up();
						repaint();
						break;
					}
				}

				if (!myWin && !canMove()) {
					myLose = true;
				}

			}
		});
		resetGame();
	}
	
	public void resetGame() {
		myScore = 0;
		myWin = false;
		myLose = false;
		myTiles = new Tile[4 * 4];
		for (int i = 0; i < myTiles.length; i++) {
			myTiles[i] = new Tile();
		}
		addTile();
		addTile();
	}

	public static void left() {
		boolean needAddTile = false;
		for (int i = 0; i < 4; i++) {
			Tile[] line = getLine(i);
			Tile[] merged = mergeLine(moveLine(line));
			setLine(i, merged);
			if (!needAddTile && !compare(line, merged)) {
				needAddTile = true;
			}
		}

		if (needAddTile) {
			addTile();
		}
	}

	public static void right() {
		myTiles = rotate(180);
		left();
		myTiles = rotate(180);
	}

	public static void up() {
		myTiles = rotate(270);
		left();
		myTiles = rotate(90);
	}

	public static void down() {
		myTiles = rotate(90);
		left();
		myTiles = rotate(270);
	}

	private static Tile tileAt(int x, int y) {
		return myTiles[x + y * 4];
	}

	private static void addTile() {
		List<Tile> list = availableSpace();
		if (!availableSpace().isEmpty()) {
			int index = (int) (Math.random() * list.size()) % list.size();
			Tile emptyTime = list.get(index);
			emptyTime.value = Math.random() < 0.9 ? 2 : 4;
		}
	}

	private static List<Tile> availableSpace() {
		final List<Tile> list = new ArrayList<Tile>(16);
		for (Tile t : myTiles) {
			if (t.isEmpty()) {
				list.add(t);
			}
		}
		return list;
	}

	private static boolean isFull() {
		return availableSpace().size() == 0;
	}

	static boolean canMove() {
		if (!isFull()) {
			return true;
		}
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				Tile t = tileAt(x, y);
				if ((x < 3 && t.value == tileAt(x + 1, y).value) || ((y < 3) && t.value == tileAt(x, y + 1).value)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean compare(Tile[] line1, Tile[] line2) {
		if (line1 == line2) {
			return true;
		} else if (line1.length != line2.length) {
			return false;
		}

		for (int i = 0; i < line1.length; i++) {
			if (line1[i].value != line2[i].value) {
				return false;
			}
		}
		return true;
	}

	private static Tile[] rotate(int angle) {
		Tile[] newTiles = new Tile[4 * 4];
		int offsetX = 3, offsetY = 3;
		if (angle == 90) {
			offsetY = 0;
		} else if (angle == 270) {
			offsetX = 0;
		}

		double rad = Math.toRadians(angle);
		int cos = (int) Math.cos(rad);
		int sin = (int) Math.sin(rad);
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				int newX = (x * cos) - (y * sin) + offsetX;
				int newY = (x * sin) + (y * cos) + offsetY;
				newTiles[(newX) + (newY) * 4] = tileAt(x, y);
			}
		}
		return newTiles;
	}

	private static Tile[] moveLine(Tile[] oldLine) {
		LinkedList<Tile> l = new LinkedList<Tile>();
		for (int i = 0; i < 4; i++) {
			if (!oldLine[i].isEmpty())
				l.addLast(oldLine[i]);
		}
		if (l.size() == 0) {
			return oldLine;
		} else {
			Tile[] newLine = new Tile[4];
			ensureSize(l, 4);
			for (int i = 0; i < 4; i++) {
				newLine[i] = l.removeFirst();
			}
			return newLine;
		}
	}

	private static Tile[] mergeLine(Tile[] oldLine) {
		LinkedList<Tile> list = new LinkedList<Tile>();
		for (int i = 0; i < 4 && !oldLine[i].isEmpty(); i++) {
			int num = oldLine[i].value;
			if (i < 3 && oldLine[i].value == oldLine[i + 1].value) {
				num *= 2;
				myScore += num;
				int ourTarget = 2048;
				if (num == ourTarget) {
					myWin = true;
				}
				i++;
			}
			list.add(new Tile(num));
		}
		if (list.size() == 0) {
			return oldLine;
		} else {
			ensureSize(list, 4);
			return list.toArray(new Tile[4]);
		}
	}

	private static void ensureSize(java.util.List<Tile> l, int s) {
		while (l.size() != s) {
			l.add(new Tile());
		}
	}

	private static Tile[] getLine(int index) {
		Tile[] result = new Tile[4];
		for (int i = 0; i < 4; i++) {
			result[i] = tileAt(i, index);
		}
		return result;
	}

	private static void setLine(int index, Tile[] re) {
		System.arraycopy(re, 0, myTiles, index * 4, 4);
	}
	public Dimension getPreferredSize() {
		return new Dimension(340, 340);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(BG_COLOR);
		g.fillRect(0, 0, getSize().width, getSize().height);
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				drawTile(g, myTiles[x + y * 4], x, y);
			}
		}
		g.setColor(new Color(100, 100, 100));
		g.setFont(new Font(FONT_NAME, Font.PLAIN, 18));
		g.drawString("GEN: " + reg.format(gen), 340, 245);
		g.drawString("Prev: " + reg.format(prevScore), 340, 270);
		g.drawString("Score: " + reg.format(meanScore), 340, 295);
		g.drawString("Diff: " + fmt.format(diff), 340, 320);
	}

	private void drawTile(Graphics g2, Tile tile, int x, int y) {
		Graphics2D g = ((Graphics2D) g2);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		int value = tile.value;
		int xOffset = offsetCoors(x);
		int yOffset = offsetCoors(y);
		g.setColor(tile.getBackground());
		g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 14, 14);
		g.setColor(tile.getForeground());
		final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
		final Font font = new Font(FONT_NAME, Font.BOLD, size);
		g.setFont(font);

		String s = String.valueOf(value);
		final FontMetrics fm = getFontMetrics(font);

		final int w = fm.stringWidth(s);
		final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

		if (value != 0)
			g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);

		if (myLose) {// go till loses
			Main.getScore();
			resetGame();
//			g.setColor(new Color(255, 255, 255, 30));
//			g.fillRect(0, 0, getWidth(), getHeight());
//			g.setColor(new Color(78, 139, 202));
//			g.setFont(new Font(FONT_NAME, Font.BOLD, 48));
//			if (myWin) {
//				g.drawString("You won!", 68, 150);
//			}
//			if (myLose) {
//				resetGame();
//				Main.getScore();
//			}
//			if (myWin || myLose) {
//				g.setFont(new Font(FONT_NAME, Font.PLAIN, 16));
//				g.setColor(new Color(128, 128, 128, 128));
//				g.drawString("Press ESC to play again", 80, getHeight() - 40);
//			}
		}
		g.setFont(new Font(FONT_NAME, Font.PLAIN, 18));
		g.drawString("Score: " + myScore, 200, 365);

	}

	private static int offsetCoors(int arg) {
		return arg * (TILES_MARGIN + TILE_SIZE) + TILES_MARGIN;
	}

	static class Tile implements Comparable<Tile>{
	    int value;
	    @Override
	    public boolean equals(Object o){
	    	return (value==((Tile)o).value);
	    }
	    public Tile() {
	      this(0);
	    }
	
	    public Tile(int num) {
	      value = num;
	    }
	
	    public boolean isEmpty() {
	      return value == 0;
	    }
	
	    public Color getForeground() {
	      return value < 16 ? new Color(0x776e65) :  new Color(0xf9f6f2);
	    }
	
	    public Color getBackground() {
	    	switch (value) {
		        case 2:    return new Color(0xeee4da);
		        case 4:    return new Color(0xede0c8);
		        case 8:    return new Color(0xf2b179);
		        case 16:   return new Color(0xf59563);
		        case 32:   return new Color(0xf67c5f);
		        case 64:   return new Color(0xf65e3b);
		        case 128:  return new Color(0xedcf72);
		        case 256:  return new Color(0xedcc61);
		        case 512:  return new Color(0xedc850);
		        case 1024: return new Color(0xedc53f);
		        case 2048: return new Color(0xedc22e);
	    	}
	    	return new Color(0xcdc1b4);
	    }
		@Override
		public int compareTo(Tile o) {
			// TODO Auto-generated method stub
			return value-o.value;
		}
	}
	public static int getTile(int num) {
			return myTiles[num].value;
	}
	public static void move(int num) {
		switch (num) {
			case 0: left();
			case 1: right();
			case 2: up();
			case 3: down();
		}
		try {
			Main.game2048.repaint();
			Main.disp.repaint();
		} catch (Exception e) {
		}
	}
}