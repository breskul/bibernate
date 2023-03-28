package com.breskul.bibernate.exception;

public class InternalException extends CommonException{
	public InternalException(String cause, String suggestedSolution, Throwable e) {
		super(cause, suggestedSolution, e);
	}
}
