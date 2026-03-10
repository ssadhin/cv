Set sc = CreateObject("MSScriptControl.ScriptControl")
sc.Language = "JScript"
On Error Resume Next
sc.AddCode WScript.Arguments(0)
If Err.Number <> 0 Then
    WScript.Echo "Error line: " & Err.Line & " - " & Err.Description
Else
    WScript.Echo "OK"
End If
