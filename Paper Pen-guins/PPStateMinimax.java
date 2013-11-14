import java.util.ArrayList;



public class PPStateMinimax extends PPState {

	static final int maxCallDepth = 4;
	int bestSrcPos, bestDestPos;
	
	/**
	 * getName - get the name of the player
	 * @return the name of the player
	 */
	public String getName() {
		return "Neller_NEGAMAX_AB_DEPTH_" + maxCallDepth;
	}
	
	/**
	 * getPlay - get the chosen play
	 * @param millisRemaining - player decision-making milliseconds remaining in the game. 
	 * @return an int array of length 3 containing the Amazon source position, Amazon destination position, and Amazon shot position.
	 * Each position is encoded in zero-based row-major form, i.e. for row r and column c on a SIZE-by-SIZE board, the position p is
	 * (r * SIZE + c).  For position p, r = p / SIZE; c = p % SIZE;
	 */
	public int[] getPlay(long millisRemaining) {
		if (!hasLegalMove()) {
			play[0] = play[1] = -1;
			return play;
		}
//		long startTime = System.currentTimeMillis();
//		long expMovesRemaining = ((SIZE * SIZE - 8) - turnsTaken) / 2;
//		long decisionMillis = millisRemaining / expMovesRemaining;
		//  Get move
		negamax(new PPState(this), maxCallDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		play[0] = bestSrcPos;
		play[1] = bestDestPos;
		return play;
	}
	
	private double negamax(PPState state, int depthRemaining) {
		if (state.gameOver() || depthRemaining == 0)
			return state.simpleEval();
//		String prefix = "                                      ".substring(0,10-depthRemaining);
//		String player = (currentPlayer == SQUARE) ? "SQUARE" : "CIRCLE";
			state.computeLegalMoves();
			int numLegalMoves = state.legalMoveCount;
			int[] srcPositions = state.legalMoves[0].clone();
			int[] destPositions = state.legalMoves[1].clone();
			double qBest = Double.NEGATIVE_INFINITY;
			int bestMove = 0;
			for (int i = 0; i < numLegalMoves; i++) {
				int srcPos = srcPositions[i];
				int destPos = destPositions[i];
				state.makeMove(srcPos, destPos);
				double q = -negamax(state, depthRemaining - 1);
//				System.out.printf("%s%s MOVE%s%f\n", prefix, player, (q > qBest) ? "*" : " ", q);
				if (q > qBest) {
					qBest = q;
					bestMove = i;
				}
//				if (depthRemaining == 2) {
//					System.out.printf("%d-%d(%d):%f\n", srcPos, destPos, this.bestShotPos, q);
//				}
				state.unmakeMove(srcPos, destPos);
			}
			bestSrcPos = srcPositions[bestMove];
			bestDestPos = destPositions[bestMove];
//			System.out.printf("%sMAX: %f\n", prefix, qBest);
			return qBest;
		
	}
	

	/**
	 * @param state PP state of search
	 * @param depthRemaining search depth remaining until evaluation cut-off
	 * @param atLeast play through this state should score at least this amount (or prune)
	 * @param atMost play through this state should score at most this amount (or prune)
	 * @return
	 */
	private double negamax(PPState state, int depthRemaining, double atLeast, double atMost) {
		if (state.gameOver() || depthRemaining == 0)
			return state.simpleEval();
//		String prefix = "                                      ".substring(0,10-depthRemaining);
//		String player = (currentPlayer == SQUARE) ? "SQUARE" : "CIRCLE";
			state.computeLegalMoves();
			int numLegalMoves = state.legalMoveCount;
			int[] srcPositions = state.legalMoves[0].clone();
			int[] destPositions = state.legalMoves[1].clone();
			double qBest = Double.NEGATIVE_INFINITY;
			int bestMove = 0;
			for (int i = 0; i < numLegalMoves; i++) {
				int srcPos = srcPositions[i];
				int destPos = destPositions[i];
				state.makeMove(srcPos, destPos);
				double q = -negamax(state, depthRemaining - 1, -atMost, -atLeast);
				state.unmakeMove(srcPos, destPos);
//				System.out.printf("%s%s MOVE%s%f\n", prefix, player, (q > qBest) ? "*" : " ", q);
				if (q > qBest) {
					qBest = q;
					bestMove = i;
					if (q > atLeast)
						atLeast = q;
					if (atLeast > atMost) break; // pruning condition
				}
//				if (depthRemaining == 2) {
//					System.out.printf("%d-%d(%d):%f\n", srcPos, destPos, this.bestShotPos, q);
//				}
			}
			bestSrcPos = srcPositions[bestMove];
			
			bestDestPos = destPositions[bestMove];
//			System.out.printf("%sMAX: %f\n", prefix, qBest);
			return qBest;
		
	}

//	int[] playoutPieceScoreVector = new int[TOTAL_PIECES];
//	public double[] randomPlayoutBoardEval(PPState state, int numPlayouts) {
//		double[] posValues = new double[NUM_POS];
//		int[] posCounts = new int[NUM_POS];
//		if (state.gameOver())
//			return posValues;
//		for (int p = 0; p < numPlayouts; p++) {
//			for (int i = 0; i < TOTAL_PIECES; i++)
//				playoutPieceScoreVector[i] = 0;
//			randomBoardEvalPlayout(state);
//		}
//		for (int i = 0; i < NUM_POS; i++)
//			posValues[i] /= posCounts[i];
//		return posValues;
//		
//	}
//	
	
	
	
	
	public static void main(String[] args) {
		// Minimax game demo:

		boolean exporting = false;

		PPStateMinimax state = new PPStateMinimax();	
		long gameMillis = 1000000;
		long startMillis = System.currentTimeMillis();
		state.init(0);
		int[] play = null;
		while (!state.gameOver()) {
			if (exporting) {
				ArrayList<Integer> highlights = new ArrayList<Integer>();
				if (play != null) {
					if (play[0] >= 0)
						highlights.add(play[0]);
					highlights.add(play[1]);
				}
				PPView.exportSVG("PPFig" + state.turnsTaken, state, highlights);
			}
			System.out.println(state.boardToString());
			play = state.getPlay((gameMillis - (System.currentTimeMillis() - startMillis)) / 2);
			System.out.println(state.moveToString(play[0], play[1]));
			System.out.println();
			state.makeMove(play[0], play[1]);
		}
		System.out.println(state.boardToString());
		if (state.getWinner() == 0)
			System.out.println("Draw.");
		else
			System.out.printf("%s wins.", state.getWinner() == SQUARE ? SQUARE_CHAR : CIRCLE_CHAR);
		if (exporting) {
			ArrayList<Integer> highlights = new ArrayList<Integer>();
			if (play != null) {
				if (play[0] >= 0)
					highlights.add(play[0]);
				highlights.add(play[1]);
			}
			PPView.exportSVG("PPFigEnd", state, highlights);
		}
		// depth 3:
		// without alpha-beta pruning:  68.793 sec
		//    with alpha-beta pruning:  11.709 sec
		// depth 4:
		// without alpha-beta pruning:  too long (over 5.5min for first move)
		//    with alpha-beta pruning:  174.074 sec
		
		/* Depth 4 AB Negamax with simple eval:

$ time java PPStateMinimax
    a b c d e f g h
 8  2 3 2 3 2 2 3 1  8
 7  1 1 3 2 1 3 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 3 2 3 1 3 2  3
 2  2 2 2 1 2 1 1 1  2
 1  3 2 2 1 1 1 1 3  1
    a b c d e f g h
X Score:  0
@ Score:  0
X to play.
Eval = 0

c3

    a b c d e f g h
 8  2 3 2 3 2 2 3 1  8
 7  1 1 3 2 1 3 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 3 1 3 2  3
 2  2 2 2 1 2 1 1 1  2
 1  3 2 2 1 1 1 1 3  1
    a b c d e f g h
X Score:  3
@ Score:  0
@ to play.
Eval = -107

f3

    a b c d e f g h
 8  2 3 2 3 2 2 3 1  8
 7  1 1 3 2 1 3 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 3 @ 3 2  3
 2  2 2 2 1 2 1 1 1  2
 1  3 2 2 1 1 1 1 3  1
    a b c d e f g h
X Score:  3
@ Score:  1
X to play.
Eval = -3

f7

    a b c d e f g h
 8  2 3 2 3 2 2 3 1  8
 7  1 1 3 2 1 X 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 3 @ 3 2  3
 2  2 2 2 1 2 1 1 1  2
 1  3 2 2 1 1 1 1 3  1
    a b c d e f g h
X Score:  6
@ Score:  1
@ to play.
Eval = -32

b1

    a b c d e f g h
 8  2 3 2 3 2 2 3 1  8
 7  1 1 3 2 1 X 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 3 @ 3 2  3
 2  2 2 2 1 2 1 1 1  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score:  6
@ Score:  3
X to play.
Eval = 5

h2

    a b c d e f g h
 8  2 3 2 3 2 2 3 1  8
 7  1 1 3 2 1 X 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 3 @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score:  7
@ Score:  3
@ to play.
Eval = -26

d8

    a b c d e f g h
 8  2 3 2 @ 2 2 3 1  8
 7  1 1 3 2 1 X 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 3 @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score:  7
@ Score:  6
X to play.
Eval = 1

c8

    a b c d e f g h
 8  2 3 X @ 2 2 3 1  8
 7  1 1 3 2 1 X 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 3 @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score:  9
@ Score:  6
@ to play.
Eval = -8

e3

    a b c d e f g h
 8  2 3 X @ 2 2 3 1  8
 7  1 1 3 2 1 X 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 @ @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score:  9
@ Score:  9
X to play.
Eval = -1

c8 - c7

    a b c d e f g h
 8  2 3 # @ 2 2 3 1  8
 7  1 1 X 2 1 X 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 @ @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 12
@ Score:  9
@ to play.
Eval = 1

d8 - g8

    a b c d e f g h
 8  2 3 # # 2 2 @ 1  8
 7  1 1 X 2 1 X 1 2  7
 6  2 1 1 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 @ @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 12
@ Score: 12
X to play.
Eval = -1

c7 - c6

    a b c d e f g h
 8  2 3 # # 2 2 @ 1  8
 7  1 1 # 2 1 X 1 2  7
 6  2 1 X 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 @ @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 13
@ Score: 12
@ to play.
Eval = -5

g8 - g7

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 1 # 2 1 X @ 2  7
 6  2 1 X 1 1 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 @ @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 13
@ Score: 13
X to play.
Eval = 4

f7 - e6

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 1 # 2 1 # @ 2  7
 6  2 1 X 1 X 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 @ @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 14
@ Score: 13
@ to play.
Eval = -7

e3 - b6

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 1 # 2 1 # @ 2  7
 6  2 @ X 1 X 2 2 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 # @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 14
@ Score: 14
X to play.
Eval = 2

e6 - g6

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 1 # 2 1 # @ 2  7
 6  2 @ X 1 # 2 X 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 # @ 3 2  3
 2  2 2 2 1 2 1 1 X  2
 1  3 @ 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 16
@ Score: 14
@ to play.
Eval = -5

b1 - b2

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 1 # 2 1 # @ 2  7
 6  2 @ X 1 # 2 X 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 2 X 2 # @ 3 2  3
 2  2 @ 2 1 2 1 1 X  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 16
@ Score: 16
X to play.
Eval = 0

c3 - b3

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 1 # 2 1 # @ 2  7
 6  2 @ X 1 # 2 X 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 X # 2 # @ 3 2  3
 2  2 @ 2 1 2 1 1 X  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 18
@ Score: 16
@ to play.
Eval = 0

b6 - b7

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 @ # 2 1 # @ 2  7
 6  2 # X 1 # 2 X 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  1 X # 2 # @ 3 2  3
 2  2 @ 2 1 2 1 1 X  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 18
@ Score: 17
X to play.
Eval = -1

b3 - a3

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 @ # 2 1 # @ 2  7
 6  2 # X 1 # 2 X 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # @ 3 2  3
 2  2 @ 2 1 2 1 1 X  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 19
@ Score: 17
@ to play.
Eval = -11

b7 - a6

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 1 # @ 2  7
 6  @ # X 1 # 2 X 2  6
 5  1 1 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # @ 3 2  3
 2  2 @ 2 1 2 1 1 X  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 19
@ Score: 19
X to play.
Eval = 0

c6 - b5

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 1 # @ 2  7
 6  @ # # 1 # 2 X 2  6
 5  1 X 1 1 1 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # @ 3 2  3
 2  2 @ 2 1 2 1 1 X  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 20
@ Score: 19
@ to play.
Eval = -1

g7 - e5

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 1 # # 2  7
 6  @ # # 1 # 2 X 2  6
 5  1 X 1 1 @ 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # @ 3 2  3
 2  2 @ 2 1 2 1 1 X  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 20
@ Score: 20
X to play.
Eval = 6

b5 - d5

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 1 # # 2  7
 6  @ # # 1 # 2 X 2  6
 5  1 # 1 X @ 2 1 3  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # @ 3 2  3
 2  2 @ 2 1 2 1 1 X  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 21
@ Score: 20
@ to play.
Eval = -11

e5 - g5

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 1 # # 2  7
 6  @ # # 1 # 2 X 2  6
 5  1 # 1 X # 2 @ 3  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # @ 3 2  3
 2  2 @ 2 1 2 1 1 X  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 21
@ Score: 21
X to play.
Eval = 4

h2 - e2

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 1 # # 2  7
 6  @ # # 1 # 2 X 2  6
 5  1 # 1 X # 2 @ 3  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # @ 3 2  3
 2  2 @ 2 1 X 1 1 #  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 23
@ Score: 21
@ to play.
Eval = 4

g5 - e7

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 X 2  6
 5  1 # 1 X # 2 # 3  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # @ 3 2  3
 2  2 @ 2 1 X 1 1 #  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 23
@ Score: 22
X to play.
Eval = -12

e2 - g2

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 X 2  6
 5  1 # 1 X # 2 # 3  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # @ 3 2  3
 2  2 @ 2 1 # 1 X #  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 24
@ Score: 22
@ to play.
Eval = -1

f3 - g3

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 X 2  6
 5  1 # 1 X # 2 # 3  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # # @ 2  3
 2  2 @ 2 1 # 1 X #  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 24
@ Score: 25
X to play.
Eval = 2

g6 - h5

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 X # 2 # X  5
 4  1 1 1 1 1 2 1 1  4
 3  X # # 2 # # @ 2  3
 2  2 @ 2 1 # 1 X #  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 27
@ Score: 25
@ to play.
Eval = 1

g3 - g4

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 X # 2 # X  5
 4  1 1 1 1 1 2 @ 1  4
 3  X # # 2 # # # 2  3
 2  2 @ 2 1 # 1 X #  2
 1  3 # 2 1 1 1 1 3  1
    a b c d e f g h
X Score: 27
@ Score: 26
X to play.
Eval = -5

g2 - h1

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 X # 2 # X  5
 4  1 1 1 1 1 2 @ 1  4
 3  X # # 2 # # # 2  3
 2  2 @ 2 1 # 1 # #  2
 1  3 # 2 1 1 1 1 X  1
    a b c d e f g h
X Score: 30
@ Score: 26
@ to play.
Eval = 2

g4 - e4

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 X # 2 # X  5
 4  1 1 1 1 @ 2 # 1  4
 3  X # # 2 # # # 2  3
 2  2 @ 2 1 # 1 # #  2
 1  3 # 2 1 1 1 1 X  1
    a b c d e f g h
X Score: 30
@ Score: 27
X to play.
Eval = -2

d5 - d3

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 1 1 1 @ 2 # 1  4
 3  X # # X # # # 2  3
 2  2 @ 2 1 # 1 # #  2
 1  3 # 2 1 1 1 1 X  1
    a b c d e f g h
X Score: 32
@ Score: 27
@ to play.
Eval = 0

e4 - d4

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 1 1 @ # 2 # 1  4
 3  X # # X # # # 2  3
 2  2 @ 2 1 # 1 # #  2
 1  3 # 2 1 1 1 1 X  1
    a b c d e f g h
X Score: 32
@ Score: 28
X to play.
Eval = -1

h1 - c1

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 1 1 @ # 2 # 1  4
 3  X # # X # # # 2  3
 2  2 @ 2 1 # 1 # #  2
 1  3 # X 1 1 1 1 #  1
    a b c d e f g h
X Score: 34
@ Score: 28
@ to play.
Eval = -1

d4 - c4

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 1 @ # # 2 # 1  4
 3  X # # X # # # 2  3
 2  2 @ 2 1 # 1 # #  2
 1  3 # X 1 1 1 1 #  1
    a b c d e f g h
X Score: 34
@ Score: 29
X to play.
Eval = 0

c1 - d1

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 1 @ # # 2 # 1  4
 3  X # # X # # # 2  3
 2  2 @ 2 1 # 1 # #  2
 1  3 # # X 1 1 1 #  1
    a b c d e f g h
X Score: 35
@ Score: 29
@ to play.
Eval = 0

c4 - b4

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 @ # # # 2 # 1  4
 3  X # # X # # # 2  3
 2  2 @ 2 1 # 1 # #  2
 1  3 # # X 1 1 1 #  1
    a b c d e f g h
X Score: 35
@ Score: 30
X to play.
Eval = -3

a3 - a2

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 @ # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  X @ 2 1 # 1 # #  2
 1  3 # # X 1 1 1 #  1
    a b c d e f g h
X Score: 37
@ Score: 30
@ to play.
Eval = 3

b2 - d2

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 @ # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  X # 2 @ # 1 # #  2
 1  3 # # X 1 1 1 #  1
    a b c d e f g h
X Score: 37
@ Score: 31
X to play.
Eval = -3

d1 - e1

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 @ # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  X # 2 @ # 1 # #  2
 1  3 # # # X 1 1 #  1
    a b c d e f g h
X Score: 38
@ Score: 31
@ to play.
Eval = 1

d2 - c2

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 @ # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  X # @ # # 1 # #  2
 1  3 # # # X 1 1 #  1
    a b c d e f g h
X Score: 38
@ Score: 33
X to play.
Eval = -3

a2 - a1

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  1 @ # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # X 1 1 #  1
    a b c d e f g h
X Score: 41
@ Score: 33
@ to play.
Eval = 3

b4 - a4

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  @ # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # X 1 1 #  1
    a b c d e f g h
X Score: 41
@ Score: 34
X to play.
Eval = -3

e1 - f1

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  1 # 1 # # 2 # X  5
 4  @ # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # # X 1 #  1
    a b c d e f g h
X Score: 42
@ Score: 34
@ to play.
Eval = 3

a4 - a5

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  @ # 1 # # 2 # X  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # # X 1 #  1
    a b c d e f g h
X Score: 42
@ Score: 35
X to play.
Eval = -3

f1 - g1

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  1 # # 2 @ # # 2  7
 6  @ # # 1 # 2 # 2  6
 5  @ # 1 # # 2 # X  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # # # X #  1
    a b c d e f g h
X Score: 43
@ Score: 35
@ to play.
Eval = 3

a6 - a7

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  @ # # 2 @ # # 2  7
 6  # # # 1 # 2 # 2  6
 5  @ # 1 # # 2 # X  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # # # X #  1
    a b c d e f g h
X Score: 43
@ Score: 36
X to play.
Eval = -3

h5 - h6

    a b c d e f g h
 8  2 3 # # 2 2 # 1  8
 7  @ # # 2 @ # # 2  7
 6  # # # 1 # 2 # X  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # # # X #  1
    a b c d e f g h
X Score: 45
@ Score: 36
@ to play.
Eval = 6

a7 - a8

    a b c d e f g h
 8  @ 3 # # 2 2 # 1  8
 7  # # # 2 @ # # 2  7
 6  # # # 1 # 2 # X  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # # # X #  1
    a b c d e f g h
X Score: 45
@ Score: 38
X to play.
Eval = -6

h6 - h7

    a b c d e f g h
 8  @ 3 # # 2 2 # 1  8
 7  # # # 2 @ # # X  7
 6  # # # 1 # 2 # #  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # # # X #  1
    a b c d e f g h
X Score: 47
@ Score: 38
@ to play.
Eval = 6

e7 - d6

    a b c d e f g h
 8  @ 3 # # 2 2 # 1  8
 7  # # # 2 # # # X  7
 6  # # # @ # 2 # #  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # # # X #  1
    a b c d e f g h
X Score: 47
@ Score: 39
X to play.
Eval = 0

h7 - h8

    a b c d e f g h
 8  @ 3 # # 2 2 # X  8
 7  # # # 2 # # # #  7
 6  # # # @ # 2 # #  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # # # X #  1
    a b c d e f g h
X Score: 48
@ Score: 39
@ to play.
Eval = 0

d6 - d7

    a b c d e f g h
 8  @ 3 # # 2 2 # X  8
 7  # # # @ # # # #  7
 6  # # # # # 2 # #  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # 1 # #  2
 1  X # # # # # X #  1
    a b c d e f g h
X Score: 48
@ Score: 41
X to play.
Eval = 1

g1 - f2

    a b c d e f g h
 8  @ 3 # # 2 2 # X  8
 7  # # # @ # # # #  7
 6  # # # # # 2 # #  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # X # #  2
 1  X # # # # # # #  1
    a b c d e f g h
X Score: 49
@ Score: 41
@ to play.
Eval = -1

d7 - e8

    a b c d e f g h
 8  @ 3 # # @ 2 # X  8
 7  # # # # # # # #  7
 6  # # # # # 2 # #  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # X # #  2
 1  X # # # # # # #  1
    a b c d e f g h
X Score: 49
@ Score: 43
X to play.
Eval = 1

pass

    a b c d e f g h
 8  @ 3 # # @ 2 # X  8
 7  # # # # # # # #  7
 6  # # # # # 2 # #  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # X # #  2
 1  X # # # # # # #  1
    a b c d e f g h
X Score: 49
@ Score: 43
@ to play.
Eval = -1

e8 - f8

    a b c d e f g h
 8  @ 3 # # # @ # X  8
 7  # # # # # # # #  7
 6  # # # # # 2 # #  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # X # #  2
 1  X # # # # # # #  1
    a b c d e f g h
X Score: 49
@ Score: 45
X to play.
Eval = 1

pass

    a b c d e f g h
 8  @ 3 # # # @ # X  8
 7  # # # # # # # #  7
 6  # # # # # 2 # #  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # X # #  2
 1  X # # # # # # #  1
    a b c d e f g h
X Score: 49
@ Score: 45
@ to play.
Eval = -1

a8 - b8

    a b c d e f g h
 8  # @ # # # @ # X  8
 7  # # # # # # # #  7
 6  # # # # # 2 # #  6
 5  @ # 1 # # 2 # #  5
 4  # # # # # 2 # 1  4
 3  # # # X # # # 2  3
 2  # # @ # # X # #  2
 1  X # # # # # # #  1
    a b c d e f g h
X Score: 49
@ Score: 48
X to play.
Eval = 1

X wins.
real    2m54.079s
user    0m0.031s
sys     0m0.000s


		 */

	}	
}
