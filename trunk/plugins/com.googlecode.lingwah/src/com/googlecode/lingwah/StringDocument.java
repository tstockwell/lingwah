package com.googlecode.lingwah;

public class StringDocument implements Document {
	
	private String _document;

	public StringDocument(String input) {
		_document= input;
	}

	@Override
	public String substring(int start, int end) {
		return _document.substring(start, end);
	}

	@Override
	public String substring(int start) {
		return _document.substring(start);
	}

	@Override
	public int length() {
		return _document.length();
	}

	@Override
	public char charAt(int i) {
		return _document.charAt(i);
	}

	@Override
	public int indexOf(int startChar, int i) {
		return _document.indexOf(startChar, i);
	}

	@Override
	public boolean startsWith(String target, int start) {
		return _document.startsWith(target, start);
	}

	@Override
	public int indexOf(String target, int i) {
		return _document.indexOf(target, i);
	}

	@Override
	public int[] translateOffset(int offset) {
		
		int line= 0;
		int column= 0;
		int end= offset;
		int pos = -1;
		do
		{
			int p = indexOf('\n', pos+1);
			if (p < 0 || end < p)
				break;
			pos= p;
			line++;
		}
		while (true);
		column= end - pos;
		line++;
		
		return new int[] { line, column};
	}

}
