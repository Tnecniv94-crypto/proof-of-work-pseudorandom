package analysis;

public class Constants {
	public final static int BLUM_NUMBER_PQ_ROUNDS = 100; // 
	public final static int MAX_BLUM_BLUM_SHUB_PQ_RATIO = 2 << 10; // 2 < p/q <= 1024
	public final static int MAX_MILLER_RABIN_BASE = 1 << 20; // 2 <= a <= 1048576
	public final static int MILLER_RABIN_ROUNDS = 1 << 64; // 64 Rounds => pseudoprime number accepted as prime with probability less than (1/4)^64 = (1/2)^128
	public final static int BIG_INTEGER_CERTANITY = MILLER_RABIN_ROUNDS << 1;
	
	public final static int EQUIDISTRIBUTION_ROUNDS = 1000;
}
