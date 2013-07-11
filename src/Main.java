import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

	public static void main(String[] args) throws IOException {

		// Цепляемся к порту 8080
		ServerSocket serverSocket = new ServerSocket(8080);

		// До бесконечности...
		while(true){

			// Из ServerSocket получаем просто Socket (client socket)
			Socket clientSocket = serverSocket.accept();

			// Создаем объект, метод run которого будет выполняться в потоке
			Client client = new Client(clientSocket);

			// Оборачиваем объект собственно в запускатель потоков
			Thread clientThread = new Thread(client, "Client thread");

			// Запускаем поток
			clientThread.start();
		}
	}
}
