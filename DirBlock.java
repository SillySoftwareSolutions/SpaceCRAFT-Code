import geom.*;
/**
 * Ein Block, der eine bestimmte Drehung hat
 */
public class DirBlock extends SBlock
{
    /**
     * ...
     */
    public DirBlock(int id, String name, String imageString)
    {
        super(id, name, imageString);
    }
    
    public DirBlock(int id, String name)
    {
        super(id, name);
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
