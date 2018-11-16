package com.scalable.c2kclient.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Cat {
	
	private long id;
	private String name;

	public Cat(Dog dog) {
		this.id = dog.getId();
		this.name = dog.getName() + "[cat]";
	}
	
}
