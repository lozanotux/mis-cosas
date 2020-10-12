package com.redhat.camel.components;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

class WelderDecoder extends CumulativeProtocolDecoder {
    static final int PAYLOAD_SIZE = 8;
    
    protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.remaining() >= PAYLOAD_SIZE) {
            byte[] buf = new byte[in.remaining()];
            in.get(buf);
            
            // first 7 bytes are the sensor ID, last is the status
            // and the result message will look something like
            // MachineID=2371748;Status=Good
            StringBuilder sb = new StringBuilder();
            sb.append("MachineID=")
            .append(new String(buf, 0, PAYLOAD_SIZE - 1)).append(";")
            .append("Status=");
            if (buf[PAYLOAD_SIZE - 1] == '1') {
                sb.append("Good");
            } else {
                sb.append("Failure");
            }
            out.write(sb.toString());
            return true;
        } else {
            return false;
        }
    }
}