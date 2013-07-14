package com.suchorukov.tarouts.http_server;

import com.suchorukov.tarouts.http_server.business.DirectoryEntry;
import com.suchorukov.tarouts.http_server.mapping.TR;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IndexWriter {

	private static String dateRepresentation(FileTime fileTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		if (fileTime == null) {
			return "";
		} else {
			return sdf.format(fileTime.toMillis());
		}
	}

	private static List<DirectoryEntry> getDirectoryEntries(Path path, Path rootPath) throws IOException {

		// Делаем поток чтения содержимого директории
		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);

		Path parentPath = path.getParent();

		// Заготавливаем список элементов директории
		List<DirectoryEntry> entries = new ArrayList<>();

		DirectoryEntry entry = new DirectoryEntry(
				"..",
				parentPath,
				true,
				0,
				null
		);
		entries.add(entry);

		// Цикл по всем элементам директории
		for (Path childPath : directoryStream) {

			BasicFileAttributeView attributeView = Files.getFileAttributeView(childPath, BasicFileAttributeView.class);

			BasicFileAttributes fileAttributes = attributeView.readAttributes();

			parentPath = rootPath.relativize(childPath);

			String fileName = childPath.getFileName().toString();

			entry = new DirectoryEntry(
					fileName,
					parentPath,
					fileAttributes.isDirectory(),
					fileAttributes.size(),
					fileAttributes.lastModifiedTime()
			);

			entries.add(entry);
		}

		// Сортируем чего насобирали
		Collections.sort(
				entries,
				new Comparator<DirectoryEntry>() {
					@Override
					public int compare(DirectoryEntry entry1, DirectoryEntry entry2) {
						if (entry1.isDirectory() == entry2.isDirectory()) {
							return entry1.getName().compareToIgnoreCase(entry2.getName());
						} else if (entry1.isDirectory()) {
							return -1;
						} else {
							return 1;
						}
					}
				}
		);

		return entries;
	}

	public static void formIndex(Path directoryPath, Writer writer, Path rootPath) throws IOException, JAXBException {

		// Это потоки для чтения шаблона
		InputStream inputStream = null;
		BufferedReader bufferedReader = null;

		// Это поток для записи содержимого index.html
		PrintWriter printWriter = null;

		try {

			// Получаем список объектов "DirectoryEntry"
			List<DirectoryEntry> entries = getDirectoryEntries(directoryPath, rootPath);

			// Делаем поток форматированного вывода, для формирования содержимого index.html
			printWriter = new PrintWriter(new BufferedWriter(writer));

			///////////////////////////////////////////////////////////////
			// ПОЛУЧАЕМ BUFFERED READER
			///////////////////////////////////////////////////////////////

			inputStream = Client.class.getResourceAsStream("template.xhtml");
			Reader reader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(reader);

			///////////////////////////////////////////////////////////////
			// ИНИЦИАЛИЗИРУЕМ МАРШАЛЛЕР
			///////////////////////////////////////////////////////////////

			// Открываем контекст, связанный с нашим классом TR
			JAXBContext jaxbContext = JAXBContext.newInstance(TR.class);
			Marshaller marshaller = jaxbContext.createMarshaller();

			// Делаем настройки маршаллера
			marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", false);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			///////////////////////////////////////////////////////////////
			// БЕРЕМ С ТЕМПЛЕЙТА - ПИШЕМ В ФАЙЛ (ИНОГДА ЧЕРЕЗ МАРШАЛЛЕР)
			///////////////////////////////////////////////////////////////

			String line = bufferedReader.readLine();
			while (line != null) {

				String trimmedLine = line.trim();

				if ("<!--HERE!-->".equals(trimmedLine)) {

					for (DirectoryEntry directoryEntry : entries) {

						FileTime lastModifiedTime = directoryEntry.getLastModifiedTime();

						//String href = directoryEntry.getPath().toString();
						String href = directoryEntry.getName().toString();

						href = href.replace(directoryEntry.getPath().getFileSystem().getSeparator(), "/");

						TR tr;

						if (directoryEntry.isDirectory()) {
							tr = new TR(
									href + "/",
									directoryEntry.getName(),
									"",
									""
							);
						} else {
							tr = new TR(
									href,
									directoryEntry.getName(),
									Long.toString(directoryEntry.getSize()),
									dateRepresentation(lastModifiedTime)
							);
						}

						// Выплевываем объект в файл
						marshaller.marshal(tr, printWriter);
					}

				} else {
					printWriter.println(line);
				}

				line = bufferedReader.readLine();
			}

			printWriter.flush();

		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
	}
}
