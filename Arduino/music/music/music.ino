#include <ArduinoJson.h>

#include <Audio.h>
#include "Arduino.h"
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#include <WiFiClientSecure.h>
#include <WebSocketsClient_Generic.h>

//Digital I/O used  //Makerfabs Audio V2.0
#define I2S_DOUT 27
#define I2S_BCLK 26
#define I2S_LRC 25

//SSD1306
#define MAKEPYTHON_ESP32_SDA 4
#define MAKEPYTHON_ESP32_SCL 5
#define SCREEN_WIDTH 128 // OLED display width, in pixelsF
#define SCREEN_HEIGHT 64 // OLED display height, in pixels
#define OLED_RESET -1    // Reset pin # (or -1 if sharing Arduino reset pin)

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

//Button
const int Pin_vol_up = 39;
const int Pin_vol_down = 36;
const int Pin_mute = 35;

const int Pin_previous = 15;
const int Pin_pause = 33;
const int Pin_next = 2;

Audio audio;

//設置WiFi名稱與密碼
const char *ssid = "IAI";
const char *password = "j304j304j304";

//音樂長度
int runtime = 0;
int length = 0;

//紀錄播音狀態
bool playmusic = false;

//websocket
WebSocketsClient webSocket;

void setup()
{
  //IO mode init
  pinMode(Pin_vol_up, INPUT_PULLUP);
  pinMode(Pin_vol_down, INPUT_PULLUP);
  pinMode(Pin_mute, INPUT_PULLUP);
  pinMode(Pin_previous, INPUT_PULLUP);
  pinMode(Pin_pause, INPUT_PULLUP);
  pinMode(Pin_next, INPUT_PULLUP);

  //Serial
  Serial.begin(115200);

  //****LCD****
  
  Wire.begin(MAKEPYTHON_ESP32_SDA, MAKEPYTHON_ESP32_SCL);
  // SSD1306_SWITCHCAPVCC = generate display voltage from 3.3V internally
  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C))
  { // Address 0x3C for 128x32
    Serial.println(F("SSD1306 allocation failed"));
    for (;;)
      ; // Don't proceed, loop forever
  }
  display.clearDisplay();
  logoshow();
  
  //****connect to WiFi****
  
  Serial.printf("Connecting to %s ", ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }
  Serial.println(" CONNECTED");
  lcd_text("Wifi CONNECT");
  
  //****Audio(I2S)****
  
  //pinr腳
  audio.setPinout(I2S_BCLK, I2S_LRC, I2S_DOUT);
  //音量
  audio.setVolume(21); // 0...21
  //IP & Port
  webSocket.begin("192.168.50.192", 3000);
  webSocket.onEvent(webSocketEvent);
}

//螢幕更新初值
uint run_time = 0;
String url = "";

void loop()
{
  //audio.loop();
  print_song_time();
  webSocket.loop();
  //螢幕更新
  if (millis() - run_time > 1000)
  {
    run_time = millis();
    display_music();
  }
  if (playmusic == true) {
    audio.loop();
  }
}

void print_song_time()
{
  runtime = audio.getAudioCurrentTime();
  length = audio.getAudioFileDuration();
}

void open_new_radio(const char* station)
{
  audio.connecttohost(station);
  runtime = audio.getAudioCurrentTime();
  length = audio.getAudioFileDuration();
  Serial.println("**********start a new radio************");
}

void display_music()
{
  int line_step = 24;
  int line = 0;
  char buff[20];
  
  sprintf(buff, "RunningTime:%d", runtime);

  display.clearDisplay();

  display.setTextSize(1);              // Normal 1:1 pixel scale
  display.setTextColor(SSD1306_WHITE); // Draw white text

  display.setCursor(0, line); // Start at top-left corner
  display.println(url);
  line += line_step;

  display.setCursor(0, line); //test
  display.println("123");
  line += line_step;

  display.setCursor(0, line);
  display.println(buff);
  line += line_step;

  display.display();
}

void logoshow(void)
{
  display.clearDisplay();

  display.setTextSize(2);              // Normal 1:1 pixel scale
  display.setTextColor(SSD1306_WHITE); // Draw white text
  display.setCursor(0, 0);             // Start at top-left corner
  display.println(F("MakePython"));
  display.setCursor(0, 20); // Start at top-left corner
  display.println(F("WEB RADIO"));
  display.setCursor(0, 40); // Start at top-left corner
  display.println(F(""));
  display.display();
  delay(2000);
}

void lcd_text(String text)
{
  display.clearDisplay();

  display.setTextSize(2);              // Normal 1:1 pixel scale
  display.setTextColor(SSD1306_WHITE); // Draw white text
  display.setCursor(0, 0);             // Start at top-left corner
  display.println(text);
  display.display();
  delay(500);
}

//**********************************************
//webSocket
void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
  //{"music":"CbST"}
  switch (type) {
    case WStype_DISCONNECTED:
      Serial.printf("[WSc] Disconnected!\n");
      break;
    case WStype_CONNECTED:
      {

        Serial.printf("[WSc] Connected to url: %s\n",  payload);

        // send message to server when Connected
        webSocket.sendTXT("Connected");
      }
      break;
    case WStype_TEXT:
      {
        StaticJsonDocument<256> doc;
        StaticJsonDocument<256> body;
        char* c = (char*) payload;
        Serial.printf("[WSc] get text: %s\n",  payload);
        DeserializationError err = deserializeJson(doc, c);
        if (err) {
          Serial.print("ERROR：");
          Serial.println(err.c_str());
          return;
        }
        String door = doc["music"];
        String get_door_number = doc["door_number"];
        
        Serial.println(door);
        //新增門禁辨別
         if (get_door_number == "J304") {
          // 如果get_door_number的值等於"J304"，執行這裡的程式碼
          //IP & Port
          url = "http://192.168.50.192:3000/music_library/" + door;
          
          const char* url2 = url.c_str();
          //Serial.printf("[WSc] get text: %s %s\n",uid ,);
          open_new_radio(url2);
          playmusic = true;
          //Serial.println(uid);
          //Serial.println(soundId);
          //Serial.println(url);  
        }
      }
      break;
    case WStype_BIN:
      Serial.printf("[WSc] get binary length: %u\n", length);
      break;
    case WStype_ERROR:
    case WStype_FRAGMENT_TEXT_START:
    case WStype_FRAGMENT_BIN_START:
    case WStype_FRAGMENT:
    case WStype_FRAGMENT_FIN:
      break;
  }
}

//**********************************************
// optional
void audio_info(const char *info)
{
  Serial.print("info        ");
  Serial.println(info);
}
void audio_id3data(const char *info)
{ //id3 metadata
  Serial.print("id3data     ");
  Serial.println(info);
}

void audio_eof_mp3(const char *info)
{ //end of file
  Serial.print("eof_mp3     ");
  Serial.println(info);
}
void audio_showstation(const char *info)
{
  Serial.print("station     ");
  Serial.println(info);
}
void audio_showstreaminfo(const char *info)
{
  Serial.print("streaminfo  ");
  Serial.println(info);
}
void audio_showstreamtitle(const char *info)
{
  Serial.print("streamtitle ");
  Serial.println(info);
}
void audio_bitrate(const char *info)
{
  Serial.print("bitrate     ");
  Serial.println(info);
}
void audio_commercial(const char *info)
{ //duration in sec
  Serial.print("commercial  ");
  Serial.println(info);
}
void audio_icyurl(const char *info)
{ //homepage
  Serial.print("icyurl      ");
  Serial.println(info);
}
void audio_lasthost(const char *info)
{ //stream URL played
  Serial.print("lasthost    ");
  Serial.println(info);
}
void audio_eof_speech(const char *info)
{
  Serial.print("eof_speech  ");
  Serial.println(info);
}
