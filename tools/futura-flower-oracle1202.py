"""Independent Python vectors from the pinned 1.20.2 dse/dzq/dzp/dkt bytecode.
Does not call production Java. Only the legacy provider noise path is modeled.
"""
import math,json,struct
from pathlib import Path
MASK=(1<<48)-1
class Random:
 def __init__(self,seed): self.s=(seed^0x5deece66d)&MASK
 def bits(self,n):
  self.s=(self.s*0x5deece66d+11)&MASK
  return self.s>>(48-n)
 def integer(self,n):
  if n&(n-1)==0:return (n*self.bits(31))>>31
  while True:
   b=self.bits(31);v=b%n
   if b-v+n-1<(1<<31):return v
 def long(self):
  def signed(v):return v-(1<<32) if v>>31 else v
  return (signed(self.bits(32))<<32)+signed(self.bits(32))
 def double(self):return ((self.bits(26)<<27)+self.bits(27))/(1<<53)
def hashcode(s):
 h=0
 for c in s:h=(31*h+ord(c))&0xffffffff
 return h-(1<<32) if h>>31 else h
def lerp(t,a,b):return a+t*(b-a)
def grad(h,x,y,z):
 h&=15;u=x if h<8 else y;v=y if h<4 else x if h in (12,14) else z
 return (-u if h&1 else u)+(-v if h&2 else v)
class Perlin:
 def __init__(self,r):
  self.origin=[r.double()*256 for _ in range(3)];self.p=list(range(256))
  for i in range(256):
   j=i+r.integer(256-i);self.p[i],self.p[j]=self.p[j],self.p[i]
 def at(self,x,y,z):
  xyz=[v+o for v,o in zip((x,y,z),self.origin)];ix,iy,iz=[math.floor(v) for v in xyz]
  x,y,z=[v-math.floor(v) for v in xyz];fade=lambda t:t*t*t*(t*(t*6-15)+10)
  u,v,w=map(fade,(x,y,z));p=lambda n:self.p[n&255]
  a=p(ix)+iy;b=p(ix+1)+iy;aa=p(a)+iz;ab=p(a+1)+iz;ba=p(b)+iz;bb=p(b+1)+iz
  return lerp(w,lerp(v,lerp(u,grad(p(aa),x,y,z),grad(p(ba),x-1,y,z)),lerp(u,grad(p(ab),x,y-1,z),grad(p(bb),x-1,y-1,z))),lerp(v,lerp(u,grad(p(aa+1),x,y,z-1),grad(p(ba+1),x-1,y,z-1)),lerp(u,grad(p(ab+1),x,y-1,z-1),grad(p(bb+1),x-1,y-1,z-1))))
class Octaves:
 def __init__(self,r,first,amps):
  seed=r.long();self.first=first;self.amps=amps
  self.levels=[Perlin(Random(seed^hashcode('octave_'+str(first+i)))) if a else None for i,a in enumerate(amps)]
 def at(self,x,y,z):
  freq=2.0**self.first;weight=2.0**(len(self.amps)-1)/(2.0**len(self.amps)-1);out=0.0
  wrap=lambda v:v-math.floor(v/33554432.0+.5)*33554432.0
  for a,p in zip(self.amps,self.levels):
   if p:out+=a*weight*p.at(wrap(x*freq),wrap(y*freq),wrap(z*freq))
   freq*=2;weight*=.5
  return out
class Normal:
 def __init__(self,seed,first,amps):
  r=Random(seed);self.a=Octaves(r,first,amps);self.b=Octaves(r,first,amps)
  nonzero=[i for i,a in enumerate(amps) if a];span=max(nonzero)-min(nonzero) if nonzero else 0
  self.factor=(1/6)/(.1*(1+1/(span+1)))
 def at(self,x,y,z):return (self.a.at(x,y,z)+self.b.at(x*1.0181268882175227,y*1.0181268882175227,z*1.0181268882175227))*self.factor
vectors=[]
for first,amps in [(0,[1]),(-3,[1]),(-10,[1]),(-3,[1,0,.5])]:
 noise=Normal(2345,first,amps)
 for x in [-10000000.25,-513.5,-1,0,17.125,34234,9999999.5]:
  for y,z in [(-64,0),(63.5,-51.25),(319,54545.5)]:
   vectors.append(dict(seed=2345,first=first,amplitudes=amps,x=x,y=y,z=z,value=noise.at(x,y,z)))
out=Path('src/main/resources/assets/htgt6/worldgen/futura_flower_noise_oracle_1202.json')
out.write_text(json.dumps(vectors,indent=2))
print(str(len(vectors))+' independent legacy provider noise vectors')

# Provider choices are generated separately from the Java provider and include RNG continuation.
f32=lambda n:struct.unpack('!f',struct.pack('!f',n))[0]
data=json.loads(Path('src/main/resources/assets/htgt6/worldgen/futura_placements_1202.json').read_text())
cache={}
def normal(p,field):
 n=p[field];key=(p['seed'],n['firstOctave'],tuple(n['amplitudes']))
 if key not in cache:cache[key]=Normal(*key)
 return cache[key]
def choose(states,n):return states[int(max(0,min(.9999,(1+n)/2))*len(states))]
def select(p,x,y,z,r):
 scale=f32(p['scale']);n=normal(p,'noise').at(x*scale,y*scale,z*scale)
 if p['type']=='minecraft:noise_provider':return choose(p['states'],n)
 if p['type']=='minecraft:noise_threshold_provider':
  if n<f32(p['threshold']):return p['low_states'][r.integer(len(p['low_states']))]
  if r.bits(24)/(1<<24)<f32(p['high_chance']):return p['high_states'][r.integer(len(p['high_states']))]
  return p['default_state']
 slow=normal(p,'slow_noise');scale=f32(p['slow_scale']);sample=lambda a,b,c:slow.at(f32(f32(a)*scale),f32(f32(b)*scale),f32(f32(c)*scale))
 nslow=sample(x,y,z);lo,hi=p['variety'];size=int(lo+max(0,min(1,(nslow+1)/2))*(hi+1-lo))
 return choose([choose(p['states'],sample(x+i*54545,y,z+i*34234)) for i in range(size)],n)
choices=[]
for key in ['flower_plain','flower_flower_forest','flower_meadow']:
 provider=data['configured']['minecraft:'+key]['config']['feature']['feature']['config']['to_place']
 for x in [-10000000,-513,-1,0,17,34234,9999999]:
  for y,z in [(-64,0),(63,-51),(319,54545)]:
   r=Random(7890);value=select(provider,x,y,z,r)
   choices.append(dict(configured=key,x=x,y=y+64,z=z,state=value,nextLong=r.long()))
Path('src/main/resources/assets/htgt6/worldgen/futura_flower_state_oracle_1202.json').write_text(json.dumps(choices,indent=2))
print(str(len(choices))+' independent provider state/RNG vectors')
