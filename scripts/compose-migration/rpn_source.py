"""Small brace-aware extraction for a fixed named-function parity ledger; not a Kotlin compiler."""
import pathlib, re, subprocess
ROOT=pathlib.Path(__file__).resolve().parents[2]
BASE='23cb6dd98805445174bd83b7237ad5f5d2c24ac9'
UI='app/src/main/java/com/celzero/bravedns/ui/'
TOKENS=r'/\*.*?\*/|//[^\n]*|""".*?"""|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\''
def mask(source):
    return re.sub(TOKENS,lambda m:''.join('\n' if c=='\n' else ' ' for c in m[0]),source,flags=re.S)
def functions(source):
    masked=mask(source);result={}
    for match in re.finditer(r'(?m)^([ ]+)(?:(?:private|override|internal|public|suspend|inline) )*fun (\w+)\s*\(',masked):
        index=masked.find('(',match.start())+1;depth=1
        while depth:
            if masked[index]=='(': depth+=1
            elif masked[index]==')': depth-=1
            index+=1
        start=masked.find('{',index);equals=masked.find('=',index);blank=masked.find('\n\n',index)
        if start<0 or (equals>=0 and equals<start) or (blank>=0 and blank<start):continue
        depth=1;end=start+1
        while depth:
            if masked[end]=='{':depth+=1
            elif masked[end]=='}':depth-=1
            end+=1
        result[match[2]]=(source[match.start():end],source[start:end])
    return result

def baseline(path):
    return subprocess.check_output(['git','show',BASE+':'+UI+path],cwd=ROOT,text=True)

def normalize(source):
    source=re.sub(TOKENS,lambda m: '' if m[0].startswith(('//','/*')) else m[0],source,flags=re.S)
    source=re.sub(r'\b(?:b|binding)\.', 'ui.',source)
    source=source.replace('android.widget.CompoundButton','RpnControlState')
    source=source.replace('ui.root.isAttachedToWindow','attached')
    source=source.replace('lifecycleOwner ?: ui.root.findViewTreeLifecycleOwner()', 'lifecycleOwner')
    source=source.replace('adapter.notifyDataSetChanged()', 'ui.revision++')
    source=re.sub(r'^.*ui.rv(?:SelectedServers|Servers).post \{.*requestLayout\(\).*\}\n','',source,flags=re.M)
    source=source.replace('MaterialAlertDialogBuilder(', 'WgAlertDialogBuilder(')
    source=source.replace('androidx.appcompat.app.AlertDialog.Builder(requireContext())', 'WgAlertDialogBuilder(requireContext())')
    for prop in ['ALPHA','TRANSLATION_X','TRANSLATION_Y','SCALE_X','SCALE_Y','ROTATION']:
        name=re.sub('_([a-z])',lambda m:m[1].upper(),prop.lower())
        source=source.replace('View.'+prop,'"'+name+'"')
    return re.sub(r'\s+','',source)
