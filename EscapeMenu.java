import javax.swing.*;
import geom.VektorI;
/**
 * Wird angezeigt, wenn man esc drügggt...  // es ist kurz vor 0 Uhr; ich kann nicht mehr schreiben
 * Gibt die Möglichkeit das Spiel zu beenden oder weiterzuspielen
 */
public class EscapeMenu extends Menu {
    private JLabel pause;
    private JButton restart;
    private JButton logout;
    private JButton exit;
    
    //Constructor 
    public EscapeMenu(Player p){
        super(p, "Pause", new VektorI(225, 320));
        
        // erstellt ein neues Label
        pause = new MenuLabel(this, "Pause", new VektorI(60,30) ,new VektorI(90,30), 30);
        
        // Erstellt einen neuen Button
        restart = new MenuButton(this, "Weiter", new VektorI(30,120), new VektorI(150, 35)){
            public void onClick(){closeMenu();}
        };
         
        logout = new MenuButton(this, "Logout", new VektorI(30,170), new VektorI(150, 35)){
            public void onClick(){logout();}
        };
        
        logout = new MenuButton(this, "Exit", new VektorI(30,220), new VektorI(150, 35)){
            public void onClick(){exit();}
        };
    }

    public void logout(){
        this.getPlayer().logout();
        dispose();
    }
    
    public void exit(){
        this.getPlayer().logout();
        this.getPlayer().exit();
        dispose();
    }
}