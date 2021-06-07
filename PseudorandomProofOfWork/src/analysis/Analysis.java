package analysis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
	 * @return a List of seeds that create an degenereated cycle
	 */
	private LinkedList<WeakSeed> checkEquidistributionBlumBlumShub(int securityParam, double difficulty) {
		LinkedList<WeakSeed> ret = new LinkedList<>();
		BlumBlumShub generator = new BlumBlumShub(securityParam);
		BigInteger p = generator.getQ(), q = generator.getQ();
		int maxPeriodLength;
		double ratio;
		
		maxPeriodLength = Functions.maxPeriodLengthBlumBlumShub(p, q);
		
		for(int i = 0; i < Constants.EQUIDISTRIBUTION_ROUNDS; i++) {
			if((ratio = equidistribution(generator, maxPeriodLength)) > difficulty || 1/ratio > difficulty) { // equidistribution differs more than difficulty from 1
				out.println("Weak seed " + generator.getSeed() + " found with ratio " + ratio + " on difficulty " + difficulty + ".");
				
				ret.add(new WeakSeed(generator.getSeed(), generator.getModulus(), generator.getP(), generator.getQ(), maxPeriodLength, ratio, securityParam));
			}
			
			System.out.println(ratio);
			
			if(i % (Constants.EQUIDISTRIBUTION_ROUNDS/100) == 0) {
				out.println((int)(Math.ceil(i/(double)Constants.EQUIDISTRIBUTION_ROUNDS * 100)) + "% of all " + Constants.EQUIDISTRIBUTION_ROUNDS + " rounds done.");
			}
			
			generator.generateSeed();
		}
		
		System.out.println("\n" + ret.size() + " weak seeds found.");
		
		return ret;
	}
	
	/**
	 * 
	 * @param securityParam
	 * @param difficulty the ratio of the accumulated zeroes sum_zeroes(period) and sum_ones(period) has
	 * 		  to be greater than sum_zeroes(period)/sum_ones(period) > {@paramref difficulty})
	 */
	private void checkEquidistributionJavaSecureRandom(int securityParam, double difficulty) {
		LinkedList<WeakSeed> ret = new LinkedList<>();
		BlumBlumShub generator = new BlumBlumShub(securityParam);
		BigInteger p = generator.getQ(), q = generator.getQ();
		int maxPeriodLength;
		double ratio;
		
		maxPeriodLength = Functions.maxPeriodLengthBlumBlumShub(p, q);
		
		for(int i = 0; i < Constants.EQUIDISTRIBUTION_ROUNDS; i++) {
			if((ratio = equidistribution(generator, maxPeriodLength)) > difficulty || 1/ratio > difficulty) { // equidistribution differs more than difficulty from 1
				out.println("Weak seed " + generator.getSeed() + " found with ratio " + ratio + " on difficulty " + difficulty + ".");
				
				ret.add(new WeakSeed(generator.getSeed(), generator.getModulus(), generator.getP(), generator.getQ(), maxPeriodLength, ratio, securityParam));
			}
			
			System.out.println(ratio);
			
			if(i % (Constants.EQUIDISTRIBUTION_ROUNDS/100) == 0) {
				out.println((int)(Math.ceil(i/(double)Constants.EQUIDISTRIBUTION_ROUNDS * 100)) + "% of all " + Constants.EQUIDISTRIBUTION_ROUNDS + " rounds done.");
			}
			
			generator.generateSeed(); // new seed
		}
	}
	
	/**
	 * calculates the sum of zeroes and ones in an pseudorandom sequence
	 * @param generator
	 * @param periodLength
	 * @return
	 */
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
	
	/**
	 * calculates the sum of zeroes and ones in an pseudorandom sequence
	 * @param generator
	 * @param periodLength
	 * @return
	 */
	private double equidistribution(BlumBlumShub generator, int periodLength) {
		ArrayList<Boolean> sequence = blumBlumShubPseudorandomSequence(generator, periodLength, Constants.SEQUENCE_WINDOW_LENGTH);
		int zeroes = 0, ones = 0;
		
		for(Boolean b : sequence) { // accumulate ones and zeroes
			if(b) {
				ones++;
			}
			else {
				zeroes++;
			}
		}
		
		return ((double)zeroes)/ones;
	}
	
	private ArrayList<Boolean> blumBlumShubPseudorandomSequence(BlumBlumShub generator, int periodLength, int windowSize) {
		ArrayList<Boolean> sequence = new ArrayList<>(periodLength/2), ret = new ArrayList<>(periodLength/2);
		String stringSequenceStart = "";
		boolean first = true;
		
		while(true) {
			sequence.add(generator.nextBoolean());
			
			if(sequence.size() > 2 * windowSize) {
				if(first) {
					for(int i = 0; i < windowSize; i++) {
						stringSequenceStart += (sequence.get(i) ? "1" : "0");
					}
					
					first = false;
				}
				
				if(checkSequenceRepeats(sequence, stringSequenceStart)) {
					for(int i = 0; i < sequence.size() - windowSize; i++) {
						ret.add(sequence.get(i));
					}
					
					return ret;
				}
			}
		}
	}
	
	/**
	 * if the sequence contains a {@value Constants.SEQUENCE_WINDOW_LENGTH} long bit sequence at the and as at the start,
	 * then the sequence is considered repeating
	 * @param sequence
	 * @param stringSequenceStart
	 * @return true if the sequence repeats, false if not
	 */
	private boolean checkSequenceRepeats(ArrayList<Boolean> sequence, String stringSequenceStart) {
		int windowSize = stringSequenceStart.length();
		
		if(sequence.size() < windowSize) {
			return false;
		}
		
		String stringSequenceEnd = "";
		
		for(int i = 0; i < windowSize; i++) {
			stringSequenceEnd += (sequence.get(sequence.size() - 1 - windowSize + i) ? "1" : "0");
		}
 		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] digestBytesStart, digestBytesEnd;
			
			digestBytesStart = digest.digest(stringSequenceStart.getBytes(StandardCharsets.US_ASCII));
			digestBytesEnd = digest.digest(stringSequenceEnd.getBytes(StandardCharsets.US_ASCII));
			
			for(int i = 0; i < digestBytesStart.length; i++) {
				if(digestBytesStart[i] != digestBytesEnd[i]) {
					return false;
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return true;
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
