import geom.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.io.ObjectStreamException;
public class Blocks_Piston extends SBlock
{
    private static BufferedImage imgOn = ImageTools.get('C', "blocks_piston_on");;
    private static BufferedImage imgOff = ImageTools.get('C', "blocks_piston_off");
    private boolean state;
    private Sandbox sb;
    private VektorI pos;
    public Blocks_Piston(){
        super("Piston");
    }
    
    public void onConstruct(){
       Metadata meta = new Metadata(){
           public int dir = 0;
           public boolean state = false;
       };
    }
    
    /**
     * Gibt die Textur des Blocks zurück
     */
    public BufferedImage getImage(Sandbox sb, VektorI pos)
    {
        Metadata meta = sb.getMetadata(pos);
        return null;
    }
    
    public void onRightclick(){
        /*
        state = !state;
        VektorI pos2 = pos.add(new VektorI(dir).multiply(-1));
        if(state == true){
            this.setImage(imgOn);
            sb.setBlock(pos2, new Blocks_PistonFront(dir));
        }else{
            this.setImage(imgOff);
            if(sb.getBlock(pos2).getName() == "Piston Front"){
                sb.setBlock(pos2, null);
            }
        }
        */
    }
    
    //@Override
    public boolean onBreak(Player p){
      /*  if(state == false) return true;
        VektorI pos2 = pos.add(new VektorI(dir).multiply(-1));
        if(sb.getBlock(pos2).getName() == "Piston Front"){
            sb.setBlock(pos2, null);
        }*/
        return true;
    }
    
}