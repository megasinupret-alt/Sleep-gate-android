from pathlib import Path
import re

root = Path('decoded')
# Locate the Google OAuth WebViewClient by the distinctive redirect/token logic.
targets=[]
for p in root.rglob('*.smali'):
    s=p.read_text(errors='ignore')
    if 'access_token=' in s and 'onPageStarted' in s and 'WebViewClient' in s:
        targets.append((p,s))

if not targets:
    raise SystemExit('OAuth WebViewClient smali not found')

patched=0
for p,s in targets:
    # Only patch the onPageStarted method. Stop the WebView immediately before
    # the callback that consumes the OAuth tokens, preventing WebView from
    # attempting to navigate to sleepgate://auth and showing ERR_UNKNOWN_URL_SCHEME.
    m = re.search(r'(\.method[^\n]*onPageStarted\([^\n]*\)V.*?)(?=\.end method)', s, re.S)
    if not m or 'access_token=' not in m.group(1):
        continue
    body=m.group(1)
    if 'Landroid/webkit/WebView;->stopLoading()V' in body:
        continue
    # Prefer the Kotlin Function2 callback invocation used by 1.39.
    needle='->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;'
    pos=body.find(needle)
    if pos < 0:
        # fallback: any callback invoke in this method
        mm=re.search(r'\n\s*invoke-interface \{[^}]+\}, [^\n]+->invoke\(', body)
        if not mm:
            continue
        insert=mm.start()
    else:
        line_start=body.rfind('\n',0,pos)+1
        insert=line_start
    patch='    invoke-virtual {p1}, Landroid/webkit/WebView;->stopLoading()V\n'
    body=body[:insert]+patch+body[insert:]
    s=s[:m.start(1)]+body+s[m.end(1):]
    p.write_text(s)
    patched += 1
    print('patched', p)

if patched != 1:
    raise SystemExit(f'Expected exactly 1 OAuth WebViewClient patch, got {patched}')
