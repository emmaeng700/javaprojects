// Emmanuel A Oppong
// 29th January, 2026

import java.util.Scanner;
import java.util.Random;

public class ConnectFour {
    static final int ROWS = 6;
    static final int COLS = 7;
    
    // defining letters as global constant
    static final char[] letters = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    
    // creating the main board as a 2D array
    static char[][] board = new char[ROWS][COLS];
    
    // defining players as global constants
    static final char[] players = {'X','O','V','H','M'};
    
    static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        // Initialize board with spaces
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                board[row][col] = ' ';
            }
        }
        
        playGame();
    }
    
    // main gameplay
    public static void playGame() {
        int numMoves = 0;
        printBoard();
        int start = 0;
        int numPlayers = 0;
        
        if (start == 0) {
            boolean selection = true;
            
            // get number of players before game starts
            while (selection && start == 0) {
                System.out.print("Enter a number of players: 2-5 players allowed... ");
                String numPlayersStr = scanner.nextLine();
                boolean valid = checkNumPlayers(numPlayersStr);
                if (valid) {
                    start++;
                    numPlayers = Integer.parseInt(numPlayersStr);
                    System.out.println(numPlayers + " players will be playing this game");
                    selection = false;
                } else {
                    System.out.println("Incorrect input type. Re-enter. ");
                }
            }
            
            // define the first turn for any number of players
            Random rand = new Random();
            int firstTurn = rand.nextInt(numPlayers);
            
            char player1 = ' ', player2 = ' ', player3 = ' ', player4 = ' ', player5 = ' ';
            
            // SETTING (firstTurn) for 2 players
            if (numPlayers == 2) {
                if (firstTurn == 0) {
                    player1 = players[0];
                    player2 = players[1];
                } else {
                    player1 = players[1];
                    player2 = players[0];
                }
            }
            // SETTING (firstTurn) for 3 players
            else if (numPlayers == 3) {
                if (firstTurn == 0) {
                    player1 = players[0];
                    player2 = players[1];
                    player3 = players[2];
                } else if (firstTurn == 1) {
                    player1 = players[1];
                    player2 = players[0];
                    player3 = players[2];
                } else {
                    player1 = players[2];
                    player2 = players[0];
                    player3 = players[1];
                }
            }
            // SETTING (firstTurn) for 4 players
            else if (numPlayers == 4) {
                if (firstTurn == 0) {
                    player1 = players[0];
                    player2 = players[1];
                    player3 = players[2];
                    player4 = players[3];
                } else if (firstTurn == 1) {
                    player1 = players[1];
                    player2 = players[0];
                    player3 = players[2];
                    player4 = players[3];
                } else if (firstTurn == 2) {
                    player1 = players[2];
                    player2 = players[0];
                    player3 = players[1];
                    player4 = players[3];
                } else {
                    player1 = players[3];
                    player2 = players[0];
                    player3 = players[1];
                    player4 = players[2];
                }
            }
            // SETTING (firstTurn) for 5 players
            else {
                if (firstTurn == 0) {
                    player1 = players[0];
                    player2 = players[1];
                    player3 = players[2];
                    player4 = players[3];
                    player5 = players[4];
                } else if (firstTurn == 1) {
                    player1 = players[1];
                    player2 = players[0];
                    player3 = players[2];
                    player4 = players[3];
                    player5 = players[4];
                } else if (firstTurn == 2) {
                    player1 = players[2];
                    player2 = players[0];
                    player3 = players[1];
                    player4 = players[3];
                    player5 = players[4];
                } else if (firstTurn == 3) {
                    player1 = players[3];
                    player2 = players[0];
                    player3 = players[1];
                    player4 = players[2];
                    player5 = players[4];
                } else {
                    player1 = players[4];
                    player2 = players[0];
                    player3 = players[1];
                    player4 = players[2];
                    player5 = players[3];
                }
            }
            
            // defining game instructions
            System.out.println("Moves are the columns eg. A or B :");
            
            // Game loop
            boolean gameOn = true;
            
            // play game only when no exit flag has been called
            while (gameOn) {
                numMoves++;
                char playerCh = ' ';
                
                // if 2 players playing
                if (numPlayers == 2) {
                    if (numMoves < 2) {
                        System.out.println(players[firstTurn] + " will go first. ");
                    }
                    
                    if ((numMoves - 1) % 2 == 0) {
                        playerCh = player1;
                    } else if ((numMoves - 1) % 2 == 1) {
                        playerCh = player2;
                    }
                    putChecker(playerCh);
                    updateBoard();
                }
                // if 3 players playing
                else if (numPlayers == 3) {
                    if (numMoves < 2) {
                        System.out.println(players[firstTurn] + " will go first. ");
                    }
                    
                    if ((numMoves - 1) % 3 == 0) {
                        playerCh = player1;
                    } else if ((numMoves - 1) % 3 == 1) {
                        playerCh = player2;
                    } else {
                        playerCh = player3;
                    }
                    putChecker(playerCh);
                    updateBoard();
                }
                // if 4 players playing
                else if (numPlayers == 4) {
                    if (numMoves < 2) {
                        System.out.println(players[firstTurn] + " will go first. ");
                    }
                    
                    if ((numMoves - 1) % 4 == 0) {
                        playerCh = player1;
                    } else if ((numMoves - 1) % 4 == 1) {
                        playerCh = player2;
                    } else if ((numMoves - 1) % 4 == 2) {
                        playerCh = player3;
                    } else {
                        playerCh = player4;
                    }
                    putChecker(playerCh);
                    updateBoard();
                }
                // if 5 players playing
                else {
                    if (numMoves < 2) {
                        System.out.println(players[firstTurn] + " will go first. ");
                    }
                    
                    if ((numMoves - 1) % 5 == 0) {
                        playerCh = player1;
                    } else if ((numMoves - 1) % 5 == 1) {
                        playerCh = player2;
                    } else if ((numMoves - 1) % 5 == 2) {
                        playerCh = player3;
                    } else if ((numMoves - 1) % 5 == 3) {
                        playerCh = player4;
                    } else {
                        playerCh = player5;
                    }
                    putChecker(playerCh);
                    updateBoard();
                }
                
                // executes only if a winner has been spotted
                if (winnerCheck()) {
                    char winner = determineWinner(numMoves, numPlayers, player1, player2, player3, player4, player5);
                    System.out.println("Player " + winner + " won!");
                    
                    System.out.print("Do you want to play again Y or y/ N or n for yes or no... anything apart from these breaks the game   ");
                    String response = scanner.nextLine();
                    boolean validResponse = playAgain(response);
                    
                    if (validResponse) {
                        if (response.equals("y") || response.equals("Y")) {
                            System.out.println(" Yes you do, Lets go once again ");
                            // reset the board for a new game
                            for (int row = 0; row < ROWS; row++) {
                                for (int col = 0; col < COLS; col++) {
                                    board[row][col] = ' ';
                                }
                            }
                            playGame();
                            return;
                        } else if (response.equals("n") || response.equals("N")) {
                            System.out.println(" No you dont, Goodbye! ");
                            gameOn = false;
                        }
                    } else {
                        System.out.println("Sorry incorrect input type. Goodbye! ");
                        gameOn = false;
                    }
                }
                // check for draw
                else if (boardFull()) {
                    System.out.println("All the columns are full with NO winner. The game ends in a Draw, WOW Brainiacs");
                    
                    System.out.print("Do you want to play again Y or y/ N or n for yes or no... anything apart from these breaks the game  ");
                    String response = scanner.nextLine();
                    boolean validResponse = playAgain(response);
                    
                    if (validResponse) {
                        if (response.equals("y") || response.equals("Y")) {
                            System.out.println(" Yes you do, Lets go once again ");
                            for (int row = 0; row < ROWS; row++) {
                                for (int col = 0; col < COLS; col++) {
                                    board[row][col] = ' ';
                                }
                            }
                            playGame();
                            return;
                        } else if (response.equals("n") || response.equals("N")) {
                            System.out.println(" No you dont, Goodbye! ");
                            gameOn = false;
                        }
                    } else {
                        System.out.println("Sorry incorrect input type. Goodbye! ");
                        gameOn = false;
                    }
                }
            }
        }
    }
    
    // Helper method to determine winner based on number of moves
    public static char determineWinner(int numMoves, int numPlayers, char player1, char player2, char player3, char player4, char player5) {
        int remainder = (numMoves - 1) % numPlayers;
        
        if (remainder == 0) return player1;
        if (remainder == 1) return player2;
        if (remainder == 2) return player3;
        if (remainder == 3) return player4;
        if (remainder == 4) return player5;
        
        return ' ';
    }
    
    // function that checks if only the top row is full
    public static boolean boardFull() {
        for (int r = 0; r < 1; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }
    
    // function that checks for a winner vertically, horizontally, positive diagonally and negative diagonally
    public static boolean winnerCheck() {
        // CHECK A WINNER VERTICALLY
        for (int c = 0; c < COLS; c++) {
            for (int r = ROWS - 1; r >= 3; r--) {
                if ((board[r][c] == 'X' && board[r-1][c] == 'X' && board[r-2][c] == 'X' && board[r-3][c] == 'X') ||
                    (board[r][c] == 'O' && board[r-1][c] == 'O' && board[r-2][c] == 'O' && board[r-3][c] == 'O') ||
                    (board[r][c] == 'M' && board[r-1][c] == 'M' && board[r-2][c] == 'M' && board[r-3][c] == 'M') ||
                    (board[r][c] == 'H' && board[r-1][c] == 'H' && board[r-2][c] == 'H' && board[r-3][c] == 'H') ||
                    (board[r][c] == 'V' && board[r-1][c] == 'V' && board[r-2][c] == 'V' && board[r-3][c] == 'V')) {
                    return true;
                }
            }
        }
        
        // CHECK A WINNER HORIZONTALLY
        for (int c = 0; c < COLS - 3; c++) {
            for (int r = ROWS - 1; r >= 0; r--) {
                if ((board[r][c] == 'X' && board[r][c+1] == 'X' && board[r][c+2] == 'X' && board[r][c+3] == 'X') ||
                    (board[r][c] == 'O' && board[r][c+1] == 'O' && board[r][c+2] == 'O' && board[r][c+3] == 'O') ||
                    (board[r][c] == 'M' && board[r][c+1] == 'M' && board[r][c+2] == 'M' && board[r][c+3] == 'M') ||
                    (board[r][c] == 'H' && board[r][c+1] == 'H' && board[r][c+2] == 'H' && board[r][c+3] == 'H') ||
                    (board[r][c] == 'V' && board[r][c+1] == 'V' && board[r][c+2] == 'V' && board[r][c+3] == 'V')) {
                    return true;
                }
            }
        }
        
        // CHECK A WINNER ON POSITIVELY SLOPED DIAGONAL
        for (int c = 0; c < COLS - 3; c++) {
            for (int r = ROWS - 1; r >= 3; r--) {
                if ((board[r][c] == 'X' && board[r-1][c+1] == 'X' && board[r-2][c+2] == 'X' && board[r-3][c+3] == 'X') ||
                    (board[r][c] == 'O' && board[r-1][c+1] == 'O' && board[r-2][c+2] == 'O' && board[r-3][c+3] == 'O') ||
                    (board[r][c] == 'M' && board[r-1][c+1] == 'M' && board[r-2][c+2] == 'M' && board[r-3][c+3] == 'M') ||
                    (board[r][c] == 'H' && board[r-1][c+1] == 'H' && board[r-2][c+2] == 'H' && board[r-3][c+3] == 'H') ||
                    (board[r][c] == 'V' && board[r-1][c+1] == 'V' && board[r-2][c+2] == 'V' && board[r-3][c+3] == 'V')) {
                    return true;
                }
            }
        }
        
        // CHECK A WINNER ON NEGATIVELY SLOPED DIAGONAL
        for (int c = COLS - 1; c >= 3; c--) {
            for (int r = ROWS - 1; r >= 3; r--) {
                if ((board[r][c] == 'X' && board[r-1][c-1] == 'X' && board[r-2][c-2] == 'X' && board[r-3][c-3] == 'X') ||
                    (board[r][c] == 'O' && board[r-1][c-1] == 'O' && board[r-2][c-2] == 'O' && board[r-3][c-3] == 'O') ||
                    (board[r][c] == 'M' && board[r-1][c-1] == 'M' && board[r-2][c-2] == 'M' && board[r-3][c-3] == 'M') ||
                    (board[r][c] == 'H' && board[r-1][c-1] == 'H' && board[r-2][c-2] == 'H' && board[r-3][c-3] == 'H') ||
                    (board[r][c] == 'V' && board[r-1][c-1] == 'V' && board[r-2][c-2] == 'V' && board[r-3][c-3] == 'V')) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // function that uses checkInput function before placing a checker on the board
    public static void putChecker(char playerCh) {
        while (true) {
            System.out.print("Player " + playerCh + ", please enter a column: ");
            String str = scanner.nextLine();
            
            boolean valid = checkInput(str);
            if (valid) {
                int c = (int)(str.charAt(0)) - 65;
                
                // checks if column letter entered is on the board
                if (c < 0 || c >= COLS) {
                    System.out.println("Out of range. Re-enter. ");
                }
                // checks if a column full
                else if (colFull(str)) {
                    System.out.println("Column " + str + " is full, Enter a different column letter");
                } else {
                    // places checker by first looping to see if a bottom row in a column is not empty
                    if (c >= 0 && c < 26) {
                        for (int r = ROWS - 1; r >= 0; r--) {
                            if (board[r][c] == ' ') {
                                board[r][c] = playerCh;
                                return;
                            }
                        }
                    }
                    printBoard();
                    break;
                }
            } else {
                System.out.println("Incorrect input type. Re-enter. ");
            }
        }
    }
    
    // function that checks to see if user wants to play again
    public static boolean playAgain(String str) {
        if (str.length() < 1 || str.length() > 1) {
            return false;
        } else if (str.length() == 1 && (str.charAt(0) == 'Y' || str.charAt(0) == 'N' || str.charAt(0) == 'y' || str.charAt(0) == 'n')) {
            return true;
        } else if (str.length() == 1 && !(str.charAt(0) == 'Y' || str.charAt(0) == 'N' || str.charAt(0) == 'y' || str.charAt(0) == 'n')) {
            return false;
        } else {
            return false;
        }
    }
    
    // function that checks to see if a column is full or not
    public static boolean colFull(String cell) {
        int c = (int)(cell.charAt(0)) - 65;
        for (int r = 0; r < ROWS; r++) {
            if (board[r][c] != ' ') {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    // checks user input as a correct number of players
    public static boolean checkNumPlayers(String str) {
        if (str.length() < 1 || str.length() > 1) {
            return false;
        } else if (str.length() == 1 && Character.isDigit(str.charAt(0))) {
            int num = Integer.parseInt(str);
            if (num >= 2 && num <= 5) {
                return true;
            }
        }
        return false;
    }
    
    // function that prints the 2D board
    public static void printBoard() {
        // prints column header letters
        for (int i = 0; i < COLS; i++) {
            if (i == 0) {
                System.out.print("    " + letters[i] + "  ");
            } else {
                System.out.print(" " + letters[i] + "  ");
            }
        }
        
        // print row numbers by side of table
        System.out.println("\n  +" + "---+".repeat(COLS));
        for (int row = 0; row < ROWS; row++) {
            System.out.print("  | ");
            for (int col = 0; col < COLS; col++) {
                System.out.print(board[row][col] + " | ");
            }
            System.out.println("\n  +" + "---+".repeat(COLS));
        }
    }
    
    public static void updateBoard() {
        // sleep, clear the screen and print recently updated board
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Clear screen - works on Unix/Linux/Mac
        System.out.print("\033[H\033[2J");
        System.out.flush();
        
        printBoard();
    }
    
    // checks user input as correct position on the board
    public static boolean checkInput(String str) {
        if (str.length() < 1 || str.length() > 1) {
            return false;
        } else if (str.length() == 1) {
            char ch = str.charAt(0);
            for (char letter : letters) {
                if (ch == letter) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
}
