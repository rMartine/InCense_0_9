package edu.incense.android.survey;

import java.io.Serializable;
import java.util.Stack;

public class SurveyController implements Serializable {
    private static final long serialVersionUID = 3516516719837433621L;
    private ReadOnlySurvey survey;
    private Answer[] answers;
    private int index; // Index of current question
    private Stack<Integer> surveyPath;

    public SurveyController(Survey survey) {
        this.survey = survey;
        answers = new Answer[getSize()];
        surveyPath = new Stack<Integer>();
        index = 0;
    }

    public void saveAnswersTo(String fileName) {
        JsonSurvey jsonSurvey = new JsonSurvey();
        jsonSurvey.toJson(fileName, answers);
    }

    public boolean isEmpty() {
        if (survey == null) {
            return true;
        }
        if (getQuestion() == null) {
            return true;
        }
        return false;
    }

    /***
     * Initializes answer of the current question if necessary, that is, if it
     * hasn't been set, it calls a constructor of a new answer and adds it to
     * the given index of the answers array.
     * 
     * @param i
     *            index of the answer to initialize (number of question in the
     *            survey)
     */
    private Answer initializeAnswer(int i) {
        if (answers[i] == null) {
            answers[i] = new Answer();
        }
        return answers[i];
    }

    /***
     * Returns current answer (initializes it if necessary)
     * 
     * @return Answer - current answer
     */
    public Answer getAnswer() {
        return initializeAnswer(index);
    }
    
    /** 
     * Get current question
     * 
     * @return
     */
    public ReadOnlyQuestion getQuestion() {
        return survey.getQuestion(index);
    }

    public boolean isFirstQuestion() {
        if (index == 0)
            return true;
        return false;
    }

    public boolean isLastQuestion() {
        if (index == (getSize() - 1))
            return true;
        return false;
    }

    public boolean isSurveyComplete() {
        if ((index + 1) < getSize()) {
            return false;
        }
        for (int i = 0; i < answers.length; i++) {
            if (answers[i] == null)
                return false;
            if (!answers[i].isSkipped() && !answers[i].isAnswered())
                return false;
        }
        return true;
    }

    /**
     * Advances to the next question
     * @return
     */
    public boolean next() {
        surveyPath.push(index); //register the question number
        int[] nextQuestions = getQuestion().getNextQuestions();
        if (getQuestion().getType() == QuestionType.RADIOBUTTONS
                && nextQuestions != null && getAnswer().isAnswered()) {
            index = nextQuestions[getAnswer().getSelectedOption()];
        } else {
            index = index < getSize() ? index + 1 : index;
        }
        if (surveyPath.peek() == index)
            return false;
        return true;
    }

    public boolean back() {
        if (surveyPath.empty())
            return false;
        else
            index = surveyPath.pop();
        return true;
    }

    public boolean skip() {
        if (getQuestion().isSkippable()) {
            getAnswer().setSkipped(true);
            return next();
        }
        return false;
    }

    private int getSize() {
        return survey.getSize();
    }
}
