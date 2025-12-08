package tj;

import tj.scenario.TJDefaultScenario;
import tj.scenario.TJDrawScenario;
import tj.scenario.TJHomeScenario;
import x.XScenarioMgr;

public class TJScenarioMgr extends XScenarioMgr {
    // constructor
    public TJScenarioMgr(TJ tj) {
        super(tj);
    }

    @Override
    protected void addScenario() {
        this.addScenario(TJHomeScenario.createSingleton(this.mApp));
        this.addScenario(TJDefaultScenario.createSingleton(this.mApp));
        this.addScenario(TJDrawScenario.createSingleton(this.mApp));
    }

    @Override
    protected void setInitCurScene() {
        this.setCurScene(TJHomeScenario.CatalogueScene.getSingleton());
    }
    
}
