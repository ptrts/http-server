package com.suchorukov.tarouts.http_server.mapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TR {

	@XmlElement(name = "td")
	public TD name;

	@XmlElement(name = "td")
	public TD size;

	@XmlElement(name = "td")
	public TD lastModifiedTime;

	public TR(String href, String name, String size, String lastModifiedTime) {
		this.name = new TD(href, name);
		this.size = new TD(href, size);
		this.lastModifiedTime = new TD(href, lastModifiedTime);
	}

	public TR() {
	}
}
