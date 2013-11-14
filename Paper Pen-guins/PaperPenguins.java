import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;


public class PaperPenguins {
	
	int size;
	long seed;
	int[][] grid;

	public PaperPenguins(int size) {
		this(size, System.currentTimeMillis());
	}
	
	public PaperPenguins(int size, long seed) {
		this.size = size;
		this.seed = seed;
		int threes = (int) Math.round(size * size / 6.0);
		int twos = (int) Math.round(size * size / 3.0);
		int[] numbers = new int[size * size];
		int index = 0;
		while (index < threes)
			numbers[index++] = 3;
		while (index < threes + twos)
			numbers[index++] = 2;
		while (index < size * size)
			numbers[index++] = 1;
		Random random = new Random(seed);
		for (int i = 0; i < size * size; i++) {
			int j = random.nextInt(size * size);
			int temp = numbers[i];
			numbers[i] = numbers[j];
			numbers[j] = temp;
		}
		grid = new int[size][size];
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				grid[i][j] = numbers[i * size + j];
	}	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++)
				sb.append(" " + grid[i][j] + " ");
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public void toFile() {
		try {

			// Write SVG file
			String filename = String.format("pp-size%d-seed%d.svg", size, seed);
			PrintWriter out = new PrintWriter(new FileWriter(filename));
			int cellWidth = 25, cellHeight = cellWidth, width = size * cellWidth, height = size * cellHeight;
			int margin = 10 + cellWidth;
			int cellMargin = (int) Math.round(Math.min(cellWidth, cellHeight) / 10.0);
			int totalWidth = width + 2 * margin, totalHeight = height + 2 * cellHeight + 2 * margin;
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
					String text = String.valueOf(grid[size - r - 1][c]);;
					out.println(String.format("<text x=\"%f\" y=\"%d\" style=\"fill: black; stroke: none\">%s</text>", ulx + cwhalf + .5, uly + cellHeight - ch3rd + 1, text));
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
			out.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" style=\"stroke:blue;stroke-width:1;fill:blue;fill-opacity:0;\"/>\n", 
					ulx + cellMargin, uly + cellMargin, cellWidth - 2 * cellMargin, cellHeight - 2 * cellMargin);
			// bottom circle
			uly = margin + (size + 2) * cellHeight;
			out.printf("<circle cx=\"%f\" cy=\"%f\" r=\"%f\" style=\"stroke:red;stroke-width:1;fill:red;fill-opacity:0;\"/>\n", 
					ulx + (cellWidth / 2) + .5, uly + (cellHeight / 2) + .5, (double) Math.min(cellWidth / 2, cellHeight / 2) - cellMargin + 1);

			out.println("</g>");
			//Figure drawing ends
			out.println("</svg>");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numGrids = 100;
		for (int i = 0; i < numGrids; i++) {
			PaperPenguins pp = new PaperPenguins(8, i);
			pp.toFile();
			System.out.println(pp);
		}
	}

}
