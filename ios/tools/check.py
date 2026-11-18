#!/usr/bin/env python3
"""
Checks the iOS text files before they go anywhere near App Store Connect.

  - every store locale has every field, within Apple's limits
    (name 30, subtitle 30, promotional text 170, description 4000, keywords 100 bytes)
  - keywords: commas only, no spaces after commas, no word repeated from the name or subtitle
  - URLs are https
  - String Catalogs parse, every key has every language, and no translation drops,
    adds or changes a placeholder such as %@ or %lld
  - the privacy manifest parses and declares no tracking

Run from the repo root:  python3 ios/tools/check.py      (exit code 1 if anything fails)
"""
import json, os, plistlib, re, sys

ROOT = os.path.join(os.path.dirname(__file__), '..')
META = os.path.join(ROOT, 'fastlane', 'metadata')
STORE_LOCALES = ['en-US', 'en-GB', 'fr-FR', 'fr-CA', 'es-ES', 'es-MX', 'pt-BR', 'pt-PT',
                 'de-DE', 'it', 'no', 'sv', 'ja', 'ko']
APP_LANGS = ['fr', 'es', 'pt-BR', 'pt-PT', 'ja', 'de', 'it', 'nb', 'sv', 'ko']
LIMITS = {'name': 30, 'subtitle': 30, 'promotional_text': 170, 'description': 4000}

errors, warnings = [], []
def err(m): errors.append(m)
def warn(m): warnings.append(m)

def read(path):
    with open(path, encoding='utf-8') as f: return f.read().rstrip('\n')

# ---------- product pages
rows = []
for loc in STORE_LOCALES:
    d = os.path.join(META, loc)
    if not os.path.isdir(d):
        err(f'{loc}: folder missing'); continue
    f = {}
    for field in list(LIMITS) + ['keywords', 'privacy_url', 'support_url']:
        p = os.path.join(d, field + '.txt')
        if not os.path.exists(p): err(f'{loc}: {field}.txt missing'); continue
        f[field] = read(p)
        if not f[field].strip(): err(f'{loc}: {field} is empty')
    for field, lim in LIMITS.items():
        n = len(f.get(field, ''))
        if n > lim: err(f'{loc}: {field} is {n} characters, limit {lim}')
    kw = f.get('keywords', '')
    b = len(kw.encode('utf-8'))
    if b > 100: err(f'{loc}: keywords are {b} bytes, limit 100')
    if len(kw) > 100: err(f'{loc}: keywords are {len(kw)} characters, limit 100')
    if ', ' in kw or ' ,' in kw: err(f'{loc}: keywords have a space next to a comma (wastes the limit)')
    words = [w.strip().lower() for w in kw.split(',') if w.strip()]
    if len(words) != len(set(words)): err(f'{loc}: a keyword is repeated')
    title = (f.get('name', '') + ' ' + f.get('subtitle', '')).lower()
    title_words = set(re.findall(r'\w+', title))
    for w in words:
        if w in title_words: warn(f'{loc}: keyword "{w}" is already in the name or subtitle')
    for u in ('privacy_url', 'support_url'):
        if u in f and not f[u].startswith('https://'): err(f'{loc}: {u} is not https')
    if 'Hävn' not in f.get('name', ''): err(f'{loc}: the name does not contain Hävn')
    rows.append((loc, len(f.get('name', '')), len(f.get('subtitle', '')), len(f.get('promotional_text', '')),
                 len(f.get('description', '')), b))
extra = sorted(set(os.listdir(META)) - set(STORE_LOCALES)) if os.path.isdir(META) else []
for e in extra: warn(f'metadata/{e}: folder not in the locale list')

# ---------- string catalogs
PH = re.compile(r'%(?:(\d+)\$)?(lld|ld|d|@|f|\.\d+f|%)')
def placeholders(s):
    out = []
    for m in PH.finditer(s):
        if m.group(2) == '%': continue
        out.append(m.group(2))
    return sorted(out)

def values(loc_entry):
    """Every string a localization holds, plural forms included."""
    if 'stringUnit' in loc_entry: return [loc_entry['stringUnit']['value']]
    out = []
    for kind in loc_entry.get('variations', {}).values():
        for form in kind.values(): out += values(form)
    return out

for name in ('Localizable.xcstrings', 'InfoPlist.xcstrings'):
    p = os.path.join(ROOT, 'Havn', name)
    try:
        cat = json.load(open(p, encoding='utf-8'))
    except Exception as e:
        err(f'{name}: does not parse ({e})'); continue
    if cat.get('sourceLanguage') != 'en': err(f'{name}: source language is not en')
    for key, entry in cat['strings'].items():
        locs = entry.get('localizations', {})
        for lang in APP_LANGS:
            if lang not in locs: err(f'{name}: "{key}" has no {lang}'); continue
        src = values(locs['en']) if 'en' in locs else [key]
        want = placeholders(src[-1])
        for lang, le in locs.items():
            for v in values(le):
                if not v.strip(): err(f'{name}: "{key}" [{lang}] is empty')
                got = placeholders(v)
                if got != want:
                    err(f'{name}: "{key}" [{lang}] placeholders {got} differ from source {want}')
        if not entry.get('comment'): warn(f'{name}: "{key}" has no comment for translators')

# ---------- privacy manifest
p = os.path.join(ROOT, 'Havn', 'PrivacyInfo.xcprivacy')
try:
    pm = plistlib.load(open(p, 'rb'))
    if pm.get('NSPrivacyTracking') is not False: err('PrivacyInfo: NSPrivacyTracking must be false')
    if pm.get('NSPrivacyTrackingDomains'): err('PrivacyInfo: tracking domains listed')
    if pm.get('NSPrivacyCollectedDataTypes'): err('PrivacyInfo: collected data types listed, but Hävn collects nothing')
    for api in pm.get('NSPrivacyAccessedAPITypes', []):
        if not api.get('NSPrivacyAccessedAPITypeReasons'): err(f'PrivacyInfo: {api.get("NSPrivacyAccessedAPIType")} has no reason')
except FileNotFoundError:
    err('PrivacyInfo.xcprivacy missing')
except Exception as e:
    err(f'PrivacyInfo.xcprivacy does not parse ({e})')

# ---------- report
print(f'{"locale":7} {"name":>5} {"sub":>4} {"promo":>6} {"desc":>5} {"kw bytes":>9}')
for r in rows: print(f'{r[0]:7} {r[1]:>5} {r[2]:>4} {r[3]:>6} {r[4]:>5} {r[5]:>9}')
print(f'limits  {30:>5} {30:>4} {170:>6} {4000:>5} {100:>9}')
for w in warnings: print('warning:', w)
for e in errors: print('ERROR:', e)
print(f'\n{len(errors)} errors, {len(warnings)} warnings')
sys.exit(1 if errors else 0)
