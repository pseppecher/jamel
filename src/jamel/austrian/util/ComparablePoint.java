package jamel.austrian.util;

@SuppressWarnings("serial")
public class ComparablePoint extends Point3d implements Comparable<ComparablePoint> {

	public ComparablePoint(double double1, double double2, double double3) {
		super( double1, double2, double3 );
	}

	public int compareTo(ComparablePoint o){
		if (this.y<o.y) return -1;
		if (this.y>o.y) return 1;
		return 0;
		}
	}