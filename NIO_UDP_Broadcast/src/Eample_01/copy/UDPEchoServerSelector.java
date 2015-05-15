package Eample_01.copy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class UDPEchoServerSelector
{

	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)

	public static void main(String[] args) throws Exception
	{
		System.out.println("Copy....");
		// Create a selector to multiplex client connections.
		Selector selector = Selector.open();

		NetworkInterface ni = NetworkInterface.getByIndex(2);
		DatagramChannel channel = DatagramChannel
		        .open(StandardProtocolFamily.INET)
		        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
		        .bind(new InetSocketAddress(5500))
		        .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
		channel.configureBlocking(false);
		InetAddress group = InetAddress.getByName("225.4.5.6");
		channel.join(group, ni);

		channel.register(selector, SelectionKey.OP_READ,
		        new UDPEchoSelectorProtocol.ClientRecord());

		UDPEchoSelectorProtocol echoSelectorProtocol = new UDPEchoSelectorProtocol();
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
					echoSelectorProtocol.handleRead(key);

				// Client socket channel is available for writing and
				// key is valid (i.e., channel not closed).
				if (key.isValid() && key.isWritable())
					echoSelectorProtocol.handleWrite(key);

				keyIter.remove();
			}
		}
	}

}