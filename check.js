var fso = new ActiveXObject('Scripting.FileSystemObject');
var text = fso.OpenTextFile('app/src/main/assets/index.html', 1).ReadAll();
// We can't really parse HTML as JS. Let's extract the <script> block.
var start = text.indexOf('<script>') + 8;
var end = text.lastIndexOf('<\/script>');
var jsCode = text.substring(start, end);

try {
    eval('function testSyntax() { ' + jsCode + ' }');
    WScript.Echo('Syntax OK');
} catch (e) {
    WScript.Echo('Syntax Error: ' + e.message);
}
