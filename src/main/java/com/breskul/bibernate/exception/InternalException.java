package com.breskul.bibernate.exception;

/**
 * Internal exceptions for utils methods
 */
public class InternalException extends CommonException {

	public InternalException(String cause, String suggestedSolution) {
		super(cause, suggestedSolution);
	}

	public InternalException(String cause, String suggestedSolution, Throwable e) {
		super(cause, suggestedSolution, e);
	}
}
