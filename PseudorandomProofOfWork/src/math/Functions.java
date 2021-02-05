package math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;

public class Functions {
	/**
	 * https://stackoverflow.com/questions/47761383/proper-carmichael-function
	 * @param n
	 * @return
	 */
	public static BigInteger carmichael(BigInteger n) {
		ArrayList<BigInteger> elements = groupElements(n);
		BigInteger ret = BigInteger.TWO;
		int remaining = elements.size();
		
		if(elements.size() == 1) {
			return BigInteger.ONE;
		}
		
		while(remaining >= 0) {
			for(BigInteger e : elements) {
				if(e.modPow(ret, n).compareTo(BigInteger.ONE) == 0) {
					remaining--;
					
					if(remaining == 0) { // current exponent ret has g^ret = 1 mod n forall g in Z_n^*
						return ret;
					}
				}
				else {
					ret = ret.add(BigInteger.ONE);
					remaining = elements.size();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param modulus of Z_modulus^*
	 * @return Z_modulus^*
	 */
	private static ArrayList<BigInteger> groupElements(BigInteger modulus) {
		LinkedList<BigInteger> elements = new LinkedList<>();
		BigInteger element = BigInteger.ONE;
		
		do { // 1 in Z_n^* forall n
			elements.add(element); 
			
		} while((element = nextGroupElement(element, modulus)) != null);
		
		
		return new ArrayList<>(elements);
	}
	
	/**
	 * 
	 * @param a in Z
	 * @param modulus of Z_modulus^*
	 * @return smallest e in Z_modulus^*: e >= a
	 */
	private static BigInteger nextGroupElement(BigInteger a, BigInteger modulus) {
		do {
			if(a.compareTo(modulus) >= 0) {
				return null;
			}
			
			a = a.add(BigInteger.ONE);
		} while(a.gcd(modulus).compareTo(BigInteger.ONE) != 0);
		
		return a;
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @return least common multiple
	 */
	public static BigInteger lcd(BigInteger a, BigInteger b) {
		return a.multiply(b).abs().divide(a.gcd(b));
	}
}
