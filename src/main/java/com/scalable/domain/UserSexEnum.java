package com.scalable.domain;

public enum UserSexEnum {
    MAN, WOMAN,
    ;

    public static UserSexEnum parse(int value) {
		for (UserSexEnum sex : UserSexEnum.values()) {
			if (value == sex.ordinal())
				return sex;
		}
		return null;
	}

}