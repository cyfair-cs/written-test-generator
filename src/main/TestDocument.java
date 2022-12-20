import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Question {
    public int Number;
    public String Text, Code, CorrectAnswer;
    public TreeMap<String, String> AnswerChoices;

    private static final Pattern QuestionNumberRegex = Pattern.compile("^\\d+");
    private static final Pattern AnswerChoicesRegex = Pattern.compile("([A-E])[\\)\\.]\\s*((?:\\s(?!\\s)|\\S)+)");
    private static final Pattern QuestionTextRegex = Pattern.compile("(?:\\s{1,2}(?!\\s)|\\S)+");

    public static Question parse(String raw) {
        final Matcher AnswerChoicesMatch = AnswerChoicesRegex.matcher(raw);
        final Matcher QuestionNumberMatch = QuestionNumberRegex.matcher(raw);
        final Matcher QuestionTextMatch = QuestionTextRegex.matcher(raw);



        if (QuestionNumberMatch.matches())
            System.out.println("Question Number:" + QuestionNumberMatch.group(0));
        if (QuestionTextMatch.matches())
            System.out.println("Question Text:" + QuestionTextMatch.group(0));
        if (AnswerChoicesMatch.matches())
            System.out.println("Answer Choices:" + AnswerChoicesMatch.group(0));

        return new Question(0, null, null, null, null);
    }

    public Question(int Number, String Text, String Code, String CorrectAnswer, TreeMap<String, String> AnswerChoices) {
        this.Number = Number;
        this.Text = Text;
        this.Code = Code;
        this.CorrectAnswer = CorrectAnswer;
        this.AnswerChoices = AnswerChoices;
    }
}

class TestDocument {
    private HashSet<Question> questions;

    public TestDocument(String path) {
        try {
            PDDocument pdf = PDDocument.load(new File(path));
            String raw = new PDFTextStripper().getText(pdf);

            raw = raw.replaceAll("UIL.+\n", "");
            String[] tokens = raw.split("QUESTION +");

            Arrays.stream(tokens)
                    .map(String::trim);

            questions = new HashSet<>();

            questions.add(Question.parse(tokens[1]));

//            for (int i = 1; i < tokens.length-1; i++)
//                questions.add(Question.parse(tokens[i]));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}