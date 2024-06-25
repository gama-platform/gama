package gama.gaml.compilation;


import java.util.List;

import gama.core.common.util.FileUtils;

public class GamaCompilationFailedException extends RuntimeException{

	private static final long serialVersionUID = 1392647532622819498L;
	
	public final List<GamlCompilationError> errorList;
	
	public GamaCompilationFailedException(final List<GamlCompilationError> errorList) {
		this.errorList = errorList;
	}
	
	@Override
	public String getMessage() {
		return "The model couldn't be compiled because of compilation errors"; 
	}
	
	public String toJsonString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("\"exception\":\"").append(getClass().getSimpleName()).append("\",");
		sb.append("\"message\":\"").append(getMessage()).append("\",");
		sb.append("\"errors\":[");

		boolean atLeastSecond = false;
		for(var error : errorList) {
			if (atLeastSecond) {
				sb.append(",");
			}
			sb.append("{");
			sb.append("\"type\":\"").append(error.errorType.name()).append("\",");
			sb.append("\"message\":\"").append(error.message).append("\",");
			sb.append("\"code\":\"").append(error.code).append("\",");
			sb.append("\"data\":[\"").append(String.join("\",\"", error.data)).append("\"],");

			sb.append("\"source\":\"").append(FileUtils.escapeFilePath(error.source.eResource().getURI().toFileString())).append("\"");	
			sb.append("}");
			atLeastSecond = true;
		}
		sb.append("]");
		sb.append('}');
		return sb.toString();
	}
}
