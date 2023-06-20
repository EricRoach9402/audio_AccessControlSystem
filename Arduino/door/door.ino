#include <Arduino.h>
#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <WebSocketsClient_Generic.h>

//#include <WebSocketsClient.h>

#include <Hash.h>
// the setup function runs once when you press reset or power the board
const int ledPin = 15;
WebSocketsClient webSocket;


const char *ssid = "IAI";
const char *password = "j304j304j304";

uint open_time;
bool opendoor = false;
void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
  switch (type) {
    case WStype_DISCONNECTED:
      Serial.printf("[WSc] Disconnected!\n");
      break;
    case WStype_CONNECTED:
      {
        Serial.printf("[WSc] Connected to url: %s\n",  payload);
        webSocket.sendTXT("Connected");
      }
      break;
    case WStype_TEXT:
      {
        StaticJsonDocument<256> doc;
        char* c = (char*) payload;
        Serial.printf("[WSc] get text: %s\n", payload);
        DeserializationError err = deserializeJson(doc, c);
        if (err) {
          Serial.print("ERROR：");
          Serial.println(err.c_str());
        }
        String get_door_number = doc["door_number"];
        //新增門禁辨別
        if (get_door_number == "J304") {
          String door = doc["target"];
          Serial.print("door = " + door);
          if (door == "door") {
            opendoor = true;
            open_time = millis();
            digitalWrite(ledPin, LOW);
          }
          Serial.println(door);
         }
      }
      break;
    case WStype_BIN:
      Serial.printf("[WSc] get binary length: %u\n", length);
      hexdump(payload, length);
      break;
  }
}


void setup() {
  // initialize digital pin LED_BUILTIN as an output.
  pinMode(ledPin, OUTPUT);
  Serial.begin(115200);
  Serial.setDebugOutput(true);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }

  webSocket.begin("192.168.50.192", 3000);
  //webSocket.begin("121.40.165.18", 8800);
  webSocket.onEvent(webSocketEvent);
  digitalWrite(ledPin, HIGH);
}

// the loop function runs over and over again forever
void loop() {
  webSocket.loop();
  if (millis() - open_time > 1000) {
    opendoor = false;
    digitalWrite(ledPin, HIGH);
  }
}
