import geom.*;
/**
 * Ein Block, der eine bestimmte Drehung hat
 */
public class DirBlock extends SBlock
{
    /**
     * ...
     */
    public DirBlock(String name, String imageString)
    {
        super(name, imageString);
    }
    
    public DirBlock(String name)
    {
        super(name);
    }
    
    /**
     * EVENT: wird aufgerufen, wenn ein Block gesetzt wird (nicht zwingend von einem Spieler)
     * @param:
     * Sandbox sb: Sandbox, in der der Block ist
     * VektorI pos: Position des Blocks in dieser Sandbox
     */
    public void onConstruct(Sandbox sb, VektorI pos){
        Meta meta = new Meta();
        meta.put("dir" , 0);
        sb.setMeta(pos, meta);
    }
    
    public static int getDir(Sandbox sb, VektorI pos){
        Meta meta = sb.getMeta(pos);
        try{ return (int)meta.get("dir"); } catch(Exception e){return 0;}
    }
}
