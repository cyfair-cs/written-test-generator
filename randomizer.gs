// Configuration
const FORM_OUTPUT_ID = '1o7agNlPBrsiFjWaxz1SpUR8-jDwtlXXK'
const JSON_INPUT_ID = '1H3Oo9ZqIuUQuj9uk-UlWLQlmnyVhpO7B'

const NUM_FORMS_CREATED = 1
const FORM_SIZE = 20
const ORDER_BY_NUMBER = true

function main() {
  // Get the folder
  const question_bank_json = DriveApp.getFileById(JSON_INPUT_ID);

  // Loop over the files
  Logger.log('Loading question bank...')
  const question_bank = readQuestionsFromFile(question_bank_json);
  Logger.log('Loaded question bank. Raw bank size: ' + question_bank.length);

  // get random 20
  for (let i = 1; i <= NUM_FORMS_CREATED; i++) {
    Logger.log('Generating question set #' + i + '.');
    let question_set = generate_question_set(question_bank);
    Logger.log('Generating form #' + i + '.');
    createQuestionsOnForm(question_set, `Randomized Written Test (${FORM_SIZE}) #${i}`);
  }
}

function generate_question_set(question_bank) {
  var question_set = [];

  while (question_set.length < FORM_SIZE) {
    var question = question_bank[ (Math.random() * question_bank.length) | 0 ];
    if (!question_set.includes(question) || !has_remove_heuristic(question)) {
      Logger.log(question.testSource + ' - ' + question.questionNumber);
      question_set.push(question);
    }
  }

  if (ORDER_BY_NUMBER) {
    Logger.log("Ordering by number...");
    question_set.sort((a, b) => a.questionNumber - b.questionNumber);
  }

  return question_set;
}

// this just filters out any questions in the question bank that are missing some data
function has_remove_heuristic(question) {
  let invalid = false;
  // No correct answer set
  if (question.correctAnswer = 'No correct answer found.')
    invalid = true;
  if (question.choices.length == 0)
    invalid = true;
  if (invalid)
    Logger.log('INVALID QUESTION: ' + question.testSource + ' - ' + question.questionNumber);
  return invalid;
}

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
    question.questionDescription = data[i].questionDescription;
    question.answers = data[i].answers;
    question.testSource = data[i].testSource;
    question.correctAnswer = data[i].correctAnswer;
    question.questionNumber = data[i].questionNumber;

    // Add the Question object to the array
    questions.push(question);
  }

  return questions;
}

function createQuestionsOnForm(questions, name) {
  // Get the form to add the questions to
  var form = FormApp.create(name); // each set will have the same test source

  // move to separate folder
  DriveApp.getFileById(form.getId()).moveTo(DriveApp.getFolderById(FORM_OUTPUT_ID));

  form.setTitle(name);
  form.setDescription("This written test practice was automatically generated. If any answers seem incorrect or anything seems out of place, please make a note of the problem number, test source and version, and notify a club officer immediately.")
  form.setIsQuiz(true);

  // Iterate over the array of questions
  for (var i = 0; i < questions.length; i++) {

    // Add the question to the form
    var question = form.addMultipleChoiceItem();
    question.setTitle('Source: ' + questions[i].testSource + ' - Question ' + questions[i].questionNumber + '\n\n' + questions[i].questionDescription)
    question.setPoints(1);

    // add choices
    var choices = [];
    questions[i].answers.forEach(choice => {
      var letter = choice.substring(0, 1);
      // Logger.log(choice + ' ' + letter + ': ' + questions[i].correctAnswer);
      // question.createChoice(choice, questions[i].correctAnswer == letter);
      choices.push(question.createChoice(choice, letter == questions[i].correctAnswer));
    });
    question.setChoices(choices);
  }
}
