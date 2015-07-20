package de.jackwhite20.comix.tasks;

import de.jackwhite20.comix.Comix;
import de.jackwhite20.comix.strategy.BalancingStrategy;
import de.jackwhite20.comix.util.TargetData;

import java.util.*;

/**
 * Created by JackWhite20 on 20.07.2015.
 */
public class CheckTargets extends TimerTask {

    private BalancingStrategy balancingStrategy;

    private List<TargetData> offlineTargets = Collections.synchronizedList(new ArrayList<>());

    public CheckTargets(BalancingStrategy balancingStrategy) {
        this.balancingStrategy = balancingStrategy;
    }

    @Override
    public void run() {
        Iterator<TargetData> iterator = balancingStrategy.getTargets().iterator();
        while (iterator.hasNext()) {
            TargetData targetData = iterator.next();
            if(!targetData.isAvailable()) {
                iterator.remove();
                offlineTargets.add(targetData);
                Comix.getLogger().info(targetData.getName() + " went offline!");
            }
        }

        Iterator<TargetData> offlineIterator = offlineTargets.iterator();
        while (offlineIterator.hasNext()) {
            TargetData offlineTargetData = offlineIterator.next();
            if(offlineTargetData.isAvailable()) {
                offlineIterator.remove();
                balancingStrategy.addTarget(offlineTargetData);
                Comix.getLogger().info(offlineTargetData.getName() + " is back online!");
            }
        }
    }

}
