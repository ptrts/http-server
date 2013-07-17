package com.suchorukov.tarouts.http_server;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client implements Runnable {

	private HttpServer server;
	private Socket clientSocket;

	private void respond(OutputStream outputStream, String code, String description, String data) throws UnsupportedEncodingException {

		Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
		writer = new BufferedWriter(writer);
		PrintWriter printWriter = new PrintWriter(writer);

		printWriter.println("HTTP/1.0 " + code + " " + description);
		printWriter.println("Content-Type: text/html");
		printWriter.println("Content-Length: " + data.length());
		printWriter.println();
		printWriter.print(data);
		printWriter.flush();
	}

	private void doGet(OutputStream outputStream, String resourceParam) throws IOException, JAXBException {

		String resource = URLDecoder.decode(resourceParam, "utf-8");

		// Удаляем корень "/", если есть
		Path resourcePath = Paths.get(resource);
		Path resourceSlashRootPath = resourcePath.getRoot();
		if (resourceSlashRootPath != null) {
			if (resourceSlashRootPath.toString().equals(resourceSlashRootPath.getFileSystem().getSeparator())) {
				resourcePath = resourcePath.getRoot().relativize(resourcePath);
			}
		}

		// Откладываем путь к ресурсу как он дан в запросе от папки где лежат файлы веб приложения на диске
		resourcePath = server.getRootDirectory().resolve(resourcePath);

		// Получаем файл, соответствующий ресурсу
		File resourceFile = resourcePath.toFile();

		if (!resourceFile.exists()) {
			respond(outputStream, "404", "Not Found", "404: File not Found");
		} else if (resourceFile.isDirectory()) {

			// Будем формировать страницу содержимого каталога через StringWriter
			StringWriter stringWriter = new StringWriter();

			// Это делает специальный класс
			IndexWriter.formIndex(resourcePath, stringWriter, server.getRootDirectory());

			respond(outputStream, "200", "ОК", stringWriter.toString());
		} else {

			// Content-Type
			String contentType = Files.probeContentType(resourcePath);

			// Content input stream
			InputStream resourceInputStream = new FileInputStream(resourceFile);

			// Content-Length
			int contentLength = resourceInputStream.available();

			// Готовим printWriter
			Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
			writer = new BufferedWriter(writer);
			PrintWriter printWriter = new PrintWriter(writer);

			printWriter.println("HTTP/1.0 200 OK");
			printWriter.println("Content-Type: " + contentType);
			printWriter.println("Content-Length: " + contentLength);
			printWriter.println();
			printWriter.flush();

			int contentLengthLeft = contentLength;

			while (true) {

				int portionLength = Math.min(1024, contentLengthLeft);

				if (portionLength == 0) {
					break;
				}

				byte[] portion = new byte[portionLength];

				// Считываем очередной символ
				resourceInputStream.read(portion);

				// Сливаем клиенту
				outputStream.write(portion);

				contentLengthLeft -= portionLength;
			}

			// Пытаемся закрыть поток вывода данных клиенту
			outputStream.flush();
		}
	}

	@Override
	public void run() {

		InputStream inputStream = null;
		BufferedReader bufferedReader = null;

		OutputStream outputStream = null;

		try {

			// bufferedReader для чтения запроса
			inputStream = clientSocket.getInputStream();
			Reader reader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(reader);

			// printWriter для записи ответа
			outputStream = clientSocket.getOutputStream();

			// Получаем первую строку запроса
			String requestString = bufferedReader.readLine();

			// Разбиваем строку запроса по пробелам
			String[] parts = requestString.split("\\s+");

			// Первый элемент - это метод запроса. Например, это может быть GET
			String requestMethod = parts[0];

			if ("GET".equalsIgnoreCase(requestMethod)) {
				doGet(outputStream, parts[1]);
			} else {
				respond(outputStream, "501", "Not Implemented", "");
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(System.out);
		} catch (IOException e) {
			e.printStackTrace(System.out);
		} catch (JAXBException e) {
			e.printStackTrace(System.out);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Client(Socket clientSocket, HttpServer server) {
		this.server = server;
		this.clientSocket = clientSocket;
	}
}
