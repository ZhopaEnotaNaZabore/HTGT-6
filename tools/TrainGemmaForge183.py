"""Bounded, local E2B QLoRA smoke test, with disjoint before/after Java evaluation."""
import os, pathlib, json, time, threading, ctypes, re, subprocess
ROOT=pathlib.Path(__file__).resolve().parents[1]
OUT=ROOT/'build/gemma183'; OUT.mkdir(parents=True,exist_ok=True)
os.environ['HF_HOME']=str(OUT/'hf-cache')
os.environ['HF_HUB_DISABLE_TELEMETRY']='1'
os.environ['HF_HUB_DISABLE_XET']='1'
os.environ['TOKENIZERS_PARALLELISM']='false'
os.environ['WANDB_DISABLED']='true'
os.environ['TORCHINDUCTOR_CACHE_DIR']=str(OUT/'torch-cache')
os.environ['TRITON_CACHE_DIR']=str(OUT/'triton-cache')

def event(stage,**fields):
    obj={'time':time.time(),'stage':stage,**fields}
    print(json.dumps(obj),flush=True)
    with (OUT/'events.jsonl').open('a',encoding='utf8') as f:f.write(json.dumps(obj)+'\n')

def watchdog():
    class Mem(ctypes.Structure):
        _fields_=[('size',ctypes.c_ulong),('load',ctypes.c_ulong)]+[(n,ctypes.c_ulonglong) for n in ['total','avail','pTotal','pAvail','vTotal','vAvail','extended']]
    while True:
        m=Mem();m.size=ctypes.sizeof(m)
        if ctypes.windll.kernel32.GlobalMemoryStatusEx(ctypes.byref(m)) and m.load>98:
            event('memory_abort',load=m.load,free=m.avail)
            os._exit(75) # Terminates only this training worker, releasing its GPU allocations.
        time.sleep(.5)

if __name__=='__main__':
    threading.Thread(target=watchdog,daemon=True).start()
    event('imports')
    from unsloth import FastModel
    from unsloth.chat_templates import train_on_responses_only
    import torch
    from datasets import Dataset
    from trl import SFTTrainer,SFTConfig
    torch.set_num_threads(4)
    event('load',model='unsloth/gemma-4-E2B-it-unsloth-bnb-4bit')
    model,processor=FastModel.from_pretrained(
        model_name='unsloth/gemma-4-E2B-it-unsloth-bnb-4bit',
        max_seq_length=512,load_in_4bit=True,full_finetuning=False,
        trust_remote_code=False,use_gradient_checkpointing='unsloth')
    tokenizer=getattr(processor,'tokenizer',processor)
    model=FastModel.get_peft_model(model,finetune_vision_layers=False,
        finetune_language_layers=True,finetune_attention_modules=True,
        finetune_mlp_modules=True,r=8,lora_alpha=8,lora_dropout=0,
        bias='none',random_state=3407)
    train=[json.loads(x) for x in (OUT/'train.jsonl').read_text(encoding='utf8').splitlines()]
    test=[json.loads(x) for x in (OUT/'eval.jsonl').read_text(encoding='utf8').splitlines()]
    assert not {x['id'] for x in train}&{x['id'] for x in test}
    def render(messages,generation=False):
        return tokenizer.apply_chat_template(messages,tokenize=False,add_generation_prompt=generation,enable_thinking=False)
    cp=(ROOT/'build/sounds179/classpath.txt').read_text().strip()
    javac=r'C:\Program Files\BellSoft\LibericaJDK-8\bin\javac.exe'
    def evaluate(label):
        event('evaluate',label=label)
        FastModel.for_inference(model)
        results=[]
        dest=OUT/label;dest.mkdir(exist_ok=True)
        for row in test:
            prompt=render(row['messages'][:-1],True)
            inputs=tokenizer(prompt,return_tensors='pt',add_special_tokens=False).to('cuda')
            with torch.inference_mode():
                ids=model.generate(**inputs,max_new_tokens=320,do_sample=False,use_cache=True)
            answer=tokenizer.decode(ids[0,inputs['input_ids'].shape[1]:],skip_special_tokens=True)
            (dest/(row['id']+'.txt')).write_text(answer,encoding='utf8')
            fenced=re.findall(r'```(?:java)?\s*([\s\S]*?)```',answer)
            code=fenced[0] if fenced else answer.strip()
            source=dest/(row['id']+'.java');source.write_text(code,encoding='utf8')
            r=subprocess.run([javac,'-encoding','UTF-8','-proc:none','-cp',cp,'-d',str(dest),str(source)],capture_output=True,text=True,timeout=60)
            (dest/(row['id']+'.javac.log')).write_text(r.stdout+r.stderr,encoding='utf8')
            results.append({'id':row['id'],'compiles':r.returncode==0,'generated_tokens':int(ids.shape[1]-inputs['input_ids'].shape[1])})
        (dest/'results.json').write_text(json.dumps(results,indent=2),encoding='utf8')
        return results
    before=evaluate('before')
    FastModel.for_training(model)
    texts=[render(row['messages']) for row in train]
    lengths=[len(tokenizer(x,add_special_tokens=False)['input_ids']) for x in texts]
    if max(lengths)>512:raise RuntimeError('Training examples exceed context; refusing truncation')
    dataset=Dataset.from_dict({'text':texts})
    args=SFTConfig(output_dir=str(OUT/'checkpoints'),dataset_text_field='text',
        max_length=512,per_device_train_batch_size=1,gradient_accumulation_steps=1,
        max_steps=12,learning_rate=1e-4,warmup_steps=1,logging_steps=1,
        optim='adamw_8bit',seed=3407,report_to='none',save_strategy='no',
        dataloader_num_workers=0,dataset_num_proc=1,bf16=True,fp16=False)
    trainer=SFTTrainer(model=model,processing_class=tokenizer,train_dataset=dataset,args=args)
    trainer=train_on_responses_only(trainer,instruction_part='<|turn>user\n',response_part='<|turn>model\n')
    assert all(any(v!=-100 for v in row['labels']) for row in trainer.train_dataset)
    event('training',steps=12,examples=len(train),max_example_tokens=max(lengths))
    result=trainer.train()
    model.save_pretrained(str(OUT/'adapter'))
    tokenizer.save_pretrained(str(OUT/'adapter'))
    changed=sum(int(torch.count_nonzero(p).item()) for n,p in model.named_parameters() if 'lora_B' in n)
    after=evaluate('after')
    report={'training':result.metrics,'nonzero_lora_B':changed,'before':before,'after':after,
            'cuda_peak_allocated_gib':torch.cuda.max_memory_allocated()/2**30,
            'limitation':'Eight tiny examples; compilation is not functional validation or proof of general improvement.'}
    (OUT/'result.json').write_text(json.dumps(report,indent=2),encoding='utf8')
    event('complete',**report)
