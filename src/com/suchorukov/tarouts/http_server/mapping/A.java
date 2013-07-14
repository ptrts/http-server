package com.suchorukov.tarouts.http_server.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement
public class A {

	@XmlAttribute(name = "href")
	private String href;

	@XmlValue
	private String value;

	public A(String href, String value) {
		this.href = href;
		this.value = value;
	}

	public A() {
	}
}
