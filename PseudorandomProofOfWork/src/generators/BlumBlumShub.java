package generators;

import java.math.BigInteger;
import java.security.SecureRandom;


public class BlumBlumShub extends SecureRandom {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PrimesGenerator primesGenerator;
	private MySecureRandom rand;
	private BigInteger seed, modulus, p, q, current;
	private int securityParam;
	
	public BlumBlumShub(int securityParam) {
		rand = new MySecureRandom();
		primesGenerator = new PrimesGenerator(securityParam, rand);
		this.securityParam = securityParam;
		
		changeModulus();
		generateSeed();
	}
	
	@Override
	public boolean nextBoolean() {
		current = current.modPow(BigInteger.TWO, modulus);
		
		return current.mod(BigInteger.TWO).compareTo(BigInteger.ONE) == 0 ? false : true;
	}
	
	public byte nextByte() {
		String bits = "";
		
		for(int i = 0; i < 8 ; i++) {
			current = current.modPow(BigInteger.TWO, modulus);
			bits += current.mod(BigInteger.TWO).compareTo(BigInteger.ZERO) == 0 ? "0" : "1";
		}
		
		return Byte.valueOf(bits, 2);
	}
	
	public void generateSeed() {
		do {
			seed = rand.nextRand(securityParam).mod(modulus);
		} while(seed.gcd(modulus).compareTo(BigInteger.ONE) != 0 || seed.gcd(p).compareTo(BigInteger.ONE) != 0 || seed.gcd(q).compareTo(BigInteger.ONE) != 0);
		// ensure gcd(seed, modulus) = 1 and p,q are not divisors of seed
		
		current = seed;
	}
	
	public void changeModulus() {
		BigInteger[] buf = primesGenerator.getRandomBlumNumber();
		
		modulus = buf[0];
		p = buf[1];
		q = buf[2];
	}
	
	/**
	 * 
	 * @return current generator parameters seed, modulus, p, q as BigInteger[] 
	 */
	public BigInteger[] getParams() {
		BigInteger[] ret = new BigInteger[4];
		
		ret[0] = seed;
		ret[1] = modulus;
		ret[2] = p;
		ret[3] = q;
		
		return ret;
	}
	
	public BigInteger getSeed() {
		return seed;
	}
	
	public BigInteger getModulus() {
		return modulus;
	}
	
	public BigInteger getP() {
		return p;
	}
	
	public BigInteger getQ() {
		return q;
	}
}
