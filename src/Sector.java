import java.util.Comparator;


public class Sector implements Comparator<Sector>, Comparable<Sector>{
	private int numLoans;
	private int numLoansDefaulted;
	
	Sector (){
		setNumLoans(0);
		setNumLoansDefaulted(0);
	}
	
	void incrementLoans() {
		setNumLoans(getNumLoans() + 1);
	}
	
	void incrementLoansDefaulted() {
		setNumLoansDefaulted(getNumLoansDefaulted() + 1);
	}

	public int getNumLoans() {
		return numLoans;
	}

	public void setNumLoans(int numLoans) {
		this.numLoans = numLoans;
	}

	public int getNumLoansDefaulted() {
		return numLoansDefaulted;
	}

	public void setNumLoansDefaulted(int numLoansDefaulted) {
		this.numLoansDefaulted = numLoansDefaulted;
	}

	@Override
	public int compare(Sector o1, Sector o2) {
		float p1,p2;
		int ret = 0;	
		
		p1 = (float)o1.getNumLoansDefaulted()/o1.getNumLoans()*100;
		p2 = (float)o2.getNumLoansDefaulted()/o2.getNumLoans()*100;
		
		if (p1 > p2)
			ret = 1;
		else if (p1 < p2)
			ret = -1;
		
		return ret;
	}

	@Override
	public int compareTo(Sector o) {
		return compare(this, o);
	}
}
