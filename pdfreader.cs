using BitMiracle.Docotic.Pdf;
using System.Text.RegularExpressions;

using (PdfDocument pdf = new PdfDocument("cw21written_v5.pdf")) {
    PdfTextExtractionOptions options = new() {
        SkipInvisibleText = true,
        WithFormatting = true
    };

    IEnumerator<string> lines = pdf.GetText(options).Split('\n').Select(e => e.Trim()).GetEnumerator();
    Stack<Question> questions = new();

    while (lines.MoveNext()) {
        string line = lines.Current;

        if (line.StartsWith("Question")) {
            int number = int.Parse(Regex.Replace(line, @"[^\d+]", ""));
            
            while (lines.MoveNext() && string.IsNullOrWhiteSpace(lines.Current)) {} // Skip empty lines
            Match question = Regex.Match(lines.Current, @"(?:\s{1,2}(?!\s)|\S)+");

            string chunk = lines.Current.Substring(question.Index + question.Length);

            questions.Push(new Question {
                Number = number,
                Text = question.Value,
                Code = !string.IsNullOrWhiteSpace(chunk) ? chunk.Trim() + '\n' : ""
            });
        }
        else if (!string.IsNullOrWhiteSpace(line) && questions.Count != 0) {
            MatchCollection answers = Regex.Matches(line, @"([A-E])[\)\.]\s*((?:\s(?!\s)|\S)+)");
            Question question = questions.Peek();

            foreach (Match m in answers) {
                question.Choices[m.Groups[1].Value] = m.Groups[2].Value;
            }

            if (answers.Count > 0) {
                Match last = answers.Last();
                string chunk = line.Substring(last.Index + last.Length).Trim();

                if (!string.IsNullOrWhiteSpace(chunk)) {
                    question.Code += chunk + '\n';
                }
            }
            else {
                question.Code += line.Trim() + '\n';
            }
        }
    }

    foreach (Question q in questions) {
        string s = $"Question: {q.Text}\n\nCode: {q.Code}\n\n";

        foreach (String choice in q.Choices.Keys) {
            s += $"{choice}) {q.Choices[choice]}\n";
        }

        Console.WriteLine(s + "\n\n");
    }
}

class Question {
    public int Number;
    public string Text = "";
    public Dictionary<string, string> Choices = new();
    public string? Code;
}
