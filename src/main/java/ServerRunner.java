import Server.FileCopyServerHandler;
import Server.OnlineServerHandler;
import Server.ServerMaintainer.Server.Server;
import Server.ServerMaintainer.ServerFactory.ServerFactory;

public class ServerRunner {

	public static void main(String[] args) {

		//if process already use port: sudo netstat -ltnp | grep "6789"

		//create sessionnumber
		//Add filepath which gets scanned
		//copy all files from this path. Rerun this on any directory

		Server isOnlineServer = ServerFactory.startServerMultithread(new OnlineServerHandler(), 6788);
//		Server isOnlineServer = ServerFactory.startServerMultithread(new OnlineServerHandler(), 5000);

		String basePath = "/home/lumb3rj4ck/Tmp/ServerFiles";
//		String basePath = "E:/Test";
		FileCopyServerHandler fileCopyServerHandler = new FileCopyServerHandler();
		Server server = ServerFactory.startServerMultithread(new FileCopyServerHandler(basePath), 6789);

//		ServerRunner2 serverRunner = new ServerRunner2();
//		serverRunner.start();

		while(true) ; //solve this different

//		server.stop(); //this should be called after a specific action
	}
}
