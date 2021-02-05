package analysis;

import java.io.PrintStream;

public class Print extends PrintStream {
	private boolean dump;
	
	public Print(PrintStream stream, boolean dump) {
		super(stream);
		
		this.dump = dump;
	}
	
	@Override
	public void println(Object print) {
		if(dump) {
			super.println(print);
		}
	}
}
