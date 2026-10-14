#!/usr/bin/env python3
"""Source-only guard for the native search EditorInfo hints; never starts an IME."""
from pathlib import Path
import json,argparse
ROOT=Path(__file__).resolve().parents[2]
P='app/src/main/java/com/celzero/bravedns/ui/compose/fields/'
BRIDGE=P+'OriginalSearchIme.kt';FIELD=P+'OriginalTextFields.kt';TEST='app/src/test/java/com/celzero/bravedns/ui/compose/fields/OriginalSearchImeTest.kt'
def check(overrides=None):
 overrides=overrides or {}
 def read(p):return overrides.get(p,(ROOT/p).read_text())
 bridge=read(BRIDGE);field=read(FIELD);test=read(TEST)
 required=['val connection = request.createInputConnection(attributes)','InputType.TYPE_MASK_CLASS == InputType.TYPE_CLASS_TEXT','InputType.TYPE_TEXT_VARIATION_PASSWORD','InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD','InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD','InputType.TYPE_TEXT_FLAG_AUTO_CORRECT.inv()','InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS','        connection\n','if (!style.search || options.autoCorrectEnabled == true)','val interceptor = remember {','nextHandler.startInputMethod(originalSearchInputRequest(request))','InterceptPlatformTextInput(interceptor = interceptor, content = content)']
 for token in required:assert token in bridge,token
 assert field.count('OriginalSearchInputMethod(style, keyboardOptions) {')==2
 assert 'application = Application::class' in test
 assert 'native.inputType and mask, info.inputType and mask' in test
 assert test.count('assertSame(connection, originalSearchInputRequest(request).createInputConnection(info))')==2
 assert 'info.initialSelStart' in test and 'info.privateImeOptions' in test and 'assertEquals(1, calls)' in test
 assert all(token not in bridge for token in ['AndroidView(', 'View(', 'InputMethodManager', 'newInputSession', '.sendKeyEvent(', '.commitText('])
 return len(required)+6

def main():
 p=argparse.ArgumentParser();p.add_argument('--self-test',action='store_true');p.add_argument('--report',type=Path);a=p.parse_args();count=check();rejected=0
 if a.self_test:
  for path,old,new in [(BRIDGE,'InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS','InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE'),(BRIDGE,'if (!style.search || options.autoCorrectEnabled == true)','if (false)'),(BRIDGE,'nextHandler.startInputMethod(originalSearchInputRequest(request))','throw IllegalStateException()'),(BRIDGE,'        connection\n','        request.createInputConnection(attributes)\n'),(FIELD,'OriginalSearchInputMethod(style, keyboardOptions) {','run {'),(TEST,'native.inputType and mask, info.inputType and mask','info.inputType and mask, info.inputType and mask')]:
   s=(ROOT/path).read_text();assert old in s
   try:check({path:s.replace(old,new)})
   except AssertionError:rejected+=1
   else:raise AssertionError('mutation survived '+old)
 result={'passed':True,'source_only':True,'checks':count,'negative_controls':rejected,'compile':'not_run','runtime':'not_run','input_session_started':False}
 if a.report:a.report.write_text(json.dumps(result,indent=2)+'\n')
 print(json.dumps(result))
if __name__=='__main__':main()
