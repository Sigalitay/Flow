import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlowMain {

    public static void main(String args[]) throws IOException {
        int r = 7;
        int c = 7;

        File outFile = new File(args[0]);
        File debugFile = new File(args[1]);
        FileWriter out = new FileWriter(outFile);
        FileWriter debug = new FileWriter(debugFile);
        int[][] matrix = new int[r + 2][c + 2];
        initPuzzle(matrix, r, c);

        int numColors = 0;
        Point[] directions = new Point[4];
        directions[0] = new Point(-1, 0);//left
        directions[1] = new Point(0, -1);//up
        directions[2] = new Point(1, 0);//right
        directions[3] = new Point(0, 1);//down

        while (availSquares(matrix) >= 3) {
            int tempMatrix[][] = new int[r + 2][c + 2];
            copyOver(matrix, tempMatrix);
            numColors++;
            //debug.write("Starting new flow with color number : " + numColors + "\n");
            Point start = new Point((int) (Math.random() * (r + 1)) + 1, (int) (Math.random() * (c + 1)) + 1);
            while (matrix[start.row][start.col] != 0) {// visited square
                start.row = (int) (Math.random() * (r + 1)) + 1;
                start.col = (int) (Math.random() * (c + 1)) + 1;
            }
            tempMatrix[start.row][start.col] = numColors;
            int flowLen = 1;
            Point currPoint = new Point(start.row, start.col);
            Point nextPoint = new Point(start.row, start.col);
            int chance = 100;
            int avail = availSquares(matrix) - 1;

            //debug.write("Beginning to generate flow with starting point: " + start.row + ", " + start.col + ". Starting with " + avail + " available squares\n");
            while ((int)(Math.random() * 100) + 1 < chance && avail > flowLen) {
                int dir = (int) (Math.random() * 4);
                nextPoint.row = currPoint.row + directions[dir].row;
                nextPoint.col = currPoint.col + directions[dir].col;
                int rotate = 0;
                //debug.write("Direction chosen is: " + dir + ". Next point is " + nextPoint.row + " " + nextPoint.col + "\n");
                while (!validDir(tempMatrix, numColors, nextPoint, currPoint, directions, debug)
                        && rotate != 3) {
                    rotate++;
                    nextPoint.row = start.row + directions[(dir + rotate) % 4].row;
                    nextPoint.col = start.col + directions[(dir + rotate) % 4].col;
                    //debug.write("Next point is invalid so choosing new direction: " + ((dir + rotate) % 4) + ". Next Point is "+ nextPoint.row + " " + nextPoint.col + "\n");
                }
                if (!validDir(tempMatrix, numColors, nextPoint, currPoint, directions, debug)
                        && rotate == 3) {// no valid path
                    //debug.write("All directions are invalid\n");
                    if(flowLen < 3)//erase path
                    {
                        //debug.write("Path is too small to write\n");
                    }
                    break;
                } else {
                    flowLen++;
                    tempMatrix[nextPoint.row][nextPoint.col] = numColors;
                    currPoint.row = nextPoint.row;
                    currPoint.col = nextPoint.col;
                    //debug.write("Successful progression found; writing to temp matrix with new point: " + nextPoint.row + " " + nextPoint.col + ", Flow len is now " + flowLen + "\n");
                }
                chance -= 5;
            }
            if (flowLen < 3) {
                numColors--;
            } else {
                copyOver(tempMatrix, matrix);
                findDeadSpace(matrix);
                printPuzzle(matrix, out);
            }

        }

        debug.close();
        out.close();
    }

    static boolean validDir(int[][] matrix, int currColor, Point newPt, Point currPoint, Point[] directions, FileWriter debug)  throws  IOException {
        if(matrix[newPt.row][newPt.col] != 0)//boundary or other flow
        {
            //debug.write("The new point is hitting a boundary or an existing flow: " + matrix[newPt.row][newPt.col] + "\n");
            return false;
        }
        else { //line right next to same flow
            for(int x = 0; x < directions.length; x++) {
                Point adjPoint = new Point(newPt.row + directions[x].row, newPt.col + directions[x].col);
                if(matrix[adjPoint.row][adjPoint.col] == currColor && !currPoint.equals(adjPoint))
                {
                    //debug.write("The new point is adjacent to current flow at: " + adjPoint.row + " " + adjPoint.col + "\n");
                    return false;
                }
            }
        }
        return true;

    }

    static void copyOver(int[][] input, int[][] copy) {
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                copy[i][j] = input[i][j];
            }
        }
    }

    static class Point {
        int row, col;

        public Point(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public boolean equals(Point compare) {
            return row == compare.row && col == compare.col;
        }
    }

    public static void initPuzzle(int[][] matrix, int r, int c) {
        for (int i = 0; i <= r + 1; i++) {
            for (int j = 0; j <= c + 1; j++) {
                if (i == 0 || j == 0 || i == r + 1 || j == c + 1) {
                    matrix[i][j] = -1;
                } else
                    matrix[i][j] = 0;
            }
        }
    }

    public static void printPuzzle(int[][] matrix, FileWriter outFile)  throws  IOException{
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                outFile.write("\t" + matrix[i][j]); // corrected print statement
            }
            outFile.write("\n");
        }
        outFile.write("\n");
    }

    public static int availSquares(int[][] matrix) {
        int total = 0;
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                if (matrix[i][j] == 0)// empty square -> not complete
                    total++;
        return total;
    }

    public static void findDeadSpace(int[][] matrix) {
        for (int i = 1; i < matrix.length; i++)
            for (int j = 1; j < matrix[i].length; j++)
                if (matrix[i][j] == 0)// empty square -> not complete
                {
                    ArrayList<Point> checkForZero = searchNeighborhood(matrix, i, j);
                    if(checkForZero.size() == 1) {
                        Point adjZero = checkForZero.get(0);
                        ArrayList<Point> checkForMoreZero = searchNeighborhood(matrix, adjZero.row, adjZero.col);
                        if(checkForMoreZero.size() == 1)
                        {
                            matrix[i][j] = -1;
                            matrix[adjZero.row][adjZero.col] = -1;
                        }
                    }
                    else if(checkForZero.size() == 0) {
                        matrix[i][j] = -1;
                    }
                }
    }

    public static ArrayList<Point> searchNeighborhood(int[][] matrix, int i, int j) {
        ArrayList<Point> points = new ArrayList<>();
        for(int r = i - 1; r <= i + 1; r++)
            for(int c = j - 1; c <= j + 1; c++)
            {
                if(matrix[r][c] == 0 && !(r == i && c == j))
                {
                     points.add(new Point(r, c));
                }
            }
        return points;
    }
}
