import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;


public class LandingGame{
	public static final int WINDOW_W = 650;
	public static final int WINDOW_H = 800;
	
	
	private GamePanel gamePanel;
	private JFrame mainFrame;
	
	public static boolean running = true;
	
	private void init(){
		gamePanel = new GamePanel();
		
		
		mainFrame = new JFrame();
		mainFrame.setResizable(false);
		mainFrame.setTitle("Transporter");
		mainFrame.getContentPane().add(gamePanel);
		mainFrame.setSize(WINDOW_W, WINDOW_H);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		mainFrame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				running = false;
			}

		});
		mainFrame.setVisible(true);
		
		gamePanel.requestFocus();

	}
	
	public static void main(String[] args){
		new LandingGame().init();
	}
}
