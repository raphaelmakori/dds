package com.dds.shared;

import java.io.Serializable;

public enum RequestType implements Serializable {
    PLACE_ORDER,
    GET_REPORT,
    GET_MENU,
    PING
}
