import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;


public class PPView {

	public static double fillOpacity = .75;
	
	public static void exportSVG(String filePrefix, PPState state) {
		exportSVG(filePrefix, state, new ArrayList<Integer>());
	}
	
	public static void exportSVG(String filePrefix, PPState state, ArrayList<Integer> highlightPos) {
		int size = PPState.SIZE;
		int[] initGrid = PPState.getInitGrid();
		int[] scoredGrid = PPState.getScoredGrid();
		int[] grid = state.getGrid();
		
		try {

			// Write SVG file
			String filename = String.format("%s.svg", filePrefix);
			PrintWriter out = new PrintWriter(new FileWriter(filename));
			int cellWidth = 25, cellHeight = cellWidth, width = size * cellWidth, height = size * cellHeight;
			int margin = 10 + cellWidth;
			int cellMargin = (int) Math.round(Math.min(cellWidth, cellHeight) / 10.0);
			int totalWidth = width + 2 * margin, totalHeight = height + cellHeight + 2 * margin;
			int ch3rd = cellHeight / 3, cwhalf = cellWidth / 2;
			String[] frontmatter = {
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
					"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1 Basic//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11-basic.dtd\">",
					"<svg version=\"1.1\" baseProfile=\"basic\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" id=\"svg-root\" width=\"" + totalWidth + "\" height=\"" + totalHeight + "\">", 
					"<title>" + filename + "</title>",
					"<desc>Paper Penguins " + filename + "</desc>"};
			for (String s : frontmatter)
				out.println(s);
			//Figure drawing begins
			out.println("<g style=\"fill: none; stroke: black; font-family: sans-serif; font-size: 12pt; text-anchor: middle\">");
			
			// yellow cell highlighting
			for (int pos : highlightPos) {
				int row = pos / size;
				int col = pos % size;
				int ulx = margin + col * cellWidth, uly = margin + (size - row - 1) * cellHeight;
				out.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" style=\"fill:yellow;\"/>\n", 
						ulx, uly, cellWidth, cellHeight);
			}
			//outer wall
			out.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" style=\"stroke-width:3;\"/>\n", margin - 1, margin - 1, width + 2, height + 2);
//			out.println(String.format("<polyline points=\"%d %d, %d %d, %d %d, %d %d, %d %d, %d, %d\" />",
//					margin, margin, margin + width, margin, margin + width, margin + height, margin, margin + height, margin, margin, margin + width, margin));
			//vertical walls
			for (int i = 0; i <= size; i++)
				out.println(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" />", margin + i * cellWidth, margin, margin + i * cellWidth, margin + height));	
			//horizontal walls
			for (int i = 0; i <= size; i++)
				out.println(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" />", margin, margin + i * cellHeight, margin + width, margin + i * cellHeight));	
			//cell numbers
			for (int r = 0; r < size; r++)
				for (int c = 0; c < size; c++) {
					int ulx = margin + c * cellWidth, uly = margin + r * cellHeight;
					//text
					String text = String.valueOf(initGrid[(size - r - 1) * size + c]);;
					out.println(String.format("<text x=\"%f\" y=\"%d\" style=\"fill: black; stroke: none\">%s</text>", ulx + cwhalf + .5, uly + cellHeight - ch3rd + 1, text));
				}
			//cell squares, circles, and x's
			for (int r = 0; r < size; r++)
				for (int c = 0; c < size; c++) {
					if (scoredGrid[(size - r - 1) * size + c] == PPState.SQUARE) {
						int ulx = margin + c * cellWidth, uly = margin + r * cellHeight;
						if (grid[(size - r - 1) * size + c] == PPState.BLOCKED) {
							out.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" style=\"stroke:blue;stroke-width:1;\"/>\n", 
									ulx + cellMargin, uly + cellMargin, cellWidth - 2 * cellMargin, cellHeight - 2 * cellMargin);
							out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" style=\"stroke:blue;stroke-width:1;\"/>\n", ulx + 1, uly + 1, ulx + cellWidth - 1, uly + cellWidth - 1);
							out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" style=\"stroke:blue;stroke-width:1;\"/>\n", ulx + cellWidth - 1, uly + 1, ulx + 1, uly + cellWidth - 1);
						}
						else
							out.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" style=\"stroke:blue;stroke-width:1;fill:blue;fill-opacity:" + fillOpacity + ";\"/>\n", 
									ulx + cellMargin, uly + cellMargin, cellWidth - 2 * cellMargin, cellHeight - 2 * cellMargin);
					}
					if (scoredGrid[(size - r - 1) * size + c] == PPState.CIRCLE) {
						int ulx = margin + c * cellWidth, uly = margin + r * cellHeight;
						if (grid[(size - r - 1) * size + c] == PPState.BLOCKED) {
							out.printf("<circle cx=\"%f\" cy=\"%f\" r=\"%f\" style=\"stroke:red;stroke-width:1;\"/>\n", 
									ulx + (cellWidth / 2) + .5, uly + (cellHeight / 2) + .5, (double) Math.min(cellWidth / 2, cellHeight / 2) - cellMargin + 1);
							out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" style=\"stroke:red;stroke-width:1;\"/>\n", ulx + 1, uly + 1, ulx + cellWidth - 1, uly + cellWidth - 1);
							out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" style=\"stroke:red;stroke-width:1;\"/>\n", ulx + cellWidth - 1, uly + 1, ulx + 1, uly + cellWidth - 1);
						}
						else
							out.printf("<circle cx=\"%f\" cy=\"%f\" r=\"%f\" style=\"stroke:red;stroke-width:1;fill:red;fill-opacity:" + fillOpacity + ";\"/>\n", 
									ulx + (cellWidth / 2) + .5, uly + (cellHeight / 2) + .5, (double) Math.min(cellWidth / 2, cellHeight / 2) - cellMargin + 1);
					}
				}
			//column letters
			for (int c = 0; c < size; c++) {
				int ulx = margin + c * cellWidth, uly = margin + -1 * cellHeight;
				//text
				String text = String.valueOf((char) ('a' + c));
				out.println(String.format("<text x=\"%f\" y=\"%d\" style=\"fill: black; stroke: none\">%s</text>", ulx + cwhalf + .5, uly + cellHeight - ch3rd + 1, text));
				uly = margin + size * cellHeight;
				out.println(String.format("<text x=\"%f\" y=\"%d\" style=\"fill: black; stroke: none\">%s</text>", ulx + cwhalf + .5, uly + cellHeight - ch3rd + 1, text));
			}
			//row numbers
			for (int r = 0; r < size; r++) {
				int ulx = margin + -1 * cellWidth, uly = margin + r * cellHeight;
				//text
				String text = String.valueOf(size - r);
				out.println(String.format("<text x=\"%f\" y=\"%d\" style=\"fill: black; stroke: none\">%s</text>", ulx + cwhalf + .5, uly + cellHeight - ch3rd + 1, text));
				ulx = margin + size * cellWidth;
				out.println(String.format("<text x=\"%f\" y=\"%d\" style=\"fill: black; stroke: none\">%s</text>", ulx + cwhalf + .5, uly + cellHeight - ch3rd + 1, text));
			}
			// bottom square
			int ulx = margin, uly = margin + (size + 1) * cellHeight;
			out.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" style=\"stroke:blue;stroke-width:1;fill:blue;fill-opacity:" + fillOpacity + ";\"/>\n", 
					ulx + cellMargin, uly + cellMargin, cellWidth - 2 * cellMargin, cellHeight - 2 * cellMargin);
			// bottom square score
			ulx += cellWidth;
			out.println(String.format("<text x=\"%f\" y=\"%d\" style=\"fill: blue; stroke: none\">%s</text>", 
					ulx + cwhalf + .5, uly + cellHeight - ch3rd + 1, String.valueOf(state.getScore()[0])));
			// bottom circle
			ulx = (int)(margin + 2.5 * cellWidth);
			out.printf("<circle cx=\"%f\" cy=\"%f\" r=\"%f\" style=\"stroke:red;stroke-width:1;fill:red;fill-opacity:" + fillOpacity + ";\"/>\n", 
					ulx + (cellWidth / 2) + .5, uly + (cellHeight / 2) + .5, (double) Math.min(cellWidth / 2, cellHeight / 2) - cellMargin + 1);
			// bottom circle score
			ulx += cellWidth;
			out.println(String.format("<text x=\"%f\" y=\"%d\" style=\"fill: red; stroke: none\">%s</text>", 
					ulx + cwhalf + .5, uly + cellHeight - ch3rd + 1, String.valueOf(state.getScore()[1])));
			// bottom to-play symbol
			ulx = margin + 5 * cellWidth; 
			if (state.currentPlayer == PPState.SQUARE)
				out.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" style=\"stroke:blue;stroke-width:1;fill:blue;fill-opacity:" + fillOpacity + ";\"/>\n", 
						ulx + cellMargin, uly + cellMargin, cellWidth - 2 * cellMargin, cellHeight - 2 * cellMargin);
			else
				out.printf("<circle cx=\"%f\" cy=\"%f\" r=\"%f\" style=\"stroke:red;stroke-width:1;fill:red;fill-opacity:" + fillOpacity + ";\"/>\n", 
						ulx + (cellWidth / 2) + .5, uly + (cellHeight / 2) + .5, (double) Math.min(cellWidth / 2, cellHeight / 2) - cellMargin + 1);
			// bottom to-play label
			double ulxd = ulx + 1.55 * cellWidth;
			out.println(String.format("<text x=\"%f\" y=\"%d\" style=\"fill: black; stroke: none\">to play</text>", 
					ulxd + cwhalf + .5, uly + cellHeight - ch3rd + 1));
			
			out.println("</g>");
			//Figure drawing ends
			out.println("</svg>");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
//		frame.dispose();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Random game demo:
		PPState state = new PPState();
		state.init(0);
		int[] play = null;
		for (int turn = 0; turn < 30; turn++) {
			System.out.println(state.boardToString());
			play = state.getPlay(100000);
			System.out.println(state.moveToString(play[0], play[1]));
			state.makeMove(play[0], play[1]);
		}
		System.out.println(state.boardToString());
		ArrayList<Integer> highlights = new ArrayList<Integer>();
		if (play != null) {
			if (play[0] >= 0)
				highlights.add(play[0]);
			highlights.add(play[1]);
		}
		exportSVG("randomTest30", state, highlights);

	}

}
