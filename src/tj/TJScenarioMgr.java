package tj;

import tj.scenario.TJColorScenario;
import tj.scenario.TJDefaultScenario;
import tj.scenario.TJDrawScenario;
import tj.scenario.TJGestureScenario;
import tj.scenario.TJHomeScenario;
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
        this.addScenario(TJDefaultScenario.createSingleton(this.mApp));
        this.addScenario(TJDrawScenario.createSingleton(this.mApp));
        this.addScenario(TJSelectScenario.createSingleton(this.mApp));
        this.addScenario(TJNavigateScenario.createSingleton(this.mApp));
        this.addScenario(TJGestureScenario.createSingleton(this.mApp));
        this.addScenario(TJColorScenario.createSingleton(this.mApp));
    }

    @Override
    protected void setInitCurScene() {
        this.setCurScene(TJDefaultScenario.ReadyScene.getSingleton());
    }
    
}
