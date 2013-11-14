
/**
 * PPPlayer - a simple interface for server-client Paper Penguins player interaction.
 * 
 * @author Todd W Neller
 *
 */
public interface PPPlayer {

	/**
	 * init - initializes the game state to a start configuration defined by a PaperPenguins grid seed.
	 */
	void init(long seed);
	
	/**
	 * getName - get the name of the player
	 * @return the name of the player
	 */
	String getName();
	
	/**
	 * getPlay - get the chosen play
	 * @param millisRemaining - player decision-making milliseconds remaining in the game. 
	 * @return an int array of length 2 containing the penguin source position and penguin destination position.  In the case of an initial piece placement, the source position is -1.
	 * Each position is encoded in zero-based row-major form, i.e. for row r and column c on a SIZE-by-SIZE board, the position p is
	 * (r * SIZE + c).  For position p, r = p / SIZE; c = p % SIZE;
	 */
	int[] getPlay(long millisRemaining);	
	
	/**
	 * makeMove - make the given play. 
	 * @param srcPos - penguin source position. In the case of an initial piece placement, the source position is -1.
	 * @param destPos - penguin destination position
	 * Each position is encoded in zero-based row-major form, i.e. for row r and column c on a SIZE-by-SIZE board, the position p is
	 * (r * SIZE + c).  For position p, r = p / SIZE; c = p % SIZE;
	 */
	void makeMove(int srcPos, int destPos);

}
