<?php
include "models.php";

class MainController {
    public function __construct(){
        unserialize($_COOKIE["user"]);
        $this->user = User::getInstance();
    }

    private function checkAuth(){
        if ($this->user === NULL || $this->user->name === NULL)
            die("Unauthorized");
    }

    public function doPublish(){
        $this->checkAuth();
        $page = unserialize($_COOKIE["draft"]);
        $fname = $_POST["fname"];
        $page->publish($fname);
        setcookie("draft", null, -1);
        die("Your blog post will be published after a while (never)<br><a href=/>Back</a>");
    }

    public function doSaveDraft(){
        $this->checkAuth();

        $page = new Page("main");
        $page->user = $this->user;
        $page->filename = $_POST["filename"];
        $page->text = $_POST["text"];

        $header = new Page("header");
        $header->user = $this->user;
        $header->filename = $_POST["filename"];
        $header->text = $_POST["header"];

        $page->header = $header;

        setcookie("draft", serialize($page));
        header("Location: /?m=ViewDraft");
    }

    public function doViewDraft(){
        $this->checkAuth();
        $page = unserialize($_COOKIE["draft"]);
        $footer = <<<EOL
        <br>

        <form method=POST action="/?m=Publish">
            <input type="hidden" name="fname" value="article.html">
        <a href="/">Back</a>
            <input type="submit" value="Pubish">
        </form>
EOL;
        die($page . $footer);
    }

    public function doLogin(){
        $user = User::login($_POST["login"], $_POST["password"]);
        if (!$user)
            die("Invalid login/password");
        setcookie("user", serialize($user));
        header("Location: /");
    }

    public function doRegister(){
        $user = User::register($_POST["login"], $_POST["password"]);
        if (!$user)
            die("Something went wrong");
        setcookie("user", serialize($user));
        header("Location: /");
    }

    public function doIndex(){
        include "index.tpl";
    }

}
