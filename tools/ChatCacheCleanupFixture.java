package com.mod.htgt6.client.vanillatweaks.CHAT;
import java.lang.reflect.Field;
import java.util.Map;

/** No GL context required: checks batching, repeated requests and reload suppression. */
public final class ChatCacheCleanupFixture {
    public static void main(String[] args)throws Exception {
        Field field=ChatImageCache.class.getDeclaredField("CACHE");field.setAccessible(true);
        Map<String,ChatImageCache.Image> cache=(Map<String,ChatImageCache.Image>)field.get(null);
        ChatImageCache.Image[] images=new ChatImageCache.Image[24];
        for(int i=0;i<images.length;i++){images[i]=new ChatImageCache.Image();cache.put("fixture:"+i,images[i]);}
        ChatImageCache.maintain();require(cache.size()==24,"unsolicited cleanup");
        ChatImageCache.clear();ChatImageCache.clear();
        require(cache.size()==24,"callback performed cleanup instead of enqueueing");
        for(int tick=1;tick<=12;tick++){
            ChatImageCache.get("https://example.invalid/not-downloaded");
            require(cache.size()==26-tick*2,"render created resources during cleanup");
            ChatImageCache.maintain();
            require(cache.size()==24-tick*2,"cleanup exceeded two entries per tick");
            for(int i=0;i<tick*2;i++)require(images[i].discarded,"in-flight decode not retired");
            if(tick==6)ChatImageCache.clear();
        }
        ChatImageCache.maintain();require(cache.isEmpty(),"cleanup did not finish");
        System.out.println("PASS client cache cleanup: 24 entries / 12 ticks, coalescing, no render-time refill, worker retirement");
    }
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
