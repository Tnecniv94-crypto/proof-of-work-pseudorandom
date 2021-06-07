package analysis;

import java.math.BigInteger;

public class WeakSeed {
	private BigInteger seed, modulus, p, q;
	private double ratio;
	private int securityParameter, periodLength;
	
	public WeakSeed(BigInteger seed, BigInteger modulus, BigInteger p, BigInteger q, int periodLength, double ratio, int securityParameter) {
		this.seed = seed;
		this.modulus = modulus;
		this.p = p;
		this.q = q;
		this.periodLength = periodLength;
		this.ratio = ratio;
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
	
	public int getPeriodLength() {
		return periodLength;
	}
	
	public double getRatio() {
		return ratio;
	}
	
	public int getSecurityParameter() {
		return securityParameter;
	}
}
