/*
 * Copyright 2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.common.Lifecycle
import org.roboquant.feeds.Event

typealias MetricResults = Map<String, Number>

/**
 * Metric represents a piece of information you want to capture during a run. Examples of metrics are
 * account value, profit and loss, alpha and beta.
 *
 * This is the interface that any metric will have to implement, so it can be invoked during a run. Notice that a metric
 * takes care of the calculations, but the storing of the results is normally done by a MetricsLogger.
 *
 * When metrics rely on state, it is important they override the [Lifecycle] methods to ensure they reset their
 * state when appropriate.
 */
interface Metric : Lifecycle {

    /**
     * Calculate the metric given the [account] and [event] and return the results. This method will be invoked at the
     * end of each step in a run.
     */
    fun calculate(account: Account, event: Event) : MetricResults

}


