package edu.incense.android.datatask.filter;

import edu.incense.android.datatask.data.AccActivityDetectorSMOData;
import edu.incense.android.datatask.data.AccRMSData;
import edu.incense.android.datatask.data.Data;

public class AccActivityDetectorSMOFilter extends DataFilter{
	//private static final String ATT_STEPS = "HighPassFilterAcc:";
	public AccActivityDetectorSMOFilter(){
		super();
		setFilterName(this.getClass().getName());
	}
	
	@Override
    public void start() {
		super.start();
	}
	
	@Override
	protected void computeSingleData(Data data) {
		accRMSFilter(data);
	}
	
	private void accRMSFilter(Data data){
		AccRMSData accRMSData = (AccRMSData) data;
        double[][] dataRMS = accRMSData.getFrame();
        int i;
        double[][] results = new double[(int)(dataRMS.length)][];
        double res;
        for (i = 1; i<=dataRMS.length; i++){
        	res = ((-5.1762)*(dataRMS[i][0]/2.651495))+1.5288;
			if (res<=0){
				results[i][0] = 0.0;
			} else {
				results[i][0] = 1.0;
			}
			results[i][1] = dataRMS[i][1];
        }
        AccActivityDetectorSMOData accRMSResults = new AccActivityDetectorSMOData(results); 
		pushToOutputs(accRMSResults);
	}
}
