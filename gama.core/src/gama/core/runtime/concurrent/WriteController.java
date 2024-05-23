package gama.core.runtime.concurrent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class WriteController {
	
	protected class OwnerWriteAsks {
		Object owner;
		StringBuilder cumulatedContent;
		
		public OwnerWriteAsks(Object owner, CharSequence initialContent) {
			this.owner = owner;
			cumulatedContent = new StringBuilder(initialContent);
		}
		
		public void appendContent(CharSequence s) {
			cumulatedContent.append(s);
		}
		
		public void appendContent(String s) {
			cumulatedContent.append(s);
		}
		
		public Object getOwner() {
			return owner;
		}
		
		public String getCumulatedContent() {
			return cumulatedContent.toString();
		}
		
	}
	
	protected class OwnerWriteAsksQueue extends LinkedBlockingQueue<OwnerWriteAsks>{
		
		Map<Object, List<OwnerWriteAsks>> tasksPerOwner;
		
		public OwnerWriteAsksQueue() {
			super();
		}
		
		@Override
		public boolean add(OwnerWriteAsks e) {
			var added = super.add(e);
			if (added) {
				var listTaskOwner = tasksPerOwner.get(e.owner);
				if (listTaskOwner == null) {
					listTaskOwner = new ArrayList<>();
					tasksPerOwner.put(e.owner, listTaskOwner);
				}
				listTaskOwner.add(e);
			}
			return added;
		}
		
		public List<OwnerWriteAsks> getAllWriteAsksOfOwner(Object owner) {
			return tasksPerOwner.get(owner);
		}
	}
	
	
	protected Map<String, ArrayDeque<OwnerWriteAsks>> fileWritingMap;
	
	public WriteController() {
		fileWritingMap = new HashMap<>();
	}
	
	public boolean askWrite(String fileId, Object owner, CharSequence content) {
		
		// If no queue yet we initialize one
		var fileExecutionStack = fileWritingMap.get(fileId);
		if (fileExecutionStack == null) {
			fileExecutionStack = new ArrayDeque<>();
			fileWritingMap.put(fileId, fileExecutionStack);
		}
		
		// we look up in the queue for the latest ask on this file, 
		// if it's by the same owner we append to it
		// else we create a new ask object
		if (fileExecutionStack.size() == 0 || ! fileExecutionStack.peekLast().owner.equals(owner)) {
			return fileExecutionStack.add(new OwnerWriteAsks(owner, content));
		}
		else {
			fileExecutionStack.peekLast().appendContent(content);
			return true;
		}
	}
	
	
	
	public boolean flushFile(String fileId) {
		FileWriter fr;
		try {
			fr = new FileWriter(new File(fileId));
			// we merge everything
			for(var asks : fileWritingMap.get(fileId)) {
				fr.append(asks.cumulatedContent);
			}
			fr.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean flushOwner(Object owner) {
		for(var entry : fileWritingMap.entrySet()) {
			// First we start by looking for the last time this owner made a write request on a file
			boolean found = false;
			var it = entry.getValue().descendingIterator();
			OwnerWriteAsks lastAskToExecute = null;
			while (it.hasNext()) {
				lastAskToExecute = it.next();
				if(lastAskToExecute.owner.equals(owner)) {
					found = true;
					break;
				}
			}
			// If the owner made at least one write request on the file, we flush everything up until its last request
			if (found) {
				FileWriter fr;
				try {
					fr = new FileWriter(new File(entry.getKey()));
					for(var ask : entry.getValue()) {
						fr.append(ask.cumulatedContent);
					}
					fr.flush();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}
	
	
}
