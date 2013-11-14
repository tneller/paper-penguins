import java.util.ArrayList;
import java.util.Random;

/**
 * PPState - a state representation for a game position in Paper Penguins.  Additionally, this implements the PPPlayer interface, 
 * so that play directly integrates with the state object.
 * 
 * @author Todd W. Neller
 *
 * Note: In the code below, a grid "position" is represented as a single integer in zero-based row-major form, 
 * i.e. for row r and column c on a SIZE-by-SIZE board, the position p is (r * SIZE + c).  
 * For position p, r = p / SIZE; c = p % SIZE; 
 */
public class PPState implements PPPlayer {
	/**
	 * a constant indicating a blocked position.  Note: <code>BLOCKED</code>, <code>SQUARE</code>, <code>CIRCLE</code> will each have unique integer values.
	 */
	public static final int BLOCKED = 0;

	/**
	 * a constant indicating a square player piece.  Note: <code>BLOCKED</code>, <code>SQUARE</code>, <code>CIRCLE</code> will each have unique integer values.
	 */
	public static final int SQUARE = -1;

	/**
	 * a constant indicating a circle player piece.  Note: <code>BLOCKED</code>, <code>SQUARE</code>, <code>CIRCLE</code> will each have unique integer values.
	 */
	public static final int CIRCLE = -2;
	
	/**
	 * dimensions of grid
	 */
	public static final int SIZE = 8;
	
	/**
	 * number of positions on the board
	 */
	public static final int NUM_POS = SIZE * SIZE;
	
	/**
	 * pieces per player
	 */
	public static final int NUM_PLAYER_PIECES = 4;
	
	/**
	 * total maximum pieces in play.
	 */
	public static final int TOTAL_PIECES = 2 * NUM_PLAYER_PIECES;
	
	/**
	 * the maximum number of legal moves a piece can have
	 */
	public static final int MAX_MOVES_PER_PIECE;
	
	/**
	 * the maximum number of legal moves per turn
	 */
	public static final int MAX_LEGAL_MOVES;
	
	/**
	 * character representing a square piece
	 */
	public static final char SQUARE_CHAR = 'X'; // unicode white square = 9633; in Firefox, see http://en.wikipedia.org/wiki/Geometric_Shapes 
	
	/**
	 * character representing a circle piece
	 */
	public static final char CIRCLE_CHAR = '@'; // unicode white circle = 9675, black circle = 9679
	
	/**
	 * character representing a blocked piece 
	 */
	public static final char BLOCKED_CHAR = '#'; // unicode black square = 9632
	
	protected static final int[] dRows = {0, -1, -1, -1, 0, 1, 1, 1};
	protected static final int[] dCols = {1, 1, 0, -1, -1, -1, 0, 1};	
	protected static int[][][] lines;
	protected static int[][] gridDistance = new int[2][NUM_POS];
	protected static final int UNREACHABLE = NUM_POS;
	protected static int[] squareSearchQueue = new int[NUM_POS], circleSearchQueue = new int[NUM_POS];
	protected static final boolean DISTANCE_TIE_GOES_TO_CURRENT_PLAYER = false;
	protected static Random random = new Random(0);
	protected static int[] play = new int[2];
	protected static int[] initGrid = new int[NUM_POS];
	protected static int[] scoredGrid = new int[NUM_POS];


	/**
	 * grid pieces indexed by row * columns + column (row-major ordering)
	 */
	protected int[] grid = new int[NUM_POS]; 
	
	/**
	 * position of pieces indexed by SQUARE(0)/CIRCLE(1), zero-based amazon piece number
	 */
	protected int[][] piecePositions = new int[2][NUM_PLAYER_PIECES];
		
	/**
	 * number of legal moves
	 */
	public int legalMoveCount;
		
	/**
	 * legal moves
	 */
	public int[][] legalMoves = new int[2][MAX_LEGAL_MOVES]; // first dimension: srcPos [0], destPos[0], second dimension: legal move number; 
	
	/**
	 * Current player, initially <code>PPState.SQUARE</code>
	 */
	protected int currentPlayer = SQUARE; 

	/**
	 * The current position of the previous Amazon just moved.
	 */
	protected int moveDestPos;
	
	/**
	 * number of turns taken in game (including initial placements)
	 */
	protected int turnsTaken = 0;
	
	/**
	 * current scores for SQUARE and CIRCLE are at indices 0 and 1, respectively.
	 */
	protected int[] score = new int[2];
	
	public PPState() {
	}
	
	/**
	 * Copy constructor.
	 * @param state - original state
	 */
	public PPState(PPState state) {
		grid = state.grid.clone();
		piecePositions = new int[2][];
		piecePositions[0] = state.piecePositions[0].clone();
		piecePositions[1] = state.piecePositions[1].clone();
		legalMoves = new int[2][MAX_LEGAL_MOVES]; // don't copy contents, just reallocate
		currentPlayer = state.currentPlayer;
		moveDestPos = state.moveDestPos;
		this.turnsTaken = state.turnsTaken;
	}
	
	static {
		// Precompute all lines of play from each grid position
		lines = new int[NUM_POS][][];
		int maxMovesPerPiece = 0;
		for (int i = 0; i < NUM_POS; i++) {
			int row = i / SIZE;
			int col = i % SIZE;
			ArrayList<ArrayList<Integer>> lineLists = new ArrayList<ArrayList<Integer>>();
			int movesPerPiece = 0;
			for (int dir = 0; dir < 8; dir++) {
				int currRow = row + dRows[dir];
				int currCol = col + dCols[dir];
				ArrayList<Integer> line = new ArrayList<Integer>();
				while (currRow >= 0 && currRow < SIZE && currCol >= 0 && currCol < SIZE) {
					line.add(currRow * SIZE + currCol);
					movesPerPiece++;
					currRow += dRows[dir];
					currCol += dCols[dir];
				}
				if (!line.isEmpty())
					lineLists.add(line);
			}
			if (movesPerPiece > maxMovesPerPiece)
				maxMovesPerPiece = movesPerPiece;
			lines[i] = new int[lineLists.size()][];
			for (int j = 0; j < lineLists.size(); j++) {
				ArrayList<Integer> line = lineLists.get(j);
				int lineLength = line.size();
				lines[i][j] = new int[lineLength];
				for (int k = 0; k < lineLength; k++)
					lines[i][j][k] = line.get(k);
			}
		}
		MAX_MOVES_PER_PIECE = maxMovesPerPiece;
		MAX_LEGAL_MOVES = NUM_PLAYER_PIECES * MAX_MOVES_PER_PIECE;
	}
	
	/**
	 * init - initializes the game state to an initial configuration created from a given random seed
	 */
	public void init(long seed) {
		PaperPenguins pp = new PaperPenguins(SIZE, seed);
		for (int pos = 0; pos < NUM_POS; pos++) {
			int row = pos / SIZE;
			int col = pos % SIZE;
			grid[pos] = pp.grid[row][col];
			scoredGrid[pos] = 0;
		}
		System.arraycopy(grid, 0, initGrid, 0, NUM_POS);
		turnsTaken = 0;
	}
	
	/**
	 * @return the scoredGrid
	 */
	public static int[] getScoredGrid() {
		return scoredGrid.clone();
	}

	/**
	 * @return the initGrid
	 */
	public static int[] getInitGrid() {
		return initGrid.clone();
	}

	/**
	 * @return the grid
	 */
	public int[] getGrid() {
		return grid.clone();
	}

	/**
	 * @return the currentPlayer
	 */
	public int getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * @return the turnsTaken
	 */
	public int getTurnsTaken() {
		return turnsTaken;
	}

	/**
	 * @return the score
	 */
	public int[] getScore() {
		return score.clone();
	}


	/**
	 * Get the contents of the given grid position row and column.
	 * @param row - position row.
	 * @param col - position column
	 * @return the contents of the given grid position row and column.
	 */
	int	get(int row, int col) {
		return get(row * SIZE + col);
	}
	
	/**
	 * Get the contents of the given grid position.
	 * @param pos - grid position
	 * Each position is encoded in zero-based row-major form, i.e. for row r and column c on a SIZE-by-SIZE board, the position p is
	 * (r * SIZE + c).  For position p, r = p / SIZE; c = p % SIZE;
	 * @return contents of the given grid position.
	 */
	int get(int pos) {
		return grid[pos];
	}	
	
	/**
	 * makeMove - make the given play. 
	 * @param srcPos - piece source position
	 * @param destPos - piece destination position
	 * Each position is encoded in zero-based row-major form, i.e. for row r and column c on a SIZE-by-SIZE board, the position p is
	 * (r * SIZE + c).  For position p, r = p / SIZE; c = p % SIZE;
	 */
	public void makeMove(int srcPos, int destPos) {  // does not check legality
//		System.out.println("MakeMove: " + moveToString(srcPos, destPos));
		int[] piecePos = (currentPlayer == SQUARE) ? piecePositions[0] : piecePositions[1];
		if (srcPos == -1) {
			if (destPos != -1) { // placement, not pass
				int pieceNumber = turnsTaken / 2;
				score[(currentPlayer == SQUARE) ? 0 : 1] += grid[destPos];
				grid[destPos] = currentPlayer;
				scoredGrid[destPos] = currentPlayer;
				piecePos[pieceNumber] = destPos;
			}
		}
		else {
			int pieceIndex = 0;
			while (pieceIndex < piecePos.length && piecePos[pieceIndex] != srcPos)
				pieceIndex++;
			if (pieceIndex == piecePos.length) { // no piece to move at srcPos; (not checking that whole move is legal here)
				System.err.println("Illegal move - no current player piece at source position " + srcPos);
				throw new RuntimeException("Illegal move - no current player piece at source position " + srcPos);
			}
			piecePos[pieceIndex] = destPos;
			score[(currentPlayer == SQUARE) ? 0 : 1] += grid[destPos];
			grid[destPos] = currentPlayer;
			scoredGrid[destPos] = currentPlayer;
			grid[srcPos] = BLOCKED;
		}
		currentPlayer = (currentPlayer == SQUARE) ? CIRCLE : SQUARE;
		turnsTaken++;
	}
	
	public void unmakeMove(int srcPos, int destPos) {  // does not check legality
		turnsTaken--;
		currentPlayer = (currentPlayer == SQUARE) ? CIRCLE : SQUARE;
		int[] piecePos = (currentPlayer == SQUARE) ? piecePositions[0] : piecePositions[1];
		if (srcPos == -1) {
			if (destPos != -1) { // placement, not pass
				int pieceNumber = turnsTaken / 2;
				score[(currentPlayer == SQUARE) ? 0 : 1] -= initGrid[destPos];
				grid[destPos] = initGrid[destPos];
				scoredGrid[destPos] = 0; 
				piecePos[pieceNumber] = 0;
			}
		}
		else {
			int pieceIndex = 0;
			while (pieceIndex < piecePos.length && piecePos[pieceIndex] != destPos)
				pieceIndex++;
			if (pieceIndex == piecePos.length) { // no piece to move at srcPos; (not checking that whole move is legal here)
				System.err.println("Illegal move - no current player piece at source position " + srcPos);
				throw new RuntimeException("Illegal move - no current player piece at source position " + srcPos);
			}
			piecePos[pieceIndex] = srcPos;
			score[(currentPlayer == SQUARE) ? 0 : 1] -= initGrid[destPos];
			grid[destPos] = initGrid[destPos];
			scoredGrid[destPos] = 0;
			grid[srcPos] = currentPlayer;
		}
	}

	/**
	 * @return whether or not the current player has a legal move.
	 */
	boolean hasLegalMove() {
		int[] piecePos = (currentPlayer == SQUARE) ? piecePositions[0] : piecePositions[1];
		for (int pos : piecePos) 
			for (int[] line : lines[pos])
				if (grid[line[0]] > 0)
					return true;
		return false;
	}
	
	/**
	 * @return whether or not the given player has a legal move.
	 */
	boolean hasLegalMove(int player) {
		int[] piecePos = (player == SQUARE) ? piecePositions[0] : piecePositions[1];
		for (int pos : piecePos) 
			for (int[] line : lines[pos])
				if (grid[line[0]] > 0)
					return true;
		return false;
	}
	
	/**
	 * Compute all legal moves.  The number of legal moves will be in legalMoveCount, and the move positions themselves will be
	 * in the 2D array legalMoves.  Array legalMoves[0] is a partially filled array filled with a legalMoveCount number of legal move source positions.
	 * Array legalMoves[1] is a partially filled array with a legalMoveCount of corresponding legal move destination positions. 
	 */
	void computeLegalMoves() { // compute legal moves for current player (amazonMoved should be false)
		legalMoveCount = 0;
		if (turnsTaken < TOTAL_PIECES) {
			// compute legal placements for first moves
			for (int pos = 0; pos < NUM_POS; pos++)
				if (grid[pos] > 0) {
					legalMoves[0][legalMoveCount] = -1;
					legalMoves[1][legalMoveCount++] = pos;
				}
			return;
		}
		int[] piecePos = (currentPlayer == SQUARE) ? piecePositions[0] : piecePositions[1];
		for (int srcPos : piecePos)
			for (int[] line : lines[srcPos]) {
				int i = 0;
				while (i < line.length && grid[line[i]] > 0) {
					legalMoves[0][legalMoveCount] = srcPos;
					legalMoves[1][legalMoveCount++] = line[i++]; // empty destination position along line of empty positions
				}
			}
	}	

	/**
	 * Convert a position row and column to a String in Chess notation.
	 * @param row - position row.
	 * @param col - position column.
	 * @return String representation of position.
	 */
	String rowColToString(int row, int col) {
		return String.format("%s%d", "" + (char) ('a' + col), row + 1);
	}
	
	/**
	 * Convert a position to a String in Chess notation.
	 * @param pos - a position is encoded in zero-based row-major form, i.e. for row r and column c on a SIZE-by-SIZE board, the position p is
	 * (r * SIZE + c).  For position p, r = p / SIZE; c = p % SIZE;
	 * @return String representation of position.
	 */
	String posToString(int pos) {
		return rowColToString(pos / SIZE, pos % SIZE);
	}
	
	
	/**
	 * Convert an entire turn move to a String in Chess notation.
	 * @param srcPos - piece source position
	 * @param destPos - piece destination position
	 * Each position is encoded in zero-based row-major form, i.e. for row r and column c on a SIZE-by-SIZE board, the position p is
	 * (r * SIZE + c).  For position p, r = p / SIZE; c = p % SIZE;
	 * @return String representation of move.
	 */
	String moveToString(int srcPos, int destPos) {
		if (srcPos == -1)
			return (destPos == -1) ? "pass" : posToString(destPos);
		return String.format("%s - %s", posToString(srcPos), posToString(destPos));
	}
	
//	void printEval() {
//		computeBoardDistances();
//		System.out.println("SQUARE Distances");
//		for (int row = SIZE - 1; row >= 0; row--) {
//			for (int col = 0; col < SIZE; col++) {
//				if (gridDistance[0][row * SIZE + col] == UNREACHABLE)
//					System.out.print("-- ");
//				else
//					System.out.printf("%2d ", gridDistance[0][row * SIZE + col]);
//			}
//			System.out.println();
//		}
//		System.out.println("CIRCLE Distances");
//		for (int row = SIZE - 1; row >= 0; row--) {
//			for (int col = 0; col < SIZE; col++) {
//				if (gridDistance[1][row * SIZE + col] == UNREACHABLE)
//					System.out.print("-- ");
//				else
//					System.out.printf("%2d ", gridDistance[1][row * SIZE + col]);
//			}
//			System.out.println();
//		}
//	}
	
	/**
	 * Compute board evaluation according to Lorentz's scheme.  All values less than UNREACHABLE, indicate the minimum 
	 * number of moves it would take a player to reach the position. To go beyond Lorentz's scheme and award ties to the 
	 * current player, set the flag DISTANCE_TIE_GOES_TO_CURRENT_PLAYER to true.

	 * @param currentPlayer - current player
	 * @param grid - contents of board positions
	 * @return simple board evaluation according to Lorentz's scheme 
	 */
	int simpleEval() {
		computeBoardDistances();
		int count = score[0] - score[1];
		for (int i = 0; i < NUM_POS; i++)
			if (gridDistance[0][i] != UNREACHABLE || gridDistance[1][i] != UNREACHABLE) // reachable by some player
				if (gridDistance[0][i] != 0 && gridDistance[1][i] != 0) { // not currently occupied by a player
					if (gridDistance[0][i] == gridDistance[1][i]) { // same distance
						if (DISTANCE_TIE_GOES_TO_CURRENT_PLAYER)
							count += (currentPlayer == SQUARE) ? 1 : -1; // if Amazon already moved, tie goes to next player.
					}
					else
						count += (gridDistance[0][i] < gridDistance[1][i]) ? grid[i] : -grid[i];
				}
		return (currentPlayer == SQUARE) ? count : -count;
	}
	
	/**
	 * Compute board distances from each player.  All values less than UNREACHABLE, indicate the minimum 
	 * number of moves it would take a player to reach the position.
	 * 
	 * @param grid - contents of board positions
	 */
	void computeBoardDistances() {
		int squareSearchQueueCount = 0, circleSearchQueueCount = 0;
		int squareSearchQueueIndex = 0, circleSearchQueueIndex = 0;
		for (int i = 0; i < NUM_POS; i++) {
			gridDistance[0][i] = UNREACHABLE;
			gridDistance[1][i] = UNREACHABLE;
			if (grid[i] == SQUARE) {
				gridDistance[0][i] = 0;
				squareSearchQueue[squareSearchQueueCount++] = i;
			}
			else if (grid[i] == CIRCLE) {
				gridDistance[1][i] = 0;
				circleSearchQueue[circleSearchQueueCount++] = i;
			}
		}
		// Propagate SQUARE moves via breadth first search
		while (squareSearchQueueIndex < squareSearchQueueCount) {
			int pos = squareSearchQueue[squareSearchQueueIndex++];
			int nextDist = gridDistance[0][pos] + 1;
			for (int[] line : lines[pos]) {
				for (int i = 0; i < line.length && grid[line[i]] > 0; i++)
					if (gridDistance[0][line[i]] > nextDist) {
						gridDistance[0][line[i]] = nextDist;
						squareSearchQueue[squareSearchQueueCount++] = line[i];
					}
			}
		}
		// Propagate CIRCLE moves via breadth first search
		while (circleSearchQueueIndex < circleSearchQueueCount) {
			int pos = circleSearchQueue[circleSearchQueueIndex++];
			int nextDist = gridDistance[1][pos] + 1;
			for (int[] line : lines[pos]) {
				for (int i = 0; i < line.length && grid[line[i]] > 0; i++)
					if (gridDistance[1][line[i]] > nextDist) {
						gridDistance[1][line[i]] = nextDist;
						circleSearchQueue[circleSearchQueueCount++] = line[i];
					}
			}
		}		
	}
	
	/**
	 * getPlay - get the chosen play
	 * @param millisRemaining - player decision-making milliseconds remaining in the game. 
	 * @return an int array of length 3 containing the Amazon source position, Amazon destination position, and Amazon shot position.
	 * Each position is encoded in zero-based row-major form, i.e. for row r and column c on a SIZE-by-SIZE board, the position p is
	 * (r * SIZE + c).  For position p, r = p / SIZE; c = p % SIZE;
	 */
	public int[] getPlay(long millisRemaining) {
		return getRandomPlay();
	}
	
	/**
	 * @return a random play, an int array of length 3 containing the Amazon source position, Amazon destination position, and Amazon shot position.
	 * Each position is encoded in zero-based row-major form, i.e. for row r and column c on a SIZE-by-SIZE board, the position p is
	 * (r * SIZE + c).  For position p, r = p / SIZE; c = p % SIZE;
	 */
	public int[] getRandomPlay() {
		// get random move
		computeLegalMoves();
		int[] play = {-1, -1}; // "pass" move only applicable if no legal moves are possible.
		if (legalMoveCount > 0) {
			int moveNum = random.nextInt(legalMoveCount);
			play[0] = legalMoves[0][moveNum];
			play[1] = legalMoves[1][moveNum];
		}
		return play;
	}
	
	/**
	 * @return the winner of the game (SQUARE or CIRCLE), or BLOCKED if the game has not ended.
	 */
	public int getWinner() {
		if (hasLegalMove(SQUARE) || hasLegalMove(CIRCLE) || score[0] == score[1])
			return 0; // denotes no winner (draw at game end)
		return (score[0] > score[1]) ? SQUARE : CIRCLE;
	}
	
	/**
	 * @return a String representation of the board.
	 */
	public String boardToString() {
		StringBuilder sb = new StringBuilder("   ");
		for (int col = 0; col < SIZE; col++)
			sb.append(" " + (char) ('a' + col));
		sb.append("\n");
		for (int row = SIZE - 1; row >= 0; row --) {
			sb.append(String.format("%2d ", row + 1));
			for (int col = 0; col < SIZE; col++) {
				char symbol = '?';
				int contents = get(row, col);
				if (contents == BLOCKED)
					symbol = BLOCKED_CHAR;
				else if (contents == SQUARE)
					symbol = SQUARE_CHAR;
				else if (contents == CIRCLE)
					symbol = CIRCLE_CHAR;
				else
					symbol = String.valueOf(contents).charAt(0);
				sb.append(" " + symbol);
			}
			sb.append(String.format(" %2d\n", row + 1));
		}
		sb.append("   ");
		for (int col = 0; col < SIZE; col++)
			sb.append(" " + (char) ('a' + col));
		sb.append("\n");
		sb.append(String.format("%s Score: %2d\n%s Score: %2d\n", SQUARE_CHAR, score[0], CIRCLE_CHAR, score[1]));
		sb.append(String.format("%s to play.\n", currentPlayer == SQUARE ? SQUARE_CHAR : CIRCLE_CHAR));
		sb.append(String.format("Eval = %d\n", simpleEval()));
		return sb.toString();
	}

	@Override
	/**
	 * getName - get the name of the player
	 * @return the name of the player
	 */
	public String getName() {
		return "RandomPlayer";
	}
	
	/**
	 * @return current player (SQUARE or CIRCLE)
	 */
	public int getPlayer() {
		return currentPlayer;
	}
	
	/**
	 * @return whether or not both players have exhausted legal moves
	 */
	public boolean gameOver() {
		return turnsTaken >= TOTAL_PIECES && !(hasLegalMove(SQUARE) || hasLegalMove(CIRCLE));
	}

	public static void main(String[] args) {
		// Random game demo:
		PPState state = new PPState();
		state.init(0);
		while (!state.gameOver()) {
			System.out.println(state.boardToString());
			int[] play = state.getPlay(100000);
			System.out.println(state.moveToString(play[0], play[1]));
			state.makeMove(play[0], play[1]);
		}
		System.out.println(state.boardToString());
		if (state.getWinner() == 0)
			System.out.println("Draw.");
		else
			System.out.printf("%s wins.", state.getWinner() == SQUARE ? SQUARE_CHAR : CIRCLE_CHAR);
	}
}
