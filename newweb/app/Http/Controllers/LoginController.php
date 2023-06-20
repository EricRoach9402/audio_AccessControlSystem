<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

class LoginController extends Controller
{
    public function login(Request $request)
    {
        // 從登入表單中獲取使用者名稱和密碼
        $username = $request->input('email');
        $password = $request->input('password');

        // 從資料庫中檢索使用者資料
        $user = DB::table('userlist')->where('email', $username)->first();

        // 檢查是否檢索到使用者資料
        if ($user) {
            // 驗證密碼
            if ($password == $user->password) {
            //if (Hash::check($password, $user->password)) {
                // 密碼驗證成功，允許登入

                // 在此處進行後續登入處理，例如建立使用者會話（Session）

                // 重定向到登入成功後的頁面
                return redirect('/');
            }
        }

        // 密碼驗證失敗或使用者不存在，顯示錯誤訊息
        return redirect()->back()->with('error', '信箱或密碼輸入錯誤');
    }
}
