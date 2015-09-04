package jamel.austrian.util;

@SuppressWarnings("serial")
public class DownwardComparablePoint extends Point3d implements Comparable<DownwardComparablePoint> {

	public DownwardComparablePoint(double double1, double double2, double double3) {
		super(double1, double2, double3);
	}

	public int compareTo(DownwardComparablePoint o){
		if (this.y<o.y) return 1;
		if (this.y>o.y) return -1;
		return 0;
		}
	}