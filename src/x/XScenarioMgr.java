package x;

import java.util.ArrayList;

public abstract class XScenarioMgr {
    // fields
    protected XApp mApp = null;
    protected ArrayList<XScenario> mScenarios = null;
    protected XScene mCurScene = null;
    public XScene getCurScene() {
        return this.mCurScene;
    }
    
    public void setCurScene(XScene scene) {
        if (this.mCurScene != null) {
            this.mCurScene.wrapUp();
        }
        scene.getReady();
        this.mCurScene = scene;
    }
    
    // constructor
    protected XScenarioMgr(XApp app) {
        this.mApp = app;
        this.mScenarios = new ArrayList<XScenario>();
        this.addScenario();
        this.setInitCurScene();
    }
    
    protected abstract void addScenario();
    protected abstract void setInitCurScene();
    
    protected void addScenario(XScenario scenario) {
        this.mScenarios.add(scenario);
    }
}
