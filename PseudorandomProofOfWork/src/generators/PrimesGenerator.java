package math;

import java.math.BigInteger;
import java.util.Random;

import analysis.Constants;

public class PrimesGenerator {
	private BigInteger prime;
	private Random rand;
	private int bitLength, maxMillerRabinBase, 
				maxPQRatioBitOffset = (int)Math.ceil(Math.log(Constants.MAX_BLUM_BLUM_SHUB_PQ_RATIO)/(2 * Math.log(2)));
	private boolean firstPrime = true;
	
	public PrimesGenerator(int bitLength, Random rand) {
		this.bitLength = bitLength;
		this.rand = rand;
		this.prime = BigInteger.TWO.pow(bitLength);
		maxMillerRabinBase = Constants.MAX_MILLER_RABIN_BASE;
		
		// Miller-Rabin-Base a must be from range 2 <= a < n - 1, if n is tested for primality 
		// and prime is before getNextPrime() call 2^bitLength = n - 1
		if(prime.compareTo(new BigInteger(String.valueOf(maxMillerRabinBase))) <= 0) {
			maxMillerRabinBase = prime.subtract(BigInteger.ONE).intValue();
		}
	}
	
	public BigInteger getRandomPrime(int congruentTo, int modulus) {
		return randomPrime(new BigInteger(String.valueOf(congruentTo)), new BigInteger(String.valueOf(modulus)), bitLength);
	}
	
	private BigInteger randomPrime(BigInteger congruentTo, BigInteger modulus, int bitLength) {
		if(congruentTo.compareTo(modulus) >= 0) {
			return null;
		}
		
		//BigInteger ret = new BigInteger(bitLength, Constants.millerRabinRounds, rand);
		BigInteger ret = BigInteger.probablePrime(bitLength, rand);
		
		while(ret.mod(modulus).compareTo(congruentTo) != 0) {
			//ret = new BigInteger(bitLength, Constants.millerRabinRounds, rand);
			ret = BigInteger.probablePrime(bitLength, rand);
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @return a Blum-Blum-Blum-Shub number n=pq with bitLength or bitLength + 1 bits on index 0,
	 * p = 3 mod 4 and p prime on index 1 and q = 3 mod 4 and q prime on index 2
	 */
	public BigInteger[] getRandomBlumBlumShubNumber() {
		BigInteger[] ret = new BigInteger[3];
		// if p has at most 10 Bits more than q: p/q <= 2^10 = 1024 
		BigInteger p, q, congruentTo = new BigInteger("3"), modulus = new BigInteger("4");
		int bitLength = this.bitLength/2, offset = (rand.nextInt() % maxPQRatioBitOffset) + 1; //bitLengthP, bitLengthQ;
		
		if(bitLength - offset < 2) {
			bitLength = 2 + offset;
		}
		
		p = randomPrime(congruentTo, modulus, bitLength + offset + 1);
		q = randomPrime(congruentTo, modulus, bitLength - offset);
		
		/*
		System.out.println("p has : " + p.bitLength() + " bits and should have " + (bitLength/2 + offset));
		System.out.println("q has : " + q.bitLength() + " bits should have " + (bitLength/2 - offset));
		System.out.println("pq has " + p.multiply(q).bitLength() + " bits and should have " + bitLength + " or " + (bitLength + 1));
		System.out.println();
		*/
		
		ret[0] = p.multiply(q);
		ret[1] = p;
		ret[2] = q;
		
		return ret;
	}
	
	/**
	 * 
	 * @return next prime number p with p > 2^bitLength
	 */
	public BigInteger getNextPrime() {
		if(firstPrime) {
			// 2^bitLength + 1
			prime.add(BigInteger.ONE);
			firstPrime = false;
		}	
		else {
			// try next prime + 2
			prime.add(BigInteger.TWO);
		}
		
		while(!millerRabinTest(prime)) {
			prime = prime.add(BigInteger.TWO);
		}
		
		return prime;
	}
	
	/**
	 * 
	 * @param congruentTo
	 * @return the next prime number p: p == congruentTo mod modulus and p > 2^bitLength
	 */
	public BigInteger getNextPrime(BigInteger congruentTo, BigInteger modulus) {
		if(prime == null) {
			prime = new BigInteger("2");
			prime = prime.pow(bitLength);
			prime.add(BigInteger.ONE);
		}
		else {
			prime.add(BigInteger.TWO); 
		}
		
		while(prime.mod(modulus).compareTo(congruentTo) != 0 || !millerRabinTest(prime)) {
			prime = prime.add(BigInteger.TWO); // increment in steps of two, because even numbers can't be prime
		}
		
		return prime;
	}
	
	/**
	 * 
	 * @param congruentTo
	 * @param modulus
	 * @return the next prime number p: p == congruentTo mod modulus and p > 2^bitLength
	 */
	public BigInteger getNextPrime(int congruentTo, int modulus) {
		return getNextPrime(new BigInteger(String.valueOf(congruentTo)), new BigInteger(String.valueOf(modulus)));
	}
	
	/**
	 * 
	 * @param testForPrimality
	 * @return true if
	 */
	private boolean millerRabinTest(BigInteger testForPrimality) {
		BigInteger[] factor = factor(testForPrimality); 
		BigInteger base = new BigInteger(String.valueOf(nextBase())), d = factor[1];
		int e = factor[0].intValue();
		
		for(int i = 0; i < Constants.MILLER_RABIN_ROUNDS; i++) {
			if(base.modPow(d, testForPrimality).compareTo(BigInteger.ONE) != 0 && !secondCondition(base, e, d, testForPrimality)) {
				return false;
			}
			
			base = new BigInteger(String.valueOf(nextBase()));
		}
		
		return true;
	}
	
	/**
	 * factors n - 1 = (2^e)*d
	 * @param n
	 * @return e and d with d odd with e at index 0 and d at index 1
	 */
	private BigInteger[] factor(BigInteger n) {
		if(n.mod(BigInteger.TWO).compareTo(BigInteger.ZERO) == 0) { // testForPrimality is even number
			return null;
		}
		
		BigInteger factor = n.subtract(BigInteger.ONE),
				divide = n.subtract(BigInteger.ONE);
		BigInteger[] division, ret = new BigInteger[2];
		
		int e = 0;
		
		do {
			division = divide.divideAndRemainder(divide);
			divide = division[0];
			e++;
		} while(division[1].compareTo(BigInteger.ZERO) != 0);
		
		ret[0] = new BigInteger(String.valueOf(e));
		ret[1] = factor.divide(BigInteger.TWO.pow(e));
		
		return ret;
	}
	
	/**
	 * @param base
	 * @param d
	 * @param e
	 * @param n
	 * @return if exists 0 <= i < e: base^(d * 2^i) == -1 mod n true, else false
	 */
	private boolean secondCondition(BigInteger base, int e, BigInteger d, BigInteger n) {
		BigInteger congruentTo = new BigInteger("-1").mod(n);
		
		for(int i = 0; i < e; i++) {
			if(base.modPow(d.multiply(BigInteger.TWO.pow(i)), n).compareTo(congruentTo) == 0) {
				return true;
			}
		}
		
		return false;
	}
	
	private int nextBase() {
		int ret = rand.nextInt(maxMillerRabinBase);
		
		while(ret <= 2) {
			ret = rand.nextInt(maxMillerRabinBase);
		}
		
		return ret;
	}
}
