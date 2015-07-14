/*
 * Copyright (c) 2015 "JackWhite20"
 *
 * This file is part of Comix.
 *
 * Comix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.jackwhite20.comix.strategy;

import de.jackwhite20.comix.util.TargetData;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by JackWhite20 on 13.07.2015.
 */
public class RoundRobinBalancingStrategy implements BalancingStrategy {

    private Object lock = new Object();

    private List<TargetData> targets;

    private AtomicInteger currentTarget = new AtomicInteger(0);

    public RoundRobinBalancingStrategy(List<TargetData> target) {
        this.targets = target;
    }

    @Override
    public TargetData selectTarget(String originHost, int originPort) {
        int now = currentTarget.getAndIncrement();

        if (now >= targets.size()) {
            now = 0;
            currentTarget.set(0);
        }

        return targets.get(now);
    }

    @Override
    public List<TargetData> geTargetAddresses() {
        return Collections.unmodifiableList(targets);
    }

}
