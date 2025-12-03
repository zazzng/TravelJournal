package X;
import java.util.ArrayList;

public abstract class XScenario {
    protected XApp mApp = null;
    public XApp getApp () {
        return this.mApp;
    }
    protected ArrayList<XScene> mScenes = null;

    protected XScenario (XApp app) {
        this.mApp = app;
        this.mScenes = new ArrayList<XScene>();
        this.addScenes();
    }

    protected abstract void addScenes();
    protected void addScene(XScene scene) {
        this.mScenes.add(scene);
    }
}
