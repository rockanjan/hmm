package util;

public class MyArray {

	public static void printTable(double[][] table) {
		System.out.println("Table...");
		for(int i=0; i<table.length; i++) {
			for(int j=0; j<table[i].length; j++) {
				System.out.print(table[i][j] + "\t");
			}
			System.out.println();
		}
	}
	
	public static void printTable(double[][] table, String title) {
		System.out.println("Table : " + title);
		printTable(table);
	}
	
	public static void printExpTable(double[][] table, String title) {
		System.out.println("Table : " + title);
		printExpTable(table);
	}
	
	public static void printExpTable(double[][] table) {
		System.out.println("Table...");
		for(int i=0; i<table.length; i++) {
			for(int j=0; j<table[i].length; j++) {
				System.out.print(Math.exp(table[i][j]) + "\t");
			}
			System.out.println();
		}
	}
	
	public static void addToMatrix(double[][] finalMatrix, double[][] newMatrix) {
		if(finalMatrix.length != newMatrix.length || finalMatrix[0].length != newMatrix[0].length) {
			throw new RuntimeException("Matrix dimension mismatch during addition");			
		}
		
		for(int i=0; i<finalMatrix.length; i++) {
			for(int j=0; j<finalMatrix[0].length; j++) {
				finalMatrix[i][j] += newMatrix[i][j];
			}
		}
	}
	
	public static double[][] getCloneOfMatrix(double[][] matrix) {
		double[][] cloned = new double[matrix.length][matrix[0].length];
		for(int i=0; i<matrix.length; i++) {
			for(int j=0; j<matrix.length; j++) {
				cloned[i][j] = matrix[i][j];
			}
		}
		return cloned;
	}
}
