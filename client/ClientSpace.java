package client;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import util.geom.*;
/**
 * recht ähnlich zu server.Space
 * Client-Kopie eines Weltalls für den Bearbeitungsmodus
 */
public class ClientSpace
{
    ArrayList<ClientMass>masses;
    long inGameTime;
    long inGameDTime;
    Manoeuvre manoeuvre;
    Timer timer;
    public ClientSpace(ArrayList<ClientMass> masses, long inGameTime, long inGameDTime, Manoeuvre manoeuvre)
    {
        this.masses=masses;
        this.inGameTime=inGameTime;
        this.inGameDTime=inGameDTime;
        this.manoeuvre=manoeuvre; //neues Manöver, alle anderen stehen bereits als Orbit- und MassChanges in den Masses drin
        calcOrbits(ClientSettings.SPACE_CALC_TIME); //so lange Zeit, damit man es gut sieht
        timerSetup();
    }

    public void timerSetup(){
        timer=new Timer();
        timer.schedule(new TimerTask(){
            public void run(){
                inGameTime=inGameTime+inGameDTime;
                for (int i=0;i<masses.size();i++){
                    Orbit o=masses.get(i).getOrbit();
                    if (o.getPos(inGameTime)!=null){
                        masses.get(i).setPos(o.getPos(inGameTime));
                    }
                    if (o.getVel(inGameTime)!=null){
                        masses.get(i).setVel(o.getVel(inGameTime));
                    }
                }
                calcOrbits(ClientSettings.SPACE_CALC_TIME); //so lange Zeit, damit man es gut sieht. Verwendet wird davon nur der geringste Teil.
            }
        },ClientSettings.SPACE_TIMER_PERIOD,ClientSettings.SPACE_TIMER_PERIOD);
    }

    /**
     * Die Parameter kommen von einem PlayerS (zur Fokussierung auf einen Planeten).
     * Es ist der Index des Planeten an pos.
     * hier kein Request (da ohnehin clientseitig)
     */
    public int getFocussedMassIndex(VektorI posClick, VektorD posToNull, VektorI screenSize, double scale){
        posClick.y=-posClick.y+screenSize.y; //invertiertes Koordinatensystem
        posClick=posClick.subtract(screenSize.divide(2));
        posClick=posClick.divide(scale);
        VektorI posClickToNull=posClick.add(posToNull.toInt());
        Integer ret=new Integer(-1);
        for (int i=0;i<masses.size();i++){
            if (masses.get(i)!=null){
                VektorD posPlanet=masses.get(i).getPos();
                double r=2;
                r=masses.get(i).getRadius()*scale;
                double distance=posPlanet.subtract(posClickToNull.toDouble()).getLength()*scale;
                if (distance < r+20){
                    ret=new Integer(i);
                    return ret;
                }
            }
        }
        ret=new Integer(-1);
        return ret;
    }

    /**
     * (hier keine Requests, da ohnehin clientseitig)
     */
    public ArrayList<VektorD> getAllPos(){
        ArrayList<VektorD> ret=new ArrayList<VektorD>();
        for (int i=0;i<masses.size();i++){
            ret.add(masses.get(i).getPos());
        }
        return ret;
    }

    public ArrayList<Integer> getAllRadii(){
        ArrayList<Integer> ret=new ArrayList<Integer>();
        for (int i=0;i<masses.size();i++){
            ret.add(masses.get(i).getRadius());
        }
        return ret;
    }

    public ArrayList<Orbit> getAllOrbits(){
        ArrayList<Orbit> ret=new ArrayList<Orbit>();
        int accuracy=(int) ClientSettings.SPACE_GET_ORBIT_ACCURACY;
        for (int i=0;i<masses.size();i++){
            Orbit o=masses.get(i).getOrbit();
            ArrayList<VektorD> pos=new ArrayList<VektorD>(o.pos.size()/accuracy);
            ArrayList<Double> mass=new ArrayList<Double>(o.pos.size()/accuracy);
            for (int j=0;j<o.pos.size();j=j+accuracy){
                pos.add(o.pos.get(j));
                mass.add(o.mass.get(j));
            }
            ret.add(new Orbit(pos,mass,o.t0,o.t1,o.dtime*accuracy));
        }
        return ret;
    }

    public VektorD getMassPos(int index){
        VektorD ret=new VektorD(Double.NaN,Double.NaN);
        if (masses.get(index)==null){

        }
        else{
            ret=masses.get(index).getPos();
        }
        return ret;
    }

    /**
     * Berechnet die (Nicht-Kepler-)Orbits aller Objekte in diesem Space ab dem Aufruf dieser Methode für (dtime) Sekunden
     */
    public void calcOrbits(long dtime){
        ArrayList<VektorD>[] poss=new ArrayList[masses.size()]; //Positionslisten
        ArrayList<VektorD>[] vels=new ArrayList[masses.size()]; //Geschwindigkeitslisten
        ArrayList<Double>[]masss=new ArrayList[masses.size()]; //Massenlisten (für MassChanges)
        for (int i=0;i<masses.size();i++){
            poss[i]=new ArrayList<VektorD>();
            poss[i].add(masses.get(i).getPos()); //erste Position, Zeit 0
            vels[i]=new ArrayList<VektorD>();
            vels[i].add(masses.get(i).getVel());
            masss[i]=new ArrayList<Double>();
            masss[i].add(masses.get(i).getMass());
        }
        for (double t=0;t<dtime;t=t+ClientSettings.SPACE_CALC_PERIOD_INGAME){
            int k=(int) Math.round(t/ClientSettings.SPACE_CALC_PERIOD_INGAME); //zeitlicher Index in poss und vels
            //k sollte immer kleiner als Double.MAX_VALUE sein
            for (int i=0;i<masses.size();i++){ //Masse, deren Orbit berechnet wird
                double m2=masss[i].get(k);
                VektorD pos2=poss[i].get(k);
                if (pos2!=null){
                    VektorD F=new VektorD(0,0); //wirkende Kraft durch Gravitation und OrbitChanges
                    for (int j=0;j<masses.size();j++){
                        double m1=masss[j].get(k);
                        VektorD pos1=poss[j].get(k);
                        if (pos1.x!=pos2.x || pos1.y!=pos2.y){
                            VektorD posDiff=pos1.subtract(pos2);
                            VektorD Fgj=posDiff.multiply(ClientSettings.G*m1*m2/Math.pow(posDiff.getLength(),3)); 
                            //geteilt durch (Abstand^3), da ja mit dem ursprünglichen Vektor wieder multipliziert wird
                            F=F.add(Fgj);
                        }
                    }
                    ArrayList<OrbitChange> oc=masses.get(i).orbitChanges;
                    int j=0;
                    while (j<oc.size()){
                        if (t+inGameTime>=oc.get(j).t0 && t+inGameTime<oc.get(j).t1){
                            F=F.add(oc.get(j).F);
                            j=j+1;
                        }
                        else if (inGameTime>oc.get(j).t1){ //OrbitChange sicher vollständig abgehandelt, kann entfernt werden
                            oc.remove(j);
                        }
                        else{
                            j=j+1;
                        }
                    }
                    j=0;
                    ArrayList<MassChange> mc=masses.get(i).massChanges;
                    double mass=masss[i].get(k);
                    while (j<mc.size()){
                        if (t+inGameTime>=mc.get(j).t0 && t+inGameTime<mc.get(j).t1){
                            double dm=mc.get(j).dMass/(mc.get(j).t1-mc.get(j).t0)*(ClientSettings.SPACE_CALC_PERIOD_INGAME);
                            mass=mass+dm;
                            j=j+1;
                        }
                        else if (inGameTime>mc.get(j).t1){
                            mc.remove(j);
                        }
                        else{
                            j=j+1;
                        }
                    }
                    if (manoeuvre!=null && i==manoeuvre.shipIndex){
                        if (t+inGameTime>=manoeuvre.oc.t0 && t+inGameTime<manoeuvre.oc.t1){
                            F=F.add(manoeuvre.oc.F);
                            double dm=manoeuvre.mc.dMass/(manoeuvre.mc.t1-manoeuvre.mc.t0)*(ClientSettings.SPACE_CALC_PERIOD_INGAME); 
                            //manoeuvre.oc.t1=manoeuvre.mc.t1, genauso mit t2
                            mass=mass+dm;
                        }
                    }
                    masss[i].add(mass);
                    VektorD dx=F.multiply(Math.pow(ClientSettings.SPACE_CALC_PERIOD_INGAME,2)).divide(m2).divide(2); //x=1/2*a*t^2
                    dx=dx.add(vels[i].get(k).multiply(ClientSettings.SPACE_CALC_PERIOD_INGAME));

                    boolean hasCrash=false;
                    for (int l=0;l<masses.size();l++){
                        if (l!=i){ //kein Zusammenstoß mit sich selbst
                            /*Intersektion eines Kreises mit einer Linie:
                            K: (x-mx)^2 + (y-my)^2 <= r^2
                            L: x = sx + dx*t
                               y = sy + dy*t
                            => (sx+dx*t-mx)^2 + (sy-dy*t-my)^2 <= r^2 nach t auflösen
                            (sx-mx)^2 + (sx-mx)*dx*t + (dx^2)*(t^2) + (sy-my)^2 + (sy-my)*dy*t + (dy^2)*(t^2) <= r^2
                            a = dx^2 + dy^2
                            b = (sx-mx)*dx + (sy-dy)*dy
                            c = (sx-mx)^2 + (sy-my)^2 - r^2
                            */
                            int r=masses.get(l).getRadius();
                            double a = Math.pow(dx.x,2) + Math.pow(dx.y,2);
                            double b = (pos2.x-poss[l].get(k).x)*dx.x + (pos2.y-poss[l].get(k).y)*dx.y;
                            double c = Math.pow(pos2.x-poss[l].get(k).x,2) + Math.pow(pos2.y-poss[l].get(k).y,2) - Math.pow(r,2);
                            double disk=Math.pow(b,2)-4*a*c; //Diskriminante
                            if (disk>=0){
                                //Zusammenstoß mit einem Planeten, hier sollte im Normalfall toCraft aufgerufen werden
                                double t1 = (-b+Math.sqrt(disk)) / (2*a);
                                double t2 = (-b-Math.sqrt(disk)) / (2*a);
                                double t0;
                                if (t1<=t2 && t1>=0 && t1<=1){
                                    t0=t1;
                                }
                                else if (t2>=0 && t2<=1){
                                    t0=t2;
                                }
                                else{
                                    t0=-1;
                                }
                                //das kleinere der beiden, das in [0;1] liegt, da t(0) mit dem Abstand vom derzeitigen Punkt zusammenhängt
                                //(t0<0 => falsche Richtung, t0>1 => zu weit, als dass der Planet tatsächlich erreicht würde)
                                if (Math.signum(t1)!=Math.signum(t2)){ //im Planeten
                                    poss[i].add(pos2);
                                    vels[i].add(new VektorD(0,0));
                                    hasCrash=true;
                                }
                                else if (t0!=-1){ //Crash in diesem Augenblick in den Planeten
                                    VektorD dx1=dx.multiply(t0);
                                    poss[i].add(pos2.add(dx1));
                                    vels[i].add(dx1.divide(ClientSettings.SPACE_CALC_PERIOD_INGAME/t0));
                                    hasCrash=true;
                                    //System.out.println("crash into planet "+dx1.divide(Settings.SPACE_CALC_PERIOD_INGAME/t0)+" "+pos2.add(dx1));
                                }
                            }
                        }
                    }
                    if (!hasCrash){
                        poss[i].add(pos2.add(dx));
                        vels[i].add(dx.divide(ClientSettings.SPACE_CALC_PERIOD_INGAME));
                        //System.out.println(dx+" "+pos2.add(dx));
                    }
                }
            }
        }
        for (int i=0;i<masses.size();i++){
            Orbit o=new Orbit(poss[i],masss[i],inGameTime,inGameTime+dtime,ClientSettings.SPACE_CALC_PERIOD_INGAME);
            masses.get(i).setOrbit(o);
        }
    }
}