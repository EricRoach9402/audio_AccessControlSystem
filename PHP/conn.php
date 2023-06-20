<?php
    function connectToDatabase($host, $dbname, $username, $password) {
        try {
            $dsn = "mysql:host=$host;dbname=$dbname";
            //建立物件
            $pdo = new PDO($dsn, $username, $password);
            //檢查是否建立成功，否則丟出異常
            $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            //echo "成功";
            return $pdo;
        } catch (PDOException $e) {
            //echo "Connection failed: " . $e->getMessage();
        }

    }
    //連線測試用
    //connectToDatabase('localhost','sound_networking_sql','root','abc123');
?>