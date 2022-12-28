# written-test-generator

This program generates google forms for practice from past UIL Computer Science tests. The dataset is finely pruned: any tests that may be added to the dataset must meet 3 criteria:

1. It must be a PDF.
2. All text in the PDF must be selectable.
3. It must be named accordingly: `contest-name_versionnumber_year.pdf` (Example: `districts_1_2010.pdf`)
4. There must be a publicly available answer key somewhere for it. (Best case scenario: it has the answer document at the bottom of the test, but if the answer key is stored on an external document, then upload it to the `keys/` directory)

## How to use

The datasets are first compiled from the PDFs to JSON files containing the question data, then they are sent (manually) to the google apps script to be converted into google forms.

1. Clone this repository, open with IntelliJ IDEA (or similar Java editor), and run the Main.java file.
2. Create a folder on google drive and upload the `output/` directory and the `automator.gs` and `randomizer.gs` scripts into it.
3. Create a folder called `forms` within the google drive folder.
4. Copy the ID of the `forms/` folder in the Google Drive to the `FORMS_OUTPUT_ID` constant in `automator.gs` and `randomizer.gs`

### Use with creating form versions of past written tests

5. Copy the [ID](https://ploi.io/documentation/database/where-do-i-get-google-drive-folder-id) of the `output/` folder in Google Drive to the `JSON_INPUT_ID` constant in `automator.gs`
6. Run the main function in `automator.gs` in google apps script to compile to the JSON data to google forms.

### Use with creating completely random written tests based on a question pool


5. Copy the [ID](https://ploi.io/documentation/database/where-do-i-get-google-drive-folder-id) of the global question pool (`output/2022 Written Test Question Pool.json`) in Google Drive to the `JSON_INPUT_ID` constant in `randomizer.gs`
6. Configure the script's constants to your needs (at the top) - form size, number of forms created, and whether the form will be sorted by question number 
7. Run the main function in `randomizer.gs` in google apps script to compile to the JSON data to google forms.
