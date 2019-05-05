import java.util.ArrayList;
import geom.*;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JButton;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
/**
 * Ein Spieler
 * Kann entweder in der Craft oder in der Space Ansicht sein
 */
public class Player implements Serializable
{
    private transient Main main; //Referenz auf den "Server", wird nicht gespeichert (logischerweise)
    private String name;
    private transient Space space; // mein privater Weltraum xD
    private transient boolean online = false;  // aktuell ob der Frame des Spielers gerade offen ist
    private boolean inCraft = true;
    private transient Frame frame;
    private PlayerS playerS;
    private PlayerC playerC;
    private transient Menu openedMenu = null;  // wenn ein Menu (z.B.: Escape Menu; ChestInterface gerade offen ist)
    private Mass currentMass;
    
    /**
     * Erstellt neuen Spieler in einem Weltraum
     * 
     * @param:
     * boolean Singleplayer: gibt an, ob das Spiel im Singleplayer Modus ist und folglich der Spieler zu beginn online ist
     */
    public Player(String name, Space space, boolean singleplayer)
    {
        this.space = space;
        this.name = name;
        //der Spawnpunkt muss nochmal überdacht werden
        
        if (singleplayer) login();
        currentMass = space.getSpawnMass();
        this.playerS=new PlayerS(this,new VektorD(0,0), currentMass);
        this.playerC=new PlayerC(this, currentMass.getSandbox(), new VektorD(50,50),frame);
    }
    
    private void makeFrame(){ //Frame-Vorbereitung (Buttons, Listener etc.) nur hier
        this.frame = new Frame(name,new VektorI(928,608),this);
        Listener l=new Listener(this);
        this.frame.addKeyListener(l);
        this.frame.addMouseListener(l);
        this.frame.addMouseMotionListener(l);
        this.frame.addWindowListener(l);
        this.frame.addMouseWheelListener(l);
    }
    
    public void disposeFrame(){
        if (frame == null)return;
        this.frame.dispose();
        frame = null;
    }
    
    public Space getSpace(){
        return space;
    }
    
    public void setSpace(Space s){ //nur für nach der Deserialisierung, damit alle Spieler den selben Space erhalten
        this.space=s;
    }
    
    public void setMain(Main m){
        if (m.getSpace()!=this.space){
            throw new IllegalArgumentException();
        }
        this.main=m;
    }
    
    public Object readResolve() throws ObjectStreamException{
        this.makeFrame();
        return this;
    }

    /**
     * gibt den Namen des Spielers zurück
     */
    public String getName(){
        return name;
    }
    
    public void login(){
        if(online)return;
        this.online = true;
        makeFrame();
    }
    
    public void logout(){
        if(!online)return;
        closeMenu();
        this.online = false;
        disposeFrame();
        main.exitIfNoPlayers();
    }
    
    public boolean isOnline(){
        return online;
    }
    
    /**
     * Wechselt die Ansicht zur Space Ansicht
     */
    public void toSpace()
    {
        if (!inCraft)return; // wenn der Spieler schon in der Space Ansicht ist, dann wird nichts getan
        inCraft = false;
        repaint();
    }
    
    /**
     * Wechselt die Ansicht zur Space Ansicht
    */
    public void toCraft()
    {
        if (inCraft)return; // wenn der Spieler schon in der Craft Ansicht ist, dann wird nichts getan
        inCraft = true;
        repaint();
    }
    
    /**
     * Wechselt die Ansicht zur anderen Ansicht
     */
    public void changePlayer()
    {
        if (inCraft) toSpace(); else toCraft();
    }
    
    /**
     * soll aufgerufen werden, wenn ein Fenster geöffnet wird
     */
    public void openMenu(Menu menu)
    {
        this.openedMenu = menu;
    }
    
    /**
     * soll vom Fenster aus aufgerufen werden, wenn ein Fenster geschlossen wird
     */
    public void removeMenu()
    {
        this.openedMenu = null;
    }
    
    /**
     * schließt das gerade geöffnete Menu
     */
    public void closeMenu()
    {
        if (openedMenu == null)return;
        openedMenu.closeMenu();
        openedMenu = null;
    }
    
    /**
     * gibt false zurück, wenn gerade ein Menü offen ist
     */
    public boolean isActive()
    {
        return openedMenu == null;
    }
    
    /**
     * schließt das gesamte Spiel
     */
    public void exit(){
        logout();
        main.exit();
    }
    
    /**
     * Tastatur Event
     * @param:
     *  char type: 'p': pressed
     *             'r': released
     *             't': typed (nur Unicode Buchstaben)
     */
    public void keyEvent(KeyEvent e, char type) {
        if (!isActive())return;  // wenn ein Menü offen ist, dann passiert nichts
        switch (e.getKeyCode()){
            case Shortcuts.open_escape_menu: 
                if (type != 'p') break;
                new EscapeMenu(this);
                break;
            case Shortcuts.change_space_craft:
                if (type != 'p') break;
                changePlayer();
                repaint();
                break;
            default:
                if (inCraft)playerC.keyEvent(e,type);
                else playerS.keyEvent(e,type);
        }
    }
    
    /**
     * Mausrad"Event"
     * @param:
     * irgend ein EventObjekt; Keine Ahnung was das kann
     */
    public void mouseWheelMoved(MouseWheelEvent e){
        if (!isActive())return;  // wenn ein Menü offen ist, dann passiert nichts
        if(!inCraft)playerS.mouseWheelMoved(e);
    }
    
    /**
     * Maus Event
     * @param:
     *  char type: 'p': pressed
     *             'r': released
     *             'c': clicked
     * entered und exited wurde nicht implementiert, weil es dafür bisher keine Verwendung gab
     */
    public void mouseEvent(MouseEvent e, char type) {
        closeMenu();
        if (inCraft)playerC.mouseEvent(e,type);
        else playerS.mouseEvent(e,type);
    }
    
    public void windowEvent(WindowEvent e, char type){
        if (type=='c'){ //Schließen des Fensters
            if (this.main!=null){
                logout();
                //main.removePlayer(this.name);
            }
            else{
                System.exit(0); //Fehler
            }
        }
    }
    
    /**
     * gibt den Vektor der größe des Bildschirms zurück
     */
    public VektorI getScreenSize(){
        return frame.getScreenSize();
    }
    
    /**
     * gibt die Masse, bei der sich der Spieler gerade befindet zurück
     */
    public Mass getCurrentMass(){
        return currentMass;
    }
    
    /**
     * ...
     */
    public Frame getFrame(){
        return frame;
    }
    
    /**
     * Grafik ausgeben
     */
    public void paint(Graphics g, VektorI screenSize){
        if (g!=null){
            if (inCraft && playerC != null)playerC.paint(g, screenSize);
            else if (playerS != null) playerS.paint(g, screenSize);
        }
    }
    public void repaint(){
        if(frame!=null)frame.repaint();
    }
}