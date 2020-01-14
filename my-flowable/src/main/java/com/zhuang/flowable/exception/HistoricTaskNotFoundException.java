package com.zhuang.flowable.exception;

/**
 * 没有找到历史的任务
 * @author zwb
 *
 */
public class HistoricTaskNotFoundException extends RuntimeException {

	public HistoricTaskNotFoundException() {
		super();
	}

	public HistoricTaskNotFoundException(String s) {
		super(s);
	}

}
