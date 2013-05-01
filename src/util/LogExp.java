package util;

import javax.management.RuntimeErrorException;

//provides four extended functions for working in log/exp domain
public class LogExp{
	
	//extended exp that can handle log(0)
	//for x = LOGZERO (NaN), eexp(x) = 0, else eexp(x) = exp(x)
	public static double exp(double x) {
		if(Double.isNaN(x)) {
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
 }