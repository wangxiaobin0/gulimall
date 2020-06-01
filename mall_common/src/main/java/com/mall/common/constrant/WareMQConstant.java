package com.mall.common.constrant;

public class WareMQConstant {

    public static final String WARE_EXCHANGE = "ware.event.exchange";

    public static final String WARE_LOCK_STOCK_QUEUE = "ware.lock.queue";

    public static final String WARE_UNLOCK_STOCK_QUEUE = "ware.unlock.queue";

    public static final String WARE_LOCK_ROUTING_KEY = "ware.lock.#";

    public static final String WARE_UNLOCK_ROUTING_KEY = "ware.unlock.#";

    public static final String DEAD_LETTER_EXCHANGE = WARE_EXCHANGE;

    public static final String DEAD_LETTER_ROUTING_KEY = WARE_UNLOCK_ROUTING_KEY;
}
