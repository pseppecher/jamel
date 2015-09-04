package jamel.austrian.widgets;


import java.util.HashMap;

import jamel.austrian.Parameters;
import jamel.austrian.util.Point3d;


public class ProductionFunction { 

	/** The mapping of known input-output combinations. */
	public final HashMap<Point3d,Integer> outputMapping = new HashMap<Point3d,Integer>();
	
	/** The mapping of known input-capacity combinations. */
	public final HashMap<Point3d,Integer> capacityMapping = new HashMap<Point3d,Integer>();
	
	/** The mapping of known input-excessLabor combinations. */
	public final HashMap<Point3d,Integer> excessLaborMapping = new HashMap<Point3d,Integer>();
	
	/** The inverse of the capacityMapping for given k. */
	public final HashMap<Point3d,Integer> laborMapping = new HashMap<Point3d,Integer>();
	
	/** The productivity of labor. */
	private static int prL;
	
	/** The productivity of combining labor with intermediate goods. 
	 *  Must be greater than 2*prL.*/
	private static int prX;
	
	/** The processing capacity of a worker. */
	private static int capL;
	
	/** The processing capacity of a machine-worker. */
	private static int capK;

	/** The durability of k. */
	private static int tauK;

	
	/** Describes the returns to fixed capital. <p>
	 *  phi=0 -> constant returns to capital intensification.<br>
	 *  phi>0 -> decreasing returns.<br>
	 *  phi<0 -> increasing returns.  */
	private static float phi;
	
	
	public ProductionFunction(Parameters parameters){
		prL = parameters.prL;
		prX = parameters.prX;
		capL =  parameters.capL;
		phi =  parameters.phi;
		tauK =  parameters.tauK;
		getOutput(0, 0, 0);
		getOutput(1, 0, 0);
		getLaborRequirement(0, 0, 0);
	}
	
	


	/** 
	 * Returns the value of the production function.
	 */
	public int getOutput(int l, int x, int k){
		
		Point3d coordinates = new Point3d(l,x,k);
		if (outputMapping.containsKey(coordinates)) return outputMapping.get(coordinates);
		int f = function(l,x,k);
		return f;
	}
	
	
	/**
	 * Returns the number of workers that are necessary to attain a certain processing capacity.
	 */
	public int getLaborRequirement(int cap, int x, int k) {

		Point3d coordinates = new Point3d(cap, x, k);
		if (laborMapping.containsKey(coordinates)) return laborMapping.get(coordinates);
		int l = 0, cap2 = 0;
		while (cap2<cap){
			l++;
			cap2 = processingCapacity(l, k);
		}
		laborMapping.put(new Point3d(cap,x,k),l);
		return l;
	}
	
	
	/**
	 * Returns the increment in capacity when adding one worker.
	 */
	public int getCapacityIncrement(int l, int x, int k) {

		int currentCapacity = getProcessingCapacity(l, x, k);
		int newCapacity = getProcessingCapacity(l+1, x, k);
		return newCapacity-currentCapacity;
	}
	
	
	/**
	 * Returns the capacity per worker to handle intermediate goods.
	 */
	public int getProcessingCapacity(int l, int x, int k) {
		Point3d coordinates = new Point3d(l,x,k);
		if (capacityMapping.containsKey(coordinates)) return capacityMapping.get(coordinates);
		int capacity = processingCapacity(l, k);
		capacityMapping.put(coordinates, capacity);
		return capacity;
	}
	

	/**
	 * Returns the number of workers who work without capital.
	 */
	public int getExcessLabor(int l, int x, int k) {
		Point3d coordinates = new Point3d(l,x,k);
		return excessLaborMapping.get(coordinates);
	}
	
	
	public float getPrL() {
		return prL;
	}
	
	
	public int getPrX() {
		return prX;
	}
	
	
	public int getCapL() {
		return capL;
	}
	
	
	public int getTauK() {
		return tauK;
	}
	
	/** 
	 * The function itself.
	 */
	private int function(int l, int x, int k){
		
		Point3d coordinates = new Point3d(l,x,k);
		int processingCapacity = 0;
		int excessLabor = 0;
		if (l==0){
			outputMapping.put(coordinates, 0);
			capacityMapping.put(coordinates, processingCapacity);
			excessLaborMapping.put(coordinates, excessLabor);
			return 0;
		}

		if (l>=k){										
			processingCapacity = capK * k + capL * (l - k);// * (1+(float)x/psi));
			excessLabor = Math.max(l - Math.min( (int) Math.ceil( (float)x/(float)capK ), k)  
					- Math.max((int) Math.ceil( (float)(x-capK*k)/(float)capL ), 0)  , 0);
			/*excessLabor = Math.max(l - Math.min( (int) Math.ceil( (float)x/(float)capK ), k) 
					- Math.max(x-capK*k, 0), 0);*/
		}
		
		// This case is currently not in use. 
		// Needed if capital accumulation is no longer limited. 
		else{
			float terms, rest, capacity;
			HashMap<Integer,Integer> processing_Capacities = new HashMap<Integer,Integer>();

			for (int i=1; i<=l; i++){
				terms = k/i;
				rest = k % i / (float) Math.pow((terms + 1),phi);
				capacity = 0.0f;
				for (float n=1; n<=terms; n++) capacity+=i/ (float) Math.pow(n,phi); 
				processingCapacity = (int) (capK*(capacity+rest));//*(1+(float)x/psi)); 
				processing_Capacities.put(i, processingCapacity); 
			}

			if (x==0) excessLabor = l;
			else excessLabor = 0;
			/*else{
				for (int j=1; j<l; j++){
					if (processing_Capacities.get(l-j)>=x) excessLabor++;
				}
			}*/
		}
		
		int output = prX * Math.min(processingCapacity, x) + prL * excessLabor;
		
		outputMapping.put(coordinates, output);
		capacityMapping.put(coordinates, processingCapacity);
		excessLaborMapping.put(coordinates, excessLabor);

		return output; 
	}

	
	/**
	 * Returns the processing capacity for a given bundle of input factors.
	 */
	private static int processingCapacity(int l, int k) {
		
		if (l>=k) return capK * k + capL * (l - k );
		else if (l==0) return 0;
		else {					
			float terms = k/l;
			float rest = k % l / (float) Math.pow((terms + 1),phi);
			float capacity = 0.0f;
			for (float n=1; n<=terms; n++) capacity += l/ (float) Math.pow(n,phi); 
			return (int) (capK*(capacity+rest));
		}
	}
}
