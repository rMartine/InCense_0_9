package edu.incense.android.datatask.filter;

import edu.incense.android.datatask.data.AccRMSData;
import edu.incense.android.datatask.data.AccVectorMagData;
import edu.incense.android.datatask.data.Data;

public class AccRMSFilter extends DataFilter{
	//private static final String ATT_STEPS = "HighPassFilterAcc:";
	private int tamVentana;
	public AccRMSFilter(){
		super();
		setFilterName(this.getClass().getName());
	}
	
	@Override
    public void start() {
		tamVentana = 30; //Aprox. 20 ms * 30
		super.start();
	}
	
	@Override
	protected void computeSingleData(Data data) {
		accRMSFilter(data);
	}
	
	private void accRMSFilter(Data data){
		AccVectorMagData vMagData = (AccVectorMagData) data;
        double[][] dataVMag = vMagData.getFrame();
        int i;
        int nMuestras = 0;
        double acumMag = 0;
        double[][] results = new double[(int)(dataVMag.length/tamVentana)][2];
        for (i = 1; i<=dataVMag.length; i++){
        	acumMag += dataVMag[i-1][0];
        	if ((i % tamVentana) == 0){
        		results[nMuestras][0] = (double) (acumMag/tamVentana);
        		results[nMuestras][1] = dataVMag[i-1][1];
        		nMuestras++;
        	}
        }
        AccRMSData accRMSResults = new AccRMSData(results); 
		pushToOutputs(accRMSResults);
	}
}
