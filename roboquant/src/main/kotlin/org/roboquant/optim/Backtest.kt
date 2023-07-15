/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.optim

import org.roboquant.Roboquant
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.common.plus
import org.roboquant.feeds.Feed


/**
 * Run back test without parameter optimizations. This is useful to get insights into the performance over different
 * timeframes.
 */
class Backtest(val feed: Feed, val roboquant: Roboquant) {

    /**
     * Run a warmup and return the remaining timeframe
     */
    private fun warmup(timeframe: Timeframe, period: TimeSpan) : Timeframe {
        require(timeframe.isFinite())
        val end = timeframe.start + period
        val tf = Timeframe(timeframe.start, end)
        roboquant.warmup(feed, tf)
        return timeframe.copy(start = end)
    }

    /**
     * Perform a single run over the provided [timeframe] using the provided [warmup] period. The timeframe is
     * including the warmup period.
     */
    fun singleRun(timeframe: Timeframe = feed.timeframe, warmup: TimeSpan = TimeSpan.ZERO) {
        val tf = if (! warmup.isZero) warmup(timeframe, warmup) else timeframe
        roboquant.run(feed, tf, name = "run-$tf")
    }

    /**
     * Run a walk forward using the provided period and warmup. The warmup is exclusive.
     */
    fun walkForward(
        period: TimeSpan,
        warmup: TimeSpan = TimeSpan.ZERO,
    ) {
        require(feed.timeframe.isFinite()) { "feed needs a finite timeframe" }
        feed.timeframe.split(period, warmup).forEach {
            val tf = if (! warmup.isZero) warmup(it, warmup) else it
            roboquant.run(feed, tf, name = "run-$tf")
            roboquant.reset(false)
        }
    }

    fun monteCarlo(
        period: TimeSpan,
        samples: Int,
        warmup: TimeSpan = TimeSpan.ZERO,
    ) {
        require(feed.timeframe.isFinite()) { "feed needs a finite timeframe" }
        feed.timeframe.sample(period + warmup, samples).forEach  {
            val tf = if (! warmup.isZero) warmup(it, warmup) else it
            roboquant.run(feed, tf, name = "run-$tf")
            roboquant.reset(false)
        }
    }


}