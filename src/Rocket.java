import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;


public class Rocket {
	final private int WIDTH = 20;
	final private int HEIGHT = 50;
	final private int SIDE_ROCKET_WIDTH = 6;
	final private int SIDE_ROCKET_HEIGHT = 20;
	
	final private int HEAD_HEIGHT = 20;
	
	private final int MAX_FLAME_LEN = 20;
	private int flame_len = 0;
	private boolean flaming = false;

	private boolean isBlasting = false;
	private final int BLASTING_DUARATION = 1000;
	private int blasingTime = 0;
	private int blastDiameter = 80;
	
	private Random random = new Random(); 
	
	boolean dead = false;
	
	float angle = (float) (Math.PI / 2);
	float x;
	float y;
	
	private final float DEFAULT_ACC_Y = .08f;//gravity
	private final float RISING_ACC = DEFAULT_ACC_Y * -1.5f;
	
	private final float X_ACC = 0.05f;
	
	float accY = DEFAULT_ACC_Y;
	float accX = 0;
	
	float speedX = 0;
	float speedY = 0;
	
	boolean cargoLoaded = false;
	
	private RocketCPU rocketThread;
	
	public Rocket(){
		stop();
		rocketThread = new RocketCPU();
		rocketThread.start();
		
		new FlameThread().start();
	}
	
	public void paint(Graphics g) {

		if (!isBlasting) {
			int tlx = (int) (x - WIDTH / 2f);
			int tly = (int) (y - HEIGHT);
			int sr1_tlx = tlx - SIDE_ROCKET_WIDTH;
			int sr2_tlx = tlx + WIDTH;
			int sr_tly = (int) (y - SIDE_ROCKET_HEIGHT);
			// head
			g.setColor(Color.RED);
			g.fillPolygon(new int[] { tlx, tlx + WIDTH, tlx + WIDTH / 2 }, new int[] { tly, tly, tly - HEAD_HEIGHT },
					3);
			// body
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(tlx, tly, WIDTH, HEIGHT - 2);
			// small rockets
			g.setColor(Color.GRAY);
			g.fillRect(sr1_tlx, sr_tly, SIDE_ROCKET_WIDTH, SIDE_ROCKET_HEIGHT);
			g.fillRect(sr2_tlx, sr_tly, SIDE_ROCKET_WIDTH, SIDE_ROCKET_HEIGHT);
			// flame
			g.setColor(Color.YELLOW);
			int flameLenTmp = sr_tly + SIDE_ROCKET_HEIGHT + (int)(flame_len * ((8 + random.nextInt(4)) / 10f));
			g.fillPolygon(
					new int[] { sr1_tlx, sr1_tlx + SIDE_ROCKET_WIDTH, sr1_tlx + SIDE_ROCKET_WIDTH / 2 },
					new int[] { sr_tly + SIDE_ROCKET_HEIGHT, sr_tly + SIDE_ROCKET_HEIGHT,
							flameLenTmp}, 3);
			g.fillPolygon(
					new int[] { sr2_tlx, sr2_tlx + SIDE_ROCKET_WIDTH, sr2_tlx + SIDE_ROCKET_WIDTH / 2 },
					new int[] { sr_tly + SIDE_ROCKET_HEIGHT, sr_tly + SIDE_ROCKET_HEIGHT,
							flameLenTmp}, 3);
			
			paintCargo(g);
		} else {
			paintBlast(g);
		}

	}
	
	void paintBlast(Graphics g){
		int alpha = 255 - (int) (255 * (float)blasingTime / BLASTING_DUARATION);
		alpha = Math.max(alpha, 0);
		g.setColor(new Color(255, 99, 22, alpha));
		
		g.fillOval((int)(x - blastDiameter / 2), (int)(y - HEIGHT/2 - blastDiameter / 2), blastDiameter, blastDiameter);
	}
	
	void paintCargo(Graphics g){
		if(cargoLoaded){
			g.setColor(new Color(117,92,61));
			g.fillRect((int)(x - 5), (int)(y - 15), 10, 10);
		}
	}
	
	void loadCargo(){
		cargoLoaded = true;
	}
	
	void unloadCargo(){
		cargoLoaded = false;
	}
	
	void fire(boolean b){
		flaming = b;
		if(b){
			accY = RISING_ACC;
		}else{
			accY = DEFAULT_ACC_Y;
		}
	}
	
	void left(boolean b){
		if(speedY == 0 && accY == 0){
			return;
		}
		if(b){
			accX = -X_ACC;
		} else {
			accX = 0;
		}
	}
	void right(boolean b){
		if(speedY == 0 && accY == 0){
			return;
		}
		if(b){
			accX = X_ACC;
		} else {
			accX = 0;
		}
	}
	
	void blast(){
		if(!isBlasting){
			dead = true;
			blastDiameter = 80;
			isBlasting = true;
			new BlastThread().start();
		}
	}
	
	void stop(){
		accY = 0;
		accX = 0;
		speedY = 0;
		speedX = 0;
	}
	
	Rectangle getRocketRectangle(){
		return new Rectangle((int)(x - WIDTH / 2 - 6), (int)(y - HEIGHT), WIDTH + 2 * SIDE_ROCKET_WIDTH, HEIGHT);
	}
	
	private class RocketCPU extends Thread{
		
		@Override
		public void run() {
			while (LandingGame.running && !dead){
				speedY += accY;
				speedX += accX;
				x += speedX;
				y += speedY;
				
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	private class BlastThread extends Thread{
		@Override
		public void run() {
			while(LandingGame.running && blasingTime < BLASTING_DUARATION){
				blastDiameter += 10;
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				blasingTime += 30;
			}
		}
	}
	
	private class FlameThread extends Thread{
		@Override
		public void run() {
			while(LandingGame.running){
				if(flaming){
					flame_len += 2;
					flame_len = Math.min(flame_len, MAX_FLAME_LEN);
				} else {
					flame_len -= 2;
					flame_len = Math.max(flame_len, 0);
				}
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
