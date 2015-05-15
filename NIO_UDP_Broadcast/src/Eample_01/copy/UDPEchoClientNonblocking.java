package Eample_01.copy;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class UDPEchoClientNonblocking
{

	private static final int TIMEOUT = 3000; // Resend timeout (milliseconds)
	private static final int MAXTRIES = 255; // Maximum retransmissions

	public static void main(String args[]) throws Exception
	{
		
		System.out.println("Copy....");
		Selector selector = Selector.open();
		// Convert input String to bytes using the default charset
		byte[] bytesToSend = "0123456789abcdefghijklmnopqrstuvwxyz".getBytes();

		// Create channel and set to nonblocking
		NetworkInterface ni = NetworkInterface.getByIndex(2);
		DatagramChannel channel = DatagramChannel
		        .open(StandardProtocolFamily.INET)
		        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
		        .bind(new InetSocketAddress(5501))
		        .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
		channel.configureBlocking(false);
		InetAddress group = InetAddress.getByName("225.4.5.6");
		MembershipKey memberShipkey = channel.join(group, ni);

		ByteBuffer writeBuf = ByteBuffer.wrap(bytesToSend);
		ByteBuffer readBuf = ByteBuffer.allocate(MAXTRIES);

		//channel = channel.connect(new InetSocketAddress("127.0.0.1", 5500));

		int totalBytesRcvd = 0; // Total bytes received so far
		int bytesRcvd; // Bytes received in last read
		channel.send(writeBuf, new InetSocketAddress("225.4.5.6", 5500));
		
		while (true)
		{ // Run forever, receiving and echoing datagrams
			// Wait for task or until timeout expires
			if (selector.select(TIMEOUT) == 0)
			{
				System.out.print(".");
				continue;
			}

			// Get iterator on set of keys with I/O to process
			Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
			while (keyIter.hasNext())
			{
				SelectionKey key = keyIter.next(); // Key is bit mask

				// Client socket channel has pending data?
				if (key.isReadable())
				{
					DatagramChannel readChannel = (DatagramChannel) key.channel();
					readChannel.read(readBuf);
					
					System.out.println("Received:"
							+ new String(readBuf.array(), 0, totalBytesRcvd));
					System.exit(0);
				}
				keyIter.remove();
			}
		}
	}
}