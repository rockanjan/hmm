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
}
