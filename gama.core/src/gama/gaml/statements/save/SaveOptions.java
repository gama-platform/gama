package gama.gaml.statements.save;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import gama.core.runtime.concurrent.BufferingController.BufferingStrategies;

public class SaveOptions {

	public final String code;
	public final boolean addHeader;
	public final String type;
	public final Object attributesToSave; 
	public final BufferingStrategies bufferingStrategy;
	public final boolean rewrite;
	protected Charset writeCharset;
	
	public SaveOptions(final String code, final boolean addHeader, final String type, final Object attributesToSave, 
			BufferingStrategies bufferingStrategy, final boolean rewrite) {
		this.code = code;
		this.addHeader = addHeader;
		this.type = type;
		this.attributesToSave = attributesToSave;
		this.bufferingStrategy = bufferingStrategy;
		this.rewrite = rewrite;
		writeCharset = StandardCharsets.UTF_8;
	}
	
	public void setCharSet(Charset c) {
		writeCharset = c;
	}
	
	public Charset getCharset() {
		return writeCharset;
	}	
	
}
