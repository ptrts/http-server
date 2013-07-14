package com.suchorukov.tarouts.http_server.mapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TD {

	@XmlElement
	public A a;

	public TD(String href, String value) {
		this.a = new A(href, value);
	}

	public TD() {
	}
}
