import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.util.Date;
import java.util.Random;

public class EelsAndEscalatorsServer extends JFrame implements EelsAndEscalatorsInterface {
	
	public static void main(String[] args) {
		EelsAndEscalatorsServer server = new EelsAndEscalatorsServer();
	}
	
	// Default Constructor
	public EelsAndEscalatorsServer() {	
		
		// BEGIN GUI BUILDING
		
		JPanel		contentPane, panel;
		JButton		exit;
		JTextArea	outputText;
		JScrollPane scrollPane;
		
		// MAIN WINDOW JPANEL
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout());
		
		// PROPERTIES OF WINDOW
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(860, 440, 450, 300);
		setResizable(false);
		setContentPane(contentPane);
		setVisible(true);
		
		// SOUTHERN PANEL FOR GUI OBJECTS
		panel = new JPanel();
		panel.setBackground(new Color(18, 148, 203));
		contentPane.add(panel, BorderLayout.SOUTH);
		
		// TERMINATE BUTTON
        exit = new JButton("Exit");
        panel.add(exit);

        // TEXTBOX FOR DEBUGGING AND CONNECTION INFO
        outputText = new JTextArea();
        scrollPane = new JScrollPane(outputText);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        
        // TERMINATE BUTTON ACTION LISTENER
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		// END GUI BUILDING
        
		// BEGIN HOSTING SERVER
		try {
			// THIS IS THE SOCKET OUR PLAYERS CONNECT ON
			ServerSocket socket = new ServerSocket(9001);
			
			// DEBUG INFO OUTPUT
			outputText.append(new Date() +
			        ": Server started at socket 9001\n");
			outputText.append("\n" + new Date() + ": Waiting for players...\n");
			
			// WAIT FOR CONNECTION OF PLAYERS, ACCEPT 4 PLAYERS THEN START
			// DEBUGGING NOTE: ONLY 1 PLAYER IS ACTIVE TO FACILITATE TESTING
			Socket player1 = socket.accept();
			outputText.append("\nPlayer 1 now connecting from " + player1.getInetAddress().getHostAddress());
			
			/*
			Socket player2 = socket.accept();
			outputText.append("\nPlayer 2 now connecting from " + player2.getInetAddress().getHostAddress());
			
			Socket player3 = socket.accept();
			outputText.append("\nPlayer 3 now connecting from " + player3.getInetAddress().getHostAddress());
			
			Socket player4 = socket.accept();
			outputText.append("\nPlayer 4 now connecting from " + player4.getInetAddress().getHostAddress());
			*/
			
			// ALL PLAYERS HAVE BEEN CONNECTED
			// BEGIN OUR SERVER LOGIC LOOP
			GameSession session = new GameSession(player1);
			new Thread(session).start();
		}
		catch (Exception e) {
			outputText.append(e.toString());
		}
		// END HOSTING SERVER
	}
	
	
}


class GameSession implements Runnable, EelsAndEscalatorsInterface {
	
	// Global Vars
	int numOfPlayers, currentPlayerTurn, x, y;
	int[] playerCharacterChoice;
	int[] dice;
	Random rand = new Random();
	
	//Create the map tiles
	final int MAP_X = 10; int MAP_Y = 3;
	private Tile<Player>[][] map; //map
	
	// Create our sockets to player clients
	private Socket player1;
	private Socket player2;
	private Socket player3;
	private Socket player4;
	
	// OUR CURRENT PLAYER SELECTION IN AND OUT STREAM
	private DataInputStream currentIn;
	private DataOutputStream currentOut;
	
	// BEGIN CONSTRUCTORS
	// @param1 Our socket connection to player1 client
	public GameSession(Socket player1) {
		// Initialize our player connections before game start
		this.player1 = player1;
		
		genMapDefault(); //generate map
		
	}
	
	// @param1 Our socket connection to player1 client
	// @param2 Our socket connection to player2 client
	public GameSession(Socket player1, Socket player2) {
		
		// Initialize our player connections before game start
		this.player1 = player1;
		this.player2 = player2;
		
		genMapDefault(); //generate map
		
	}
	
	// @param1 Our socket connection to player1 client
	// @param2 Our socket connection to player2 client
	// @param3 Our socket connection to player3 client
	public GameSession(Socket player1, Socket player2, Socket player3) {
		
		// Initialize our player connections before game start
		this.player1 = player1;
		this.player2 = player2;
		this.player3 = player3;
		
		genMapDefault(); ////generate map
	}
	
	// @param1 Our socket connection to player1 client
	// @param2 Our socket connection to player2 client
	// @param3 Our socket connection to player3 client
	// @param4 Our socket connection to player4 client
	public GameSession(Socket player1, Socket player2, Socket player3, Socket player4) {
		
		// Initialize our player connections before game start
		this.player1 = player1;
		this.player2 = player2;
		this.player3 = player3;
		this.player4 = player4;
		
		genMapDefault(); //generate map
	}
	
	// END CONSTRUCTORS
	
	
	public void run() {
		try {
			currentPlayerTurn = -1;
			// Initialize our in and out streams to send/receive from our player clients
			// DEBUGGING NOTE: PLAYER IN/OUT STREAMS ARE COMMENTED OUT FOR TESTING PURPOSES
			DataInputStream fromP1 = new DataInputStream(player1.getInputStream());
			/*
			DataInputStream fromP2 = new DataInputStream(player2.getInputStream());
			DataInputStream fromP3 = new DataInputStream(player3.getInputStream());
			DataInputStream fromP4 = new DataInputStream(player4.getInputStream());
			*/
			
			
			DataOutputStream toP1 = new DataOutputStream(player1.getOutputStream());
			/*
			DataOutputStream toP2 = new DataOutputStream(player2.getOutputStream());
			DataOutputStream toP3 = new DataOutputStream(player3.getOutputStream());
			DataOutputStream toP4 = new DataOutputStream(player4.getOutputStream());
			*/
			
			toP1.writeInt(PLAYER_WAIT);
			/*
			toP2.writeInt(PLAYER_WAIT);
			toP3.writeInt(PLAYER_WAIT);
			toP4.writeInt(PLAYER_WAIT);
			*/
			// SERVER MAIN LOGIC LOOP
			while (true) {
				// DEBUGGING NOTE: ALL TURNS ARE PLAYER1 FOR TESTING PURPOSES
				// BELOW DECIDES WHICH PLAYER IS THE CURRENT PLAYER
				switch(PLAYER_TURN_ORDER[whosNext(currentPlayerTurn)]) {
					case PLAYER1_TURN:
						currentIn = fromP1;
						currentOut = toP1;
						break;
						
					case PLAYER2_TURN:
						currentIn = fromP1;
						currentOut = toP1;
						break;
						
					case PLAYER3_TURN:
						currentIn = fromP1;
						currentOut = toP1;
						break;
						
					case PLAYER4_TURN:
						currentIn = fromP1;
						currentOut = toP1;
						break;
				}
				
				// TELL THE CLIENT THAT IT IS THIS PLAYERS TURN
				currentOut.writeInt(PLAYER_GO);
				
				// RECEIVE THE REQUEST FROM THE CLIENT
					switch (currentIn.readInt()) {
						case SEND_ROLL_REQUEST:
							dice = rollDice();
							currentOut.writeInt(dice[0]);
							currentOut.writeInt(dice[1]);
							currentOut.writeInt(PLAYER_WAIT);
							break;	
					}

				}
			}
		catch (Exception e) {
			System.out.println(e.toString());
		}
		// END SERVER MAIN LOGIC LOOP
	}
	
	
	
	private void sendMove(DataOutputStream out, int x, int y) throws IOException{
		out.writeInt(x);
		out.writeInt(y);
	}
	
	
	private int whosNext(int c) {
		if (c > 2) 
			c = 0;
		else
			c += 1;
		return c;
	}
	
	// Add a player to the game
	public void addPlayer() {
		if (numOfPlayers < 4) {
			numOfPlayers++;
		}
		else {
			System.out.println(MAX_CONNECTED);
		}
	}
	
	// @return Returns number of players in game
	public int getNumOfPlayers() {
		return numOfPlayers;
	}
	
	public int whosNext() {
		if(currentPlayerTurn < 4)
			return currentPlayerTurn+1;
		else
			return 0;	
	}	
	
	public int setNext() {
		if(currentPlayerTurn < 4)
			return currentPlayerTurn++;
		else
			return currentPlayerTurn = 0;	
	}	
	
	
	public int[] rollDice(){
		int[] dice = { rand.nextInt(6) +1, rand.nextInt(6) +1 };
		return dice; // dice implementation
	}
	
	public void genMapDefault(){ //regular map generation - added to server TODO
		map = new Tile[MAP_Y][MAP_X];
		for(int x = 0; x < MAP_X; x++){
			for(int y = 0; y < MAP_Y; y++){
				map[y][x] = new Tile<Player>(x, y);
			}
		}
		map[0][0].setStart();
		map[0][1].setEel1();
		map[0][9].setEel1();
		map[0][5].setEel2();
		
	}
	
	public void genMapRandom(){ //generates a random map - TODO
		
	}
	
	public void movePlayer(Player player, int currentXPosition, int currentYPosition){ //used after the dice has been rolled
		map[player.getYLocation()][player.getXLocation()].removePlayer(player); //remove player from tile
		int moveAmount;
		int totalDice = dice[0] + dice[1];
		
		while(totalDice > 0){
			if(player.getXLocation()+totalDice > MAP_X-1 || (player.getXLocation()-totalDice < 0 && player.getYLocation() == 1)){
				moveAmount = (MAP_X-1) - player.getXLocation(); //space between the end of the map and current Tile
				totalDice = totalDice - moveAmount - 1; //
				player.setXLocation(MAP_X-1);
				player.setYLocation(player.getYLocation()+1);
			}
			else if(player.getYLocation() == 1){ //moves player to the left if in the middle section
				player.setXLocation(player.getXLocation()-1);
				totalDice--;
			}
			else{
				player.setXLocation(player.getXLocation()+1);
				totalDice--;
			}
		}
		
		//transfer to other tiles if 
		switch(map[player.getYLocation()][player.getXLocation()].getTrait()){ //TODO - add player transfer to end of escalator/eel 
			case 1: //eel1 = 1;
				break;
			case 2: //eel2 = 2;
				break;
			case 3: //eel3 = 3;
				break;
			case 4: //eel4 = 4;
				break;
			case 5: //escalator1H = 5;
				break;
			case 6: //escalator2H = 6; 
				break;
			case 7: //escalator3H = 7
				map[player.getYLocation()][player.getXLocation()].addPlayer(player); //add player to tile
				break;
		}
	}
}