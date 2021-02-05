package analysis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.LinkedList;
import java.util.Random;

import generators.BlumBlumShub;
import math.Functions;

public class Analysis {
	private Print out;
	
	public Analysis(boolean dump) {
		out = new Print(System.out, dump);
	}
	
	/**
	 * 
	 * @param securityParam
	 * @param difficulty the ratio of the accumulated zeroes sum_zeroes(period) and sum_ones(period) has
	 * 		  to be greater than sum_zeroes(period)/sum_ones(period) > {@paramref difficulty})
	 * @return
	 */
	private LinkedList<WeakSeed> checkEquidistributionBlumBlumShub(int securityParam, double difficulty) {
		LinkedList<WeakSeed> ret = new LinkedList<>();
		BlumBlumShub generator = new BlumBlumShub(securityParam);
		BigInteger p = generator.getQ(), q = generator.getQ(), maxPeriodLength;
		double ratio;
		
		maxPeriodLength = Functions.maxPeriodLengthBlumBlumShub(p, q);
		
		for(int i = 0; i < Constants.EQUIDISTRIBUTION_ROUNDS; i++) {
			if((ratio = equidistribution(generator, maxPeriodLength)) > difficulty || 1/ratio > difficulty) {
				out.println("Weak seed " + generator.getSeed() + " found with ratio " + ratio + " on difficulty " + difficulty + ".");
				
				ret.add(new WeakSeed(generator.getSeed(), generator.getModulus(), generator.getP(), generator.getQ(), maxPeriodLength, ratio, securityParam));
			}
			
			System.out.println(ratio);
			
			if(i % (Constants.EQUIDISTRIBUTION_ROUNDS/100) == 0) {
				out.println((int)(Math.ceil(i/(double)Constants.EQUIDISTRIBUTION_ROUNDS * 100)) + "% of all " + Constants.EQUIDISTRIBUTION_ROUNDS + " rounds done.");
			}
			
			generator.generateSeed();
		}
		
		return ret;
	}
	
	private double equidistribution(Random generator, BigInteger periodLength) {
		BigInteger zeroes = BigInteger.ZERO, ones = BigInteger.ONE;
		boolean[] period, follow;
		int index = 0;
		
		period = new boolean[periodLength.intValue()];
		follow = new boolean[periodLength.intValue()];
		
		for(BigInteger i = BigInteger.ZERO; i.compareTo(periodLength) == -1; i = i.add(BigInteger.ONE)) {
			if(generator.nextBoolean()) {
				ones = ones.add(BigInteger.ONE);
				period[index++] = true;
			}
			else {
				zeroes = zeroes.add(BigInteger.ONE);
				period[index++] = false;
			}
		}
		
		index = 0;
		
		for(BigInteger i = BigInteger.ZERO; i.compareTo(periodLength) == -1; i = i.add(BigInteger.ONE)) {
			if(generator.nextBoolean()) {
				follow[index++] = true;
			}
			else {
				follow[index++] = false;
			}
		}
		
		return new BigDecimal(zeroes).divide(new BigDecimal(ones), MathContext.DECIMAL64).doubleValue();
	}
	
	/*private boolean isPeriod(boolean[] period, boolean[] follow) {
		if(period.length != follow.length) {
			return false;
		}
		
		for(int i = 0; i < period.length; i++) {
			if(period[i] != follow[i]) {
				out.println("mismatch at index: " + i);
				return false;
			}
		}
		
		return true;
	}*/
	
	public double checkEquidistribution(String pseudorandomGeneratorName, int securityParam, double difficulty) {
		switch(pseudorandomGeneratorName.toLowerCase()) {
			case "blumblumshub":
				checkEquidistributionBlumBlumShub(securityParam, difficulty);
				break;
			case "javasecuerandom":
				
			default:
				out.println("Pseudorandom generator\"" + pseudorandomGeneratorName + "\" not known.");
		}
		
		return -1;
	}
	
	public static void main(String[] args) {
		new Analysis(true).checkEquidistribution("blumblumshub", 24, 1.5D);
	}
}
