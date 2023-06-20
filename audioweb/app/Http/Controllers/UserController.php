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
        return view('combined', ['users' => $users, 'logintime' => $logintime]);
    }
}
