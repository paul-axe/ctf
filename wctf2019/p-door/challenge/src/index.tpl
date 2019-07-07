<html>
<body>
<h1>Online blog platform</h1>
<hr>
    <a href="#" onclick="show('main')">[Home]</a>


<?php if ($this->user) { ?>
    <a href="#" onclick="show('write')">[Create]</a>
    <?php if (isset($_COOKIE["draft"])) { ?>
    <a href="?m=ViewDraft">[View Last Draft]</a>
    <?php } ?>
<?php } else { ?>
    <a href="#" onclick="show('register')">[Register]</a>
    <a href="#" onclick="show('login')">[Login]</a>
<?php } ?>
<hr>

<script type="text/template" id="main-block">
    Welcome!
</script>

<script type="text/template" id="write-block">
    <form method="POST" action="?m=SaveDraft">
        Title: <br>
        <textarea name="header" style="width: 500px; height: 300px;"></textarea>
        <br>
        Content: <br>
        <textarea name="text" style="width: 500px; height: 300px;"></textarea>
        <br>
        <input type="submit">
    </form>
</script>

<script type="text/template" id="register-block">
    <h2>Register</h2>
    <form method="POST" action="?m=Register">
        Login:<br>
        <input type="text" name="login">
        <br>
        Password:<br>
        <input type="password" name="password">
        <br>
        <input type="submit">
    </form>
</script>

<script type="text/template" id="login-block">
    <h2>Login</h2>
    <form method="POST" action="?m=Login">
        Login:<br>
        <input type="text" name="login">
        <br>
        Password:<br>
        <input type="password" name="password">
        <br>
        <input type="submit">
    </form>
</script>

<div id="content"></div>

<script>
function show(block){
    document.getElementById("content").innerHTML = document.getElementById(block+"-block").innerHTML;
}
show('main');
</script>
</body>
</html>
