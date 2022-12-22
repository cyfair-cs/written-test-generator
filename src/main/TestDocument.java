import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Question {
    private int questionNumber;
    private String questionDescription;
    private List<String> answers;

    public Question(int questionNumber, String questionDescription, List<String> answers) {
        this.questionNumber = questionNumber;
        this.questionDescription = questionDescription;
        this.answers = answers;
    }

    public static Question parseQuestion(String input) {
//        System.out.println(input);

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
            Pattern answerPattern = Pattern.compile("(?:([ABCDE]\\..*))");
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
            questionDescription = questionDescription.replaceAll("(?:([ABCDE]\\..*))", "").trim();
        }

        // Return a new Question object with the extracted information
        return new Question(questionNumber, questionDescription, answers);
    }

    @Override
    public String toString() {
        String out = "";
        out += "Question " + questionNumber + ":\n\n";
        out += questionDescription + "\n";
        char letter = 'A';
        for (String answerChoice: answers)
            out += letter++ + ": " + answerChoice + "\n";
        return out;
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
}

class TestDocument {
    private ArrayList<Question> questions;
    private String name;
    public TestDocument(File document, String name) {
        this.name = name;
        try {
            PDDocument pdf = PDDocument.load(document);
            String raw = new PDFTextStripper().getText(pdf);
            pdf.close();

            raw = raw.replaceAll("UIL.+\n", "");
            String[] tokens = raw.split("QUESTION +");

            Arrays.stream(tokens).map(String::trim);

            questions = new ArrayList<>();

            for (int i = 1; i < tokens.length-1; i++)
                questions.add(Question.parseQuestion(tokens[i]));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONArray toJSON() {
        JSONArray jsonQuestions = new JSONArray();
        for (Question question: questions) {
            JSONObject jsonQuestion = new JSONObject();
            jsonQuestion.put("questionNumber", question.getQuestionNumber());
            jsonQuestion.put("questionDescription", question.getQuestionDescription());
            JSONArray jsonAnswers = new JSONArray();
            for (String answer : question.getAnswers()) {
                jsonAnswers.put(answer);
            }
            jsonQuestion.put("answers", jsonAnswers);
            jsonQuestions.put(jsonQuestion);
        }
        return jsonQuestions;
    }

    public String getName() {
        return name;
    }
}