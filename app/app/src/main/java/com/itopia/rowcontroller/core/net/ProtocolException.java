package com.itopia.rowcontroller.core.net;

import java.io.IOException;

public class ProtocolException extends IOException {
    public ProtocolException() {
        super();
    }

    public ProtocolException(String s) {
        super(s);
    }

    public ProtocolException(Throwable cause) {
        super(cause);
    }
}
