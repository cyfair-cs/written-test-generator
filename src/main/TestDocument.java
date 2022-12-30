package main;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TestDocument {
    public static TestDocument QUESTION_POOL = new TestDocument("Global Question Pool");
    private ArrayList<Question> questions;
    private final String name;

    public TestDocument(String name) {
       this.name = name;
       questions = new ArrayList<>();
    }

    @SuppressWarnings("All")
    public TestDocument(File document, String raw_name) {
        this.name = getVerboseContestName(raw_name);
        try {
            PDDocument pdf = PDDocument.load(document);
            String raw = new PDFTextStripper().getText(pdf);
            pdf.close();

            raw = raw.replaceAll("UIL.+\n", "");
            String[] tokens = raw.split("QUESTION +");

            questions = new ArrayList<>();

            for (int i = 1; i < tokens.length-1; i++) {
                Question question = Question.parseQuestion(tokens[i], this.name);
                questions.add(question);
                QUESTION_POOL.getQuestions().add(question);
            }

            loadKeysFromText(tokens[tokens.length-1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getVerboseContestName(String input) {
        String contestName, testVersion, testYear, testSourceName = input;
        String[] testSourceTokens = input.split("_");

        // no test version
        if (testSourceTokens.length == 2) {
            contestName = simplifyContestName(testSourceTokens[0]);
            testYear = testSourceTokens[1];
            testSourceName = contestName + ", " + testYear;
        }
        // has test version
        else if (testSourceTokens.length == 3) {
            contestName = simplifyContestName(testSourceTokens[0]);
            testVersion = testSourceTokens[1];
            testYear = testSourceTokens[2];
            testSourceName = contestName + " Version " + testVersion + ", " + testYear;
        }

        return testSourceName;
    }

    private static String simplifyContestName(String input) {
        return switch (input) {
            case "district", "districts" -> "Districts";
            case "regional", "regionals" -> "Regionals";
            case "invitational", "invitationals" -> "Invitational";
            case "utcs-invitational" -> "University of Texas CS Invitational";
            case "practice" -> "Practice";
            case "state" -> "State";
            case "cs08d" -> "CS08d";
            case "seven-lakes" -> "Seven Lakes";
            case "tompkins" -> "Tompkins";
            default -> input;
        };
    }

    public void loadKeysFromText(String answerText) {
        // Computer Science Contest #1415-01 Key -> slhs 20
        if (answerText.contains("Computer Science Answer Key") || answerText.contains("Computer Science Contest #1415-01 Key")) {

            answerText = answerText.substring(Math.max(answerText.indexOf("Computer Science Answer Key"), answerText.indexOf("October 11, 2014"))).trim();

            if (answerText.contains("Notes:"))
                answerText = answerText.substring(0, answerText.indexOf("Notes:")).trim();
            if (answerText.contains("Note to Graders: "))
                answerText = answerText.substring(0, answerText.indexOf("Note to Graders:")).trim();

            Pattern correctAnswerPattern = Pattern.compile("([0-9][0-9]?[.)].*)");
            Matcher correctAnswerMatcher = correctAnswerPattern.matcher(answerText);

            HashMap<Integer, String> correctAnswers = new HashMap<>();

            while (correctAnswerMatcher.find()) {
                String lineMatch = correctAnswerMatcher.group(0);
                Pattern answerTokenPattern = Pattern.compile("\\d\\d?[.)]\\s+[A-E]");
                Matcher answerTokenMatcher = answerTokenPattern.matcher(lineMatch);
                while (answerTokenMatcher.find()) {
                    String[] matchTokens = answerTokenMatcher.group(0).split("\\s+");
                    int questionNumber = Integer.parseInt(matchTokens[0].replaceAll("[:.)]",""));
                    String questionAnswer = matchTokens[1];

                    if (!correctAnswers.containsKey(questionNumber))
                        correctAnswers.put(questionNumber, questionAnswer);
                }
            }

            for (int questionNumber: correctAnswers.keySet()) {
                Optional<Question> questionFound =  questions.stream().filter(q -> q.getQuestionNumber() == questionNumber).findFirst();
                questionFound.ifPresent(question -> question.setCorrectAnswer(correctAnswers.get(questionNumber)));
            }

//            System.out.println("===========================");
        }
    }

    public JSONArray toJSON() {
        JSONArray json = new JSONArray();
        for (Question question: questions)
            json.put(question.toJSON());
        return json;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public String getName() {
        return name;
    }
}