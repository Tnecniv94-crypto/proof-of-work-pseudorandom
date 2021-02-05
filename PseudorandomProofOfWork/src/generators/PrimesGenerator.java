package generators;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import analysis.Constants;

public class PrimesGenerator {
	private BigInteger prime;
	private Random rand;
	private int bitLength, maxMillerRabinBase, 
	 			/*
	 			 *  determines the difference in bitLength for p and q, to ensure 2 < p/q <= Constants.MAX_BLUM_BLUM_SHUB_PQ_RATIO
	 			 *  for Blum-Number n = pq with p = 3 mod 4 and q = 3 mod 4 and p,q prime
	 			 */
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
	
	/**
	 * 
	 * @param congruentTo
	 * @param modulus
	 * @return a random prime number p with {@link bitLength} bits and p = congruentTo mod modulus
	 */
	public BigInteger randomPrime(BigInteger congruentTo, BigInteger modulus) {
		return randomPrime(congruentTo, modulus, bitLength);
	}
	
	private BigInteger randomPrime(BigInteger congruentTo, BigInteger modulus, int bitLength) {
		if(congruentTo.compareTo(modulus) >= 0) {
			return null;
		}
		
		BigInteger ret = BigInteger.probablePrime(bitLength, rand);
		
		while(ret.mod(modulus).compareTo(congruentTo) != 0) {
			ret = BigInteger.probablePrime(bitLength, rand);
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @param congruentTo
	 * @param modulus
	 * @return a random prime number p with {@link bitLength} bits and p = congruentTo mod modulus and p = 2q + 1 with q prime
	 */
	public BigInteger randomStrongPrime(BigInteger congruentTo, BigInteger modulus) {
		return randomStrongPrime(congruentTo, modulus, bitLength);
	}
	
	private BigInteger randomStrongPrime(BigInteger congruentTo, BigInteger modulus, int bitLength) {
		if(congruentTo.compareTo(modulus) >= 0) {
			return null;
		}
		
		BigInteger ret;
		
		do {
			ret = BigInteger.probablePrime(bitLength, rand);
			ret = ret.multiply(BigInteger.TWO).add(BigInteger.ONE);
		} while(ret.mod(modulus).compareTo(congruentTo) != 0 || !ret.isProbablePrime(Constants.BIG_INTEGER_CERTANITY));
		
		return ret;
	}
	
	/**
	 * Will create {@link Constants.BLUM_NUMBER_PQ_ROUNDS} pairs of random (p,q), which are both strong primes and will
	 * pick the pair with the smallest value of gcd((p - 3)/2, (q - 3)/2));
	 * 
	 * @return a Blum-number n=pq with bitLength or bitLength + 1 bits on index 0,
	 * p = 3 mod 4 and p prime on index 1 and q = 3 mod 4 and q prime on index 2
	 */
	public BigInteger[] getRandomBlumNumber() {
		LinkedList<BigInteger[]> pairs = new LinkedList<>();
		Iterator<BigInteger[]> it;
		BigInteger[] ret = new BigInteger[3], buf = null;
		BigInteger p, q, congruentTo = new BigInteger("3"), modulus = new BigInteger("4"), smallestGcd = null, gcd;
		// if p has at most 10 Bits more than q: p/q <= 2^10 = 1024 
		int bitLength = this.bitLength/2, offset = (rand.nextInt() % maxPQRatioBitOffset) + 1; //bitLengthP, bitLengthQ;
		
		if(bitLength - offset < 2) {
			bitLength = 2 + offset;
		}
		
		for(int i = 0; i < Constants.BLUM_NUMBER_PQ_ROUNDS; i++) {
			p = randomStrongPrime(congruentTo, modulus, bitLength + offset);
			q = randomStrongPrime(congruentTo, modulus, bitLength - offset);
			
			buf = new BigInteger[2];
			buf[0] = p;
			buf[1] = q;
			pairs.add(buf);
		}
		
		buf = null;
		it = pairs.iterator();
		
		while(it.hasNext()) {
			buf = it.next();
		
			if(smallestGcd == null) {
				smallestGcd = buf[0].subtract(congruentTo).divide(BigInteger.TWO).gcd(buf[1].subtract(congruentTo).divide(BigInteger.TWO)); // gcd((p - 3)/2, (q - 3)/2))
				ret[1] = buf[0];
				ret[2] = buf[1];
			}
			else if((gcd = buf[0].subtract(congruentTo).divide(BigInteger.TWO).gcd(buf[1].subtract(congruentTo).divide(BigInteger.TWO))).compareTo(smallestGcd) == -1) { // smaller gcd found
				ret[1] = buf[0];
				ret[2] = buf[1];
				
				if(gcd.compareTo(BigInteger.ONE) == 0) { // better gcd than one not possible
					ret[0] = buf[0].multiply(buf[1]);
					
					return ret;
				}
				
				smallestGcd = gcd;
			}
		}
		
		ret[0] = buf[0].multiply(buf[1]);
		ret[1] = buf[0];
		ret[2] = buf[1];
		
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
