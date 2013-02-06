/**
 * 
 */
package edu.incense.android.test;

import edu.incense.android.survey.Question;
import edu.incense.android.survey.QuestionType;
import edu.incense.android.survey.Survey;

/**
 * @author mxpxgx
 *
 */
public class SurveyGenerator {
    
    public static Survey createWanderingMindSurvey(){
        Survey survey = new Survey();
        survey.setId(101);
        survey.setTitle("Wandering Mind");
        
        Question question = new Question();
        question.setQuestion("How are you feeling right now?");
        question.setType(QuestionType.SEEKBAR);
        question.setSkippable(false);
        String[] options = { "Bad", "Good" };
        question.setOptions(options);
        int[] nextQuestions1 = { 1 };
        question.setNextQuestions(nextQuestions1);
        survey.add(question);
        
        question = new Question();
        question.setQuestion("How are you feeling right now?");
        question.setType(QuestionType.OPENTEXT);
        question.setSkippable(false);
        int[] nextQuestions2 = { 2 };
        question.setNextQuestions(nextQuestions2);
        survey.add(question);
        
        question = new Question();
        question.setQuestion("Are you thinking about something other than what youÕre currently doing?");
        question.setType(QuestionType.RADIOBUTTONS);
        question.setSkippable(false);
        String[] options2 = { "No", "Yes, something pleasant", "Yes, something neutral", "Yes, something unpleasant" };
        question.setOptions(options2);
        int[] nextQuestions3 = { 0 };
        question.setNextQuestions(nextQuestions3);
        survey.add(question);
        
        return survey;
    }
    
    public static Survey createInicioSurvey(){
    	Survey survey = new Survey();
        survey.setId(102);
        survey.setTitle("INICIO de Recorrido");
        
        Question question = new Question();
        question.setQuestion("¿Cuántos pasajeros puede llevar el vehículo de manera regular?");
        question.setType(QuestionType.OPENNUMERIC);
        question.setSkippable(false);
        int[] nextQuestions1 = { 1 };
        question.setNextQuestions(nextQuestions1);
        survey.add(question);
        
        question = new Question();
        question.setQuestion("¿Cuántos pasajeros lleva el vehículo?");
        question.setType(QuestionType.OPENNUMERIC);
        question.setSkippable(false);
        int[] nextQuestions2 = { 0 };
        question.setNextQuestions(nextQuestions2);
        survey.add(question);
        
		return survey;
    }
    
    public static Survey createSubidaSurvey(){
    	Survey survey = new Survey();
        survey.setId(103);
        survey.setTitle("SUBIDA durante Recorrido");
        
        Question question = new Question();
        question.setQuestion("¿Cuántos pasajeros abordaron el vehículo?");
        question.setType(QuestionType.OPENNUMERIC);
        question.setSkippable(false);
        int[] nextQuestions1 = { 0 };
        question.setNextQuestions(nextQuestions1);
        survey.add(question);
        
		return survey;
    }
    
    public static Survey createBajadaSurvey(){
    	Survey survey = new Survey();
        survey.setId(104);
        survey.setTitle("BAJADA durante Recorrido");
        
        Question question = new Question();
        question.setQuestion("¿Cuántos pasajeros descendieron del vehículo?");
        question.setType(QuestionType.OPENNUMERIC);
        question.setSkippable(false);
        int[] nextQuestions1 = { 0 };
        question.setNextQuestions(nextQuestions1);
        survey.add(question);
        
		return survey;
    }
    
    public static Survey createParadaSurvey(){
    	Survey survey = new Survey();
        survey.setId(105);
        survey.setTitle("PARADA durante Recorrido");
        
        Question question = new Question();
        question.setQuestion("¿Motivo por el cuál el conductor detuvo el vehículo?");
        question.setType(QuestionType.RADIOBUTTONS);
        question.setSkippable(false);
        String[] optionsA = { "Parada indebida", "Parada oficial / Base", "Personales", "Otro"};
        question.setOptions(optionsA);
        int[] nextQuestions1 = { 0, 0, 0, 1 };
        question.setNextQuestions(nextQuestions1);
        survey.add(question);
        
        question = new Question();
        question.setQuestion("Describa brevemente:");
        question.setType(QuestionType.OPENTEXT);
        question.setSkippable(false);
        int[] nextQuestions2 = { 0 };
        question.setNextQuestions(nextQuestions2);
        survey.add(question);
        
		return survey;
    }
}
