package edu.incense.android.datatask.data;

public class VAD16BitData extends Data{
	public static byte CLASIFY = 0;
	public static byte ENTROPY = 1;
	public static byte CENTROID = 2;
	public static byte BANDWIDTH = 3;
	public static byte ZCR = 4;
	public static byte RMS = 5;
	public static byte VAL_VOICE = 1;
	public static byte VAL_NOVOICE = -1;
	private double[][] vadClassified;
	
	private VAD16BitData(DataType type, double[][] vadClassified) {
		super(type);
		this.vadClassified = vadClassified;
	}
	
	public VAD16BitData(double[][] vadClassified) {
		this(DataType.VAD16BIT, vadClassified);
	}
	
	public double[][] getResults(){
		return vadClassified;
	}
	
	public void setResults(double[][] vadClassified){
		this.vadClassified = vadClassified;
	}

}
