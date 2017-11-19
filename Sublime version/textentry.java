import java.util.Arrays;
import java.util.Collections;

// TODO: if drag off screen, don't type letter
// TODO: mark where user first clicked (help gauge distance needed?)
// TODO: tap "currentLetter" box to repeat a letter?
// TODO: can create "rings" of distances to visually represent where finger needs to go for each letter after click
// TODO: use phone rotation for delete/space

String[] phrases; //contains all of the phrases
int totalTrialNum = 4; //the total number of phrases to be tested - set this low for testing. Might be ~10 for the real bakeoff!
int currTrialNum = 0; // the current trial number (indexes into trials array above)
float startTime = 0; // time starts when the first letter is entered
float finishTime = 0; // records the time of when the final trial ends
float lastTime = 0; //the timestamp of when the last trial was completed
float lettersEnteredTotal = 0; //a running total of the number of letters the user has entered (need this for final WPM computation)
float lettersExpectedTotal = 0; //a running total of the number of letters expected (correct phrases)
float errorsTotal = 0; //a running total of the number of errors (when hitting next)
String currentPhrase = ""; //the current target phrase
String currentTyped = ""; //what the user has typed so far
final int DPIofYourDeviceScreen = 441; //you will need to look up the DPI or PPI of your device to make sure you get the right scale!!
                                      //http://en.wikipedia.org/wiki/List_of_displays_by_pixel_density
final float sizeOfInputArea = DPIofYourDeviceScreen*1; //aka, 1.0 inches square!

char currentLetter = '\u0009';
boolean clicked = false;
int numSpaces = 2;
int clickX = 0;
int clickY = 0;
int screenOffsetX = 200;
int screenOffsetY = 200;
int buttonWidth = int(sizeOfInputArea/2);
int buttonHeight = int(sizeOfInputArea/4);


private class Button
{
  int x = 0;
  int y = 0;
  int width = buttonWidth;
  int height = buttonHeight;
  String character = "";
  boolean selected = false;
}

ArrayList<Button> buttons = new ArrayList<Button>();

//You can modify anything in here. This is just a basic implementation.
void setup()
{
  phrases = loadStrings("phrases2.txt"); //load the phrase set into memory
  Collections.shuffle(Arrays.asList(phrases)); //randomize the order of the phrases
    
  orientation(PORTRAIT); //can also be LANDSCAPE -- sets orientation on android device
  size(1000, 1000); //Sets the size of the app. You may want to modify this to your device. Many phones today are 1080 wide by 1920 tall.
  textFont(createFont("Arial", 24)); //set the font to arial 24
  noStroke(); //my code doesn't use any strokes.
  
  // qwert; top left
  Button b = new Button();
    b.x = 0;
    b.y = buttonHeight; //second row (of four)
    b.character = "q w e r t";
    buttons.add(b);
    System.out.println("y is : " + b.y);

  // asdfg; middle left
  b = new Button();
    b.x = 0;
    b.y = buttonHeight * 2;
    b.character = "a s d f g";
    buttons.add(b);
    System.out.println("next y is : " + b.y);
    
  // zxcv; bottom left
  b = new Button();
    b.x = 0;
    b.y = buttonHeight * 3;
    b.character = "z x c v";
    buttons.add(b);
    
  // yuiop; top right
  b = new Button();
    b.x = buttonWidth;
    b.y = buttonHeight;
    b.character = "y u i o p";
    buttons.add(b);

  // hjkl; middle right
  b = new Button();
    b.x = buttonWidth;
    b.y = buttonHeight * 2;
    b.character = "h j k l";
    buttons.add(b);
    
  // bnm; bottom right
  b = new Button();
    b.x = buttonWidth;
    b.y = buttonHeight * 3;
    b.character = "b n m";
    buttons.add(b);

  // delete button
  b = new Button();
    b.width = int(sizeOfInputArea/3);
    b.character = "delete";
    buttons.add(b);

  // space button
  b = new Button();
    b.x = int(sizeOfInputArea/3) * 2;
    b.width = int(sizeOfInputArea/3);
    b.character = " ";
    buttons.add(b);
}

//You can modify anything in here. This is just a basic implementation.
void draw()
{
  background(0); //clear background

 // image(watch,-200,200);
  fill(100);
  rect(200, 200, sizeOfInputArea, sizeOfInputArea); //input area should be 2" by 2"
  
  drawKeyboard();
  textFont(createFont("Arial", 24)); //set the font to arial 24

  // update + draw current letter
  updateCurrentLetter();
  if (startTime != 0) {
    pushMatrix();
    translate(screenOffsetX, screenOffsetY);
    textAlign(CENTER);

    strokeWeight(2);
    stroke(255);

    fill(255, 0, 0);
    int third = int(sizeOfInputArea/3);
    rect(third, 0, third, buttonHeight);

    fill(255);
    text(currentLetter, third * 1.5, 0 + buttonHeight/2);

    noStroke();

    popMatrix();
  }

  if (finishTime!=0)
  {
    fill(255);
    textAlign(CENTER);
    text("Finished", 280, 150);
    return;
  }

  if (startTime==0 & !mousePressed)
  {
    fill(255);
    textAlign(CENTER);
    text("Click to start time!", 280, 150); //display this messsage until the user clicks!
  }

  if (startTime==0 & mousePressed)
  {
    nextTrial(); //start the trials!
  }

  if (startTime!=0)
  {
    //you will need something like the next 10 lines in your code. Output does not have to be within the 2 inch area!
    textAlign(LEFT); //align the text left
    fill(128);
    text("Phrase " + (currTrialNum+1) + " of " + totalTrialNum, 70, 50); //draw the trial count
    fill(255);
    text("Target:   " + currentPhrase, 70, 100); //draw the target string
    text("Entered:  " + currentTyped, 70, 140); //draw what the user has entered thus far 
    fill(255, 0, 0);
    rect(800, 00, 200, 200); //drag next button
    fill(255);
    text("NEXT > ", 850, 100); //draw next label
  }
  
}

boolean didMouseClick(float x, float y, float w, float h) //simple function to do hit testing
{
  return (mouseX > x && mouseX<x+w && mouseY>y && mouseY<y+h); //check to see if it is in button bounds
}


void mousePressed()
{  
  if (startTime == 0) { return; }
  
  clicked = true;

  clickX = mouseX;
  clickY = mouseY;
  checkSelected();

  //You are allowed to have a next button outside the 2" area
  if (didMouseClick(800, 00, 200, 200)) //check if click is in next button
  {
    nextTrial(); //if so, advance to next trial
  }
}

void updateCurrentLetter() 
{
if (startTime == 0) { return; }
// System.out.println("started");
if (clicked == false) { return; }
  for (int i=0; i<buttons.size(); i++) {
    Button b = buttons.get(i);
    if (b.selected) {
      // System.out.println("selected: " + b.character);
      if (b.character != "delete" && b.character != " ") {
        int releaseX = mouseX;
        int releaseY = mouseY;
        
        PVector v = new PVector(releaseX-clickX, releaseY-clickY);
        float dragDistance = v.mag();
        // System.out.println("mag is: " + dragDistance);
        if (dragDistance < 50) {
          currentLetter = b.character.charAt(0);
        } else if (dragDistance < 100) {
          currentLetter = b.character.charAt(1 * numSpaces);
        } else if (dragDistance < 150) {
          currentLetter = b.character.charAt(2 * numSpaces);
        } else if (dragDistance < 200) {
          // if there is no fourth char, do third
          if (b.character.length() - 1 >= (3 * numSpaces)) {
            currentLetter = b.character.charAt(3 * numSpaces);;
          } else {
            currentLetter = b.character.charAt(2 * numSpaces);
          }
        } else {
          // if there is no fifth char, do fourth, if there is no fourth char, do third
          if (b.character.length() - 1 >= (4 * numSpaces)) {
            currentLetter = b.character.charAt(4 * numSpaces);
          } else {
            if (b.character.length() - 1 >= (3 * numSpaces)) {
              currentLetter = b.character.charAt(3 * numSpaces);
            } else {
              currentLetter = b.character.charAt(2 * numSpaces);
            }
          }
        }
      }
      break;
    }
  }
}

void mouseReleased() 
{
if (startTime == 0) { return; }
clicked = false;
currentLetter = '\u0009';

  for (int i=0; i<buttons.size(); i++) {
    Button b = buttons.get(i);
    if (b.selected) {
      b.selected = false;
      if (b.character == "delete") {
        if (currentTyped.length() > 0)
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
      }
      else if (b.character == " ") {
        currentTyped += b.character;
      }
      else {
        int releaseX = mouseX;
        int releaseY = mouseY;
        
        PVector v = new PVector(releaseX-clickX, releaseY-clickY);
        float dragDistance = v.mag();

        if (dragDistance < 50) {
          currentTyped += b.character.charAt(0);
        } else if (dragDistance < 100) {
          currentTyped += b.character.charAt(1 * numSpaces);
        } else if (dragDistance < 150) {
          currentTyped += b.character.charAt(2 * numSpaces);
        } else if (dragDistance < 200) {
          // if there is no fourth char, do third
          if (b.character.length() - 1 >= (3 * numSpaces)) {
            currentTyped += b.character.charAt(3 * numSpaces);;
          } else {
            currentTyped += b.character.charAt(2 * numSpaces);
          }
        } else {
          // if there is no fifth char, do fourth, if there is no fourth char, do third
          if (b.character.length() - 1 >= (4 * numSpaces)) {
            currentTyped += b.character.charAt(4 * numSpaces);
          } else {
            if (b.character.length() - 1 >= (3 * numSpaces)) {
              currentTyped += b.character.charAt(3 * numSpaces);
            } else {
              currentTyped += b.character.charAt(2 * numSpaces);
            }
          }
        }
      }
      break;
    }
  }
}

void checkSelected() {
  for (int i=0; i<buttons.size(); i++) {
    Button b = buttons.get(i);
    
    b.selected = false;
    int transformedMouseX = int(mouseX - screenOffsetX);
    int transformedMouseY = int(mouseY - screenOffsetY);
    if (transformedMouseX >= b.x && transformedMouseX < b.x+b.width && transformedMouseY >= b.y && transformedMouseY < b.y+b.height) {
      b.selected = true;
    }
  }
}


void nextTrial()
{
  if (currTrialNum >= totalTrialNum) //check to see if experiment is done
    return; //if so, just return

    if (startTime!=0 && finishTime==0) //in the middle of trials
  {
    System.out.println("==================");
    System.out.println("Phrase " + (currTrialNum+1) + " of " + totalTrialNum); //output
    System.out.println("Target phrase: " + currentPhrase); //output
    System.out.println("Phrase length: " + currentPhrase.length()); //output
    System.out.println("User typed: " + currentTyped); //output
    System.out.println("User typed length: " + currentTyped.length()); //output
    System.out.println("Number of errors: " + computeLevenshteinDistance(currentTyped.trim(), currentPhrase.trim())); //trim whitespace and compute errors
    System.out.println("Time taken on this trial: " + (millis()-lastTime)); //output
    System.out.println("Time taken since beginning: " + (millis()-startTime)); //output
    System.out.println("==================");
    lettersExpectedTotal+=currentPhrase.length();
    lettersEnteredTotal+=currentTyped.length();
    errorsTotal+=computeLevenshteinDistance(currentTyped.trim(), currentPhrase.trim());
  }

  //probably shouldn't need to modify any of this output / penalty code.
  if (currTrialNum == totalTrialNum-1) //check to see if experiment just finished
  {
    finishTime = millis();
    System.out.println("==================");
    System.out.println("Trials complete!"); //output
    System.out.println("Total time taken: " + (finishTime - startTime)); //output
    System.out.println("Total letters entered: " + lettersEnteredTotal); //output
    System.out.println("Total letters expected: " + lettersExpectedTotal); //output
    System.out.println("Total errors entered: " + errorsTotal); //output
    
    float wpm = (lettersEnteredTotal/5.0f)/((finishTime - startTime)/60000f); //FYI - 60K is number of milliseconds in minute
    System.out.println("Raw WPM: " + wpm); //output
    
    float freebieErrors = lettersExpectedTotal*.05; //no penalty if errors are under 5% of chars
    
    System.out.println("Freebie errors: " + freebieErrors); //output
    float penalty = max(errorsTotal-freebieErrors,0) * .5f;
    
    System.out.println("Penalty: " + penalty);
    System.out.println("WPM w/ penalty: " + (wpm-penalty)); //yes, minus, becuase higher WPM is better
    System.out.println("==================");
    
    currTrialNum++; //increment by one so this mesage only appears once when all trials are done
    return;
  }

  if (startTime==0) //first trial starting now
  {
    System.out.println("Trials beginning! Starting timer..."); //output we're done
    startTime = millis(); //start the timer!
  }
  else
  {
    currTrialNum++; //increment trial number
  }

  lastTime = millis(); //record the time of when this trial ended
  currentTyped = ""; //clear what is currently typed preparing for next trial
  currentPhrase = phrases[currTrialNum]; // load the next phrase!
  //currentPhrase = "abc"; // uncomment this to override the test phrase (useful for debugging)
}



//=========SHOULD NOT NEED TO TOUCH THIS METHOD AT ALL!==============
int computeLevenshteinDistance(String phrase1, String phrase2) //this computers error between two strings
{
  int[][] distance = new int[phrase1.length() + 1][phrase2.length() + 1];

  for (int i = 0; i <= phrase1.length(); i++)
    distance[i][0] = i;
  for (int j = 1; j <= phrase2.length(); j++)
    distance[0][j] = j;

  for (int i = 1; i <= phrase1.length(); i++)
    for (int j = 1; j <= phrase2.length(); j++)
      distance[i][j] = min(min(distance[i - 1][j] + 1, distance[i][j - 1] + 1), distance[i - 1][j - 1] + ((phrase1.charAt(i - 1) == phrase2.charAt(j - 1)) ? 0 : 1));

  return distance[phrase1.length()][phrase2.length()];
}

void drawKeyboard() {
  if (startTime == 0) { return; }

  pushMatrix();
  translate(screenOffsetX, screenOffsetY);
  strokeWeight(2);
  stroke(255);
  textAlign(CENTER);
  for(int i=0; i<buttons.size(); i++) {
    Button b = buttons.get(i);
    fill(255, 0, 0);
    if (b.selected) {
      fill(0, 255, 0);
    }
    rect(b.x, b.y, b.width, b.height);
    fill(255);
    text(b.character, b.x + b.width/2, b.y + b.height/2);
  }
  popMatrix();

  noStroke();
}