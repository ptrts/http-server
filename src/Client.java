import java.io.*;
import java.net.Socket;

public class Client implements Runnable {

	private static int i = 0;

	private Socket clientSocket;

	@Override
	public void run() {

		// Объявляем начало выполнения запроса
		System.out.println("Query #"+(++i));

		InputStream inputStream = null;
		BufferedReader bufferedReader = null;

		OutputStream outputStream = null;
		PrintWriter printWriter = null;

		try {

			// Поток байтов
			inputStream = clientSocket.getInputStream();
			// Символьный поток
			Reader reader = new InputStreamReader(inputStream);
			// Буфферизированный символьный поток
			bufferedReader = new BufferedReader(reader);

			// Поток вывода байтов
			outputStream = clientSocket.getOutputStream();
			// Символьный поток вывода
			Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
			// Буферизированный символьный поток вывода
			writer = new BufferedWriter(writer);
			// Буферизированный поток форматированного вывода
			printWriter = new PrintWriter(writer);

			//читаем первую строку запроса, игнорируем все заголовки которые идут дальше первой строки
			StringBuilder sb = new StringBuilder();
			int c;
			while((c = inputStream.read())!=-1 && c!=10 && c!=13){
				sb.append((char)c);
			}
			//получаем команду и ее аргументы
			String data = sb.toString();
			String args[] = data.split(" ");
			String cmd = args[0].trim().toUpperCase();
			// пишем ответ Hello world
			String s = "<html><title>test</title><body>Hello <b>world</b></body></html>";
			//пишем статус ответа
			outputStream.write("HTTP/1.0 200 OK\r\n".getBytes());
			//минимально необходимые заголовки, тип и длина
			outputStream.write("Content-Type: text/html\r\n".getBytes());
			outputStream.write(("Content-Length: " + s.length() + "\r\n").getBytes());
			//пустая строка отделяет заголовки от тела
			outputStream.write("\r\n".getBytes());
			//тело
			outputStream.write(s.getBytes());
			outputStream.flush();











			//printWriter.println("HTTP/1.1 200 OK");
			//printWriter.println("Server: MyServer");
			//printWriter.println("Content-Type: text/html; charset=utf-8");
			//printWriter.println("Connection: close");
			//printWriter.println("");
			//
			//String line = bufferedReader.readLine();
			//while (line != null) {
			//	printWriter.println(line);
			//	line = bufferedReader.readLine();
			//}
			//
			//clientSocket.shutdownInput();
			//clientSocket.shutdownOutput();
			//clientSocket.close();

		} catch (IOException e) {
			e.printStackTrace(printWriter);
		} finally {

			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (printWriter != null) {
					printWriter.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public Client(Socket clientSocket) {
		// Инициализируем окружение нашего будущего потока
		this.clientSocket = clientSocket;
	}
}
