"""Lossless PNG filter/DEFLATE optimization. Keeps every non-IDAT chunk verbatim.
Explicit scope excludes backports. No resizing, palette changes or deletions.
"""
import argparse, hashlib, json, struct, zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'src/main/resources/assets/htgt6/textures/blocks'
OWN_DIRS = {'index', 'materialicons', 'atlases'}
EXCLUDED = ('coral', 'kelp', 'seagrass', 'turtle_egg')
EXCLUDED_NAMES = {'conduit', 'sea_pickle'}

def chunks(raw):
    assert raw[:8] == b'\x89PNG\r\n\x1a\n'
    pos = 8
    while pos < len(raw):
        length = struct.unpack('>I', raw[pos:pos+4])[0]
        part = raw[pos:pos+length+12]
        kind, data = part[4:8], part[8:-4]
        assert zlib.crc32(kind+data) & 0xffffffff == struct.unpack('>I', part[-4:])[0]
        yield kind, data, part
        pos += length+12

def paeth(a,b,c):
    p=a+b-c; pa=abs(p-a); pb=abs(p-b); pc=abs(p-c)
    return a if pa<=pb and pa<=pc else b if pb<=pc else c

def predictors(kind,a,b,c):
    return (0,a,b,(a+b)//2,paeth(a,b,c))[kind]

def decode(filtered, w, h, bpp):
    stride=w*bpp; previous=bytearray(stride); rows=[]; pos=0
    for _ in range(h):
        kind=filtered[pos]; assert kind<=4
        row=bytearray(filtered[pos+1:pos+1+stride]); pos+=stride+1
        for x in range(stride):
            row[x]=(row[x]+predictors(kind,row[x-bpp] if x>=bpp else 0,previous[x],previous[x-bpp] if x>=bpp else 0))&255
        rows.append(row); previous=row
    assert pos == len(filtered)
    return rows

def adaptive(rows,bpp):
    previous=bytearray(len(rows[0])); out=bytearray()
    for row in rows:
        choices=[]
        for kind in range(5):
            filtered=bytearray(len(row))
            for x,v in enumerate(row):
                filtered[x]=(v-predictors(kind,row[x-bpp] if x>=bpp else 0,previous[x],previous[x-bpp] if x>=bpp else 0))&255
            choices.append((sum(min(v,256-v) for v in filtered),kind,filtered))
        _,kind,filtered=min(choices)
        out.append(kind); out.extend(filtered); previous=row
    return bytes(out)

def optimize(path, apply):
    raw=path.read_bytes(); parts=list(chunks(raw)); ihdr=parts[0][1]
    w,h,depth,color,_,_,interlace=struct.unpack('>IIBBBBB',ihdr)
    packed=b''.join(data for kind,data,_ in parts if kind==b'IDAT')
    filtered=zlib.decompress(packed); streams=[filtered]
    # Large textures get DEFLATE-only optimization; bounded filtering cost.
    bpp={0:1,2:3,4:2,6:4}.get(color)
    pixels=None
    if depth==8 and interlace==0 and bpp and w*h<=262144:
        rows=decode(filtered,w,h,bpp); pixels=b''.join(rows)
        streams.append(adaptive(rows,bpp))
    best=packed; best_stream=filtered
    for stream in streams:
        for strategy in (zlib.Z_DEFAULT_STRATEGY,zlib.Z_FILTERED,zlib.Z_RLE):
            encoder=zlib.compressobj(9,zlib.DEFLATED,15,9,strategy)
            candidate=encoder.compress(stream)+encoder.flush()
            if len(candidate)<len(best): best=candidate; best_stream=stream
    assert zlib.decompress(best)==best_stream
    if best_stream!=filtered:
        assert b''.join(decode(best_stream,w,h,bpp))==pixels
    idat=struct.pack('>I',len(best))+b'IDAT'+best+struct.pack('>I',zlib.crc32(b'IDAT'+best)&0xffffffff)
    result=bytearray(raw[:8]); emitted=False
    for kind,_,part in parts:
        if kind==b'IDAT':
            if not emitted: result.extend(idat); emitted=True
        else: result.extend(part)
    if len(result)>=len(raw): result=raw
    assert [(k,d) for k,d,_ in chunks(result) if k!=b'IDAT']==[(k,d) for k,d,_ in parts if k!=b'IDAT']
    if apply and result!=raw: path.write_bytes(result)
    return dict(path=path.relative_to(ROOT).as_posix(),width=w,height=h,before=len(raw),after=len(result),saved=len(raw)-len(result),sha256Before=hashlib.sha256(raw).hexdigest())

def main():
    parser=argparse.ArgumentParser(); parser.add_argument('--apply',action='store_true'); args=parser.parse_args()
    files=[]
    for path in sorted(BASE.rglob('*.png')):
        rel=path.relative_to(BASE)
        if len(rel.parts)>1 and rel.parts[0] not in OWN_DIRS: continue
        if len(rel.parts)==1 and (any(word in path.stem for word in EXCLUDED) or path.stem in EXCLUDED_NAMES): continue
        files.append(path)
    report=[optimize(path,args.apply) for path in files]
    groups={}
    for path in files: groups.setdefault(hashlib.sha256(path.read_bytes()).hexdigest(),[]).append(path.relative_to(ROOT).as_posix())
    output=dict(applied=args.apply,count=len(report),before=sum(r['before'] for r in report),after=sum(r['after'] for r in report),changed=sum(r['saved']>0 for r in report),files=report,identicalFiles=[v for v in groups.values() if len(v)>1])
    dest=ROOT/'docs/assets/block-optimization'; dest.mkdir(parents=True,exist_ok=True)
    (dest/('report.json' if args.apply else 'dry-run.json')).write_text(json.dumps(output,indent=2),encoding='utf-8')
    print(json.dumps({k:v for k,v in output.items() if k not in ('files','identicalFiles')}),flush=True)
    print('Duplicate groups:',len(output['identicalFiles']),flush=True)

if __name__=='__main__': main()
