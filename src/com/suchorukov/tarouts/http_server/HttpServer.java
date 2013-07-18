package com.suchorukov.tarouts.http_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpServer {

	private Path rootDirectory;

	public void start() throws IOException {

		// Цепляемся к порту 8080
		ServerSocket serverSocket = new ServerSocket(8080);

		// До бесконечности...
		while (true) {

			// Из ServerSocket получаем просто Socket (client socket)
			Socket clientSocket = serverSocket.accept();

			// Создаем объект, метод run которого будет выполняться в потоке
			Client client = new Client(clientSocket, this);

			// Оборачиваем объект собственно в запускатель потоков
			Thread clientThread = new Thread(client, "Client thread");

			// Запускаем поток
			clientThread.start();
		}
	}

	public HttpServer(Path rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	public static void main(String[] args) throws IOException {
		Path rootPath = Paths.get(args[0]);
		HttpServer server = new HttpServer(rootPath);
		server.start();
	}

	public Path getRootDirectory() {
		return rootDirectory;
	}
}
