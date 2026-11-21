package dev.kalan.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class KalanClickGuiScreen extends Screen {
    private static final int W = 840, H = 520;
    private static final int OVERLAY = 0x9906030D;
    private static final int PANEL = 0xF50D0A15;
    private static final int SIDEBAR = 0xFF100C19;
    private static final int CARD = 0xFF1B1724;
    private static final int CARD_TOP = 0xFF27212F;
    private static final int PURPLE = 0xFF9B4DFF;
    private static final int PURPLE_DARK = 0xFF4B267A;
    private static final int WHITE = 0xFFF5F0FF;
    private static final int MUTED = 0xFFAAA0B9;
    private static final int LINE = 0xFF39264E;

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private KalanClickGuiCategory category = KalanClickGuiCategory.HUD;
    private KalanClickGuiEntry selected;
    private String search = "";
    private int scroll;
    private boolean dragging;
    private int dragX, dragY;

    public KalanClickGuiScreen() { super(Text.literal("Kalan Client")); }

    private int px() { return Math.min(W, Math.max(300, width - 28)); }
    private int py() { return Math.min(H, Math.max(240, height - 28)); }
    private int left() { return (width - px()) / 2; }
    private int top() { return (height - py()) / 2; }
    private int side() { return Math.max(66, Math.min(78, px() / 9)); }
    private void txt(DrawContext c, String s, int x, int y, int color) {
        c.drawTextWithShadow(mc.textRenderer, s, x, y, color);
    }
    private int textW(String s) { return mc.textRenderer.getWidth(s); }

    @Override public void renderBackground(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, OVERLAY);
    }

    @Override public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);
        int x=left(), y=top(), w=px(), h=py(), sw=side();
        rounded(c,x,y,w,h,18,PANEL);
        rounded(c,x+1,y+1,w-2,h-2,17,0xFF0E0B15);
        rounded(c,x+8,y+8,sw-14,h-16,14,SIDEBAR);

        // Left brand rail.
        rounded(c,x+14,y+14,sw-26,57,12,0xFF21142F);
        txt(c,"✦",x+23,y+25,PURPLE);
        txt(c,"KALAN",x+38,y+20,WHITE);
        txt(c,"CLIENT",x+38,y+36,MUTED);
        int idx=0;
        for(KalanClickGuiCategory cat:KalanClickGuiCategory.values()) {
            int by=y+88+idx*62;
            boolean active=category==cat;
            if(active) rounded(c,x+13,by,sw-24,50,11,0xFF3A205B);
            String symbol=switch(cat) { case HUD -> "◇"; case COMBAT -> "⚔"; case SETTINGS -> "⚙"; };
            int tw=textW(symbol);
            txt(c,symbol,x+13+(sw-24-tw)/2,by+16,active?PURPLE: MUTED);
            idx++;
        }

        int cx=x+sw+16, cw=w-sw-32;
        // Header search and close button.
        rounded(c,cx,y+15,cw-48,48,23,0xFF08070D);
        txt(c,"⌕",cx+17,y+31,PURPLE);
        txt(c,search.isEmpty()?"Search...":search,cx+43,y+32,search.isEmpty()?0xFF777080:WHITE);
        rounded(c,x+w-53,y+19,34,39,10,0xFF21152E);
        txt(c,"×",x+w-42,y+30,PURPLE);

        if(selected!=null) renderSettings(c,cx,y,cw,h);
        else renderCards(c,cx,y,cw,h);

        String player=mc.player==null?"Not in world":mc.player.getName().getString();
        txt(c,"Kalan Client  •  Player: "+player,x+sw+18,y+h-22,MUTED);
        txt(c,"RIGHT SHIFT",x+w-99,y+h-22,PURPLE);
    }

    private void renderCards(DrawContext c,int x,int y,int cw,int h) {
        List<KalanClickGuiEntry> entries=visibleEntries();
        if(entries.isEmpty()) { txt(c,"Ничего не найдено",x+20,y+112,MUTED); return; }
        int gap=13, cols=cw>=600?3:cw>=390?2:1;
        int cardW=(cw-gap*(cols-1))/cols, cardH=92, start=y+82;
        int bottom=y+h-42;
        for(int i=0;i<entries.size();i++) {
            int col=i%cols,row=i/cols;
            int ax=x+col*(cardW+gap), ay=start+row*(cardH+gap)-scroll;
            if(ay+cardH<y+70 || ay>bottom) continue;
            KalanClickGuiEntry e=entries.get(i);
            boolean on=e.isEnabled(), hover=KalanClickGuiLayout.contains((int)mouseX,(int)mouseY,ax,ay,cardW,cardH);
            rounded(c,ax,ay,cardW,cardH,13,on?PURPLE:0xFF65408B);
            rounded(c,ax+2,ay+2,cardW-4,cardH-4,11,CARD);
            rounded(c,ax+10,ay+9,cardW-20,34,9,CARD_TOP);
            String title=trim(e.getTitle(),cardW-38);
            txt(c,title,ax+(cardW-textW(title))/2,ay+20,WHITE);
            // Toggle pill, matching the reference.
            rounded(c,ax+13,ay+57,49,20,10,on?PURPLE:0xFF403A49);
            rounded(c,ax+(on?39:16),ay+60,14,14,8,0xFFFFFFFF);
            txt(c,"☷",ax+cardW-29,ay+57,PURPLE);
            if(hover) rounded(c,ax+1,ay+1,cardW-2,2,1,0xFFB77BFF);
        }
    }

    private void renderSettings(DrawContext c,int x,int y,int cw,int h) {
        KalanHudElement e=KalanHudRegistry.find(selected.getPanel());
        if(e==null) { selected=null; return; }
        txt(c,"‹",x+12,y+86,PURPLE);
        txt(c,selected.getTitle(),x+35,y+86,WHITE);
        txt(c,"НАСТРОЙКИ HUD-ЭЛЕМЕНТА",x+12,y+115,MUTED);
        drawSlider(c,x,y+160,cw,"Масштаб",e.getScale(),0.5f,2.0f,
                String.format(Locale.ROOT,"%.1f",e.getScale()));
        drawSlider(c,x,y+232,cw,"Прозрачность",e.getOpacity(),0.1f,1.0f,
                Math.round(e.getOpacity()*100)+"%");
        txt(c,"Изменяй значения нажатием по шкале",x+12,y+292,MUTED);
        txt(c,"ПКМ / ESC — назад",x+12,y+h-55,PURPLE);
    }

    private void drawSlider(DrawContext c,int x,int y,int cw,String label,float value,float min,float max,String valueText) {
        txt(c,label,x+12,y,WHITE);
        txt(c,valueText,x+cw-48,y,PURPLE);
        rounded(c,x+12,y+25,cw-24,8,4,0xFF393141);
        int fill=Math.round((cw-24)*(value-min)/(max-min));
        rounded(c,x+12,y+25,Math.max(5,fill),8,4,PURPLE);
        rounded(c,x+8+fill,y+21,16,16,8,WHITE);
    }

    private List<KalanClickGuiEntry> visibleEntries() {
        List<KalanClickGuiEntry> source=switch(category) {
            case HUD -> KalanClickGuiEntries.getHudEntries();
            case COMBAT -> KalanClickGuiEntries.getCombatEntries();
            case SETTINGS -> KalanClickGuiEntries.getAll();
        };
        if(search.isBlank()) return source;
        List<KalanClickGuiEntry> result=new ArrayList<>();
        String q=search.toLowerCase(Locale.ROOT);
        for(KalanClickGuiEntry e:source)
            if(e.getTitle().toLowerCase(Locale.ROOT).contains(q)||e.getDescription().toLowerCase(Locale.ROOT).contains(q)) result.add(e);
        return result;
    }

    private String trim(String s,int max) {
        if(textW(s)<=max) return s;
        while(!s.isEmpty()&&textW(s+"…")>max) s=s.substring(0,s.length()-1);
        return s+"…";
    }

    @Override public boolean mouseClicked(double mx,double my,int button) {
        int x=left(),y=top(),w=px(),h=py(),sw=side(),cx=x+sw+16,cw=w-sw-32;
        if(button==0&&mx>=x+w-57&&my>=y+15&&my<=y+60){close();return true;}
        int i=0;
        for(KalanClickGuiCategory cat:KalanClickGuiCategory.values()){
            int by=y+88+i*62;
            if(button==0&&KalanClickGuiLayout.contains((int)mx,(int)my,x+13,by,sw-24,50)){
                category=cat;selected=null;scroll=0;return true;
            } i++;
        }
        if(selected!=null){
            if(button==1||(button==0&&mx<cx+45&&my<y+105)){selected=null;return true;}
            if(button==0){
                KalanHudElement e=KalanHudRegistry.find(selected.getPanel());
                if(e!=null){
                    if(my>=y+176&&my<=y+202)e.setScale(e.getScale()+(mx<cx+cw/2?-0.1f:0.1f));
                    else if(my>=y+248&&my<=y+274)e.setOpacity(e.getOpacity()+(mx<cx+cw/2?-0.1f:0.1f));
                    KalanHudLayoutStore.save();
                } return true;
            } return true;
        }
        List<KalanClickGuiEntry> entries=visibleEntries();
        int gap=13,cols=cw>=600?3:cw>=390?2:1,cardW=(cw-gap*(cols-1))/cols,cardH=92,start=y+82;
        for(int n=0;n<entries.size();n++){
            int ax=cx+(n%cols)*(cardW+gap),ay=start+(n/cols)*(cardH+gap)-scroll;
            if(!KalanClickGuiLayout.contains((int)mx,(int)my,ax,ay,cardW,cardH))continue;
            KalanClickGuiEntry e=entries.get(n);
            if(button==1||(button==0&&mx>=ax+cardW-42)){selected=e;return true;}
            if(button==0){e.toggle();return true;}
        }
        return super.mouseClicked(mx,my,button);
    }

    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical) {
        scroll=Math.max(0,Math.min(1000,scroll-(int)(vertical*28)));
        return true;
    }

    @Override public boolean charTyped(char chr,int modifiers) {
        if(selected==null&&!Character.isISOControl(chr)&&search.length()<32){search+=chr;return true;}
        return super.charTyped(chr,modifiers);
    }

    @Override public boolean keyPressed(int key,int scan,int modifiers) {
        if(key==GLFW.GLFW_KEY_ESCAPE){if(selected!=null)selected=null;else close();return true;}
        if(selected==null&&key==GLFW.GLFW_KEY_BACKSPACE&&!search.isEmpty()){search=search.substring(0,search.length()-1);return true;}
        return super.keyPressed(key,scan,modifiers);
    }
    @Override public void close(){selected=null;mc.setScreen(null);}
    @Override public boolean shouldPause(){return false;}

    private static void rounded(DrawContext c,int x,int y,int w,int h,int r,int color) {
        if(w<=0||h<=0)return;
        int radius=Math.max(1,Math.min(r,Math.min(w,h)/2));
        c.fill(x+radius,y,x+w-radius,y+h,color);
        for(int row=0;row<radius;row++){
            double d=radius-row-0.5;
            int inset=radius-(int)Math.floor(Math.sqrt(radius*radius-d*d));
            c.fill(x+inset,y+row,x+w-inset,y+row+1,color);
            c.fill(x+inset,y+h-row-1,x+w-inset,y+h-row,color);
        }
    }
}
