package lab3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.lang.Math.*;

public class Main {
	
	//array of visited cities
	static boolean visited[];
	
	//Final path length 
	static double finalPath = Integer.MAX_VALUE;
	
	//Saved path when completed
	static double savedPath[];
	
	//length used for cityArray
	static int arrayLength = 18;
	
	
	public static void main(String[] args) {
		try {
			File myObj = new File("/Users/rowanhaver/eclipse-workspace/CST3170/src/test1tsp.txt");
			Scanner fileReader =  new Scanner(myObj);

			//Change array length to file length
			int[][] cityArray = new int[arrayLength][3];
			//reads each line of the file and inserts it into a 2D array
			while (fileReader.hasNextLine()) {
				for(int city=0; city < cityArray.length; city++) {
					for(int coord=0; coord < cityArray[city].length; coord++) {
						int data = fileReader.nextInt();
						cityArray[city][coord]=data;
					}
				}
			}
			//start time
			double startTime = System.currentTimeMillis();
			//creates cost matrix which is used to calculate branchAndBound
			createCostMatrix(cityArray);
			//ends time
			double elapsedMilliSeconds = System.currentTimeMillis() - startTime;
			System.out.println("Time: " + elapsedMilliSeconds/1000 + " Seconds"	);
			System.out.println("Minimum cost: " + finalPath);
			System.out.println("Path taken");
			for(int i = 0; i <= cityArray.length; i++) {
				System.out.println(savedPath[i]+1);	
			}
			
			fileReader.close();
			//if error in file it catches it
		} catch(FileNotFoundException error){
			System.out.println("Didnt read file");
			error.printStackTrace();
		}
	
	}
	
	public static void createCostMatrix(int[][] test1Array) {
		//Creates costMatrix used to calculate lowerbound for branch and bound
		double[][] costMatrix = new double[test1Array.length][test1Array.length];
		//Loop through costMatrix row
		for(int matrixRow=0; matrixRow<costMatrix.length; matrixRow++) {
			//Loop through costMatrix column
			for(int matrixColumn=0; matrixColumn<costMatrix.length; matrixColumn++) {
				//Calculates distance between 2 city coordinates
				//Euclidean distance equation sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1))
				costMatrix[matrixRow][matrixColumn] = Math.sqrt(((test1Array[matrixColumn][1]-test1Array[matrixRow][1]) * (test1Array[matrixColumn][1]-test1Array[matrixRow][1])) 
						+ ((test1Array[matrixColumn][2] - test1Array[matrixRow][2]) * (test1Array[matrixColumn][2] - test1Array[matrixRow][2])));;
			}
		}
		//passes costMatrix as an array
		printMatrix(costMatrix);
		
		//sets lower bound
		double lowerBound = 0;
		//declares and initialize array
		//current path stores all the path the branch and bound will travel through
		int currentPath[] = new int[costMatrix.length+1];
		
		//calculate starting lower bound 
		for(int matrixRow = 0; matrixRow < costMatrix.length; matrixRow++) {
			double min[] = firstSecondSmallest(costMatrix, matrixRow);
			lowerBound += min[0] + min[1];
		}
		//lower bound / 2 to find the lower bound value
		lowerBound = lowerBound / 2;
		//initialize array
		visited = new boolean[costMatrix.length];
		//fill visited array with false
		Arrays.fill(visited, false);
		//set first array with true
		visited[0] = true;
		
		//current paths are all -1 as the numbers of the path will be put into the array
		Arrays.fill(currentPath, -1);
		currentPath[0]=0;
		
		//branch and bound recursive function
		branchAndBoundTSP(costMatrix, lowerBound, 0, 1, currentPath);
		
		
	}
	
	//recursive function
	//Calculates the lower bound and moves to the next level depending on curr_res sieze
	public static void branchAndBoundTSP(double costMatrix[][], double lowerBound, double columnValue, int level, int currentPath[] ) {
		
		//checks if its at the final city
		if(level == costMatrix.length) {
			//Checks if it is not 0
			if(costMatrix[currentPath[level-1]][currentPath[0]] != 0) {
				//Calculates the total 
				double total = columnValue + costMatrix[currentPath[level-1]][currentPath[0]];
				//if total is less then the final path
				if(total < finalPath) {
					//save final path
					savedPath = new double[costMatrix.length+1];
					for(int i = 0; i < costMatrix.length; i++) {
						savedPath[i] = currentPath[i];
					}
					//last path goes back to 0
					savedPath[costMatrix.length] = 0;
					//final path calculation = the new total
					finalPath = total;
				}
			}
		}
		
		
		//iteration through each column of a level
		for(int levelColumn = 0; levelColumn < costMatrix.length; levelColumn++) {
			//checks if current column has been visited and not = 0
			if(visited[levelColumn] == false && costMatrix[currentPath[level-1]][levelColumn] != 0) {
				//save lower bound to save bound
				double saveBound = lowerBound;
				//value of current column in cost matrix
				columnValue += costMatrix[currentPath[level-1]][levelColumn];
				
				//Calling findSecondSmallest function
				double min1[] = firstSecondSmallest(costMatrix, currentPath[level-1]);
				double min2[] = firstSecondSmallest(costMatrix, levelColumn);
				
				//Checks if level is 1 as lower bound calculation is different to levels beyond 1
				if(level==1) {
					//min[0] is first smallest number
					lowerBound -= (((min1[0])+(min2[0]))/2);
				}
				else {
					//min[1] is second smallest number
					lowerBound -= (((min1[1])+(min2[0]))/2);
				}
				
				//lower bound calculation = lowerBound + columnValue
				//if lower bound calculation is less then final path, it explores that node
				if(lowerBound + columnValue < finalPath) {
					//if top condition is passed it means this current calculation 
					//is better then the final path already set
					//sets currentPath level row to the column which was chosen
					currentPath[level] = levelColumn;
					//sets current column to true
					visited[levelColumn] = true;
					
					//calls function with new values to go to the next level chosen
					branchAndBoundTSP(costMatrix, lowerBound, columnValue, level + 1, currentPath);
				}				
				//After branchAndBoundTSP reaches the last level, the program
				//back tracks in order to try new solutions
				
				// reset to previous levels columnValue
				columnValue -= costMatrix[currentPath[level-1]][levelColumn];
				//reset to previous levels lower bound
				lowerBound = saveBound;
				
				//reset visited arrays with false
				for(int fillArray = 0; fillArray < costMatrix.length; fillArray++) {
					visited[fillArray] = false;
				}
				//fill array with true except for level-1
				for (int j = 0; j <= level - 1; j++) {
					visited[currentPath[j]] = true;
				}
				
				
			}
		}
		
	}
	
	//calculates the first and second smallest number in the cost matrix
	static double[] firstSecondSmallest(double[][] costMatrix, int matrixRow) {
		double firstMin = Integer.MAX_VALUE;
		double secondMin = Integer.MAX_VALUE;
		//calculates the first minimum number
        for (int matrixColumn = 0; matrixColumn < costMatrix.length; matrixColumn++){  
            if (costMatrix[matrixRow][matrixColumn] < firstMin && matrixColumn != matrixRow ){  
                  firstMin = costMatrix[matrixRow][matrixColumn];
            }  
        }  
        //calculates the second minimum number
        for (int matrixColumn = 0; matrixColumn <costMatrix.length; matrixColumn++) {
        	if(costMatrix[matrixRow][matrixColumn] < secondMin && costMatrix[matrixRow][matrixColumn] > firstMin) {
        		secondMin = costMatrix[matrixRow][matrixColumn];
        	}
        }
        //stores firstMin and secondMin values in array
        double[] minValues = new double[2];
        minValues[0] = firstMin;
        minValues[1] = secondMin;
		return minValues;
	
	}
	
	//Prints matrix
	public static void printMatrix(double [][] costMatrix) {
		//for loop through the costMatrix 2D array to print it
		System.out.println("Cost Matrix: ");
		for(int matrixColumn=0; matrixColumn < costMatrix.length; matrixColumn++) {
			for(int matrixRow=0; matrixRow < costMatrix[matrixColumn].length; matrixRow++) {
				System.out.print(costMatrix[matrixColumn][matrixRow]+ ",");	
			}
			System.out.println();
		}
	}
}
