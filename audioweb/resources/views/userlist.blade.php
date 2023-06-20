<h2>User List</h2>
<table>
    <thead>
        <tr>
            <th>ID</th>
            <th>Email</th>
            <th>Account</th>
        </tr>
    </thead>
    <tbody>
        @foreach($users as $user)
        <tr>
            <td>{{ $user->id }}</td>
            <td>{{ $user->email }}</td>
            <td>{{ $user->account }}</td>
        </tr>
        @endforeach
    </tbody>
</table>

<h2>Login Time</h2>
<table>
    <thead>
        <tr>
            <th>ID</th>
            <th>Login Time</th>
        </tr>
    </thead>
    <tbody>
        @foreach($logintime as $time)
        <tr>
            <td>{{ $time->id }}</td>
            <td>{{ $time->login_time }}</td>
        </tr>
        @endforeach
    </tbody>
</table>
