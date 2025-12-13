package tj;

import tj.scenario.TJDefaultScenario;
import tj.scenario.TJDrawScenario;
import tj.scenario.TJHomeScenario;
import tj.scenario.TJImageScenario;
import tj.scenario.TJMapScenario;
import tj.scenario.TJNavigateScenario;
import tj.scenario.TJSelectScenario;
import x.XScenarioMgr;

public class TJScenarioMgr extends XScenarioMgr {
    // constructor
    public TJScenarioMgr(TJ tj) {
        super(tj);
    }

    @Override
    protected void addScenario() {
        this.addScenario(TJMapScenario.createSingleton(this.mApp));
        this.addScenario(TJHomeScenario.createSingleton(this.mApp));
        this.addScenario(TJDefaultScenario.createSingleton(this.mApp));
        this.addScenario(TJDrawScenario.createSingleton(this.mApp));
        this.addScenario(TJSelectScenario.createSingleton(this.mApp));
        this.addScenario(TJNavigateScenario.createSingleton(this.mApp));
        this.addScenario(TJImageScenario.createSingleton(this.mApp));
    }

    @Override
    protected void setInitCurScene() {
        this.setCurScene(TJMapScenario.MapReadyScene.getSingleton());
    }
    
}
