import java.io.*;
import java.net.Socket;

public class Client implements Runnable {

	private static int i = 0;

	private Socket clientSocket;

	@Override
	public void run() {

		// Объявляем начало выполнения запроса
		System.out.println("Query #" + (++i));

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
			while ((c = inputStream.read()) != -1 && c != 10 && c != 13) {
				sb.append((char) c);
			}
			//получаем команду и ее аргументы
			String data = sb.toString();
			String args[] = data.split(" ");
			String cmd = args[0].trim().toUpperCase();


			// пишем ответ Hello world
			String s = "<html><title>test</title><body>Hello <b>world</b></body></html>";

			printWriter.println("HTTP/1.0 200 OK");
			printWriter.println("Content-Type: text/html");
			printWriter.println("Content-Length: " + s.length());
			printWriter.println();
			printWriter.print(s);
			printWriter.flush();

			Writer stringWriter = new StringWriter();

			String line = bufferedReader.readLine();
			while (line != null) {
				stringWriter.println(line);
				line = bufferedReader.readLine();
			}


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
