import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.*;
import java.net.*;
import java.util.Random;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class ClientFrame extends JFrame implements EelsAndEscalatorsInterface, Runnable {

	// GUI ELEMENTS
	private JPanel panel;
	private JLabel contentPane, playerSprite, playerSprite2, playerSprite3, playerSprite4, lblDi, lblDi_1, lblStatus;
	private JTextArea outputText;
	private JScrollPane scrollPane;
	private DataInputStream inputStream; //fromServer
	private DataOutputStream outputStream; //toServer
	private JButton btnRoll;
	
	// BOOLEAN ELEMENTS
	private boolean waiting = true;
	private boolean myTurn = false;
	
	// SERVER CONNECTION INFO
	private String host = "127.0.0.1";
	private int port = 9001;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						ClientFrame frame = new ClientFrame();
						frame.connectToServer();
				} catch (Exception e) {
					e.printStackTrace();
				}	
				}
			});
	}

	/**
	 * Create the frame.
	 */
	public ClientFrame() {
		// THESE URLS TELL JAVA THAT THESE ARE REQUIRED RESOURCES
		// SO THAT THEY MAY BE PACKED INTO A JAR CORRECTLY
		URL sprite1 = ClientFrame.class.getResource("/resources/123.png");
		URL sprite2 = ClientFrame.class.getResource("/resources/124.png");
		URL sprite3 = ClientFrame.class.getResource("/resources/125.png");
		URL sprite4 = ClientFrame.class.getResource("/resources/126.png");
		URL background = ClientFrame.class.getResource("/resources/EelsAndEscalatorsYouLose.jpg");
		
		// BEGIN PLAYER SPRITE CREATION
		playerSprite = new JLabel(rescaleSprite(sprite1, 20, 20));
		playerSprite2 = new JLabel(rescaleSprite(sprite2, 20, 20));
		playerSprite3 = new JLabel(rescaleSprite(sprite3, 20, 20));
		playerSprite4 = new JLabel(rescaleSprite(sprite4, 20, 20));
		// END PLAYER SPRITE CREATION
		
		// CREATE OUR MAIN WINDOW AND BACKGROUND
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		setResizable(false);
		contentPane = new JLabel(rescaleSprite(background, 800, 600));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		
		// DICE WINDOW
		panel = new JPanel();
		panel.setBackground(new Color(18, 148, 203));
		contentPane.add(panel);
		panel.setSize(150,50);
		panel.setLocation(20,500);
		btnRoll = new JButton("Roll");
		panel.add(btnRoll);
		lblDi = new JLabel("0");
		lblDi_1 = new JLabel("0");
		lblStatus = new JLabel("Turn: ");
		panel.add(lblDi_1);
		panel.add(lblDi);
		panel.add(lblStatus);
		
		// TEXT AREA
		outputText = new JTextArea();
		outputText.setWrapStyleWord(true);
		outputText.setLineWrap(true);
        scrollPane = new JScrollPane(outputText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentPane.add(scrollPane);
        scrollPane.setSize(120,300);
        scrollPane.setLocation(650,100);
		
		
		
		// ADD PLAYER SPRITE
		// Player 1
		contentPane.add(playerSprite);
		playerSprite.setLocation(180, 380);
		playerSprite.setSize(20,20);
		
		// Player 2
		contentPane.add(playerSprite2);
		playerSprite2.setLocation(180, 400);
		playerSprite2.setSize(20,20);
		
		// Player 3
		contentPane.add(playerSprite3);
		playerSprite3.setLocation(180, 420);
		playerSprite3.setSize(20,20);
		
		// Player 4
		contentPane.add(playerSprite4);
		playerSprite4.setLocation(180, 440);
		playerSprite4.setSize(20,20);
		
		// DISPLAY OUR GUI
		setVisible(true);

		// JBUTTON BTNROLL ACTION LISTENER
		btnRoll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try { 
					if(myTurn) {
						// REQUEST DICE FROM THE SERVER AND DISPLAY THEM ON THE CLIENT
						outputStream.writeInt(SEND_ROLL_REQUEST);
						lblDi.setText(Integer.toString(inputStream.readInt()));
						lblDi_1.setText(Integer.toString(inputStream.readInt()));
						
						// RESUME LISTENING FROM THE SERVER
						// OUR TURN IS OVER
						waiting = false;
					}
					else
						outputText.append("\n" + WAIT_MESSAGE);
					
				} catch (Exception err) {
					outputText.append(err.toString());
				}
			}
		});
		
		contentPane.addMouseListener(new MouseListener() {
		public void mouseClicked(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			outputText.append("X: " + x + ", Y: " + y + "\n");
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

	});
		
	}
	
	
	public void sendWelcomeMessage(int playerID) {
		outputText.append("Welcome to Eels and Escalators! Looks like you're Player " + playerID + " so you will go " + playerID + "\n\n");
		outputText.append("Typically Eels will take you back and Escalators will progress you forward, but since our designer obviously didn't realize this, we have a mix. \n\n");
		outputText.append("The game ends when a player reaches the finish line. Good luck!\n\n");
	}
	
	// HELPER FUNCTION FOR RESIZING SPRITES
	public ImageIcon rescaleSprite(URL path, int h, int w) {
		ImageIcon plSprite = new ImageIcon(path); 
		Image image = plSprite.getImage(); 
		image = image.getScaledInstance(h, w, Image.SCALE_SMOOTH);
		return new ImageIcon(image);
	}
	
	//@param1 Host of server
	//@param2 Port of server
	//@return Server connected status
	public void connectToServer() {
		
		try {
			// Create our socket to connect to the server
			Socket socket = new Socket(host, port);
			
			// Client in and out streams to and from the server
			inputStream = new DataInputStream(socket.getInputStream());
			outputStream = new DataOutputStream(socket.getOutputStream());
			
			// CONNECTED - START GAME LOOP
			Thread thread = new Thread(this);
		    thread.start();
			
		}
		catch (Exception e) {
			System.out.println("Problem with connectToServer()");
			
		}	
	}
	

	public void run() {
		int playerID = 1, currentPlayer = 1;
		try {
		playerID = inputStream.readInt();
		sendWelcomeMessage(playerID);
		}
		catch (Exception e) {
			System.out.println(e.toString());
		}
		while(true) {
			try {
				switch(inputStream.readInt()) {
					
					// NOT OUR TURN
					case PLAYER_WAIT:
						myTurn = false;
						break;
					
					// ALLOW PLAYER TO MOVE
					case PLAYER_GO:
						myTurn = true;
						waitForPlayer();
						break;
					
					// UPDATE GAME BOARD
					case END_PLAYER_TURN:
						currentPlayer = inputStream.readInt();
						receiveMove(currentPlayer, inputStream.readInt(), inputStream.readInt(), inputStream.readInt(), inputStream.readInt());
						updateStatus(playerID, currentPlayer);
						break;
					
					// PLAYER HAS WON THE GAME
					case PLAYER_WON:
						win(currentPlayer);
						break;
						
					case PLAYER_LOST:
						lost(currentPlayer, playerID);
						break;
				}
				
			}
			catch (Exception ex) {
				System.out.println(ex.toString());
			}
			}
		}
	
	// WAIT FOR PLAYER TO ROLL/ACTION
	private void waitForPlayer() throws InterruptedException {
		    while (waiting) {
		      Thread.sleep(100);
		    }
		    waiting = true;
		  }

	private void updateStatus(int myID, int current) {
			lblStatus.setText("PLAYER " + current + " WENT LAST");
	}
	// END GAME IF CALLED
	private void win(int current) {
		outputText.append("PLAYER " + current + " HAS WON THE GAME!\n");
		panel.remove(btnRoll);
		panel.remove(lblDi_1);
		lblDi.setText("GAME OVER");
	}
	
	private void lost(int player, int playerID) {
		outputText.append("PLAYER " + player + " HAS LOST THE GAME!\n");
		if (player == playerID) {
			panel.remove(btnRoll);
			panel.remove(lblDi_1);
			lblDi.setText("GAME OVER");
		}
	}
	
	// UPDATE GAMEBOARD WITH NEW POSITIONS
	private void receiveMove(int currentPlayer, int x, int y, int eel, int esc){
        switch (currentPlayer){
            case 1:
            	if (eel == 1)
            		outputText.append("Player " + currentPlayer + " has slided down an eel!\n");
            	if (esc == 1)
            		outputText.append("Player " + currentPlayer + " has taken an escalator up!\n");
            	playerSprite.setLocation(x, y); 
                break;
            case 2:
            	if (eel == 1)
            		outputText.append("Player " + currentPlayer + " has slided down an eel!\n");
            	if (esc == 1)
            		outputText.append("Player " + currentPlayer + " has taken an escalator up!\n");
            	playerSprite2.setLocation(x, y);
                break;
            case 3:
            	if (eel == 1)
            		outputText.append("Player " + currentPlayer + " has slided down an eel!\n");
            	if (esc == 1)
            		outputText.append("Player " + currentPlayer + " has taken an escalator up!\n");
            	playerSprite3.setLocation(x, y); 
                break;
            case 4:
            	if (eel == 1)
            		outputText.append("Player " + currentPlayer + " has slided down an eel!\n");
            	if (esc == 1)
            		outputText.append("Player " + currentPlayer + " has taken an escalator up!\n");
            	playerSprite4.setLocation(x, y); 
                break;
                
        } 
	}
}
