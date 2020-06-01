package com.mall.common.constrant;

public class OrderMQConstant {

    public static final String ORDER_EXCHANGE = "order.event.exchange";

    public static final String ORDER_CREATE_ORDER_QUEUE = "order.create.queue";

    public static final String ORDER_CANCEL_ORDER_QUEUE = "order.cancel.queue";

    public static final String ORDER_CREATE_ROUTING_KEY = "order.create.#";

    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel.#";

    public static final String DEAD_LETTER_EXCHANGE = ORDER_EXCHANGE;

    public static final String DEAD_LETTER_ROUTING_KEY = ORDER_CANCEL_ROUTING_KEY;
}
