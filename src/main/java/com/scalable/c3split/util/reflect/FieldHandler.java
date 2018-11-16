package com.scalable.c3split.util.reflect;

import java.lang.reflect.Field;

public interface FieldHandler {

	public void handle(int index, Field field, Object value);

}