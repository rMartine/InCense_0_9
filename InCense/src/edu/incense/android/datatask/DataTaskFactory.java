package edu.incense.android.datatask;

import java.util.List;
import android.content.Context;
import edu.incense.android.datatask.filter.*;
import edu.incense.android.datatask.model.Task;
import edu.incense.android.datatask.sink.*;
import edu.incense.android.datatask.trigger.*;
import edu.incense.android.sensor.*;

public class DataTaskFactory {
    public static DataTask createDataTask(Task task, Context context) {
        DataTask dataTask = null;

        switch (task.getTaskType()) {
        case AccelerometerSensor:
            long frameTime = task.getLong(AccelerometerSensor.ATT_FRAMETIME, 1000);
            long duration = task.getLong(AccelerometerSensor.ATT_DURATION, 500);
            Sensor sensor = AccelerometerSensor.createAccelerometer(
                    context, frameTime, duration);
            if (task.getSampleFrequency() > 0) {
                sensor.setSampleFrequency(task.getSampleFrequency());
            } else if (task.getPeriodTime() > 0) {
                sensor.setPeriodTime(task.getPeriodTime());
            }
            dataTask = new DataSource(sensor);
            task.setPeriodTime(1000);
            task.setSampleFrequency(-1.0f);
            break;
        case TimerSensor:
            long period = task.getLong("period", 1000);
            TimerSensor ts = new TimerSensor(context, period);
            dataTask = new DataSource(ts);
            ts.addSourceTask((DataSource) dataTask);
            break;
        case AudioSensor:
            long audioDuration = task.getLong("duration", -1);
            AudioSensor as = new AudioSensor(context, task.getSampleFrequency());
            dataTask = new AudioDataSource(as, audioDuration);
            as.addSourceTask((AudioDataSource) dataTask); // AudioSensor is
                                                          // faster than
                                                          // DataTask
            break;
        case BluetoothSensor:
            dataTask = new DataSource(new BluetoothSensor(context));
            break;
        case BluetoothConnectionSensor:
            dataTask = new DataSource(new BluetoothConnectionSensor(context,
                    task.getString("address", "")));
            break;
        case GpsSensor:
            dataTask = new DataSource(new GpsSensor(context));
            break;
        case GyroscopeSensor:
            long frameTime2 = task.getLong(AccelerometerSensor.ATT_FRAMETIME, 1000);
            long duration2 = task.getLong(AccelerometerSensor.ATT_DURATION, 500);
            Sensor sensor2 = AccelerometerSensor.createGyroscope(
                    context, frameTime2, duration2);
            if (task.getSampleFrequency() > 0) {
                sensor2.setSampleFrequency(task.getSampleFrequency());
            } else if (task.getPeriodTime() > 0) {
                sensor2.setPeriodTime(task.getPeriodTime());
            }
            dataTask = new DataSource(sensor2);
            task.setPeriodTime(frameTime2);
            task.setSampleFrequency(-1.0f);
            break;
        case CallSensor:
            dataTask = new DataSource(new PhoneCallSensor(context));
            break;
        case StateSensor:
            dataTask = new DataSource(new PhoneStateSensor(context));
            break;
        case PowerConnectionSensor:
            dataTask = new DataSource(new PowerConnectionSensor(context));
            break;
        case NfcSensor:
            dataTask = new DataSource(new NfcSensor(context));
            break;
        case WifiScanSensor:
            dataTask = new DataSource(new WifiScanSensor(context));
            break;
        case WifiConnectionSensor:
            // String[] ap = task.getStringArray("accessPoints");
            // List<String> apList = Arrays.asList(ap);
            dataTask = new DataSource(new WifiConnectionSensor(context));
            break;
        case AccelerometerMeanFilter:
            dataTask = new AccelerometerMeanFilter();
            break;
        case DataSink:
            // Set SinkWritter type (Json)
            // It will write results to a JSON file
            dataTask = new DataSink(new JsonSinkWritter(context));
            ((DataSink) dataTask).setName(task.getName());
            break;
        case AudioSink:
            // Set SinkWritter type (Json)
            // It will write results to a RAW file
            dataTask = new AudioSink(new RawAudioSinkWritter(context));
            ((DataSink) dataTask).setName(task.getName());
            break;
        case ShakeFilter:
            dataTask = new ShakeFilter();
            break;
        case MovementFilter:
            double threshold = task.getDouble("threshold", 1000);
            dataTask = new MovementFilter();
            ((MovementFilter) dataTask).setMovementThreshold((float)threshold);
            break;
        case FalseTimerFilter:
            long timeLength = task.getLong("timeLength", 1000);
            String attributeName = task.getString("attributeName", "");
            dataTask = new FalseTimerFilter();
            ((FalseTimerFilter) dataTask).setTimeLength(timeLength);
            ((FalseTimerFilter) dataTask).setAttributeName(attributeName);
            break;
        case MovementTimeFilter:
            long maxNoInput = task.getLong("maxNoInput", 30000L);
            long maxNoMovement = task.getLong("maxNoMovement", 5000L);
            dataTask = new MovementTimeFilter();
            ((MovementTimeFilter) dataTask).setMaxNoInput(maxNoInput);
            ((MovementTimeFilter) dataTask).setMaxNoMovement(maxNoMovement);
            break;
        case WifiTimeConnectedFilter:
            dataTask = new WifiTimeConnectedFilter();
            break;
        case SurveyTrigger:
            dataTask = new SurveyTrigger(context);
            ((SurveyTrigger) dataTask).setSurveyName("mainSurvey");// task.getString("surveyName",
            break;
        case Trigger:
            String matches = task.getString(JsonTrigger.MATCHES, null);
            JsonTrigger jsonTrigger = new JsonTrigger();
            List<Condition> conditionsList = jsonTrigger.toConditions(task.getJsonNode());
            dataTask = new GeneralTrigger(context, conditionsList, matches);
            break;
        case StopTrigger:
            String matches2 = task.getString(JsonTrigger.MATCHES, null);
            JsonTrigger jsonTrigger2 = new JsonTrigger();
            List<Condition> conditionsList2 = jsonTrigger2.toConditions(task
                    .getJsonNode());
            dataTask = new StopTrigger(context, conditionsList2, matches2);
            break;
        case AccHighPassFilter:
        	dataTask = new AccHighPassFilter();
        	break;
        case AccVectorMagFilter:
        	dataTask = new AccVectorMagFilter();
        	break;
        case StepsFilter:
        	dataTask = new StepsFilter();
        	break;
        case CaloriesFilter:
        	dataTask = new CaloriesFilter();
        	break;
        case AccRMSFilter:
        	dataTask = new AccRMSFilter();
        	break;
        case AccActivityDetectorSMOFilter:
        	dataTask = new AccActivityDetectorSMOFilter();
        	break;
        case VAD16BitFilter:
        	dataTask = new VAD16BitFilter();
        	break;
        case NFCPublicTransportFilter:
        	dataTask = new NFCPublicTransportFilter();
        	break;
        default:
            return null;
        }
        if (task.getSampleFrequency() > 0) {
            dataTask.setSampleFrequency(task.getSampleFrequency());
        } else if (task.getPeriodTime() > 0) {
            dataTask.setPeriodTime(task.getPeriodTime());
        }
        dataTask.setTaskType(task.getTaskType());
        dataTask.setName(task.getName());
        dataTask.setTriggered(task.isTriggered());
        return dataTask;
    }
}
