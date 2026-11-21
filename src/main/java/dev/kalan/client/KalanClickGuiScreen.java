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
    private static final int PANEL = 0xF50B0814;
    private static final int SIDEBAR = 0xFF100B1B;
    private static final int CARD = 0xFF171320;
    private static final int CARD_TOP = 0xFF211B2B;
    private static final int PURPLE = 0xFF963CFF;
    private static final int PURPLE_LIGHT = 0xFFB77BFF;
    private static final int WHITE = 0xFFF5F0FF;
    private static final int MUTED = 0xFFAAA0B9;
    private static final int LINE = 0xFF39264E;

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private KalanClickGuiCategory category = KalanClickGuiCategory.HUD;
    private KalanClickGuiEntry selected;
    private String search = "";
    private int scroll;
    private boolean searchFocused;

    public KalanClickGuiScreen() { super(Text.literal("Kalan Client")); }

    private int px() { return Math.min(W, Math.max(320, width - 28)); }
    private int py() { return Math.min(H, Math.max(260, height - 28)); }
    private int left() { return (width - px()) / 2; }
    private int top() { return (height - py()) / 2; }
    private int side() { return Math.max(78, Math.min(94, px() / 9)); }
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
        rounded(c,x,y,w,h,18,0xFF6D36B1);
        rounded(c,x+2,y+2,w-4,h-4,16,PANEL);

        // The brand block occupies the upper-left corner, as in the reference.
        rounded(c,x+10,y+10,sw+58,66,13,0xFF21142F);
        txt(c,"◢",x+23,y+25,PURPLE_LIGHT);
        txt(c,"Kalan Client",x+51,y+17,WHITE);
        String player=mc.player==null?"Player: —":"Player: "+mc.player.getName().getString();
        txt(c,trim(player,sw+40),x+51,y+39,MUTED);

        // Sidebar rail and category navigation.
        rounded(c,x+10,y+84,sw-2,h-96,13,SIDEBAR);
        int idx=0;
        for(KalanClickGuiCategory cat:KalanClickGuiCategory.values()) {
            int by=y+98+idx*59;
            boolean active=category==cat;
            if(active) rounded(c,x+17,by,sw-16,48,11,0xFF43216D);
            String symbol=switch(cat) {
                case HUD -> "◇";
                case COMBAT -> "⚒";
                case SETTINGS -> "♟";
            };
            int tw=textW(symbol);
            txt(c,symbol,x+17+(sw-16-tw)/2,by+14,active?PURPLE_LIGHT:0xFF9870C8);
            idx++;
        }

        int cx=x+sw+24, cw=w-sw-42;
        int searchW=cw-52;
        rounded(c,cx,y+17,searchW,48,23,0xFF09070F);
        rounded(c,cx+1,y+18,searchW-2,46,22,searchFocused?0xFF21132F:0xFF0B0912);
        txt(c,"⌕",cx+16,y+31,PURPLE_LIGHT);
        String shown=search.isEmpty()?"Search...":search;
        txt(c,shown,cx+43,y+32,search.isEmpty()?0xFF777080:WHITE);
        if(searchFocused && (System.currentTimeMillis()/500)%2==0) {
            int caretX=cx+43+textW(search);
            c.fill(caretX,y+29,caretX+1,y+46,PURPLE_LIGHT);
        }
        rounded(c,x+w-53,y+18,35,43,11,0xFF21152E);
        txt(c,"×",x+w-42,y+29,PURPLE_LIGHT);
        c.fill(x+sw+10,y+77,x+w-12,y+78,LINE);

        if(selected!=null) renderSettings(c,cx,y,cw,h);
        else renderCards(c,cx,y,cw,h);

        txt(c,"KALAN CLIENT  •  "+(mc.player==null?"MENU":mc.player.getName().getString()),
                x+sw+18,y+h-22,MUTED);
        txt(c,"RIGHT SHIFT",x+w-103,y+h-22,PURPLE_LIGHT);
    }

    private void renderCards(DrawContext c,int x,int y,int cw,int h) {
        List<KalanClickGuiEntry> entries=visibleEntries();
        if(entries.isEmpty()) { txt(c,"Ничего не найдено",x+20,y+112,MUTED); return; }
        int gap=11, cols=cw>=570?3:cw>=370?2:1;
        int cardW=(cw-gap*(cols-1))/cols, cardH=91, start=y+91;
        int bottom=y+h-39;
        for(int i=0;i<entries.size();i++) {
            int col=i%cols,row=i/cols;
            int ax=x+col*(cardW+gap), ay=start+row*(cardH+gap)-scroll;
            if(ay+cardH<y+82 || ay>bottom) continue;
            KalanClickGuiEntry e=entries.get(i);
            boolean on=e.isEnabled();
            boolean hover=KalanClickGuiLayout.contains((int)mouseX,(int)mouseY,ax,ay,cardW,cardH);
            rounded(c,ax,ay,cardW,cardH,14,hover?PURPLE_LIGHT:0xFF7650A6);
            rounded(c,ax+2,ay+2,cardW-4,cardH-4,12,CARD);
            rounded(c,ax+10,ay+9,cardW-20,34,10,CARD_TOP);
            String title=trim(e.getTitle(),cardW-36);
            txt(c,title,ax+(cardW-textW(title))/2,ay+20,WHITE);

            // Purple pill toggle and settings glyph mirror the reference controls.
            rounded(c,ax+13,ay+57,49,20,10,on?PURPLE:0xFF393342);
            rounded(c,ax+(on?39:16),ay+60,14,14,8,0xFFFFFFFF);
            txt(c,"☷",ax+cardW-30,ay+56,PURPLE_LIGHT);
            if(hover) rounded(c,ax+1,ay+1,cardW-2,2,1,PURPLE_LIGHT);
        }
        int rows=(entries.size()+cols-1)/cols;
        int maxScroll=Math.max(0,rows*(cardH+gap)-(bottom-start));
        scroll=Math.max(0,Math.min(scroll,maxScroll));
    }

    private void renderSettings(DrawContext c,int x,int y,int cw,int h) {
        KalanHudElement e=KalanHudRegistry.find(selected.getPanel());
        if(e==null) { selected=null; return; }
        txt(c,"‹",x+12,y+91,PURPLE_LIGHT);
        txt(c,selected.getTitle(),x+38,y+91,WHITE);
        txt(c,"НАСТРОЙКИ ЭЛЕМЕНТА",x+12,y+121,MUTED);
        drawSlider(c,x,y+166,cw,"Масштаб",e.getScale(),0.5f,2.0f,
                String.format(Locale.ROOT,"%.1f",e.getScale()));
        drawSlider(c,x,y+241,cw,"Прозрачность",e.getOpacity(),0.1f,1.0f,
                Math.round(e.getOpacity()*100)+"%");
        txt(c,"Нажми слева или справа на шкале, чтобы изменить значение",x+12,y+306,MUTED);
        txt(c,"ПКМ / ESC — назад",x+12,y+h-55,PURPLE_LIGHT);
    }

    private void drawSlider(DrawContext c,int x,int y,int cw,String label,float value,float min,float max,String valueText) {
        txt(c,label,x+12,y,WHITE);
        txt(c,valueText,x+cw-48,y,PURPLE_LIGHT);
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
            if(e.getTitle().toLowerCase(Locale.ROOT).contains(q)
                    ||e.getDescription().toLowerCase(Locale.ROOT).contains(q)) result.add(e);
        return result;
    }

    private String trim(String s,int max) {
        if(textW(s)<=max) return s;
        while(!s.isEmpty()&&textW(s+"…")>max) s=s.substring(0,s.length()-1);
        return s+"…";
    }

    @Override public boolean mouseClicked(double mx,double my,int button) {
        int x=left(),y=top(),w=px(),h=py(),sw=side(),cx=x+sw+24,cw=w-sw-42;
        if(button==0&&mx>=x+w-57&&my>=y+15&&my<=y+65){close();return true;}
        if(button==0&&mx>=cx&&mx<=cx+cw-52&&my>=y+17&&my<=y+65) {
            searchFocused=true; selected=null; return true;
        }
        if(button==0) searchFocused=false;
        int i=0;
        for(KalanClickGuiCategory cat:KalanClickGuiCategory.values()) {
            int by=y+98+i*59;
            if(button==0&&KalanClickGuiLayout.contains((int)mx,(int)my,x+17,by,sw-16,48)) {
                category=cat;selected=null;scroll=0;return true;
            }
            i++;
        }
        if(selected!=null) {
            if(button==1||(button==0&&mx<cx+45&&my<y+110)){selected=null;return true;}
            if(button==0) {
                KalanHudElement e=KalanHudRegistry.find(selected.getPanel());
                if(e!=null) {
                    if(my>=y+182&&my<=y+207)e.setScale(e.getScale()+(mx<cx+cw/2?-0.1f:0.1f));
                    else if(my>=y+257&&my<=y+282)e.setOpacity(e.getOpacity()+(mx<cx+cw/2?-0.1f:0.1f));
                    KalanHudLayoutStore.save();
                }
                return true;
            }
            return true;
        }
        List<KalanClickGuiEntry> entries=visibleEntries();
        int gap=11,cols=cw>=570?3:cw>=370?2:1,cardW=(cw-gap*(cols-1))/cols,cardH=91,start=y+91;
        for(int n=0;n<entries.size();n++) {
            int ax=cx+(n%cols)*(cardW+gap),ay=start+(n/cols)*(cardH+gap)-scroll;
            if(!KalanClickGuiLayout.contains((int)mx,(int)my,ax,ay,cardW,cardH))continue;
            KalanClickGuiEntry e=entries.get(n);
            if(button==1||(button==0&&mx>=ax+cardW-43)){selected=e;return true;}
            if(button==0){e.toggle();return true;}
        }
        return super.mouseClicked(mx,my,button);
    }

    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical) {
        if(mx<left()+side()+20) return super.mouseScrolled(mx,my,horizontal,vertical);
        scroll=Math.max(0,Math.min(2000,scroll-(int)(vertical*32)));
        return true;
    }

    @Override public boolean charTyped(char chr,int modifiers) {
        if(searchFocused&&!Character.isISOControl(chr)&&search.length()<32){search+=chr;scroll=0;return true;}
        return super.charTyped(chr,modifiers);
    }

    @Override public boolean keyPressed(int key,int scan,int modifiers) {
        if(key==GLFW.GLFW_KEY_ESCAPE) {
            if(selected!=null)selected=null;
            else if(searchFocused)searchFocused=false;
            else close();
            return true;
        }
        if(searchFocused&&key==GLFW.GLFW_KEY_BACKSPACE&&!search.isEmpty()) {
            search=search.substring(0,search.length()-1);scroll=0;return true;
        }
        if(searchFocused&&key==GLFW.GLFW_KEY_ENTER){searchFocused=false;return true;}
        return super.keyPressed(key,scan,modifiers);
    }
    @Override public void close(){selected=null;mc.setScreen(null);}
    @Override public boolean shouldPause(){return false;}

    private static void rounded(DrawContext c,int x,int y,int w,int h,int r,int color) {
        if(w<=0||h<=0)return;
        int radius=Math.max(1,Math.min(r,Math.min(w,h)/2));
        c.fill(x+radius,y,x+w-radius,y+h,color);
        for(int row=0;row<radius;row++) {
            double d=radius-row-0.5;
            int inset=radius-(int)Math.floor(Math.sqrt(radius*radius-d*d));
            c.fill(x+inset,y+row,x+w-inset,y+row+1,color);
            c.fill(x+inset,y+h-row-1,x+w-inset,y+h-row,color);
        }
    }
}
