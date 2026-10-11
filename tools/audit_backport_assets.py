"""Read-only static census. Differences are candidates, not automatic parity failures."""
import json, zipfile, pathlib, hashlib, collections
ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets'
OUT = ROOT / 'docs/backport-audit-2026-09-13'
OUT.mkdir(exist_ok=True)
jar = zipfile.ZipFile(ROOT / 'tmp/minecraft-1.20.2-client.jar')
jar_names = set(jar.namelist())
def norm(v):
    if isinstance(v, dict): return {k:norm(x) for k,x in v.items()}
    if isinstance(v, list): return [norm(x) for x in v]
    if isinstance(v,str) and not v.startswith('#') and (':' in v or '/' in v):
        return v.split(':')[-1].split('/')[-1]
    return v
rows=[]; errors=[]; totals=collections.Counter()
references=[]
def reference(owner,value,kind):
    namespace, value = value.split(':',1) if ':' in value else (owner.split('/')[0],value)
    if value.startswith('builtin/'): return
    if kind=='blockstate' and '/' not in value: value='block/'+value
    target=namespace+'/models/'+value+'.json'
    if not (ASSETS/target).exists(): references.append({'owner':owner,'target':target})
def walk_variants(owner,value):
    if isinstance(value,dict):
        if 'model' in value: reference(owner,value['model'],'blockstate')
        for child in value.values(): walk_variants(owner,child)
    elif isinstance(value,list):
        for child in value: walk_variants(owner,child)
for folder in ['models/block','models/item','blockstates']:
    for path in sorted((ASSETS/'htgt6'/folder).rglob('*.json')):
        rel=path.relative_to(ASSETS).as_posix()
        try: local=json.loads(path.read_text(encoding='utf-8-sig'))
        except Exception as exc: errors.append({'path':rel,'error':str(exc)}); continue
        if folder=='blockstates': walk_variants(rel,local)
        elif 'parent' in local: reference(rel,local['parent'],'parent')
        ref='assets/minecraft/'+folder+'/'+path.name
        row={'path':rel,'reference':ref}
        try: original=json.loads(jar.read(ref))
        except KeyError: row['status']='no_same_basename_reference'
        else:
            row['status']='normalized_equal' if norm(local)==norm(original) else 'different'
            row['different_top_keys']=[k for k in sorted(set(local)|set(original)) if norm(local.get(k))!=norm(original.get(k))]
        totals[folder+':'+row['status']]+=1; rows.append(row)
textures=[]
for path in sorted((ASSETS/'htgt6/textures').rglob('*.png')):
    rel=path.relative_to(ASSETS).as_posix()
    candidates=['assets/minecraft/textures/'+kind+'/'+path.name for kind in ['block','item']]
    for ref in candidates:
        if ref not in jar_names: continue
        textures.append({'path':rel,'reference':ref,'byte_equal':path.read_bytes()==jar.read(ref)})
        break
sources=collections.Counter()
for path in (ROOT/'src/main/java/com/mod/htgt6/minecraft1202/Module').rglob('*.java'):
    parts=path.relative_to(ROOT/'src/main/java/com/mod/htgt6/minecraft1202/Module').parts
    sources['/'.join(parts[:2]) if len(parts)>2 else 'module_root']+=1
summary={'reference_sha1':hashlib.sha1((ROOT/'tmp/minecraft-1.20.2-client.jar').read_bytes()).hexdigest(),
         'json_files':len(rows),'parse_errors':errors,'comparisons':dict(totals),
         'missing_model_references':references,
         'texture_basename_matches':len(textures),'texture_byte_equal':sum(r['byte_equal'] for r in textures),
         'java_source_counts':dict(sorted(sources.items()))}
for name,value in [('summary',summary),('json-comparison',rows),('texture-comparison',textures)]:
    (OUT/(name+'.json')).write_text(json.dumps(value,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False,indent=2))
