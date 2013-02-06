/**
 * 
 */
package edu.incense.android.test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.os.Environment;
import android.text.format.Time;
import edu.incense.android.R;
import edu.incense.android.datatask.filter.MovementFilter;
import edu.incense.android.datatask.filter.WifiTimeConnectedFilter;
import edu.incense.android.datatask.model.Task;
import edu.incense.android.datatask.model.TaskRelation;
import edu.incense.android.datatask.model.TaskType;
import edu.incense.android.datatask.trigger.Condition;
import edu.incense.android.datatask.trigger.GeneralTrigger;
import edu.incense.android.project.Project;
import edu.incense.android.sensor.WifiConnectionSensor;
import edu.incense.android.session.Session;
import edu.incense.android.survey.Survey;

/**
 * Project examples for testing
 * 
 * @author Moises Perez (mxpxgx@gmail.com)
 * @version 0.1, May 20, 2011
 * 
 */
public class ProjectGenerator {
    /**
     * Audio project
     */
    public static void buildProjectJsonA(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Session
        Session session = new Session();
        session.setDurationUnits(60);
        session.setDurationMeasure("minutes");
        session.setAutoTriggered(true);
        Time time = new Time();
        time.setToNow();
        time.set(time.monthDay - 1, time.month, time.year);
        session.setStartDate(time.normalize(false));

        time.setToNow();
        time.set(time.monthDay + 7, time.month, time.year);
        session.setEndDate(time.normalize(false));

        session.setName("GPS Session");

        List<Task> tasks = new ArrayList<Task>();

        Task audioSensor = TaskGenerator.createAudioSensor(mapper, 44100,
                1000 * 60 * 15); // rate: 44100Hz, duration: 25 seconds
        tasks.add(audioSensor);

        Task audioSink = TaskGenerator.createTaskWithPeriod(mapper,
                "AudioSink", TaskType.AudioSink, 1000);
        tasks.add(audioSink);

        Condition ifTimerSaysSo = TaskGenerator.createCondition("value",
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifTimerSaysSo);
        Task audioTrigger = TaskGenerator.createTrigger(mapper, "AudioTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(audioTrigger);

        Task timerSensor = TaskGenerator.createTimerSensor(mapper, 1000,
                1000 * 60 * 30); // each 2min
        tasks.add(timerSensor);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(timerSensor.getName(), audioTrigger
                                .getName()),
                        new TaskRelation(audioTrigger.getName(), audioSensor
                                .getName()),
                        new TaskRelation(audioSensor.getName(), audioSink
                                .getName()) });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);

        writeProject(context, mapper, project);
    }

    /**
     * Survey + Shake
     * 
     * @param resources
     */
    public static void buildProjectJsonB(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Survey
        Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(1);
        session.setDurationMeasure("minutes");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 5, 1000, 500);
        tasks.add(accSensor);

        Task shakeFilter = new Task();
        shakeFilter.setName("ShakeFilter");
        shakeFilter.setTaskType(TaskType.ShakeFilter);
        shakeFilter.setPeriodTime(1000);
        tasks.add(shakeFilter);

        Condition ifShake = TaskGenerator.createCondition("isShake", GeneralTrigger.DataType.BOOLEAN.name(), GeneralTrigger.booleanOperators[0]); // "is true"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifShake);
        
        Task surveyTrigger = TaskGenerator.createTrigger(mapper, "SurveyTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(surveyTrigger);

        Task nfcSensor = TaskGenerator.createNfcSensor(mapper, 44100);
        tasks.add(nfcSensor);

        List<TaskRelation> relations = Arrays.asList(
			new TaskRelation[] {
	            new TaskRelation(accSensor.getName(), shakeFilter.getName()),
	            new TaskRelation(shakeFilter.getName(), surveyTrigger.getName()),
	            new TaskRelation(surveyTrigger.getName(), "mainSurvey")
	        }
		);

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(1);
        project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * GPS + Wifi + Acc, no audio
     * 
     * @param resources
     */
    public static void buildProjectJsonE(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 4L); // 4days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        Task wifiSensor = TaskGenerator.createWifiConnectionSensor(mapper,
                1000, new String[] { "AppleBS4" });
        tasks.add(wifiSensor);

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 20,
                10000, 5000);
        tasks.add(accSensor);

        Condition ifNotConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[1]); // "is false"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifNotConnected);
        Task gpsTrigger = TaskGenerator.createTrigger(mapper, "GpsTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsTrigger);

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        // Task gpsSensor = TaskGenerator.createGpsSensor(mapper, period);
        // tasks.add(gpsSensor);

        Condition ifConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is false"
        conditions = new ArrayList<Condition>();
        conditions.add(ifConnected);
        Task gpsStopTrigger = TaskGenerator.createStopTrigger(mapper,
                "GpsStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsStopTrigger);

        Task audioSensor = TaskGenerator.createAudioSensor(mapper, 44100,
                1000 * 25); // rate: 44100Hz, duration: 25 seconds
        tasks.add(audioSensor);

        Task audioSink = TaskGenerator.createTaskWithPeriod(mapper,
                "AudioSink", TaskType.AudioSink, 1000);
        tasks.add(audioSink);

        Condition ifTimerSaysSo = TaskGenerator.createCondition("value",
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifTimerSaysSo);
        Task audioTrigger = TaskGenerator.createTrigger(mapper, "AudioTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(audioTrigger);

        Task timerSensor = TaskGenerator.createTimerSensor(mapper, 1000,
                1000 * 60 * 60 * 3); // each 3 hour
        tasks.add(timerSensor);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(timerSensor.getName(), audioTrigger
                                .getName()),
                        new TaskRelation(audioTrigger.getName(), audioSensor
                                .getName()),
                        new TaskRelation(audioSensor.getName(), audioSink
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), gpsTrigger
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), gpsStopTrigger
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(accSensor.getName(), dataSink
                                .getName()) });
        // new TaskRelation(gpsSensor.getName(), dataSink.getName()),
        // new TaskRelation(gpsStopTrigger.getName(), gpsSensor.getName()),
        // new TaskRelation(gpsTrigger.getName(), gpsSensor.getName())});

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(1);
        project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    private static void writeProject(Context context, ObjectMapper mapper, Project project) {
        String projectFilename = context.getResources().getString(R.string.project_filename);
        String parentDirectory = context.getResources().getString(R.string.application_root_directory) + "/project/";;
        File parent = new File(Environment.getExternalStorageDirectory(), parentDirectory);
        parent.mkdirs();
        try {
            File file = new File(parent, projectFilename);
            //OutputStream output = context.openFileOutput(projectFilename, 0);
            mapper.writeValue(file, project);
            //mapper.writeValue(output, project);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
            System.out.println(e);
        } catch (JsonMappingException e) {
            e.printStackTrace();
            System.out.println(e);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    /**
     * Audio
     * 
     * @param resources
     */
    public static void buildProjectJsonF(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Session
        Session session = new Session();
        session.setDurationUnits(1); // 1 minute
        session.setDurationMeasure("minutes");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        Task wifiSensor = TaskGenerator.createWifiConnectionSensor(mapper,
                1000, new String[] { "AppleBS4" });
        tasks.add(wifiSensor);

        Task wifiFilter = new Task();
        wifiFilter.setName("WifiTimeConnectedFilter");
        wifiFilter.setTaskType(TaskType.WifiTimeConnectedFilter);
        wifiFilter.setPeriodTime(1000);
        tasks.add(wifiFilter);

        Condition ifTimeDisconnectedGreater = TaskGenerator.createCondition(
                WifiTimeConnectedFilter.ATT_TIMEDISCONNECTED,
                GeneralTrigger.DataType.NUMERIC.name(),
                GeneralTrigger.numericOperators[2],// "is greater than"
                String.valueOf(5000));
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifTimeDisconnectedGreater);
        Task surveyTrigger = TaskGenerator.createTrigger(mapper,
                "SurveyTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(surveyTrigger);

        Condition ifTimeConnectedGreater = TaskGenerator.createCondition(
                WifiTimeConnectedFilter.ATT_TIMECONNECTED,
                GeneralTrigger.DataType.NUMERIC.name(),
                GeneralTrigger.numericOperators[2],// "is greater than"
                String.valueOf(5000));
        conditions = new ArrayList<Condition>();
        conditions.add(ifTimeConnectedGreater);
        Task surveyTrigger2 = TaskGenerator.createTrigger(mapper,
                "SurveyTrigger2", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(surveyTrigger2);

        // Task nfcSensor = TaskGenerator.createNfcSensor(mapper, 44100);
        // tasks.add(nfcSensor);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(wifiSensor.getName(), wifiFilter
                                .getName()),
                        new TaskRelation(wifiFilter.getName(), surveyTrigger
                                .getName()),
                        new TaskRelation(wifiFilter.getName(), surveyTrigger2
                                .getName()),
                        new TaskRelation(surveyTrigger2.getName(), "mainSurvey"),
                        new TaskRelation(surveyTrigger.getName(), "mainSurvey") });

        session.setTasks(tasks);
        session.setRelations(relations);

        Survey survey = SurveyGenerator.createWanderingMindSurvey();

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(1);
        project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * Accelerometer
     * 
     * @param context
     */
    public static void buildProjectJsonG(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 4L); // 4days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 44,
                10000, 10000);
        tasks.add(accSensor);

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] { new TaskRelation(accSensor
                        .getName(), dataSink.getName()) });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);

        writeProject(context, mapper, project);
    }

    /**
     * GPS
     * 
     * @param context
     */
    public static void buildProjectJsonH(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Session
        Session session = new Session();
        session.setName("mainSession");
        session.setDurationUnits(24L * 4L); // 4 days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())
        session.setAutoTriggered(true);
        Time time = new Time();
        time.setToNow();
        time.set(time.monthDay - 1, time.month, time.year);
        session.setStartDate(time.normalize(false));

        time.setToNow();
        time.set(time.monthDay + 7, time.month, time.year);
        session.setEndDate(time.normalize(false));

        session.setNotices(true);
        session.setRepeat(false);
        session.setSessionType("Automatic");

        List<Task> tasks = new ArrayList<Task>();

        Task accSensor = TaskGenerator.createGpsSensor(mapper, 10000);
        tasks.add(accSensor);

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] { new TaskRelation(accSensor
                        .getName(), dataSink.getName()) });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);

        writeProject(context, mapper, project);
    }

    /**
     * WiFi
     * 
     * @param context
     */
    public static void buildProjectJsonI(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 4L); // 4days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        Task wifiSensor = TaskGenerator.createWifiConnectionSensor(mapper,
                1000, new String[] { "AppleBS4" });
        tasks.add(wifiSensor);

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] { new TaskRelation(wifiSensor
                        .getName(), dataSink.getName()) });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);

        writeProject(context, mapper, project);
    }

    /**
     * GPS + Wifi + Acc, no audio
     * 
     * @param resources
     */
    public static void buildProjectJsonJ(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 4L); // 4days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        Task wifiSensor = TaskGenerator.createWifiConnectionSensor(mapper,
                1000, new String[] { "AppleBS4" });
        tasks.add(wifiSensor);

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 44,
                10000, 5000);
        tasks.add(accSensor);

        Condition ifNotConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[1]); // "is false"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifNotConnected);
        Task gpsTrigger = TaskGenerator.createTrigger(mapper, "GpsTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsTrigger);

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        Task gpsSensor = TaskGenerator.createGpsSensor(mapper, 1000L * 30L);
        tasks.add(gpsSensor);

        Condition ifConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifConnected);
        Task gpsStopTrigger = TaskGenerator.createStopTrigger(mapper,
                "GpsStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsStopTrigger);

        Task audioSensor = TaskGenerator.createAudioSensor(mapper, 44100,
                1000 * 25); // rate: 44100Hz, duration: 25 seconds
        tasks.add(audioSensor);

        Task audioSink = TaskGenerator.createTaskWithPeriod(mapper,
                "AudioSink", TaskType.AudioSink, 1000);
        tasks.add(audioSink);

        Condition ifTimerSaysSo = TaskGenerator.createCondition("value",
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifTimerSaysSo);
        Task audioTrigger = TaskGenerator.createTrigger(mapper, "AudioTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(audioTrigger);

        Task timerSensor = TaskGenerator.createTimerSensor(mapper, 1000,
                1000 * 60 * 60 * 3); // each 3 hour
        tasks.add(timerSensor);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(timerSensor.getName(), audioTrigger
                                .getName()),
                        new TaskRelation(audioTrigger.getName(), audioSensor
                                .getName()),
                        new TaskRelation(audioSensor.getName(), audioSink
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), gpsTrigger
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), gpsStopTrigger
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(accSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(gpsSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(gpsStopTrigger.getName(), gpsSensor
                                .getName()),
                        new TaskRelation(gpsTrigger.getName(), gpsSensor
                                .getName()) });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(1);
        project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * GPS + Wifi + Acc + audio
     * 
     * @param resources
     */
    public static void buildProjectJsonK(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 4L); // 4days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        Task wifiSensor = TaskGenerator.createWifiConnectionSensor(mapper,
                1000, new String[] { "AppleBS4" });
        tasks.add(wifiSensor);

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 20,
                10000, 5000);
        tasks.add(accSensor);

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        Task gpsSensor = TaskGenerator.createGpsSensor(mapper, 1000L * 30L);
        tasks.add(gpsSensor);

        Task audioSensor = TaskGenerator.createAudioSensor(mapper, 44100,
                1000 * 25); // rate: 44100Hz, duration: 25 seconds
        tasks.add(audioSensor);

        Task audioSink = TaskGenerator.createTaskWithPeriod(mapper,
                "AudioSink", TaskType.AudioSink, 1000);
        tasks.add(audioSink);

        Condition ifTimerSaysSo = TaskGenerator.createCondition("value",
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifTimerSaysSo);
        Task audioTrigger = TaskGenerator.createTrigger(mapper, "AudioTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(audioTrigger);

        Task timerSensor = TaskGenerator.createTimerSensor(mapper, 1000,
                1000 * 60 * 60 * 1); // each 3 hour
        tasks.add(timerSensor);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(timerSensor.getName(), audioTrigger
                                .getName()),
                        new TaskRelation(audioTrigger.getName(), audioSensor
                                .getName()),
                        new TaskRelation(audioSensor.getName(), audioSink
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(accSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(gpsSensor.getName(), dataSink
                                .getName()), });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(1);
        project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * POWER
     * 
     * @param context
     */
    public static void buildProjectJsonL(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Session
        Session session = new Session();
        session.setName("mainSession");
        session.setDurationUnits(24L * 4L); // 4 days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())
        session.setAutoTriggered(true);
        Time time = new Time();
        time.setToNow();
        time.set(time.monthDay - 1, time.month, time.year);
        session.setStartDate(time.normalize(false));

        time.setToNow();
        time.set(time.monthDay + 7, time.month, time.year);
        session.setEndDate(time.normalize(false));

        session.setNotices(true);
        session.setRepeat(false);
        session.setSessionType("Automatic");

        List<Task> tasks = new ArrayList<Task>();

        Task powerSensor = TaskGenerator.createPowerConnectionSensor(mapper,
                1000);
        tasks.add(powerSensor);

        Condition ifPower = TaskGenerator.createCondition("value",
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[1]); // "is true"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifPower);
        Task surveyTrigger = TaskGenerator.createTrigger(mapper,
                "SurveyTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(surveyTrigger);

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        // Survey
        Survey survey = SurveyGenerator.createWanderingMindSurvey();

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(powerSensor.getName(), surveyTrigger
                                .getName()),
                        new TaskRelation(surveyTrigger.getName(), "mainSurvey") });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(1);
        project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * Acc + Movement
     * 
     * @param context
     */
    public static void buildProjectJsonM(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Session
        Session session = new Session();
        session.setName("mainSession");
        session.setDurationUnits(24L * 4L); // 4 days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())
        session.setAutoTriggered(true);
        Time time = new Time();
        time.setToNow();
        time.set(time.monthDay - 1, time.month, time.year);
        session.setStartDate(time.normalize(false));

        time.setToNow();
        time.set(time.monthDay + 7, time.month, time.year);
        session.setEndDate(time.normalize(false));

        session.setNotices(true);
        session.setRepeat(false);
        session.setSessionType("Automatic");

        List<Task> tasks = new ArrayList<Task>();

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 20,
                10000, 10000);
        tasks.add(accSensor);

        Task movementFilter = TaskGenerator.createMovementFilter(mapper, 1000,
                0.3f);
        tasks.add(movementFilter);

        Condition ifMovement = TaskGenerator.createCondition("isMovement",
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifMovement);
        Task surveyTrigger = TaskGenerator.createTrigger(mapper,
                "SurveyTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(surveyTrigger);

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        // Survey
        Survey survey = SurveyGenerator.createWanderingMindSurvey();

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(accSensor.getName(), movementFilter
                                .getName()),
                        new TaskRelation(movementFilter.getName(),
                                surveyTrigger.getName()),
                        new TaskRelation(surveyTrigger.getName(), "mainSurvey") });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(1);
        project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * GPS + Wifi + Acc + audio + NFC
     * 
     * @param resources
     */
    public static void buildProjectJsonN(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 4L); // 4days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        // Sensors

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 20,
                10000, 10000);
        tasks.add(accSensor);

        Task audioSensor = TaskGenerator.createAudioSensor(mapper, 44100,
                1000 * 60 * 2); // rate: 44100Hz, duration: 2 minutes
        audioSensor.setTriggered(true);
        tasks.add(audioSensor);

        Task gpsSensor = TaskGenerator.createGpsSensor(mapper, 1000L * 30L);
        gpsSensor.setTriggered(true);
        tasks.add(gpsSensor);

        Task nfcSensor = TaskGenerator.createNfcSensor(mapper, 500);
        tasks.add(nfcSensor);

        Task accTimerSensor = TaskGenerator.createTimerSensor(mapper, 1000,
                1000 * 20); // each 20 seconds
        accTimerSensor.setTriggered(true);
        tasks.add(accTimerSensor);

        Task wifiSensor = TaskGenerator.createWifiConnectionSensor(mapper,
                1000, new String[] { "AppleBS4" });
        tasks.add(wifiSensor);

        // Filters

        Task movementFilter = TaskGenerator.createMovementFilter(mapper, 1000,
                0.3f);
        tasks.add(movementFilter);

        Task falseTimerFilter = TaskGenerator.createFalseTimerFilter(mapper,
                1000, 5000L, "isMovement");
        tasks.add(falseTimerFilter);

        // Triggers

        Condition ifNotConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[1]); // "is false"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifNotConnected);
        Task gpsTrigger = TaskGenerator.createTrigger(mapper, "GpsTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsTrigger);

        Condition ifConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifConnected);
        Task gpsStopTrigger = TaskGenerator.createStopTrigger(mapper,
                "GpsStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsStopTrigger);

        Condition ifNotNull = TaskGenerator.createCondition("message",
                GeneralTrigger.DataType.TEXT.name(),
                GeneralTrigger.textOperators[3], null, "null"); // "is not"
        conditions = new ArrayList<Condition>();
        conditions.add(ifNotNull);
        Task audioTrigger = TaskGenerator.createTrigger(mapper, "AudioTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(audioTrigger);

        Condition ifNotMovement = TaskGenerator.createCondition("falseEvent",
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifNotMovement);
        Task accStopTrigger = TaskGenerator.createStopTrigger(mapper,
                "AccStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(accStopTrigger);
        Task timerTrigger = TaskGenerator.createTrigger(mapper, "TimerTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(timerTrigger);

        Condition ifTimerSaysSo = TaskGenerator.createCondition("value",
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifTimerSaysSo);
        Task accTrigger = TaskGenerator.createTrigger(mapper, "AccTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(accTrigger);

        // Sinks

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        Task audioSink = TaskGenerator.createTaskWithPeriod(mapper,
                "AudioSink", TaskType.AudioSink, 1000);
        tasks.add(audioSink);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(wifiSensor.getName(), gpsTrigger
                                .getName()),
                        new TaskRelation(gpsTrigger.getName(), gpsSensor
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), gpsStopTrigger
                                .getName()),
                        new TaskRelation(gpsStopTrigger.getName(), gpsSensor
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(gpsSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(nfcSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(nfcSensor.getName(), audioTrigger
                                .getName()),
                        new TaskRelation(audioTrigger.getName(), audioSensor
                                .getName()),
                        new TaskRelation(audioSensor.getName(), audioSink
                                .getName()),
                        new TaskRelation(accSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(accSensor.getName(), movementFilter
                                .getName()),
                        new TaskRelation(movementFilter.getName(),
                                falseTimerFilter.getName()),
                        new TaskRelation(falseTimerFilter.getName(),
                                accStopTrigger.getName()),
                        new TaskRelation(accStopTrigger.getName(), accSensor
                                .getName()),
                        new TaskRelation(falseTimerFilter.getName(),
                                timerTrigger.getName()),
                        new TaskRelation(timerTrigger.getName(), accTimerSensor
                                .getName()),
                        new TaskRelation(accTimerSensor.getName(), accTrigger
                                .getName()),
                        new TaskRelation(accTrigger.getName(), accSensor
                                .getName()) });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);
        // project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * ACC
     * 
     * @param resources
     */
    public static void buildProjectJsonO(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 4L); // 4days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        // Sensors

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 44,
                10000, 10000);
        tasks.add(accSensor);

        Task accTimerSensor = TaskGenerator.createTimerSensor(mapper, 1000,
                1000 * 20); // each 20 seconds
        accTimerSensor.setTriggered(true);
        tasks.add(accTimerSensor);

        // Task powerSensor = TaskGenerator.createPowerConnectionSensor(mapper,
        // 1000);
        // tasks.add(powerSensor);

        // Filters

        Task movementFilter = TaskGenerator.createMovementFilter(mapper, 1000,
                0.3f);
        tasks.add(movementFilter);

        Task falseTimerFilter = TaskGenerator.createFalseTimerFilter(mapper,
                1000, 5000L, "isMovement");
        tasks.add(falseTimerFilter);

        // Triggers

        Condition ifNotMovement = TaskGenerator.createCondition("falseEvent",
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifNotMovement);
        Task accStopTrigger = TaskGenerator.createStopTrigger(mapper,
                "AccStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(accStopTrigger);
        Task timerTrigger = TaskGenerator.createTrigger(mapper, "TimerTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(timerTrigger);

        // Condition ifMovement = TaskGenerator.createCondition("falseEvent",
        // GeneralTrigger.DataType.BOOLEAN.name(),
        // GeneralTrigger.booleanOperators[1]); // "is false"
        // conditions = new ArrayList<Condition>();
        // conditions.add(ifMovement);
        // Task timerStopTrigger = TaskGenerator
        // .createStopTrigger(mapper, "TimerStopTrigger", 1000,
        // GeneralTrigger.matches[0], conditions);
        // tasks.add(timerStopTrigger);

        Condition ifTimerSaysSo = TaskGenerator.createCondition("value",
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifTimerSaysSo);
        Task accTrigger = TaskGenerator.createTrigger(mapper, "AccTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(accTrigger);

        // Condition ifPower = TaskGenerator.createCondition("value",
        // GeneralTrigger.DataType.BOOLEAN.name(),
        // GeneralTrigger.booleanOperators[0]); // "is true"
        // conditions = new ArrayList<Condition>();
        // conditions.add(ifPower);
        // Task accStopTrigger2 = TaskGenerator.createStopTrigger(mapper,
        // "AccStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        // tasks.add(accStopTrigger2);
        //
        // Condition ifNoPower = TaskGenerator.createCondition("value",
        // GeneralTrigger.DataType.BOOLEAN.name(),
        // GeneralTrigger.booleanOperators[1]); // "is false"
        // conditions = new ArrayList<Condition>();
        // conditions.add(ifNoPower);
        // Task accTrigger2 = TaskGenerator.createTrigger(mapper, "AccTrigger",
        // 1000, GeneralTrigger.matches[0], conditions);
        // tasks.add(accTrigger2);

        // Sinks

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(accSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(accSensor.getName(), movementFilter
                                .getName()),
                        new TaskRelation(movementFilter.getName(),
                                falseTimerFilter.getName()),
                        new TaskRelation(falseTimerFilter.getName(),
                                accStopTrigger.getName()),
                        new TaskRelation(accStopTrigger.getName(), accSensor
                                .getName()),
                        // new TaskRelation(falseTimerFilter.getName(),
                        // timerStopTrigger.getName()),
                        // new TaskRelation(timerStopTrigger.getName(),
                        // accTimerSensor.getName()),
                        new TaskRelation(falseTimerFilter.getName(),
                                timerTrigger.getName()),
                        new TaskRelation(timerTrigger.getName(), accTimerSensor
                                .getName()),
                        new TaskRelation(accTimerSensor.getName(), accTrigger
                                .getName()),
                        new TaskRelation(accTrigger.getName(), accSensor
                                .getName())
                // new TaskRelation(powerSensor.getName(), accTrigger2
                // .getName()),
                // new TaskRelation(accTrigger2.getName(), accSensor
                // .getName()),
                // new TaskRelation(powerSensor.getName(), accStopTrigger2
                // .getName()),
                // new TaskRelation(accStopTrigger2.getName(), accSensor
                // .getName()),

                });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);
        // project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * GPS + Wifi
     * 
     * @param resources
     */
    public static void buildProjectJsonP(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 4L); // 4days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        // Sensors

        Task gpsSensor = TaskGenerator.createGpsSensor(mapper, 1000L * 30L);
        gpsSensor.setTriggered(true);
        tasks.add(gpsSensor);

        Task wifiSensor = TaskGenerator.createWifiConnectionSensor(mapper,
                1000, new String[] { "AppleBS4" });
        tasks.add(wifiSensor);

        // Filters

        // Triggers

        Condition ifNotConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[1]); // "is false"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifNotConnected);
        Task gpsTrigger = TaskGenerator.createTrigger(mapper, "GpsTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsTrigger);

        Condition ifConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifConnected);
        Task gpsStopTrigger = TaskGenerator.createStopTrigger(mapper,
                "GpsStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsStopTrigger);

        // Sinks

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(wifiSensor.getName(), gpsTrigger
                                .getName()),
                        new TaskRelation(gpsTrigger.getName(), gpsSensor
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), gpsStopTrigger
                                .getName()),
                        new TaskRelation(gpsStopTrigger.getName(), gpsSensor
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(gpsSensor.getName(), dataSink
                                .getName()) });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);
        // project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * GPS + Wifi + Acc + audio + NFC without movement
     * 
     * @param resources
     */
    public static void buildProjectJsonQ(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 25L); // 21days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        // Sensors

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 30,
                10000, 5000);
        tasks.add(accSensor);

        Task audioSensor = TaskGenerator.createAudioSensor(mapper, 22050,
                1000 * 60 * 2); // rate: 44100Hz, duration: 2 minutes
        audioSensor.setTriggered(true);
        tasks.add(audioSensor);

        Task gpsSensor = TaskGenerator.createGpsSensor(mapper, 1000L * 60L);
        gpsSensor.setTriggered(true);
        tasks.add(gpsSensor);

        Task nfcSensor = TaskGenerator.createNfcSensor(mapper, 1000);
        tasks.add(nfcSensor);

        Task wifiSensor = TaskGenerator.createWifiConnectionSensor(mapper,
                1000, new String[] { "AppleBS4" });
        tasks.add(wifiSensor);

        // Triggers

        Condition ifNotConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[1]); // "is false"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifNotConnected);
        Task gpsTrigger = TaskGenerator.createTrigger(mapper, "GpsTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsTrigger);

        Condition ifConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifConnected);
        Task gpsStopTrigger = TaskGenerator.createStopTrigger(mapper,
                "GpsStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsStopTrigger);

        Condition ifNotNull = TaskGenerator.createCondition("message",
                GeneralTrigger.DataType.TEXT.name(),
                GeneralTrigger.textOperators[3], null, "null"); // "is not"
        conditions = new ArrayList<Condition>();
        conditions.add(ifNotNull);
        Task audioTrigger = TaskGenerator.createTrigger(mapper, "AudioTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(audioTrigger);

        // Sinks

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        Task audioSink = TaskGenerator.createTaskWithPeriod(mapper,
                "AudioSink", TaskType.AudioSink, 1000);
        tasks.add(audioSink);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(wifiSensor.getName(), gpsTrigger
                                .getName()),
                        new TaskRelation(gpsTrigger.getName(), gpsSensor
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), gpsStopTrigger
                                .getName()),
                        new TaskRelation(gpsStopTrigger.getName(), gpsSensor
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(gpsSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(nfcSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(nfcSensor.getName(), audioTrigger
                                .getName()),
                        new TaskRelation(audioTrigger.getName(), audioSensor
                                .getName()),
                        new TaskRelation(audioSensor.getName(), audioSink
                                .getName()),
                        new TaskRelation(accSensor.getName(), dataSink
                                .getName()) });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);
        // project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * Audio + NFC
     * 
     * @param resources
     */
    public static void buildProjectJsonR(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 4L); // 4days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        // Sensors

        Task audioSensor = TaskGenerator.createAudioSensor(mapper, 44100,
                1000 * 60 * 2); // rate: 44100Hz, duration: 2 minutes
        audioSensor.setTriggered(true);
        tasks.add(audioSensor);

        Task nfcSensor = TaskGenerator.createNfcSensor(mapper, 500);
        tasks.add(nfcSensor);

        // Triggers

        Condition ifNotNull = TaskGenerator.createCondition("message",
                GeneralTrigger.DataType.TEXT.name(),
                GeneralTrigger.textOperators[3], null, "null"); // "is not"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifNotNull);
        Task audioTrigger = TaskGenerator.createTrigger(mapper, "AudioTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(audioTrigger);

        // Sinks

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        Task audioSink = TaskGenerator.createTaskWithPeriod(mapper,
                "AudioSink", TaskType.AudioSink, 1000);
        tasks.add(audioSink);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(nfcSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(nfcSensor.getName(), audioTrigger
                                .getName()),
                        new TaskRelation(audioTrigger.getName(), audioSensor
                                .getName()),
                        new TaskRelation(audioSensor.getName(), audioSink
                                .getName()) });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);
        // project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }

    /**
     * GPS + Wifi + Acc + audio + NFC with Movement filters
     * 
     * @param resources
     */
    public static void buildProjectJsonS(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 25L); // 21days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        // Sensors

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 30, 10000, 10000);
        tasks.add(accSensor);

        Task audioSensor = TaskGenerator.createAudioSensor(mapper, 22050, 1000 * 60 * 2); // rate: 44100Hz, duration: 2 minutes
        audioSensor.setTriggered(true);
        tasks.add(audioSensor);

        Task gpsSensor = TaskGenerator.createGpsSensor(mapper, 1000L * 60L * 2L); //each 2 minutes
        gpsSensor.setTriggered(true);
        tasks.add(gpsSensor);

        Task nfcSensor = TaskGenerator.createNfcSensor(mapper, 1000);
        tasks.add(nfcSensor);

        Task wifiSensor = TaskGenerator.createWifiConnectionSensor(mapper,
                1000, new String[] { "AppleBS4" });
        tasks.add(wifiSensor);

        // Filters

        Task movementFilter = TaskGenerator.createMovementFilter(mapper, 1000, 0.3f);
        tasks.add(movementFilter);

        Task movementTimeFilter = TaskGenerator.createMovementTimeFilter(
                mapper, 1000, 30000L, 5000L);
        tasks.add(movementTimeFilter);

        // Triggers

        Condition ifNotConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[1]); // "is false"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifNotConnected);
        
        Task gpsTrigger = TaskGenerator.createTrigger(mapper, "GpsTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsTrigger);

        Condition ifConnected = TaskGenerator.createCondition(
                WifiConnectionSensor.ATT_ISCONNECTED,
                GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifConnected);
        Task gpsStopTrigger = TaskGenerator.createStopTrigger(mapper,
                "GpsStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsStopTrigger);

        Condition ifNotNull = TaskGenerator.createCondition("message",
                GeneralTrigger.DataType.TEXT.name(),
                GeneralTrigger.textOperators[3], null, "null"); // "is not"
        conditions = new ArrayList<Condition>();
        conditions.add(ifNotNull);
        Task audioTrigger = TaskGenerator.createTrigger(mapper, "AudioTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(audioTrigger);

        Condition ifMoveTimeEvent = TaskGenerator.createCondition(
                "moveTimeEvent", GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[0]); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifMoveTimeEvent);
        Task accTrigger = TaskGenerator.createTrigger(mapper, "AccTrigger",
                1000, GeneralTrigger.matches[0], conditions);
        tasks.add(accTrigger);

        Condition ifNotMoveTimeEvent = TaskGenerator.createCondition(
                "moveTimeEvent", GeneralTrigger.DataType.BOOLEAN.name(),
                GeneralTrigger.booleanOperators[1]); // "is false"
        conditions = new ArrayList<Condition>();
        conditions.add(ifNotMoveTimeEvent);
        Task accStopTrigger = TaskGenerator.createStopTrigger(mapper,
                "AccStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(accStopTrigger);

        // Sinks

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);

        Task audioSink = TaskGenerator.createTaskWithPeriod(mapper,
                "AudioSink", TaskType.AudioSink, 1000);
        tasks.add(audioSink);

        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(wifiSensor.getName(), gpsTrigger
                                .getName()),
                        new TaskRelation(gpsTrigger.getName(), gpsSensor
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), gpsStopTrigger
                                .getName()),
                        new TaskRelation(gpsStopTrigger.getName(), gpsSensor
                                .getName()),
                        new TaskRelation(wifiSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(gpsSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(nfcSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(nfcSensor.getName(), audioTrigger
                                .getName()),
                        new TaskRelation(audioTrigger.getName(), audioSensor
                                .getName()),
                        new TaskRelation(audioSensor.getName(), audioSink
                                .getName()),
                        new TaskRelation(accSensor.getName(), movementFilter
                                .getName()),
                        new TaskRelation(movementFilter.getName(),
                                movementTimeFilter.getName()),
                        new TaskRelation(movementTimeFilter.getName(),
                                accTrigger.getName()),
                        new TaskRelation(movementTimeFilter.getName(),
                                accStopTrigger.getName()),
                        new TaskRelation(accTrigger.getName(), accSensor
                                .getName()),
                        new TaskRelation(accStopTrigger.getName(), accSensor
                                .getName()),
                        new TaskRelation(accSensor.getName(), dataSink
                                .getName()) });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);
        // project.put("mainSurvey", survey);

        writeProject(context, mapper, project);
    }
    
    public static void buildProjectJsonT(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 4L); // 4days
        session.setDurationMeasure("hours");

        //Tasks
        List<Task> tasks = new ArrayList<Task>();

        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 44, 10000, 10000);
        tasks.add(accSensor);

        // Filters
        Task hPassFilter = TaskGenerator.createAccHighPassFilter(mapper, 5000);
        tasks.add(hPassFilter);
        
        Task vMagnitudeFilter = TaskGenerator.createAccVectorMagFilter(mapper, 4000);
        tasks.add(vMagnitudeFilter);
        
        /*Task stepsCounterFilter = TaskGenerator.createStepsFilter(mapper, 3000);
        tasks.add(stepsCounterFilter);
        
        Task caloriesFilter = TaskGenerator.createCaloriesFilter(mapper, 2000);
        tasks.add(caloriesFilter);*/

        // Triggers

        // Sinks
        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink", TaskType.DataSink, 1000);
        tasks.add(dataSink);

        //Relations
        List<TaskRelation> relations = Arrays.asList(
			new TaskRelation[] {
	                new TaskRelation(accSensor.getName(), hPassFilter.getName()),
	                new TaskRelation(hPassFilter.getName(), vMagnitudeFilter.getName()),
	                new TaskRelation(vMagnitudeFilter.getName(), dataSink.getName())
	        }
		);

        //Write project
        session.setTasks(tasks);
        session.setRelations(relations);
        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);
        writeProject(context, mapper, project);
    }
    
    public static void buildProjectJsonU(Context context) {
        ObjectMapper mapper = new ObjectMapper();
        
        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 7L); // 90Days
        session.setDurationMeasure("hours");

        List<Task> tasks = new ArrayList<Task>();

        // Sensors
        Task audioSensor = TaskGenerator.createAudioSensor(mapper, 8000, 1000 * 60 * 2); // rate: 8000Hz, duration: 2 minutes
        audioSensor.setTriggered(false);
        tasks.add(audioSensor);
        
        //Filters
        Task vad16Bit = TaskGenerator.createVAD16BitFilter(mapper, 1000 * 60 * 2);
        tasks.add(vad16Bit);

        // Sinks
        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink", TaskType.DataSink, 1000 * 60 * 2);
        tasks.add(dataSink);

        List<TaskRelation> relations = Arrays.asList(
        		new TaskRelation[] {
                        new TaskRelation(audioSensor.getName(), vad16Bit.getName()),
                        new TaskRelation(vad16Bit.getName(), dataSink.getName())
        		}
        );

        session.setTasks(tasks);
        session.setRelations(relations);

        //Write project
        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);
        writeProject(context, mapper, project);
    }
    
    /**
     * Survey + Shake
     * 
     * @param resources
     */
    public static void buildProjectJsonW(Context context) {
    	ObjectMapper mapper = new ObjectMapper();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 7L); // 90Days
        session.setDurationMeasure("hours");

        List<Task> tasks = new ArrayList<Task>();
        
        // Surveys
        Survey surveyINICIO = SurveyGenerator.createInicioSurvey();
        Survey surveySUBIDA = SurveyGenerator.createSubidaSurvey();
        Survey surveyBAJADA = SurveyGenerator.createBajadaSurvey();
        Survey surveyPARADA = SurveyGenerator.createParadaSurvey();

        Task nfcSensor = TaskGenerator.createNfcSensor(mapper, 500);
        tasks.add(nfcSensor);
        
        Task gpsSensor = TaskGenerator.createGpsSensor(mapper, 60000L); //each minute
        gpsSensor.setTriggered(true);
        tasks.add(gpsSensor);
        
        //Trigger GPS
        Condition ifGpsStart = TaskGenerator.createCondition("message", GeneralTrigger.DataType.TEXT.name(), GeneralTrigger.textOperators[0], null, "INICIO"); // "is false"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifGpsStart);
        
        Task gpsTrigger = TaskGenerator.createTrigger(mapper, "GpsTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsTrigger);

        Condition ifGpsStop = TaskGenerator.createCondition("message", GeneralTrigger.DataType.TEXT.name(), GeneralTrigger.textOperators[0], null, "FIN"); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifGpsStop);
    
        Task gpsStopTrigger = TaskGenerator.createStopTrigger(mapper, "GpsStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsStopTrigger);
        
        //Trigger de INICIO
        Condition ifSurveyINICIO = TaskGenerator.createCondition("message", GeneralTrigger.DataType.TEXT.name(), GeneralTrigger.textOperators[0], null, "INICIO"); // "is true"
        ArrayList<Condition> conditionsINICIO = new ArrayList<Condition>();
        conditionsINICIO.add(ifSurveyINICIO);
        
        Task surveyTriggerINICIO = TaskGenerator.createTrigger(mapper, "SurveyTriggerINICIO", 1000, GeneralTrigger.matches[0], conditionsINICIO);
        tasks.add(surveyTriggerINICIO);
        
        //Trigger de SUBIDA
        Condition ifSurveySUBIDA = TaskGenerator.createCondition("message", GeneralTrigger.DataType.TEXT.name(), GeneralTrigger.textOperators[0], null, "SUBIDA"); // "is true"
        ArrayList<Condition> conditionsSUBIDA = new ArrayList<Condition>();
        conditionsSUBIDA.add(ifSurveySUBIDA);
        
        Task surveyTriggerSUBIDA = TaskGenerator.createTrigger(mapper, "SurveySUBIDA", 1000, GeneralTrigger.matches[0], conditionsSUBIDA);
        tasks.add(surveyTriggerSUBIDA);
        
        //Trigger de BAJADA
        Condition ifSurveyBAJADA = TaskGenerator.createCondition("message", GeneralTrigger.DataType.TEXT.name(), GeneralTrigger.textOperators[0], null, "BAJADA"); // "is true"
        ArrayList<Condition> conditionsBAJADA = new ArrayList<Condition>();
        conditionsBAJADA.add(ifSurveyBAJADA);
        
        Task surveyTriggerBAJADA = TaskGenerator.createTrigger(mapper, "SurveyBAJADA", 1000, GeneralTrigger.matches[0], conditionsBAJADA);
        tasks.add(surveyTriggerBAJADA);
        
        //Trigger de PARADA
        Condition ifSurveyPARADA = TaskGenerator.createCondition("message", GeneralTrigger.DataType.TEXT.name(), GeneralTrigger.textOperators[0], null, "PARADA"); // "is true"
        ArrayList<Condition> conditionsPARADA = new ArrayList<Condition>();
        conditionsPARADA.add(ifSurveyPARADA);
        
        Task surveyTriggerPARADA = TaskGenerator.createTrigger(mapper, "SurveyPARADA", 1000, GeneralTrigger.matches[0], conditionsPARADA);
        tasks.add(surveyTriggerPARADA);
        
        // Sinks
        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink", TaskType.DataSink, 1000);
        tasks.add(dataSink);

        List<TaskRelation> relations = Arrays.asList(
			new TaskRelation[] {
				new TaskRelation(nfcSensor.getName(), dataSink.getName()),
				new TaskRelation(gpsSensor.getName(), dataSink.getName()),
				
	            new TaskRelation(nfcSensor.getName(), surveyTriggerINICIO.getName()),
	            new TaskRelation(surveyTriggerINICIO.getName(), "InicioSurvey"),
	            new TaskRelation(nfcSensor.getName(), surveyTriggerSUBIDA.getName()),
	            new TaskRelation(surveyTriggerSUBIDA.getName(), "SubidaSurvey"),
	            new TaskRelation(nfcSensor.getName(), surveyTriggerBAJADA.getName()),
	            new TaskRelation(surveyTriggerBAJADA.getName(), "BajadaSurvey"),
	            new TaskRelation(nfcSensor.getName(), surveyTriggerPARADA.getName()),
	            new TaskRelation(surveyTriggerPARADA.getName(), "ParadaSurvey"),
	            
	            new TaskRelation(nfcSensor.getName(), gpsTrigger.getName()),
	            new TaskRelation(gpsTrigger.getName(), gpsSensor.getName()),
	            new TaskRelation(nfcSensor.getName(), gpsStopTrigger.getName()),
	            new TaskRelation(gpsStopTrigger.getName(), gpsSensor.getName())
	        }
		);

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(4);
        project.put("InicioSurvey", surveyINICIO);
        project.put("SubidaSurvey", surveySUBIDA);
        project.put("BajadaSurvey", surveyBAJADA);
        project.put("ParadaSurvey", surveyPARADA);

        writeProject(context, mapper, project);
    }
    
    public static void buildProjectJsonX(Context context) {
    	ObjectMapper mapper = new ObjectMapper();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 7L); // 90Days
        session.setDurationMeasure("hours");

        List<Task> tasks = new ArrayList<Task>();
        
        Task nfcSensor = TaskGenerator.createNfcSensor(mapper, 1000);
        tasks.add(nfcSensor);
        
        Task gpsSensor = TaskGenerator.createGpsSensor(mapper, 60000L); //each minute
        gpsSensor.setTriggered(true);
        tasks.add(gpsSensor);
        
        //Trigger GPS
        Condition ifGpsStart = TaskGenerator.createCondition("message", GeneralTrigger.DataType.TEXT.name(), GeneralTrigger.textOperators[0], null, "INICIO"); // "is false"
        ArrayList<Condition> conditions = new ArrayList<Condition>();
        conditions.add(ifGpsStart);
        
        Task gpsTrigger = TaskGenerator.createTrigger(mapper, "GpsTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsTrigger);

        Condition ifGpsStop = TaskGenerator.createCondition("message", GeneralTrigger.DataType.TEXT.name(), GeneralTrigger.textOperators[0], null, "FIN"); // "is true"
        conditions = new ArrayList<Condition>();
        conditions.add(ifGpsStop);
    
        Task gpsStopTrigger = TaskGenerator.createStopTrigger(mapper, "GpsStopTrigger", 1000, GeneralTrigger.matches[0], conditions);
        tasks.add(gpsStopTrigger);
        
        // Sinks
        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink", TaskType.DataSink, 1000);
        tasks.add(dataSink);

        List<TaskRelation> relations = Arrays.asList(
			new TaskRelation[] {
				new TaskRelation(nfcSensor.getName(), dataSink.getName()),
				new TaskRelation(gpsSensor.getName(), dataSink.getName()),
				
	            new TaskRelation(nfcSensor.getName(), gpsTrigger.getName()),
	            new TaskRelation(gpsTrigger.getName(), gpsSensor.getName()),
	            new TaskRelation(nfcSensor.getName(), gpsStopTrigger.getName()),
	            new TaskRelation(gpsStopTrigger.getName(), gpsSensor.getName())
	        }
		);

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(0);
        writeProject(context, mapper, project);
    }
    
    public static void buildProjectJsonProyMarzoNFC(Context context) {
        ObjectMapper mapper = new ObjectMapper();

        // Survey survey = SurveyGenerator.createWanderingMindSurvey();

        // Session
        Session session = new Session();
        session.setDurationUnits(24L * 35L); // 35days
        session.setDurationMeasure("hours");
        // session.setStartDate(new Calendar())

        List<Task> tasks = new ArrayList<Task>();

        // Surveys
        Survey surveyPreocupado = SurveyGenerator.createWanderingMindSurvey();
        Survey surveyDeprimido = SurveyGenerator.createWanderingMindSurvey();
        Survey surveyAgresivo = SurveyGenerator.createWanderingMindSurvey();
        Survey surveyPregunta = SurveyGenerator.createWanderingMindSurvey();
        Survey surveyProblematico = SurveyGenerator.createWanderingMindSurvey();

        
        ///Sensors
        Task nfcSensor = TaskGenerator.createNfcSensor(mapper, 500);
        tasks.add(nfcSensor);

        Task surveySensor = TaskGenerator.createCaloriesFilter(mapper, 500);
        tasks.add(surveySensor); // PARA AGREGAR MENSAJE DE AUDIO, EL SURVEY SE AGREGO COMO UN SENSOR QUE PERMITIERA LEER LA RESPUESTA SI/NO PARA GRABAR
        
        Task audioSensor = TaskGenerator.createAudioSensor(mapper, 22050,
                1000 * 60 * 1/2); // rate: 44100Hz, duration: 30 seg.
        audioSensor.setTriggered(true);
        tasks.add(audioSensor);
        
    
        Task accSensor = TaskGenerator.createAccelerometerSensor(mapper, 30,
                10000, 10000);
        tasks.add(accSensor);

        // Triggers
        
        
   //// createCondition recibe los siguientes parámetros: nombre del dato de entrada, tipo de dato, operación que se realizara sobre el dato, fecha cuando se están comparando fechas (en este caso no son fechas, por eso es null), valor con el que se compara el dato.
   //// muchos de los valores que pueden tomar están definidos al inicio de la clase GeneralTrigger.
   //// te pongo un ejemplo de cómo podría quedar una de las condiciones que necesitas:
   /// Cambio el operador, ahora en lugar de "is not" (3), es "contains" (0). Tambien cambio el contenido que debe contener el mensaje de NFC ("PREOCUPADO") 

     
        
        Task audioSink = TaskGenerator.createTaskWithPeriod(mapper,
                "AudioSink", TaskType.AudioSink, 1000);
        tasks.add(audioSink);
        
        
        
        /*  CONDICIONES Y TRIGGER PARA INICIAR SENSOR DE AUDIO  PARA CUALQUIERA DEL LOS SURVEYS*/
        Condition ifDeseaGrabar = TaskGenerator.createCondition("lastAnswer",
                   GeneralTrigger.DataType.TEXT.name(),
                   GeneralTrigger.textOperators[2], null, "0"); // "is"
          
       ArrayList<Condition> conditionsAudio = new ArrayList<Condition>();
       conditionsAudio.add(ifDeseaGrabar);
           
    Task audioTrigger = TaskGenerator.createTrigger(mapper,
                "AudioTrigger", 1000, GeneralTrigger.matches[0], conditionsAudio); //"any"
        tasks.add(audioTrigger);
        

        
      /*  CONDICIONES Y TRIGGER PARA SURVEY DE PREOCUPADO*/
        Condition ifPreocupado = TaskGenerator.createCondition("message",
                   GeneralTrigger.DataType.TEXT.name(),
                   GeneralTrigger.textOperators[0], null, "ANSIEDAD / PREOCUPACION"); // "contains"
          
       ArrayList<Condition> conditionsPreocupado = new ArrayList<Condition>();
           conditionsPreocupado.add(ifPreocupado);
           
    Task surveyTriggerPreocupado = TaskGenerator.createTrigger(mapper,
                "SurveyTriggerPreocupado", 1000, GeneralTrigger.matches[0], conditionsPreocupado); //"any"
        tasks.add(surveyTriggerPreocupado);
     
        
        
/*  CONDICIONES Y TRIGGER PARA SURVEY DE DEPRIMIDO*/
     Condition ifDeprimido = TaskGenerator.createCondition("message",
                GeneralTrigger.DataType.TEXT.name(),
                GeneralTrigger.textOperators[0], null, "TRISTEZA / DEPRESION"); // "contains"
     ArrayList<Condition> conditionsDeprimido = new ArrayList<Condition>();
          conditionsDeprimido.add(ifDeprimido); 
          
    Task surveyTriggerDeprimido = TaskGenerator.createTrigger(mapper,
                "SurveyTriggerDeprimido", 1000, GeneralTrigger.matches[0], conditionsDeprimido); //"any"
         tasks.add(surveyTriggerDeprimido);      
    
/* CONDICIONES Y TRIGGER PARA SURVEY DE AGRESIVO*/         
    Condition ifAgresivo = TaskGenerator.createCondition("message",
                    GeneralTrigger.DataType.TEXT.name(),
                    GeneralTrigger.textOperators[0], null, "AGRESION / AGITACION"); // "contains"
    ArrayList<Condition> conditionsAgresivo= new ArrayList<Condition>();
              conditionsAgresivo.add(ifAgresivo); 
          
        Task surveyTriggerAgresivo = TaskGenerator.createTrigger(mapper,
                "SurveyTriggerAgresivo", 1000, GeneralTrigger.matches[0], conditionsAgresivo); //"any"
        tasks.add(surveyTriggerAgresivo);
        
        
        /* CONDICIONES Y TRIGGER PARA SURVEY DE PREGUNTAS REPETITIVAS*/         
        Condition ifPregunta = TaskGenerator.createCondition("message",
                        GeneralTrigger.DataType.TEXT.name(),
                        GeneralTrigger.textOperators[0], null, "PREGUNTAS"); // "contains"
        ArrayList<Condition> conditionsPregunta= new ArrayList<Condition>();
                  conditionsPregunta.add(ifPregunta); 
              
            Task surveyTriggerPregunta = TaskGenerator.createTrigger(mapper,
                    "SurveyTriggerPregunta", 1000, GeneralTrigger.matches[0], conditionsPregunta); //"any"
            tasks.add(surveyTriggerPregunta);
            
            
            /* CONDICIONES Y TRIGGER PARA SURVEY DE COMPORTAMIENTO PROBLEMATICO*/         
            Condition ifProblematico = TaskGenerator.createCondition("message",
                            GeneralTrigger.DataType.TEXT.name(),
                            GeneralTrigger.textOperators[0], null, "COMPORTAMIENTO PROBLEMATICO"); // "contains"
            ArrayList<Condition> conditionsProblematico= new ArrayList<Condition>();
                      conditionsProblematico.add(ifProblematico); 
                  
                Task surveyTriggerProblematico = TaskGenerator.createTrigger(mapper,
                        "SurveyTriggerProblematico", 1000, GeneralTrigger.matches[0], conditionsProblematico); //"any"
                tasks.add(surveyTriggerProblematico);      
                
                
                /* CONDICIONES Y TRIGGER PARA SURVEY DE WANDERING*/         
                Condition ifDeambular = TaskGenerator.createCondition("message",
                                GeneralTrigger.DataType.TEXT.name(),
                                GeneralTrigger.textOperators[0], null, "DEAMBULAR"); // "contains"
                ArrayList<Condition> conditionsDeambular= new ArrayList<Condition>();
                          conditionsDeambular.add(ifDeambular); 
                      
                    Task surveyTriggerDeambular = TaskGenerator.createTrigger(mapper,
                            "SurveyTriggerDeambular", 1000, GeneralTrigger.matches[0], conditionsDeambular); //"any"
                    tasks.add(surveyTriggerDeambular);  

        // Sinks

        Task dataSink = TaskGenerator.createTaskWithPeriod(mapper, "DataSink",
                TaskType.DataSink, 1000);
        tasks.add(dataSink);
           
        List<TaskRelation> relations = Arrays
                .asList(new TaskRelation[] {
                        new TaskRelation(accSensor.getName(), dataSink
                                .getName()),
                        
                        new TaskRelation(nfcSensor.getName(), dataSink
                                .getName()),
                        new TaskRelation(nfcSensor.getName(), surveyTriggerPreocupado.getName()),
                        new TaskRelation(surveyTriggerPreocupado.getName(), "PreocupadoSurvey"),
                        
                        new TaskRelation(nfcSensor.getName(), surveyTriggerDeprimido.getName()),
                        new TaskRelation(surveyTriggerDeprimido.getName(), "DeprimidoSurvey"),
                        
                        new TaskRelation(nfcSensor.getName(), surveyTriggerAgresivo.getName()),
                          new TaskRelation(surveyTriggerAgresivo.getName(), "AgresivoSurvey"),
                 
                          new TaskRelation(nfcSensor.getName(), surveyTriggerProblematico.getName()),
                          new TaskRelation(surveyTriggerProblematico.getName(), "ProblematicoSurvey"),
                          
                          new TaskRelation(nfcSensor.getName(), surveyTriggerPregunta.getName()),
                          new TaskRelation(surveyTriggerPregunta.getName(), "PreguntaSurvey"),
                          
                          
                          new TaskRelation(surveySensor.getName(), audioTrigger.getName()), 
                        new TaskRelation(audioTrigger.getName(), audioSensor.getName()),
                        new TaskRelation(audioSensor.getName(), audioSink.getName())
                        });

        session.setTasks(tasks);
        session.setRelations(relations);

        Project project = new Project();
        project.setSessionsSize(1);
        project.put("mainSession", session);
        project.setSurveysSize(5);
        project.put("PreocupadoSurvey", surveyPreocupado);
        project.put("DeprimidoSurvey", surveyDeprimido);
        project.put("AgresivoSurvey", surveyAgresivo);
        project.put("PreguntaSurvey", surveyPregunta);
        project.put("ProblematicoSurvey", surveyProblematico);

        writeProject(context, mapper, project);
    }
}
