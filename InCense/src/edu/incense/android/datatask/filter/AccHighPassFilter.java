package edu.incense.android.datatask.filter;

//import android.util.Log;
import edu.incense.android.datatask.data.AccHighPassData;
import edu.incense.android.datatask.data.AccelerometerFrameData;
import edu.incense.android.datatask.data.Data;

public class AccHighPassFilter extends DataFilter {
	//private static final String ATT_STEPS = "HighPassFilterAcc:";
	public double[] gravedad = new double[3];
	public double[] accLineal = new double[3];
	public double alpha;
	
	public AccHighPassFilter(){
		super();
		setFilterName(this.getClass().getName());
	}
	
	@Override
    public void start() {
		gravedad[0] = 0.0;
		gravedad[1] = 0.0;
		gravedad[2] = 0.0;
		accLineal[0] = 0.0;
		accLineal[1] = 0.0;
		accLineal[2] = 0.0;
		alpha = 0.8;
		
		super.start();
	}

	@Override
	protected void computeSingleData(Data data) {
		hPassFilter(data);
	}
	
	private void hPassFilter(Data data){
		AccelerometerFrameData accData = (AccelerometerFrameData) data;
        double[][] dataAcc = accData.getFrame();
        double[][] dataAccLineal = new double[dataAcc.length][4];
        for(int i=0; i<dataAcc.length; i++){
        	gravedad[0] = alpha * gravedad[0] + (1 - alpha) * dataAcc[i][0];
			gravedad[1] = alpha * gravedad[1] + (1 - alpha) * dataAcc[i][1];
			gravedad[2] = alpha * gravedad[2] + (1 - alpha) * dataAcc[i][2];
			accLineal[0] = dataAcc[i][0] - gravedad[0];
			accLineal[1] = dataAcc[i][1] - gravedad[1];
			accLineal[2] = dataAcc[i][2] - gravedad[2];
			dataAccLineal[i][0] = accLineal[0];
			dataAccLineal[i][1] = accLineal[1];
			dataAccLineal[i][2] = accLineal[2];
			dataAccLineal[i][3] = dataAcc[i][3];
    	}
        AccHighPassData accLinealData = new AccHighPassData(dataAccLineal); 
		pushToOutputs(accLinealData);
	}
}
