const express = require('express');
const SocketServer = require('ws').Server;
const bodyParser = require('body-parser');
const crypto = require('crypto');
const events = require('events');
const mysql = require('mysql');

const app = express();
const server = app.listen(3000, () => console.log("\n系统启动完成，注意连接端口为：3000\n"));
const wss = new SocketServer({ server });
const em = new events.EventEmitter();
const con = mysql.createConnection({
  host: "localhost",
  user: "root",
  password: "abc123",
  database: "sound_networking_sql"
});

con.connect(function (err) {
  if (err) throw err;
  console.log("Connected to database\n");
});

const codes = {};
let MusicID = null;
let code = 0;
let time_out = 0;

for (let i = 0; i < 4; i++) {
  const prefix = String.fromCharCode(65 + i);

  for (let j = 0; j < 26; j++) {
    const suffix = String.fromCharCode(65 + j);

    const key = prefix + suffix;
    codes[code++] = key;

    if (key === 'DZ') {
      break;
    }
  }
}

function numberToLetter() {
  const byte = crypto.randomBytes(1);
  const randomInt = byte[0] % 104;
  return codes[randomInt];
}

let account = "尚未登入";
let door_number = "尚未選擇";

app.use(express.static(__dirname));
app.use(bodyParser.json());

app.post('/send', async function (req, res) {
  const jsonData = req.body;

  if (account !== jsonData["Account"]) {
    MusicID = null;
    time_out = 0;
    account = jsonData["Account"];
  }
  if (door_number !== jsonData["post_door_number"]) {
    MusicID = null;
    time_out = 0;
    door_number = jsonData["post_door_number"];
  }

  try {
    if (MusicID === null) {
      console.log(account + "登入成功" + "\n");
      MusicID = numberToLetter();
      const obj = { "door_number": door_number, "music": MusicID + ".mp3", "target": "N" };
      const str = JSON.stringify(obj);
      console.log("选择门禁为：" + door_number + "\n");
      console.log("音乐开始播放" + MusicID + "\n");
      em.emit('FirstEvent', str);
      res.json({ 'message': '登入成功' });
    } else if (jsonData["doorpassword"] !== undefined) {
      const getpassword = jsonData["doorpassword"];
      if (MusicID === getpassword) {
        console.log("验证正确\n");
        console.log("开启门禁\n");
        const obj = { "door_number": door_number, "music": "stop", "target": "door" };
        const str = JSON.stringify(obj);
        em.emit('FirstEvent', str);
        res.json({ 'message': 'success' });
        MusicID = null;

        let datetime = new Date();
        let formatted_datetime = datetime.getFullYear() + "-" + (datetime.getMonth() + 1) + "-" + datetime.getDate() + " " + datetime.getHours() + ":" + datetime.getMinutes() + ":" + datetime.getSeconds();
        let sql = `INSERT INTO logintime (account, door, time) VALUES ('${account}','${door_number}', '${formatted_datetime}')`;

        con.query(sql, function (err, result) {
          if (err) throw err;
        });
      } else {
        time_out++;
        console.log("验证错误，收到的验证码为：" + getpassword + "\n" + time_out);
        console.log("请重新尝试" + "\n");
        MusicID = numberToLetter();
        const obj = { "door_number": door_number, "music": MusicID + ".mp3", "target": "N" };
        const str = JSON.stringify(obj);

        if (time_out >= 3) {
          time_out = 0;
          MusicID = null;
          console.log("已达错误上限，请重新登录\n");
          return;
        }

        await sleep(1000); // 等待1秒
        console.log("音乐重新播放" + MusicID + "\n");
        em.emit('FirstEvent', str);
        res.json({ 'message': 'ERROR' });
      }
    } else {
      console.log("发生错误，请重新登录" + "\n");
      res.json({ 'message': '请重新登录' });
    }
  } catch (error) {
    console.log("发生错误，请重新登录" + "\n");
    res.json({ 'message': '请重新登录' });
  }
});

function sleep(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

process.on("exit", function () {
  con.end(function (err) {
    if (err) throw err;
    console.log("Database connection closed");
  });
});

app.post('/message', function (req, res) {
  const clients = wss.clients;

  clients.forEach(client => {
    client.send(JSON.stringify(req.body));
  });

  res.json({ 'message': 'success' });
});

wss.on('connection', ws => {
  console.log('Client connected');

  ws.on('message', data => {
    let clients = wss.clients;

    clients.forEach(client => {
      client.send(data);
    });
  });

  em.on('FirstEvent', function (data) {
    ws.send(data);
  });

  ws.on('close', () => {
    console.log('Close connected');
  });
});
