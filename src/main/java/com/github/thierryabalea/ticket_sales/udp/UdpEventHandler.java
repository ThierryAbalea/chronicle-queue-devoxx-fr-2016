package com.github.thierryabalea.ticket_sales.udp;

import com.lmax.disruptor.EventHandler;
import javolution.io.Struct;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpEventHandler implements EventHandler<Message>
{
    private final int port;
    private final ByteBuffer buffer = ByteBuffer.allocate(1400);
    private DatagramChannel channel;
    private final String host;

    public UdpEventHandler(String host, int port)
    {
        this.host = host;
        this.port = port;
    }
    
    public void bind() throws IOException
    {
        channel = DatagramChannel.open();
        channel.connect(new InetSocketAddress(host, port));
    }
    
    public void onEvent(Message message, long sequence, boolean endOfBatch) throws Exception
    {

        Bytes<ByteBuffer> byteBuffer = Bytes.elasticByteBuffer();
        Wire wire = new TextWire(byteBuffer);

        wire.getValueOut().object(message);
        System.out.println(byteBuffer);

        int size = byteBuffer.length();
        
        if (buffer.remaining() < size + 4)
        {
            flush();
        }
        
        buffer.putInt(size);
       // ByteBuffer messageBuffer = message.getByteBuffer();
      //  int messagePosition = message.getByteBufferPosition();

      //  messageBuffer.position(messagePosition);
      //  messageBuffer.limit(messagePosition + size);
     //   buffer.put(messageBuffer);
        buffer.put(ByteBuffer.wrap(byteBuffer.toByteArray()));
        byteBuffer.clear();
      //  messageBuffer.clear();
        
        if (endOfBatch)
        {
            flush();
        }
    }

    public void onEventOld(Struct message, long sequence, boolean endOfBatch) throws Exception
    {
        int size = 0; // message.getSize();

        if (buffer.remaining() < size + 4)
        {
            flush();
        }

        buffer.putInt(size);
        ByteBuffer messageBuffer = message.getByteBuffer();
        int messagePosition = message.getByteBufferPosition();

        messageBuffer.position(messagePosition);
        messageBuffer.limit(messagePosition + size);
        buffer.put(messageBuffer);
        messageBuffer.clear();

        if (endOfBatch)
        {
            flush();
        }
    }

    private void flush() throws IOException
    {
        buffer.flip();
        
        DatagramChannel channel = bindAndGetChannel();
        
        while (buffer.remaining() > 0)
        {
            channel.write(buffer);
        }
        
        buffer.clear();
    }
    
    private DatagramChannel bindAndGetChannel() throws IOException
    {
        if (null == channel)
        {
            bind();
        }
        
        return channel;
    }
}
