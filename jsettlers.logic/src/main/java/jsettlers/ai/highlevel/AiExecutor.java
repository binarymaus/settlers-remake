/*******************************************************************************
 * Copyright (c) 2015 - 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.ai.highlevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jsettlers.common.logging.StatisticsStopWatch;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.grid.MainGrid;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.network.client.interfaces.ITaskScheduler;
import jsettlers.network.synchronic.timer.INetworkTimerable;

/**
 * The AiExecutor holds all IWhatToDoAi high level KIs and executes them when NetworkTimer notifies it.
 * 
 * @author codingberlin
 */
public class AiExecutor implements INetworkTimerable {

	private final List<Callable<Void>> lightWhatToDoAis;
	private final List<Callable<Void>> heavyWhatToDoAis;
	private final AiStatistics aiStatistics;
	private final StatisticsStopWatch updateStatisticsStopWatch = new StatisticsStopWatch();
	private final StatisticsStopWatch applyLightRulesStopWatch = new StatisticsStopWatch();
	private final StatisticsStopWatch applyHeavyRulesStopWatch = new StatisticsStopWatch();
	private final ExecutorService statisticsUpdaterPool;

	public AiExecutor(PlayerSetting[] playerSettings, MainGrid mainGrid, ITaskScheduler taskScheduler) {
		ExecutorService re;
		try {
			re = (ExecutorService) Executors.class.getMethod("newWorkStealingPool").invoke(null);
		} catch(Throwable thrown) {
			thrown.printStackTrace();
			re = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		}
		statisticsUpdaterPool = re;

		aiStatistics = new AiStatistics(mainGrid, statisticsUpdaterPool);
		aiStatistics.updateStatistics();
		this.lightWhatToDoAis = new ArrayList<>();
		this.heavyWhatToDoAis = new ArrayList<>();
		WhatToDoAiFactory aiFactory = new WhatToDoAiFactory();
		for (byte playerId = 0; playerId < playerSettings.length; playerId++) {
			PlayerSetting playerSetting = playerSettings[playerId];
			if (playerSetting.isAvailable() && playerSetting.getPlayerType().isAi()) {
				IWhatToDoAi whatToDoAi = aiFactory.buildWhatToDoAi(
						playerSettings[playerId].getPlayerType(),
						playerSettings[playerId].getCivilisation(),
						aiStatistics,
						mainGrid.getPartitionsGrid().getPlayer(playerId),
						mainGrid,
						mainGrid.getMovableGrid(),
						taskScheduler);

				lightWhatToDoAis.add(() -> {
					whatToDoAi.applyLightRules();
					return null;
				});

				heavyWhatToDoAis.add(() -> {
					whatToDoAi.applyHeavyRules();
					return null;
				});
			}
		}
	}

	@Override
	public void timerEvent() {
		// every second
		applyLightRulesStopWatch.restart();
		try {
			statisticsUpdaterPool.invokeAll(lightWhatToDoAis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		applyLightRulesStopWatch.stop("computerplayer:applyLightRules()");

		// every ten seconds
		if((MatchConstants.clock().getTime()/1000)%10 == 0) {
			updateStatisticsStopWatch.restart();
			aiStatistics.updateStatistics();
			updateStatisticsStopWatch.stop("computerplayer:updateStatistics()");

			applyHeavyRulesStopWatch.restart();
			try {
				statisticsUpdaterPool.invokeAll(heavyWhatToDoAis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			applyHeavyRulesStopWatch.stop("computerplayer:applyHeavyRules()");
		}
	}

	public StatisticsStopWatch getUpdateStatisticsStopWatch() {
		return updateStatisticsStopWatch;
	}

	public StatisticsStopWatch getApplyLightRulesStopWatch() {
		return applyLightRulesStopWatch;
	}

	public StatisticsStopWatch getApplyHeavyRulesStopWatch() {
		return applyHeavyRulesStopWatch;
	}
}
