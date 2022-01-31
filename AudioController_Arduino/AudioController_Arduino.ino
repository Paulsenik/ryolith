#include "EEPROM.h";

const byte DT[] = {10,9,8,7};
const byte CLK[] = {6,5,4,3};
const byte SW[] = {A3,A2,A1,A0};
const byte LED = 11;

const int MINDELAY = 75; // cooldown before rotating again
const byte VALUE_STEP_BIG = 40;
const byte VALUE_STEP_SMALL = 5;

// LED
long lastLED_ON;
const int LED_ON_TIME = 150;

// rot-encoder statemachine with cooldown
int eState[sizeof(CLK)], eLastState[sizeof(CLK)];
long eLastRot[sizeof(CLK)];

// audio Volume 0-1023
int lastValues[sizeof(CLK)];

// encoder_btn with cooldown
bool SW_value[sizeof(SW)];
bool muted[sizeof(SW)], currentlyPressed[sizeof(SW)];
long SW_lastPress[sizeof(SW)];

void setup() {

  pinMode(LED, OUTPUT);
  digitalWrite(LED, HIGH); // Light up on start

  EEPROM.begin();

  for(int i=0;i<sizeof(CLK);i++){
    pinMode (CLK[i],INPUT);
    pinMode (DT[i],INPUT);
    pinMode(SW[i], INPUT_PULLUP);
    eLastState[i] = digitalRead(CLK[i]);

    // read
    EEPROM.get(i*2,lastValues[i]);

    // muted
    muted[i] = false;
    currentlyPressed[i] = false;
    SW_value[i] = true;
  }

  Serial.begin(9600);
  digitalWrite(LED, LOW);
}

void loop() {
  for(int i=0;i<sizeof(CLK);i++){
    checkPot(i);
  }

  if(digitalRead(LED) == HIGH){
    if(lastLED_ON + LED_ON_TIME < millis()){
      digitalWrite(LED,LOW);
    }
  }
}

void checkPot(int i){
  eState[i] = digitalRead(CLK[i]);
  if (eState[i] != eLastState[i]){
    if(eLastRot[i] + MINDELAY < millis()){

      if (digitalRead(DT[i]) != eState[i]){
        if(currentlyPressed[i])
          lastValues[i]-=VALUE_STEP_SMALL;
        else
          lastValues[i]-=VALUE_STEP_BIG;
        //Serial.println(-1);
      }else{
        //Serial.println(1);
        if(currentlyPressed[i])
          lastValues[i]+=VALUE_STEP_SMALL;
        else
          lastValues[i]+=VALUE_STEP_BIG;
      }

      // muted
      muted[i] = false;

      // 0-1023
      lastValues[i] = (lastValues[i] > 1023 ? 1023 : (lastValues[i] < 0 ? 0 : lastValues[i]));

      // save
      EEPROM.put(i*2,lastValues[i]);

      // Output
      sendValue(i,lastValues[i]);

      eLastRot[i] = millis();
    }
  }
  eLastState[i] = eState[i];

  // SW-Button
  int tempValue = digitalRead(SW[i]);
  if(SW_value[i] != tempValue){
    if(SW_lastPress[i] + MINDELAY < millis()){

      SW_value[i] = tempValue;

      if(SW_value[i] == false){ // pressing
        currentlyPressed[i] = true;
      }else{
        currentlyPressed[i] = false;
        if(SW_lastPress[i] > eLastRot[i]){
          muteAction(i);
        }
      }

      SW_lastPress[i] = millis();
    }
  }
}

void muteAction(int i) {
  if(muted[i]){
    muted[i] = false;
    sendValue(i,lastValues[i]);
  }else{
    muted[i] = true;
    sendValue(i,0);
  }
}

void sendValue(int index, int value){
  Serial.print("am[CTRL");
  Serial.print(index);
  Serial.print("|=>");
  Serial.print(value);
  Serial.println("]");

  // LED indicator
  lastLED_ON = millis();
  digitalWrite(LED, HIGH);
}
