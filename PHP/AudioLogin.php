<?php
    //導入conn.php
    require 'conn.php';
    //設定SQL參數
    $host = "localhost";
    $dbname = "sound_networking_sql";
    $username = "root";
    $password = "abc123";
    //連線SQL
    $PDO = connectToDatabase($host,$dbname,$username,$password);
    //準備SQL查詢
    $search = $PDO->prepare("SELECT * FROM userlist WHERE account = :account_number AND password = :account_password");

    if ($_SERVER['REQUEST_METHOD'] === 'POST') {

        // 接收 JSON 字串
        $json = file_get_contents('php://input');
        // 將 JSON 字串轉換為 PHP 陣列
        $data = json_decode($json, true);
        // 可以使用下面的方式讀取陣列中的數據
        if ($data) {
            //取出Json內資料
            $account_number = $data["StudenNumber"];
            $account_password = $data["password"];
            $get_door = $data["door"];
            //$door_password = $data["doorpassword"];
            //$music_number = $data["MusicNumber"];
            //避免注入攻擊，將參數進行綁定
            $search->bindParam(":account_number",$account_number);
            $search->bindParam(":account_password",$account_password);
            #$search->bindParam(":get_door",$get_door);
            //執行搜尋
            $search->execute();
            //取得搜尋結果
            $result = $search->fetch();
            // 比對 PHP 中的字串與 SQL 中的值是否一致
            if ($result) {
                // 帳號和密碼正確
                $response = array("message" => "success");
                $json_response = json_encode($response);
                header('Content-Type: application/json');
                echo $json_response;
                
                // 傳送 POST 請求至 Node.js 的 WebSocket 伺服器
                $ch = curl_init();
                curl_setopt($ch, CURLOPT_HTTPHEADER, [
                    'Content-Type: application/json'
                ]);
                curl_setopt($ch, CURLOPT_URL, "http://localhost:3000/send");
                curl_setopt($ch, CURLOPT_POST, 1);
                curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode([
                    //'MusicNumber' => $music_number,
                    //'doorpassword' => $door_password,
                    'Account' => $account_number,
                    'post_door_number' => $get_door
                ]));
                curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
                $response = curl_exec($ch);
                
                curl_close($ch);
                ///echo $json_response;
            } 
            else {
                // 帳號和密碼不正確
                $response = array("message" => "ERROR");
                $json_response = json_encode($response);
                header('Content-Type: application/json');
                echo $json_response;
            }     
        } 
        else {
            // JSON解码失败
            $response = array("message", "未成功接收JSON");
            header('Content-Type: application/json');
            echo json_encode($response);
        }   
    }
?>