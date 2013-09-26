package model.param;

import java.util.Random;

import javax.management.RuntimeErrorException;

import util.MyArray;
import util.Stats;

public class MultinomialRegular extends MultinomialBase{
	public MultinomialRegular(int x, int y) {
		super(x, y);	
	}
	
	@Override
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
		checkDistribution();
	}
	
	@Override
	public void smooth() {
		//hyperparameter
		double small = 1e-2;
		for(int i=0; i<y; i++) {
			for(int j=0; j<x; j++) {
				if(count[j][i] == 0) {
					Stats.totalFixes++;
					count[j][i] = small;
				}
			}
		}		
	}
	
	@Override
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
				if(count[j][i] == 0) {
					//System.err.println("Prob distribution zero after normalization");
				}
			}
		}
		//MyArray.printTable(count);
		checkDistribution();
	}
	
	@Override
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
				throw new RuntimeException("Distribution sums to Infinite");
			}
			if(Math.abs(sum - 1.0) > tolerance) {
				//System.err.println("Distribution sums to : " + sum);
				throw new RuntimeException("Distribution sums to : " + sum);
			}
		}
	}
	
	@Override
	public void printDistribution() {
		MyArray.printTable(count);
	}

	@Override
	public void normalize(MultinomialBase other) {
		for(int i=0; i<y; i++) {
			double sum = 0;
			for(int j=0; j<x; j++) {
				//smoothing done using previous probability
				if(count[j][i] == 0) {
					count[j][i] = other.count[j][i];
				}
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
}
