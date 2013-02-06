package edu.incense.android.datatask.data;

public class CaloriesData extends Data{
	public final static int DISTANCE = 0;
	public final static int AVGSPEED = 1;
	public final static int ENERGYEXPENDITURECALORIES= 2;
	public final static int EVENTTIMESTAMP = 3;
	private double[][] frame;
    
    private CaloriesData(DataType type, double[][] frame) {
        super(type);
        this.frame = frame;
    }
    
    public CaloriesData(double[][] frame) {
        this(DataType.CALORIES, frame);
    }

    /**
     * @param frame
     *            the frame to set
     */
    public void setFrame(double[][] frame) {
        this.frame = frame;
    }

    /**
     * @return the frame
     */
    public double[][] getFrame() {
        return frame;
    }
}
