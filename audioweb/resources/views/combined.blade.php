<style>
table {
  border-collapse: collapse;
  width: 100%;
}
th, td {
  text-align: left;
  padding: 8px;
  border: 1px solid #ddd;
}
th {
  background-color: #f2f2f2;
}
</style>

<h2>User List</h2>
<table>
    <thead>
        <tr>
            <th>Account</th>
            <th>Email</th>
        </tr>
    </thead>
    <tbody>
        @foreach($users as $user)
        <tr>
            <td style="border: 1px solid #ddd;">{{ $user->account }}</td>
            <td style="border: 1px solid #ddd;">{{ $user->email }}</td>
        </tr>
        @endforeach
    </tbody>
</table>

<h2>Login Time</h2>
<table>
    <thead>
        <tr>
            <th style="border: 1px solid #ddd;">Account</th>
            <th style="border: 1px solid #ddd;">Login Time</th>
        </tr>
    </thead>
    <tbody>
        @foreach($logintime as $time)
        <tr>
            <td style="border: 1px solid #ddd;">{{ $time->account }}</td>
            <td style="border: 1px solid #ddd;">{{ $time->time }}</td>
        </tr>
        @endforeach
    </tbody>
</table>
