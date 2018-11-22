package com.scalable.c4redic.strategy;

public interface ShardingStrategy {

	public <T> int key2node(T key, int nodeCount);

}