import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class GamePanel extends JPanel implements KeyListener{
	
	
	private Rocket rocket;
	
	private RepaintThread repaintThread;
	private SpaceStationMoveThread spaceStationMoveThread;
	private StarMoveThread starMoveThread;
	
	final int PLAT_X = LandingGame.WINDOW_W / 2 - 25;//platform x
	final int PLAT_Y = LandingGame.WINDOW_H - 120;
	
	final int INIT_SPACE_STATION_X = 470;
	final int INIT_SPACE_STATION_Y = 50;
	
	int spaceStationX;
	int spaceStationY;
	
	final int SPACE_STATION_W = 120;
	final int SPACE_STATION_H = 50;
	
	final int CARGO_X = PLAT_X;
	final int CARGO_Y = PLAT_Y + 35;
	
	int remainingCargoNum;
	int cargoOnStation;
	
	boolean canUnload;
	
	final String SPACE_STATION = "Space Station";
	final String HIT_SPACE = "HIT SPACE TO LOAD CARGO";
	
	Rectangle spaceStationRect;
	Rectangle unloadingAreaRect;
	Rectangle rocketRect;
	
	Random random = new Random();
	
	final int STAR_NUM = 100;
	int[][] starXY;
	int starOffset = 0;
	
	public GamePanel(){
		super();
		setLayout(null);
		init();
		
	}
	
	private void init(){
		addKeyListener(this);
		setBackground(new Color(0x000000));
		
		generateStars();
		
		remainingCargoNum = 4;
		cargoOnStation = 0;
		canUnload = false;
		
		spaceStationX = INIT_SPACE_STATION_X;
		spaceStationY = INIT_SPACE_STATION_Y;
		
		rocket = new Rocket();
		rocket.x = PLAT_X + 25;
		rocket.y = PLAT_Y;
		
		repaintThread = new RepaintThread();
		repaintThread.start();
		
		spaceStationMoveThread = new SpaceStationMoveThread();
		spaceStationMoveThread.start();
		
		starMoveThread = new StarMoveThread();
		starMoveThread.start();
	}
	
	void generateStars(){
		starXY = new int[STAR_NUM][2];
		for(int i = 0; i < STAR_NUM; i++){
			int x = random.nextInt(LandingGame.WINDOW_W);
			int y = random.nextInt(LandingGame.WINDOW_H);
			starXY[i][0] = x;
			starXY[i][1] = y;
		}
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		//paint stars
		paintStars(g);
		
		//paint land
		g.setColor(new Color(0x444444));
		g.fillArc(-1000, LandingGame.WINDOW_H - 100 , 2000 + LandingGame.WINDOW_W, 2000, 70, 110);
		
		//paint platform
		g.fillRect(PLAT_X, PLAT_Y, 50, 8);
		
		final int PLAT_FOOT_X1 = PLAT_X + 5;
		final int PLAT_FOOT_X2 = PLAT_X + 14;
		final int PLAT_FOOT_X3 = PLAT_X + 50 - 14;
		final int PLAT_FOOT_X4 = PLAT_X + 50 - 5;
		
		g.setColor(Color.GRAY);
		g.fillPolygon(new int[]{PLAT_FOOT_X1, PLAT_FOOT_X2, PLAT_FOOT_X4, PLAT_FOOT_X3}, new int[]{PLAT_Y + 8, PLAT_Y + 8, PLAT_Y + 30, PLAT_Y + 30}, 4);
		g.fillPolygon(new int[]{PLAT_FOOT_X3, PLAT_FOOT_X4, PLAT_FOOT_X2, PLAT_FOOT_X1}, new int[]{PLAT_Y + 8, PLAT_Y + 8, PLAT_Y + 30, PLAT_Y + 30}, 4);
		
		paintCargo(g);
		
		paintSpaceStation(g);
		
		rocket.paint(g);
		
		if(rocket.y >= PLAT_Y && rocket.x >= PLAT_X && rocket.x <= PLAT_X + 50 && rocket.speedY > 0){
			if(rocket.speedY > 2 && !rocket.dead){
				rocket.blast();
				gameOver();
			} else {
				if(remainingCargoNum == 0 && !rocket.cargoLoaded){
					gameWin();
				} 
			}
			rocket.stop();
			rocket.y = PLAT_Y;
		} else if(rocket.y >= PLAT_Y + 60 && !rocket.dead){
			rocket.y = PLAT_Y + 60;
			rocket.stop();
			rocket.blast();
			gameOver();
		} else if (rocketRect.intersects(spaceStationRect) && !rocket.dead){
			rocket.stop();
			rocket.blast();
			gameOver();
		}
			
		g.setColor(Color.BLACK);
	}
	
	void paintCargo(Graphics g){
		
		for(int i = 0; i < remainingCargoNum; i++){
			int x = CARGO_X + i * 22;
			g.setColor(new Color(117, 92, 61));
			g.fillRect(x, CARGO_Y, 20, 20);
			g.setColor(Color.BLACK);
			g.drawLine(x, CARGO_Y, x + 19, CARGO_Y + 19);
			g.drawLine(x + 19, CARGO_Y, x, CARGO_Y + 19);
		}
		
		g.setColor(Color.WHITE);
		g.drawString(HIT_SPACE, CARGO_X, CARGO_Y + 35);
	}
	
	void paintSpaceStation(Graphics g){
		//draw space station
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(spaceStationX, spaceStationY, SPACE_STATION_W, SPACE_STATION_H);
		g.setColor(Color.GRAY);
		g.fillPolygon(new int[]{spaceStationX, spaceStationX, spaceStationX - 50}, 
				new int[]{spaceStationY, spaceStationY + SPACE_STATION_H, spaceStationY + SPACE_STATION_H}, 3);
		
		g.fillPolygon(new int[]{spaceStationX + SPACE_STATION_W - 10, spaceStationX + SPACE_STATION_W - 50, spaceStationX + SPACE_STATION_W - 10}, 
				new int[]{spaceStationY - 40, spaceStationY, spaceStationY}, 3);
		
		spaceStationRect = new Rectangle(spaceStationX - 50, spaceStationY, 50 + SPACE_STATION_W, SPACE_STATION_H);
		
		//draw unloading area
		if(canUnload){
			g.setColor(Color.GREEN);
		} else {
			g.setColor(Color.WHITE);
		}
		unloadingAreaRect = new Rectangle(spaceStationX + SPACE_STATION_W + 2, spaceStationY - 30, 45, 85);
		g.drawRoundRect(unloadingAreaRect.x, unloadingAreaRect.y, unloadingAreaRect.width, unloadingAreaRect.height, 5, 5);
		
		if(Math.abs(rocket.speedY) < 1){
			rocketRect = rocket.getRocketRectangle();
			canUnload = unloadingAreaRect.contains(rocketRect);
		} else {
			canUnload = false;
		}
		
		//draw word
		g.setColor(Color.RED);
		g.drawString(SPACE_STATION, spaceStationX + 5, spaceStationY + 15);
		
		//draw cargo
		for(int i = 0; i < cargoOnStation; i++){
			int x = spaceStationX + 5 + i * 22;
			int y = spaceStationY + 20;
			g.setColor(new Color(117, 92, 61));
			g.fillRect(x, y, 20, 20);
			
			g.setColor(Color.BLACK);
			g.drawLine(x, y, x + 19, y + 19);
			g.drawLine(x + 19, y, x, y + 19);
		}
	}
	
	private void paintStars(Graphics g){
		for(int i = 0; i < STAR_NUM; i++){
			g.setColor(Color.WHITE);
			g.drawOval((starXY[i][0] + starOffset) % LandingGame.WINDOW_W, starXY[i][1], 2, 2);
		}
	}
	
	private void gameOver(){
		System.out.println("gameover");
		showReplay(false);
		
	}
	
	private void gameWin(){
		System.out.println("win");
		showReplay(true);
	}
	
	private void showReplay(boolean wonOrLoss) {
		String str = (wonOrLoss ? "You Won!" : "Bad Luck...");
		final JLabel text = new JLabel(str);
		text.setFont(new Font(null, Font.BOLD, 20));
		text.setForeground(Color.GREEN);
		text.setBounds(LandingGame.WINDOW_W / 2 - 50, LandingGame.WINDOW_H / 2 - 60, 300, 30);
		add(text);
		
		final JButton btnReplay = new JButton("REPLAY");
		btnReplay.setBounds(LandingGame.WINDOW_W / 2 - 50, LandingGame.WINDOW_H / 2 - 15, 100, 30);
		add(btnReplay);
		
		btnReplay.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				remove(btnReplay);
				remove(text);
				init();
				requestFocus();
			}
		});
	}
	
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_UP) {
			rocket.fire(true);
		}
		if(e.getKeyCode() == KeyEvent.VK_LEFT){
			rocket.left(true);
		}
		if(e.getKeyCode() == KeyEvent.VK_RIGHT){
			rocket.right(true);
		}
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			if(rocket.y == PLAT_Y && rocket.speedY == 0 && rocket.accY == 0){
				if(!rocket.cargoLoaded){
					rocket.loadCargo();
					remainingCargoNum--;
				}
			} else if(canUnload){
				if(rocket.cargoLoaded){
					rocket.unloadCargo();
					cargoOnStation++;
				}
			}
		
			
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_UP) {
			rocket.fire(false);
		}
		if(e.getKeyCode() == KeyEvent.VK_LEFT){
			rocket.left(false);
		}
		if(e.getKeyCode() == KeyEvent.VK_RIGHT){
			rocket.right(false);
		}	
	}
	
	private class RepaintThread extends Thread{
		@Override
		public void run() {
			while (LandingGame.running){
				repaint();
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class StarMoveThread extends Thread{
		@Override
		public void run() {
			while (LandingGame.running && !rocket.dead){
				if(starOffset == 0){
					starOffset = LandingGame.WINDOW_W;
				}
				starOffset--;
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class SpaceStationMoveThread extends Thread{
		@Override
		public void run() {
			while (LandingGame.running && !rocket.dead){
				spaceStationX -= (1 - random.nextInt(2));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
