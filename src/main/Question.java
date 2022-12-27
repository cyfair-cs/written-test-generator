package main;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Question {
    private final int questionNumber;
    private final String questionDescription, testSource;
    private final List<String> answers;
    private String correctAnswer;

    public Question(int questionNumber, String questionDescription, String testSource, List<String> answers) {
        this.questionNumber = questionNumber;
        this.questionDescription = questionDescription;
        this.answers = answers;
        this.testSource = testSource;
    }

    public static Question parseQuestion(String input, String testSource) {
        // Initialize variables
        int questionNumber = -1;
        String questionDescription = "";
        List<String> answers = new ArrayList<>();

        // Use regular expressions to extract information from input
        // ^(\d+)\s*(.*?)(?:\n(int.*\n)*)?(?:\n([ABCDE]\..*))
        Pattern pattern = Pattern.compile("^(\\d+)\\s*(.*)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            // Extract question number
            questionNumber = Integer.parseInt(matcher.group(1));

            // Extract question description
            questionDescription = matcher.group(2).trim();

            // Extract Answers from Question Description
            Pattern answerPattern = Pattern.compile("(([ABCDE]\\..*))");
            Matcher answerMatcher = answerPattern.matcher(questionDescription);
//            System.out.println(questionDescription);
            while (answerMatcher.find()) {
                String rawAnswer = answerMatcher.group(0).trim();
//                System.out.println(rawAnswer);
                for (String answerChoice: rawAnswer.split("[ABCDE]\\. ")) {
                    if (!answerChoice.isBlank())
                        answers.add(answerChoice.trim());
                }
            }
            questionDescription = questionDescription.replaceAll("(([ABCDE]\\..*))", "").trim();
        }

        // Return a new Question object with the extracted information
        return new Question(questionNumber, questionDescription, testSource, answers);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Question ").append(questionNumber).append(":\n\n");
        out.append(questionDescription).append("\n");
        char letter = 'A';
        for (String answerChoice: answers)
            out.append(letter++).append(": ").append(answerChoice).append("\n");
        return out.toString();
    }

    public List<String> getAnswers() {
        return answers;
    }

    public String getQuestionDescription() {
        return questionDescription;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getTestSource() {
        return testSource;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        JSONArray jsonAnswers = new JSONArray();
        char letter = 'A';
        for (String answer : answers) {
            jsonAnswers.put(letter++ + ": " + answer);
        }

        json.put("answers", jsonAnswers);
        json.put("questionNumber", questionNumber);
        json.put("questionDescription", questionDescription);
        json.put("correctAnswer", correctAnswer == null ? "No correct answer found." : correctAnswer);
        json.put("testSource", testSource);

        return json;
    }
}
