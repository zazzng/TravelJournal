package x;

import java.util.ArrayList;

public abstract class XScenario {
    // fields 
    protected XApp mApp = null;
    public XApp getApp(){
        return this.mApp;
    }
    protected ArrayList<XScene> mScenes = null;
    
    // constructor
    protected XScenario(XApp app) {
        this.mApp = app;
        this.mScenes = new ArrayList<XScene>();
        this.addScenes();
    }
    
    // abstract methods
    protected abstract void addScenes();
    
    // concrete methods
    protected void addScene(XScene scene) {
        this.mScenes.add(scene);
    }
}
