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
    // 建立 SQL 新增語句
    $insert = $PDO->prepare("INSERT INTO userlist (email, account, password) VALUES (?, ?, ?)");
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        // 接收 JSON 字串
        $json = file_get_contents('php://input');
        // 將 JSON 字串轉換為 PHP 陣列
        $data = json_decode($json, true);
        // 可以使用下面的方式讀取陣列中的數據
        if ($data) {
            //取出Json內資料
            $email = $data["email"];
            $account_number = $data["account_number"];
            $account_password = $data["account_password"];
            //避免注入攻擊，將參數進行綁定
            $insert->bindParam(1,$email);
            $insert->bindParam(2,$account_number);
            $insert->bindParam(3,$account_password);
                // 執行 SQL 新增語句
            if ($insert->execute()) {
                //新增成功
                $response = array("message" => "success");
                $json_response = json_encode($response);
                header('Content-Type: application/json');
                echo $json_response;
            } else {
                // 新增失敗
                $response = array("message" => "ERROR");
                $json_response = json_encode($response);
                header('Content-Type: application/json');
                echo $json_response;
            }
        }
        else{
            // JSON解码失败
            $response = array("message" => "未成功收到JSON");
            $json_response = json_encode($response);
            header('Content-Type: application/json');
            echo $json_response;
        }
    }
?>