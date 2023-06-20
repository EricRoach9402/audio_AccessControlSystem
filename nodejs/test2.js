//import express 和 ws 套件
const express = require('express')
const SocketServer = require('ws').Server
//監聽
var events=require('events')
var em=new events.EventEmitter()

//接收PHP傳遞過來的消息
//var app = express();   

//指定開啟的 port;監聽websocket用
const PORT = 3001
//創建 express 的物件，並綁定及監聽 3001 port ，且設定開啟後在 console 中提示
const server = express()
    .listen(PORT, () => console.log(`Listening on ${PORT}`))

//將 express 交給 SocketServer 開啟 WebSocket 的服務
const wss = new SocketServer({ server })

//當 WebSocket 從外部連結時執行
wss.on('connection', ws => {
    console.log('Client connected')
    em.on("FirstEvent",function(data){
        console.log(data)
        ws.send(data)
    })
    //對 message 設定監聽，接收從 Client 發送的訊息
    ws.on('message', data => {
            //取得所有連接中的 client
            let clients = wss.clients

            //做迴圈，發送訊息至每個 client
            clients.forEach(client => {
                client.send(data)
            })
    })

    ws.on('close', () => {
        console.log('Close connected')
    })
})

//接收PHP傳遞過來的消息
var app = express();
app.use(express.static(__dirname));

app.use(require('body-parser').urlencoded({extended: true}));
app.post('/send', function (req, res) {
    console.log(req.body);
    var jsonData = req.body;
    //const res_data = JSON.parse(jsonData);
    //console.log(jsonData["OpenDoor"][0])
    if (jsonData["OpenDoor"][0]==="Y"){
        //jsonData["OpenDoor"] = "N"
        var MusicID = jsonData["MusicNumber"][0];

        //console.log(jsonData["OpenDoor"][0])
        //em.emit('FirstEvent','我已經進入了第一事件的監聽函式!');
        var obj = { "music":"stop","target":"door"};
        var str = JSON.stringify(obj);
        em.emit('FirstEvent',str);
    }
    if (jsonData["OpenDoor"][0]==="N"){
        //jsonData["OpenDoor"] = "N"
        var MusicID = jsonData["MusicNumber"][0];

        //console.log(jsonData["OpenDoor"][0])
        //em.emit('FirstEvent','我已經進入了第一事件的監聽函式!');
        var obj = { "music":"SaEE.mp3","target":"N"};
        var str = JSON.stringify(obj);
        em.emit('FirstEvent',str);
    }

  //res.json({'name':'locke','key':'123'});回傳Json
  //console.log(get);

});

//接收PHP用
app.listen(3000); 
console.log('Listening on port 3000'); 