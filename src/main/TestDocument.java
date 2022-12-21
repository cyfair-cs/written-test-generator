import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Question {
    private int questionNumber;
    private String questionDescription;
    private String sourceCode;
    private List<String> answers;

    public Question(int questionNumber, String questionDescription, String sourceCode, List<String> answers) {
        this.questionNumber = questionNumber;
        this.questionDescription = questionDescription;
        this.sourceCode = sourceCode;
        this.answers = answers;
    }

    public static Question parseQuestion(String input) {
        // Initialize variables
        int questionNumber = -1;
        String questionDescription = "";
        String sourceCode = "";
        List<String> answers = new ArrayList<>();

        // Use regular expressions to extract information from input
        Pattern pattern = Pattern.compile("^(\\d+)\\s*(.*?)(?:\\n(int.*\\n)*)?(?:\\n([ABCDE]\\..*))", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            // Extract question number
            questionNumber = Integer.parseInt(matcher.group(1));

            // Extract question description
            questionDescription = matcher.group(2).trim();

            // Extract source code, if present
            if (matcher.group(3) != null) {
                sourceCode = matcher.group(3).trim();
            }

            // Extract answers
            String[] answerLines = matcher.group(4).split("\\n");
            for (String answerLine : answerLines) {
                answers.add(answerLine.trim());
            }
        }

        // Return a new Question object with the extracted information
        return new Question(questionNumber, questionDescription, sourceCode, answers);
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getQuestionDescription() {
        return questionDescription;
    }

    public void setQuestionDescription(String questionDescription) {
        this.questionDescription = questionDescription;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }
}

class TestDocument {
    private ArrayList<Question> questions;

    public TestDocument(String path) {
        try {
            PDDocument pdf = PDDocument.load(new File(path));
            String raw = new PDFTextStripper().getText(pdf);
            pdf.close();

            raw = raw.replaceAll("UIL.+\n", "");
            String[] tokens = raw.split("QUESTION +");

            Arrays.stream(tokens).map(String::trim);

            questions = new ArrayList<>();

            for (int i = 1; i < tokens.length-1; i++)
                questions.add(Question.parseQuestion(tokens[i]));

            for (Question q: questions)
                System.out.println(q.getQuestionNumber());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}