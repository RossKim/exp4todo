// XMLHttpRequest のサンプル

function xhrsample() {
    var req = new XMLHttpRequest();
    // 送信先のURLを指定
    req.open("POST", "/sampleajax/convert");
    // 結果が帰ってきた際に実行されるハンドラを指定
    req.onreadystatechange = function () {
        // readyState == 4: 修了
        if (req.readyState != 4) {
            return;
        }
        // status == 200: 成功
        if (req.status != 200) {
            // 成功しなかった．失敗であることを表示して抜ける．
            alert("失敗．");
            return;
        }
        
        // body にはサーバから返却された文字列が格納される．
        var body = req.responseText;
        // デバッグ表示 
        alert('body: ' + body);

        // 戻ってきた JSON 文字列を JavaScript オブジェクトに変換
        var data = eval("(" + body + ")");
        // 入力に利用したインプットボックスの値を，サーバから戻ってきた値で書き換える
        document.getElementById("f").value = data.result;
    }
    
    // Content-Type の指定
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    // <input id="f"> に入力された文字列をエンコードして送信        
    req.send("p=" + enc(document.getElementById("f").value));
}

// 入力文字列を urlencode して返す
function enc(s) {
    return encodeURIComponent(s).replace(/%20/g, '+');
}
