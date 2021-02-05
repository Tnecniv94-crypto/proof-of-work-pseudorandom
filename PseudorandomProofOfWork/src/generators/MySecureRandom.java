package generators;

import java.math.BigInteger;
import java.security.SecureRandom;

public class MySecureRandom extends SecureRandom {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 * @param bitLength
	 * @return a random number with bitLength bits
	 */
	public BigInteger nextRand(int bitLength) {
		String bits = "";
		
		for(int i = 0; i < bitLength; i++) {
			bits += nextBoolean() ? "1" : "0"; 
		}
		
		return new BigInteger(bits, 2);
	}

}
