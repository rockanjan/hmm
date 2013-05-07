package util;

import javax.management.RuntimeErrorException;

//provides four extended functions for working in log/exp domain
public class LogExp{
	
	//extended exp that can handle log(0)
	//for x = LOGZERO (NaN), eexp(x) = 0, else eexp(x) = exp(x)
	public static double exp(double x) {
		if(Double.isNaN(x)) {
			throw new RuntimeException("x is NaN for calculating exp(x)");
		}
		return Math.exp(x);		
	}
	
	/*
	 * return exp(x) value with fix for NaN
	 */
	public static double expFix(double x) {
		if(Double.isNaN(x)) {
			Stats.totalFixes++;
			return 0.0;
		}
		return Math.exp(x);		
	}
	
	public static double log(double x) {
		if(x == 0) {
			return Double.NaN;
		} else if (x < 0) {
			throw new RuntimeException("Negative value of x for log(x)");
		}
		return Math.log(x);
	}
	
	/*
	 * return log value with fix for zero cases
	 */
	public static double logFix(double x) {
		if(x == 0) {
			Stats.totalFixes++;
			return -Double.MAX_EXPONENT;
		} else if (x < 0) {
			throw new RuntimeException("Negative value of x for log(x)");
		}
		return Math.log(x);
	}
	
	
	//log( exp(a) + exp(b) )
	public static double logsumexp(double a, double b) {
		if(Double.isInfinite(a)) {
			throw new RuntimeException("LogSum first term is infinite");
		}
		if(Double.isInfinite(b)) {
			throw new RuntimeException("LogSum second term is infinite");
		}		
		
		if( a > b) {
			return a + Math.log1p(Math.exp(b - a));
		} else {
			return b + Math.log1p(Math.exp(a - b));
		}
	}
	
	public static double logsumexp(double[] values) {
		double result = 0.0;
		double MAX = -Double.MAX_VALUE;
		for(int i=0; i<values.length; i++) {
			if(values[i] > MAX) {
				MAX = values[i];
			}
		}
		double expsum = 0.0;
		for(int i=0; i<values.length; i++) {
			expsum += Math.exp(values[i] - MAX);
		}
		result = MAX + Math.log(expsum); 
		return result;
	}
	
	public static void main(String[] args) {
		double a = -7;
		double b = -20;
		System.out.println(a + Math.log(1 + exp(b - a)));
		System.out.println(logsumexp(a, b));
		double[] values = {a, b};
		System.out.println(logsumexp(values));
	}
 }