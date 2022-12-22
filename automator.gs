/* Author: Mufaro Machaya (nickelulz)
 * 
 * Description: Loader for test question data into google forms.
 * Note: This is still just a prototype. It is still in the works!
 */

const FORM_OUTPUT_ID = '1o7agNlPBrsiFjWaxz1SpUR8-jDwtlXXK'
const JSON_INPUT_ID = '1Ube8o-klz0rbYv3h54YjQGaT-aC2xS0q'

// Get the folder
var json_input_folder = DriveApp.getFolderById(JSON_INPUT_ID);

// Get an iterator for the files in the folder
var json_files = json_input_folder.getFiles();

// Loop over the files
while (json_files.hasNext())
  createQuestionsOnForm(readQuestionsFromFile(json_files.next()));

function readQuestionsFromFile(file) {
  // Read the JSON file into a string
  var content = file.getBlob().getDataAsString();

  // Parse the JSON string into an object
  var data = JSON.parse(content);

  // Create an array to store the questions
  var questions = [];

  // Iterate over the array of questions in the JSON object
  for (var i = 0; i < data.length; i++) {

    // Create a new Question object
    var question = {};
    question.questionDescription = questionData.questionDescription;
    question.answers = data[i].answers;
    question.testSource = data[i].testSource;
    question.correctAnswer = data[i].correctAnswer;
    question.questionNumber = data[i].questionNumber;

    // Add the Question object to the array
    questions.push(question);
  }

  return questions;
}

function createQuestionsOnForm(questions) {
  // Get the form to add the questions to
  var form = FormApp.create(questions[0].testSource); // each set will have the same test source

  // move to separate folder
  DriveApp.getFileById(form.getId()).moveTo(DriveApp.getFolderById(FORM_OUTPUT_ID));

  form.setTitle(questions[0].testSource);
  form.setDescription("This written test practice was automatically generated. If any answers seem incorrect or anything seems out of place, please make a note of the problem number, test source and version, and notify a club officer immediately.")

  // Iterate over the array of questions
  for (var i = 0; i < questions.length; i++) {

    // Get the correct answer
    var correctAnswer = questions[i].correctAnswer;

    // Create a list of choices for the question
    var choices = [];
    for (var j = 0; j < questions[i].answers.length; j++) {
      choices.push(questions[i].answers[j]);
    }

    // Add the question to the form
    var question = form.addMultipleChoiceItem();
    question.setTitle(question.questionNumber + ":\n" + question.questionDescription)
    question.setChoiceValues(choices)
    // question.setCorrectAnswer(correctAnswer);
  }
}
