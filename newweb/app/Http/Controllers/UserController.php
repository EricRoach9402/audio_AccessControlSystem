<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class UserController extends Controller
{
    public function combined()
    {
        $users = DB::table('userlist')->get();
        $logintime = DB::table('logintime')->get();
        #dd($users, $logintime); // 檢查變數的內容
        return view('dist.index', ['users' => $users, 'logintime' => $logintime]);
    }
}