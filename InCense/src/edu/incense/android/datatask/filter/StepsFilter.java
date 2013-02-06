package edu.incense.android.datatask.filter;
import edu.incense.android.datatask.data.AccVectorMagData;
import edu.incense.android.datatask.data.Data;
import edu.incense.android.datatask.data.StepsData;

public class StepsFilter extends DataFilter{
	public short n = 10; //Variable para saber cuantos datos se usarán para suavizar al señal.
	public double[] datoSuavizado = new double[n]; //Aquí s eguardara el dato de la señal suavizada.
	public byte primerasMuestras; //Necesario para llenar el vector de datos suavizado al inicio
	public double nivel = 1.14351917629104; //1 desviación estandar
	//public double nivel = 0.892897124657045; //2 desviaciones estandar
	public double ventanaTiempoPaso = 20000000.0;
	public double[] registroDatos = new double[2];
	public short cuentaPasos; //Acumulador para contar pasos
	public double timeAnt; //Timestamp del evento anterior para que no cuente dos eventos muy seguidos
	
	public StepsFilter(){
		super();
		setFilterName(this.getClass().getName());
	}
	
	@Override
	protected void computeSingleData(Data data) {
		countSteps(data);
	}
	
	@Override
    public void start() {
        registroDatos[0] = 10.0;
        registroDatos[1] = 10.0;
        cuentaPasos=0;
    	timeAnt=0.0;
		primerasMuestras = 0;
		datoSuavizado[0] = 0.0;
		datoSuavizado[1] = 0.0;
		datoSuavizado[2] = 0.0;
		datoSuavizado[3] = 0.0;
		datoSuavizado[4] = 0.0;
		datoSuavizado[5] = 0.0;
		datoSuavizado[6] = 0.0;
		datoSuavizado[7] = 0.0;
		datoSuavizado[8] = 0.0;
		datoSuavizado[9] = 0.0;
		super.start();
    }

	private void countSteps(Data data) {
		double avgSuavizado;//variable auxiliar para hacer el suavizado de las muestras 
		int i, j; //Contador auxiliar apara cualquier ciclo
		AccVectorMagData accMagnitude = (AccVectorMagData) data;
		double[][] frame = accMagnitude.getFrame();
		double[] stepsTime = new double[frame.length];
		double[] steps;

		for (j=0; j<frame.length; j++){
			if (primerasMuestras < n){
				datoSuavizado[primerasMuestras] = frame[j][0];
				avgSuavizado = 0;
				primerasMuestras++;
				for (i=0; i<primerasMuestras; i++){
					avgSuavizado = avgSuavizado + datoSuavizado[i];
				}
				avgSuavizado = avgSuavizado / primerasMuestras;
			} else {
				avgSuavizado = 0;
				for (i=0; i<(n-1); i++){
					datoSuavizado[i] = datoSuavizado[i+1];
					avgSuavizado = avgSuavizado + datoSuavizado[i];
				}
				datoSuavizado[i] =  frame[j][0];
				avgSuavizado = (avgSuavizado + datoSuavizado[i]) / primerasMuestras;
			}
			registroDatos[0] = registroDatos[1];
			registroDatos[1] = avgSuavizado;
			stepsTime[j] = 0.0;
			if ((registroDatos[0] >= nivel && registroDatos[1] < nivel) && ((frame[j][1] - timeAnt)>=ventanaTiempoPaso)){
				cuentaPasos++;
				stepsTime[cuentaPasos-1] = frame[j][1];
				timeAnt = frame[j][1];
			}
		}
		steps = new double[cuentaPasos];
		for (j=0; j<cuentaPasos; j++){
			steps[j] = stepsTime[j];
		}
		StepsData stepsData = new StepsData(steps);
		pushToOutputs(stepsData);
	}
}
