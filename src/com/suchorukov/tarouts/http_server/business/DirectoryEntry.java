package com.suchorukov.tarouts.http_server.business;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public class DirectoryEntry {

	private String name;
	private Path path;
	private boolean isDirectory;
	private long size;
	private FileTime lastModifiedTime;

	public DirectoryEntry(String name, Path path, boolean isDirectory, long size, FileTime lastModifiedTime) {
		this.name = name;
		this.path = path;
		this.isDirectory = isDirectory;
		this.size = size;
		this.lastModifiedTime = lastModifiedTime;
	}

	public String getName() {
		return name;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public long getSize() {
		return size;
	}

	public FileTime getLastModifiedTime() {
		return lastModifiedTime;
	}

	@Override
	public String toString() {
		return name;
	}

	public Path getPath() {
		return path;
	}
}
