<?php


$redis = new Redis();
$redis->connect("db", 6379) or die("Cant connect to database");

class User {
    const CACHE_PATH = "/tmp/cache/";

    public $name;
    public $perms;

    private static $_instance = null;

    public function __construct($name){
        $this->name = $name;
        $this->perms = 4;
        self::$_instance = $this;
    }

    public function __wakeup(){
        self::$_instance = $this;
    }

    public static function getInstance(): ?User {
        return self::$_instance;
    }

    public function checkWritePermissions() {
        if (!$this->name || !ctype_alnum($this->name))
            die("Invalid user");
        if ( !(($this->perms >> 2)&1) )
            die("Access denied");
    }

    public function getCacheDir(): string {
        $dir_path = self::CACHE_PATH . $this->name;
        if (!is_dir($dir_path)){
            mkdir($dir_path);
        }
        return $dir_path;
    }
    

    public static function login($login, $password): ?User {
        global $redis;
        if (!ctype_alnum($login))
            die("Only alphanumeric logins allowed!");
        $dbhash = $redis->get($login);
        if($dbhash === False || $dbhash !== sha1($password))
            return null;

        return new User($login);
    }

    public static function register($login, $password): ?User {
        global $redis;
        if (!ctype_alnum($login))
            die("Only alphanumeric logins allowed!");
        $hash = sha1($password);
        if (!$redis->setNx($login, $hash))
            return null;

        return new User($login);
    }
}


class Cache {
    public static function writeToFile($path, $content) {
        $info = pathinfo($path);
        if (!is_dir($info["dirname"]))
            throw new Exception("Directory doesn't exists");
        if (is_file($path))
            throw new Exception("File already exists");
        file_put_contents($path, $content);
    }
}

class Page {
    const TEMPLATES = array("main" => "main.tpl", "header" => "header.tpl");
    public $view;
    public $text;
    public $template;
    public $header;

    public function __construct(string $template) {
        $this->template = $template;

    }

    public function __toString(): string {
        return $this->render();
    }

    public function publish($filename) {
        $user = User::getInstance();
        $ext = substr(strstr($filename, "."), 1);

        $path = $user->getCacheDir() . "/" . microtime(true) . "." . $ext;
        $user->checkWritePermissions();
        Cache::writeToFile($path, $this);
    }

    public function renderVars(): string {
        $content = $this->view["content"];
        foreach ($this->vars as $k=>$v){
            $v = htmlspecialchars($v);
            $content = str_replace("@@$k@@", $v, $content);
        }
        return $content;
    }

    private function getHeader(): ?Page {
        return $this->header;
    }

    public function render(): string {
        $user = User::getInstance();

        if (!array_key_exists($this->template, self::TEMPLATES))
            die("Invalid template");

        $tpl = self::TEMPLATES[$this->template];

        $this->view = array();

        $this->view["content"] = file_get_contents($tpl);
        $this->vars["user"]  = $user->name;
        $this->vars["text"]  = $this->text."\n";
        $this->vars["rendered"] = microtime(true);

        $content = $this->renderVars();
        $header = $this->getHeader();

        return $header.$content;
    }
}
