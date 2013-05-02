package model;

import java.util.Random;

import javax.management.RuntimeErrorException;

import util.MyArray;

public class Multinomial {
	//x,y == P(x given y)
	int x,y;
	public double[][] count;
	
	public Multinomial(int x, int y) {
		this.x = x; this.y = y;
		count = new double[x][y];
	}
	
	public void initializeRandom(Random r) {
		double small = 1e-100;
		for(int i=0; i<y; i++) {
			double sum = 0;
			for(int j=0; j<x; j++) {
				count[j][i] = r.nextDouble() + small;
				sum += count[j][i];
			}
			//normalize
			for(int j=0; j<x; j++) {
				count[j][i] = count[j][i] / sum;
			}
		}
	}
	
	public double get(int x, int y) {
		return count[x][y];
	}
	
	public void addToCounts(int x, int y, double value) {
		count[x][y] += value;
	}
	
	private void smooth() {
		double small = 1e-100;
		for(int i=0; i<y; i++) {
			for(int j=0; j<x; j++) {
				if(count[j][i] == 0) {
					count[j][i] = small;
				}
			}
		}
	}
	
	public void normalize() {
		smooth();
		for(int i=0; i<y; i++) {
			double sum = 0;
			for(int j=0; j<x; j++) {
				sum += count[j][i];
			}
			//MyArray.printTable(count);
			//normalize
			if(sum == 0) {
				throw new RuntimeException("Sum = 0 in normalization");
			}
			for(int j=0; j<x; j++) {
				count[j][i] = count[j][i] / sum;
				if(Double.isNaN(count[j][i])) {
					System.out.format("count[%d][%d] = %f\n", j,i,count[j][i]);
					System.out.format("sum = %f\n", sum);
					throw new RuntimeException("Probability after normalization is NaN");
				}
			}
		}
		//MyArray.printTable(count);
		checkDistribution();
	}
	
	public void checkDistribution() {
		double tolerance = 1e-5;
		
		for(int i=0; i<y; i++) {
			double sum = 0;
			for(int j=0; j<x; j++) {
				sum += count[j][i];
			}
			if(Double.isNaN(sum)) {
				throw new RuntimeException("Distribution sums to NaN");
			}
			if(Double.isInfinite(sum)) {
				throw new RuntimeException("Distribution sums to NaN");
			}
			if(Math.abs(sum - 1.0) > tolerance) {
				//System.err.println("Distribution sums to : " + sum);
				throw new RuntimeException("Distribution sums to : " + sum);
			}
		}
	}
	
	public void cloneFrom(Multinomial source) {
		for(int i=0; i<y; i++) {
			for(int j=0; j<x; j++) {
				count[j][i] = source.count[j][i];
			}
		}
	}
	
	public void printDistribution() {
		MyArray.printTable(count);
	}
}
