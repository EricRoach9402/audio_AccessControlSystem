<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\UserController;
use App\Http\Controllers\LoginController;

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider and all of them will
| be assigned to the "web" middleware group. Make something great!
|
*/

Route::get('/', function () {
    $userController = new UserController();
    $data = $userController->combined()->getData();
    return view('dist.index', $data);
});
Route::get('/tables', function () {
    $userController = new UserController();
    $data = $userController->combined()->getData();
    return view('dist/tables', $data);
});
Route::get('/login', function () {
    $userController = new UserController();
    $data = $userController->combined()->getData();
    return view('dist/login', $data);
});
Route::post('/login', [LoginController::class, 'login'])->name('login');

//Route::get('/', [UserController::class, 'combined']);
// Route::get('/tables', function () {
//     return view('dist/tables');
// });
#Route::get('/dist/index', [UserController::class, 'combined']);