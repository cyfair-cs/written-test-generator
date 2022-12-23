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
}

class TestDocument {
    private ArrayList<Question> questions;
    private final String name;
    public TestDocument(File document, String raw_name) {
        this.name = getVerboseContestName(raw_name);
        try {
            PDDocument pdf = PDDocument.load(document);
            String raw = new PDFTextStripper().getText(pdf);
            pdf.close();

            raw = raw.replaceAll("UIL.+\n", "");
            String[] tokens = raw.split("QUESTION +");

            questions = new ArrayList<>();

            for (int i = 1; i < tokens.length-1; i++)
                questions.add(Question.parseQuestion(tokens[i], this.name));

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

            Pattern correctAnswerPattern = Pattern.compile("(?:([0-9][0-9]?[\\.\\)].*))");
            Matcher correctAnswerMatcher = correctAnswerPattern.matcher(answerText);

            HashMap<Integer, String> correctAnswers = new HashMap<>();

            while (correctAnswerMatcher.find()) {
                String lineMatch = correctAnswerMatcher.group(0);
                Pattern answerTokenPattern = Pattern.compile("\\d\\d?[\\.\\)]\\s+[A-E]");
                Matcher answerTokenMatcher = answerTokenPattern.matcher(lineMatch);
                while (answerTokenMatcher.find()) {
                    String[] matchTokens = answerTokenMatcher.group(0).split("\\s+");
                    int questionNumber = Integer.parseInt(matchTokens[0].replaceAll("[:\\.\\)]",""));
                    String questionAnswer = matchTokens[1];

                    if (!correctAnswers.containsKey(questionNumber))
                        correctAnswers.put(questionNumber, questionAnswer);
                }
            }

            for (int questionNumber: correctAnswers.keySet()) {
                Optional<Question> questionFound =  questions.stream().filter(q -> q.getQuestionNumber() == questionNumber).findFirst();
                if (questionFound.isPresent())
                    questionFound.get().setCorrectAnswer(correctAnswers.get(questionNumber));
            }

//            System.out.println("===========================");
        }
    }

    public JSONArray toJSON() {
        JSONArray jsonQuestions = new JSONArray();
        for (Question question: questions) {
            JSONObject jsonQuestion = new JSONObject();

            JSONArray jsonAnswers = new JSONArray();
            char letter = 'A';
            for (String answer : question.getAnswers()) {
                jsonAnswers.put(letter++ + ": " + answer);
            }

            jsonQuestion.put("answers", jsonAnswers);
            jsonQuestion.put("questionNumber", question.getQuestionNumber());
            jsonQuestion.put("questionDescription", question.getQuestionDescription());
            jsonQuestion.put("correctAnswer", question.getCorrectAnswer() == null ? "No correct answer found." : question.getCorrectAnswer());
            jsonQuestion.put("testSource", question.getTestSource());

            jsonQuestions.put(jsonQuestion);
        }
        return jsonQuestions;
    }

    public String getName() {
        return name;
    }
}