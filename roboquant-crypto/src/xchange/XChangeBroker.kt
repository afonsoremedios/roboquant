/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.xchange

import org.knowm.xchange.Exchange
import org.knowm.xchange.currency.CurrencyPair
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.InternalAccount
import org.roboquant.common.AssetType
import org.roboquant.common.Currency
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.math.BigDecimal
import kotlin.math.absoluteValue
import org.knowm.xchange.dto.Order as CryptoOrder
import org.knowm.xchange.dto.trade.LimitOrder as CryptoLimitOrder
import org.knowm.xchange.dto.trade.MarketOrder as CryptoMarketOrder

/**
 * Generic Crypto currency broker implementation for exchanges that are supported by the XChange package.
 *
 * XChange is a Java library providing a streamlined API for interacting with 60+ Bitcoin and Altcoin exchanges
 * See also https://knowm.org/open-source/xchange/ for a complete overview of the supported exchanges.
 *
 * @constructor
 *
 * @param exchange
 */
class XChangeBroker(exchange: Exchange, baseCurrencyCode: String = "USD") : Broker {


    private val _account = InternalAccount(Currency.getInstance(baseCurrencyCode))

    override val account: Account
        get() = _account.toAccount()


    private val logger = Logging.getLogger(XChangeBroker::class)
    private val tradeService = exchange.tradeService
    private val accountService = exchange.accountService
    private val supportCurrencies = exchange.exchangeSymbols
    private val placedOrders = mutableMapOf<String, SingleOrder>()
    private var orderId = 0

    init {
        logger.info("Created CryptoBroker for $exchange")
        updateAccount()
    }

    /**
     * Update the account
     * TODO: implement real update
     *
     */
    private fun updateAccount() {
        val info = accountService.accountInfo
        for (wallet in info.wallets) {
            logger.info { "${wallet.key} ${wallet.value}" }
        }
    }


    /**
     * Place orders on a XChange supported exchange using the trade service.
     * @TODO test with a real account on several XChange supported exchanges
     *
     * @param orders
     * @return
     */
    override fun place(orders: List<Order>, event: Event): Account {
        for (order in orders.filterIsInstance<SingleOrder>()) {
            val asset = order.asset
            if (asset.type == AssetType.CRYPTO) {

                val currencyPair = CurrencyPair(asset.symbol, asset.currencyCode)

                if (supportCurrencies != null && currencyPair !in supportCurrencies) {
                    logger.warning { "Unsupported currency pair $currencyPair for exchange" }
                    return account
                }
                val orderId = orderId++.toString()
                when (order) {
                    is LimitOrder -> {
                        trade(currencyPair, order, orderId)
                        placedOrders[orderId] = order
                    }
                    is MarketOrder -> {
                        trade(currencyPair, order)
                        placedOrders[orderId] = order
                    }
                    else -> {
                        logger.warning { "CryptoBroker supports only market and limit orders, received ${order::class} instead" }
                    }
                }

            } else {
                logger.warning { "CryptoBroker supports only CRYPTO assets, received ${asset.type} instead" }
            }
        }

        return account
    }


    /**
     * Place a limit order for a currency pair
     *
     * @param currencyPair
     * @param order
     * @param orderId
     */
    private fun trade(currencyPair: CurrencyPair, order: LimitOrder, orderId: String) {
        val amount = BigDecimal(order.quantity.absoluteValue)
        val orderType = if (order.buy) CryptoOrder.OrderType.BID else CryptoOrder.OrderType.ASK
        val limitPrice = BigDecimal(order.limit)
        val limitOrder = CryptoLimitOrder(orderType, amount, currencyPair, orderId, null, limitPrice)
        val returnValue = tradeService.placeLimitOrder(limitOrder)
        println("Limit Order return value: $returnValue")
    }

    /**
     * place a market order for a currency pair
     *
     * @param currencyPair
     * @param order
     */
    private fun trade(currencyPair: CurrencyPair, order: MarketOrder) {
        val amount = BigDecimal(order.quantity.absoluteValue)
        val orderType = if (order.buy) CryptoOrder.OrderType.BID else CryptoOrder.OrderType.ASK
        val marketOrder = CryptoMarketOrder(orderType, amount, currencyPair)
        val returnValue = tradeService.placeMarketOrder(marketOrder)
        println("Market Order return value: $returnValue")
    }

}